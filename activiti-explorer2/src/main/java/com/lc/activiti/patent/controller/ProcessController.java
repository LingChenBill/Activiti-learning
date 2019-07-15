package com.lc.activiti.patent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.activiti.patent.model.ProcessModel;
import com.lc.activiti.pojo.ProcessTemplate;
import com.lc.activiti.service.ProcessTemplateService;
import com.lc.activiti.utils.ActivitiUtils;
import com.sun.deploy.net.HttpResponse;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 流程控制器.
 *
 * @author zyz.
 */
@RestController
@RequestMapping("process")
public class ProcessController {

    private static Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ProcessEngine processEngine;


    @Autowired
    private ProcessTemplateService processTemplateService;

    /**
     * 获取流程列表.
     *
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String,Object>> processList() {

        Map<String,Object> content = new HashMap<>();

        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<Model> modelList = repositoryService.createModelQuery().list();

        // 流程列表.
        List<ProcessModel> processModelList = new ArrayList<>();

        List<ProcessTemplate> processTemplateList = processTemplateService.selectProcessTemplateList();

        for (ProcessTemplate processTemplate : processTemplateList) {
            ProcessModel processModel = new ProcessModel();
            processModel.setModelId(processTemplate.getModelid());
            processModel.setProcessTemplateId(processTemplate.getId().toString());
            processModel.setDeptName(processTemplate.getDeptname());
            processModel.setProcessName(processTemplate.getProcessname());
            processModel.setCreateTime(processTemplate.getCreatetime());

            for (Model model : modelList) {
                if (model.getId().equals(processModel.getModelId())) {
                    processModel.setModelId(model.getId());
                    break;
                } else if (!StringUtils.isEmpty(model.getId()) && StringUtils.isEmpty(processModel.getModelId())) {
                    processModel.setModelId(model.getId());
                    break;
                }
            }
            processModelList.add(processModel);
        }

        content.put("processList", processModelList);

        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    /**
     * 拆分流程项目。
     *
     * @param splitNum
     */
    @GetMapping("split")
    public void splitProcess(@RequestParam("splitNum") String splitNum,
                             @RequestParam("modelId") String modelId) {

        RepositoryService repositoryService = processEngine.getRepositoryService();

        try {
            Model model = repositoryService.getModel(modelId);
            JsonNode jsonNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(model.getId()));

            BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);
            // 修正模型图片起始点信息.
            bpmnModel = ActivitiUtils.fixGraphicInfo(bpmnModel);
            bpmnModel = ActivitiUtils.refreshXml(bpmnModel);

            List<Process> processes = bpmnModel.getProcesses();
            String processId = processes.get(0).getId();
            logger.info("processes size = {}", processes.size());

            // 修改前图片.
            repositoryService.validateProcess(bpmnModel);
            // 输出流程图片
            DefaultProcessDiagramGenerator generator2 = new DefaultProcessDiagramGenerator();
            InputStream inputStream0 = generator2.generateDiagram(bpmnModel, "png",
                    new ArrayList<>(), new ArrayList<>(),
                    "宋体", "宋体", "宋体",
                    processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0 );

            ActivitiUtils.writeToLocal("C:\\Users\\Administrator\\Downloads\\images\\" + processId + "_old.png", inputStream0);

            String tempId = null;
            String tempEndId = null;

            for (FlowElement element: processes.get(0).getFlowElements()) {
                logger.info("element id = {}, name = {}", element.getId(), element.getName());
                if ("译腾".equals(element.getName())) {
                    tempId = element.getId();
                } else if ("拆分".equals(element.getName())) {
                    tempEndId = element.getId();
                }
            }

            logger.info("tempId = {}, tempEndId = {}", tempId, tempEndId);

            for(FlowElement e : processes.get(0).getFlowElements()) {
                if (e instanceof SequenceFlow) {
                    SequenceFlow flow = (SequenceFlow)e;
                    if (flow.getSourceRef().equals(tempId)) {
                        flow.setTargetRef("splitExclusiveGate");
                    } else if (flow.getTargetRef().equals("split-project")) {
                        flow.setSourceRef("splitExclusiveGate");
                    }
                }
            }

            // 排他网关.
            ExclusiveGateway exclusiveGateway = ActivitiUtils.createExclusiveGateway("splitExclusiveGate");
            GraphicInfo gateGraphicInfo = ActivitiUtils.generateGraphicInfo(520.0, 150.0, 40.0, 40.0);
            List<SequenceFlow> gateOutgoingFlows = new ArrayList<>();

