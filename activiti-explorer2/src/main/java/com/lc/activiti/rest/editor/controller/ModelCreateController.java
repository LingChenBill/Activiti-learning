package com.lc.activiti.rest.editor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lc.activiti.pojo.ProcessTemplate;
import com.lc.activiti.service.ProcessTemplateService;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 模型控制器.
 *
 * @author zyz.
 */
@RestController
@RequestMapping("models")
public class ModelCreateController {

    @Autowired
    ProcessEngine processEngine;
    @Autowired
    ObjectMapper objectMapper;

//    @Resource
    @Autowired
    private ProcessTemplateService processTemplateService;

//    /**
//     * 新建一个空模型
//     * @return
//     * @throws UnsupportedEncodingException
//     */
//    @GetMapping("newModel")
//    public String newModel() throws UnsupportedEncodingException {
//        RepositoryService repositoryService = processEngine.getRepositoryService();
//        //初始化一个空模型
//        Model model = repositoryService.newModel();
//
//        //设置一些默认信息
//        String name = "new-process";
//        String description = "";
//        int revision = 1;
//        String key = "process";
//
//        ObjectNode modelNode = objectMapper.createObjectNode();
//        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
//        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
//        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);
//
//        model.setName(name);
//        model.setKey(key);
//        model.setMetaInfo(modelNode.toString());
//
//        repositoryService.saveModel(model);
//        String id = model.getId();
//
//        //完善ModelEditorSource
//        ObjectNode editorNode = objectMapper.createObjectNode();
//        editorNode.put("id", "canvas");
//        editorNode.put("resourceId", "canvas");
//        ObjectNode stencilSetNode = objectMapper.createObjectNode();
//        stencilSetNode.put("namespace",
//                "http://b3mn.org/stencilset/bpmn2.0#");
//        editorNode.put("stencilset", stencilSetNode);
//        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
//        return "success";
//    }

    /**
     * 新建一个空模型
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/createModel/{processTemplateId}")
//    @PostMapping("/createModel/{processTemplateId}")
    public ResponseEntity<Object> createModel(@PathVariable String processTemplateId) throws UnsupportedEncodingException {

        // 查询流程模板.
        int processTemplateIdInt = Integer.parseInt(processTemplateId);
        ProcessTemplate processTemplate = processTemplateService.selectProcessTemplateById(processTemplateIdInt);

        String url = "";
        if (ObjectUtils.isEmpty(processTemplate) || ObjectUtils.isEmpty(processTemplate.getModelid())) {
            RepositoryService repositoryService = processEngine.getRepositoryService();
            //初始化一个空模型
            Model model = repositoryService.newModel();

            //设置一些默认信息
            String name = "new-process";
            String description = "";
            int revision = 1;
            String key = "process";

            ObjectNode modelNode = objectMapper.createObjectNode();
            modelNode.put("name", name);
            modelNode.put("description", description);
            modelNode.put("revision", revision);

            model.setName(name);
            model.setKey(key);
            model.setMetaInfo(modelNode.toString());

            repositoryService.saveModel(model);
            String id = model.getId();

            //完善ModelEditorSource
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace",
                    "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
            url = "/modeler.html?modelId=" + id +"&processTemplateId="+processTemplateId;
        } else {
            url = "/modeler.html?modelId=" + processTemplate.getModelid() +"&processTemplateId="+processTemplateId;
        }

        return new ResponseEntity<Object>(url, HttpStatus.OK);
    }

    /**
     * 获取所有模型
     * @return
     */
    @GetMapping("modelList")
    public List<Model> modelList(){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        List<Model> models = repositoryService.createModelQuery().list();
        return models;
    }

    /**
     * 删除模型
     * @param id
     * @return
     */
    @DeleteMapping("{id}")
    public String deleteModel(@PathVariable("id")String id){
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.deleteModel(id);
        return "success";
    }

    /**
     * 发布模型为流程定义
     * @param id
     * @return
     * @throws Exception
     */
    @PostMapping("{id}/deployment")
    public String deploy(@PathVariable("id")String id) throws Exception {

        //获取模型
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return ("模型数据为空，请先设计流程并成功保存，再进行发布。");
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            return ("数据模型不符要求，请至少设计一条主线流程。");
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);

        return "success";
    }

}
