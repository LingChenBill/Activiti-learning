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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            List<Process> processes = bpmnModel.getProcesses();
            String processId = processes.get(0).getId();
            logger.info("processes size = {}", processes.size());

            UserTask tranTask = ActivitiUtils.createUserTask("tranUserTask", "翻译任务", null);
            UserTask controlTask = ActivitiUtils.createUserTask("conUserTask", "内控", null);
            SequenceFlow flow1 = ActivitiUtils.createSequenceFlow("tranUserTask", "conUserTask");
            SequenceFlow flow2 = ActivitiUtils.createSequenceFlow("conUserTask", "splitTask");
            // SequenceFlow flow3 = ActivitiUtils.createSequenceFlow("tranUserTask", "conUserTask");
//

            List<String> changedIds = new ArrayList<String>();
            String tempId = null;
            String endTempId = null;

            for(FlowElement e : processes.get(0).getFlowElements()) {
                //System.out.println(e.getId() + ":" + e.getName());
                if ("译腾".equals(e.getName())) {
                    tempId = e.getId();
                    changedIds.add(e.getId());
                }

                if ("拆分".equals(e.getName())) {
                    endTempId = e.getId();
//                    changedIds.add(e.getId());
                }

            }

            for (FlowElement e: processes.get(0).getFlowElements()) {

                if (e instanceof SequenceFlow) {
                    SequenceFlow flow = (SequenceFlow)e;
                    if (flow.getSourceRef().equals(tempId)) {
                        flow.setTargetRef("tranUserTask");
                    } else if (flow.getTargetRef().equals(endTempId)) {
                        flow.setSourceRef("conUserTask");
                    }
                }
            }




            processes.get(0).addFlowElement(tranTask);
            processes.get(0).addFlowElement(controlTask);
            processes.get(0).addFlowElement(flow1);
            processes.get(0).addFlowElement(flow2);

            GraphicInfo graphicInfoSp = ActivitiUtils.gegenerateGraphicInfo(425, 75, 140, 100);

            List<GraphicInfo> graphicInfoList1=new ArrayList<>();
            graphicInfoList1.add(ActivitiUtils.gegenerateGraphicInfo(300, 105, 30, 30));
            graphicInfoList1.add(ActivitiUtils.gegenerateGraphicInfo(425, 75, 30, 30));

            List<GraphicInfo> graphicInfoList2=new ArrayList<>();
            graphicInfoList2.add(ActivitiUtils.gegenerateGraphicInfo(425, 75, 30, 30));
            graphicInfoList2.add(ActivitiUtils.gegenerateGraphicInfo(641.5, 92, 30, 30));

//            bpmnModel.addGraphicInfo();
            bpmnModel.addFlowGraphicInfoList(flow1.getId(), graphicInfoList1);
            bpmnModel.addFlowGraphicInfoList(flow2.getId(), graphicInfoList2);

            repositoryService.validateProcess(bpmnModel);

            // 输出流程图片
            DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
            InputStream inputStream = generator.generateDiagram(bpmnModel, "png",
                    new ArrayList<>(), new ArrayList<>(),
                    "宋体", "宋体", "宋体",
                    processEngine.getProcessEngineConfiguration().getClassLoader(), 1.0 );

            ActivitiUtils.writeToLocal("/Users/zhuyangze/Desktop/" + processId + ".png", inputStream);
//            MyTest.writeToLocal("d:\\a.png", inputStream);



        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