            // 添加拆分节点.
            int splitNumbers = Integer.valueOf(splitNum);
            for (int i = 1; i <= splitNumbers; i++) {

                // 拆分:翻译任务.
                UserTask tranTask = ActivitiUtils.createUserTask("tranTask" + i, "翻译任务" + i, null);
                GraphicInfo graphicInfoSp = ActivitiUtils.generateGraphicInfo(630.0, 0.0 + 70 * (i - 0), 80.0, 60.0);

                // 拆分:内控.
                UserTask controlTask = ActivitiUtils.createUserTask("controlTask" + i, "内控" + i, null);
                GraphicInfo conGraphicInfo = ActivitiUtils.generateGraphicInfo(770.0, 0.0 + 70 * (i - 0), 80.0, 60.0);

                // 顺序流线(tranTask -> controlTask).
                SequenceFlow tran2conFlow = ActivitiUtils.createSequenceFlow("tranTask" + i, "controlTask" + i);
                List<GraphicInfo> tran2conGraphicInfoList  = new ArrayList<>();
                tran2conGraphicInfoList.add(ActivitiUtils.generateGraphicInfo(490.0, 175.0, 30, 30));
                tran2conGraphicInfoList.add(ActivitiUtils.generateGraphicInfo(645.0, 175.0, 30, 30));

                tranTask.setOutgoingFlows(Arrays.asList(tran2conFlow));
                controlTask.setIncomingFlows(Arrays.asList(tran2conFlow));

                processes.get(0).addFlowElement(tranTask);
                processes.get(0).addFlowElement(tran2conFlow);

                // 顺序流线(controlTask -> tempEndId).
                SequenceFlow con2TempEndFlow = ActivitiUtils.createSequenceFlow("controlTask" + i, tempEndId);
                List<GraphicInfo> con2TempEndGraphicInfoList  = new ArrayList<>();
                con2TempEndGraphicInfoList.add(ActivitiUtils.generateGraphicInfo(490.0, 175.0, 30, 30));
                con2TempEndGraphicInfoList.add(ActivitiUtils.generateGraphicInfo(645.0, 175.0, 30, 30));

                controlTask.setOutgoingFlows(Arrays.asList(con2TempEndFlow));

                processes.get(0).addFlowElement(controlTask);
                processes.get(0).addFlowElement(con2TempEndFlow);

                // 排他网关流程线.
                SequenceFlow flowAdd = ActivitiUtils.createSequenceFlow("splitExclusiveGate", "tranTask" + i);
                gateOutgoingFlows.add(flowAdd);
                processes.get(0).addFlowElement(flowAdd);

                bpmnModel.addGraphicInfo(tranTask.getId(), graphicInfoSp);
                bpmnModel.addGraphicInfo(controlTask.getId(), conGraphicInfo);
                bpmnModel.addFlowGraphicInfoList(tran2conFlow.getId(), tran2conGraphicInfoList);
                bpmnModel.addFlowGraphicInfoList(con2TempEndFlow.getId(), con2TempEndGraphicInfoList);
            }

            // 排他网关.
            exclusiveGateway.setOutgoingFlows(gateOutgoingFlows);
            processes.get(0).addFlowElement(exclusiveGateway);
            bpmnModel.addGraphicInfo(exclusiveGateway.getId(), gateGraphicInfo);


            repositoryService.validateProcess(bpmnModel);

            // 输出流程图片
            DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            InputStream inputStream = generator.generateDiagram(bpmnModel, "png",
                    new ArrayList<>(), new ArrayList<>(),
                    "宋体", "宋体", "宋体",
                    processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0 );

            ActivitiUtils.writeToLocal("C:\\Users\\Administrator\\Downloads\\images\\" + processId + ".png", inputStream);


            // 修改的流程模型重新部署发布.
            byte[] bytes = new BpmnXMLConverter().convertToXML(bpmnModel);

            Deployment deploy = repositoryService.createDeployment()
                    .name("split-process.bpmn20.xml")
                    .addString("split-process.bpmn20.xml", new String(bytes, "UTF-8"))
                    .deploy();
            logger.info("deploy id = {}, name = {}", deploy.getId(), deploy.getName());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
