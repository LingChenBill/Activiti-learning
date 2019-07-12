package com.lc.activiti.patent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lc.activiti.pojo.ProcessTemplate;
import com.lc.activiti.service.ProcessTemplateService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

/**
 * Model控制器.
 *
 */
@RestController
@RequestMapping("/model")
public class ModelController {

    @Autowired
    ProcessEngine processEngine;

    @Autowired
    private ProcessTemplateService processTemplateService;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 新建一个空模型(与业务key关联).
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/createModel/{processTemplateId}")
    public ResponseEntity<Object> createModel(@PathVariable String processTemplateId) throws UnsupportedEncodingException {

        // 查询流程模板.
        int processTemplateIdInt = Integer.parseInt(processTemplateId);
        ProcessTemplate processTemplate = processTemplateService.selectProcessTemplateById(processTemplateIdInt);

        if (ObjectUtils.isEmpty(processTemplate)
                || ObjectUtils.isEmpty(processTemplate.getModelid())) {

            RepositoryService repositoryService = processEngine.getRepositoryService();
            // 初始化一个空模型.
            Model model = repositoryService.newModel();

            // 设置一些默认信息.
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

            // 完善ModelEditorSource.
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);

            // 保存模型.
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
        }

        return ResponseEntity.ok().build();
    }

}
