package com.lc.activiti.patent.controller;

import com.lc.activiti.patent.model.ProcessModel;
import com.lc.activiti.pojo.ProcessTemplate;
import com.lc.activiti.service.ProcessTemplateService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
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

}
