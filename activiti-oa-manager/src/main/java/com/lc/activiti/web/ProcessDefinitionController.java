package com.lc.activiti.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lc.activiti.model.DynamicFormModel;
import com.lc.activiti.model.FormDataModel;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程定义控制类.
 *
 */
@RestController
@RequestMapping("/process")
public class ProcessDefinitionController {

    private static Logger logger = LoggerFactory.getLogger(ProcessDefinitionController.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormService formService;

    /**
     * 读取启动流程的表单字段.
     *
     * @param processDefinitionId
     * @return
     */
    @GetMapping("/getform/start")
    public ResponseEntity<Map<String,Object>> readStartForm(@RequestParam("processDefinitionId") String processDefinitionId) {

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        // 获取是否有开始formkey属性.
        boolean hasStartFormKey = processDefinition.hasStartFormKey();

        Map<String,Object> content = new HashMap<>();

        if (hasStartFormKey) {
            Object renderedStartForm = formService.getRenderedStartForm(processDefinitionId);
            FormDataModel formDataModel = new FormDataModel();
            BeanUtils.copyProperties(renderedStartForm, formDataModel);
            content.put("startFormData", renderedStartForm);
            content.put("processDefinition", processDefinition);
        } else {
            // 获取动态表单字段.
            StartFormData startFormData = formService.getStartFormData(processDefinitionId);

            FormDataModel formDataModel = new FormDataModel();
            BeanUtils.copyProperties(startFormData, formDataModel);

            List<DynamicFormModel> dynamicFormModelList = getDynamicFormModels(startFormData);

            content.put("startFormData", formDataModel);
            content.put("dynamicForm", dynamicFormModelList);
        }

        content.put("hasStartFormKey", hasStartFormKey);
        content.put("processDefinitionId", processDefinitionId);
        content.put("status", HttpStatus.OK);

        logger.info("content: = {}", ToStringBuilder.reflectionToString(content, ToStringStyle.JSON_STYLE));
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    /**
     * 画面Bean转换.
     *
     * @param startFormData
     * @return
     */
    private List<DynamicFormModel> getDynamicFormModels(StartFormData startFormData) {
        List<DynamicFormModel> dynamicFormModelList = new ArrayList<>();


        List<FormProperty> formProperties = startFormData.getFormProperties();
        for (FormProperty formProperty : formProperties) {
            DynamicFormModel formModel = new DynamicFormModel();
            formModel.setId(formProperty.getId());
            formModel.setName(formProperty.getName());
            formModel.setTypeName(formProperty.getType().getName());
            dynamicFormModelList.add(formModel);
        }
        return dynamicFormModelList;
    }

}
