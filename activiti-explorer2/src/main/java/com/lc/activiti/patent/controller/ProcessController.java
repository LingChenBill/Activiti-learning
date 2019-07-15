package com.lc.activiti.patent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.activiti.patent.model.ProcessModel;
import com.lc.activiti.pojo.ProcessTemplate;
import com.lc.activiti.service.ProcessTemplateService;
import com.lc.activiti.utils.ActivitiUtils;
import com.sun.deploy.net.HttpResponse;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
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

            SubProcess subProcess = null;
            String tempId = null;
            String tempEndId = null;
            UserTask translationUserTask = null;
            List<SequenceFlow> translationUserTaskOutgoingFlows = null;


            for (FlowElement element: processes.get(0).getFlowElements()) {
                logger.info("element id = {}, name = {}", element.getId(), element.getName());
                if ("译腾".equals(element.getName())) {
                    tempId = element.getId();
                    //subProcess = (SubProcess)element.clone();
                    // translationUserTask = (UserTask)element;
                    // List<SequenceFlow> outgoingFlows = translationUserTask.getOutgoingFlows();
                } else if ("拆分".equals(element.getName())) {
                    tempEndId = element.getId();
                }
            }

            logger.info("tempId = {}, tempEndId = {}", tempId, tempEndId);

            for(FlowElement e : processes.get(0).getFlowElements()) {
                if (e instanceof SequenceFlow) {
                    SequenceFlow flow = (SequenceFlow)e;
                    if (flow.getSourceRef().equals(tempId)) {
                        flow.setTargetRef("tranTask1");
                    } else if (flow.getTargetRef().equals("split-project")) {
                        flow.setSourceRef("tranTask1");
                    }
                }
            }

            // 添加拆分节点.
            int splitNumbers = Integer.valueOf(splitNum);
            for (int i = 1; i <= splitNumbers; i++) {

                UserTask tranTask = ActivitiUtils.createUserTask("tranTask" + i, "翻译任务" + i, null);
//                UserTask tranTask2 = ActivitiUtils.createUserTask("tranTask2", "翻译任务2", null);
                GraphicInfo graphicInfoSp = ActivitiUtils.generateGraphicInfo(600.0, 35.0 + 75 * (i - 0), 80.0, 70.0);
//                GraphicInfo graphicInfoSp2 = ActivitiUtils.generateGraphicInfo(600.0, 110.0, 80.0, 70.0);

                SequenceFlow flow = ActivitiUtils.createSequenceFlow("tranTask" + i, tempEndId);
                List<GraphicInfo> graphicInfoList  = new ArrayList<>();
                graphicInfoList.add(ActivitiUtils.generateGraphicInfo(490.0, 175.0, 30, 30));
                graphicInfoList.add(ActivitiUtils.generateGraphicInfo(645.0, 175.0, 30, 30));

                tranTask.setOutgoingFlows(Arrays.asList(flow));

                processes.get(0).addFlowElement(tranTask);
                processes.get(0).addFlowElement(flow);


                if (i > 1) {
                    UserTask translation = (UserTask)processes.get(0).getFlowElement(tempId);
                    SequenceFlow flowAdd = ActivitiUtils.createSequenceFlow(tempId, "tranTask" + i);
                    translation.getOutgoingFlows().add(flowAdd);
                    processes.get(0).addFlowElement(flowAdd);
                }

                bpmnModel.addGraphicInfo(tranTask.getId(), graphicInfoSp);
                bpmnModel.addFlowGraphicInfoList(flow.getId(), graphicInfoList);
            }



            // 不循环,手动添加节点.--------
//            UserTask tranTask = ActivitiUtils.createUserTask("tranTask1", "翻译任务1", null);
//            UserTask tranTask2 = ActivitiUtils.createUserTask("tranTask2", "翻译任务2", null);
//            GraphicInfo graphicInfoSp = ActivitiUtils.generateGraphicInfo(600.0, 35.0, 80.0, 70.0);
//            GraphicInfo graphicInfoSp2 = ActivitiUtils.generateGraphicInfo(600.0, 110.0, 80.0, 70.0);
//
//
//            SequenceFlow flow1 = ActivitiUtils.createSequenceFlow("tranTask1", "split-project");
//            List<GraphicInfo> graphicInfoList1=new ArrayList<>();
//            graphicInfoList1.add(ActivitiUtils.generateGraphicInfo(490.0, 175.0, 30, 30));
//            graphicInfoList1.add(ActivitiUtils.generateGraphicInfo(645.0, 175.0, 30, 30));
//
//            SequenceFlow flow2 = ActivitiUtils.createSequenceFlow("translation", "tranTask2");
//            SequenceFlow flow3 = ActivitiUtils.createSequenceFlow("tranTask2", "split-project");
//
//            tranTask.setOutgoingFlows(Arrays.asList(flow1));
//            tranTask2.setIncomingFlows(Arrays.asList(flow2));
//            tranTask2.setOutgoingFlows(Arrays.asList(flow3));
//            processes.get(0).addFlowElement(tranTask);
//            processes.get(0).addFlowElement(tranTask2);
//
//            processes.get(0).addFlowElement(flow1);
//            processes.get(0).addFlowElement(flow2);
//            processes.get(0).addFlowElement(flow3);
//            UserTask translation = (UserTask)processes.get(0).getFlowElement("translation");
//            translation.getOutgoingFlows().add(flow2);
//
//            bpmnModel.addGraphicInfo(tranTask.getId(), graphicInfoSp);
//            bpmnModel.addGraphicInfo(tranTask2.getId(), graphicInfoSp2);
//            bpmnModel.addFlowGraphicInfoList(flow1.getId(), graphicInfoList1);
//            bpmnModel.addFlowGraphicInfoList(flow2.getId(), graphicInfoList1);
//            bpmnModel.addFlowGraphicInfoList(flow3.getId(), graphicInfoList1);
            // 不循环,手动添加节点.--------

            repositoryService.validateProcess(bpmnModel);

            // 输出流程图片
            DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            InputStream inputStream = generator.generateDiagram(bpmnModel, "png",
                    new ArrayList<>(), new ArrayList<>(),
                    "宋体", "宋体", "宋体",
                    processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0 );

            ActivitiUtils.writeToLocal("C:\\Users\\Administrator\\Downloads\\images\\" + processId + ".png", inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
