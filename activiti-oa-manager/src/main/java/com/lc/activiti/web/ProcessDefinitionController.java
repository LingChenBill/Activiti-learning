package com.lc.activiti.web;

import com.lc.activiti.model.DynamicFormModel;
import com.lc.activiti.model.FormDataModel;
import com.lc.activiti.model.FormVariables;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

    @Autowired
    private IdentityService identityService;

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
     * 启动实例.
     *
     * @param formVariables
     * @param request
     * @return
     */
    @PostMapping(value = "/process-instance/start")
    @RequestMapping(value = "/process-instance/start", method = RequestMethod.POST)
    public ResponseEntity startProcessInstance(@RequestBody FormVariables formVariables, HttpServletRequest request) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(formVariables.getProcessDefinitionId())
                .singleResult();

        boolean hasStartFormKey = processDefinition.hasStartFormKey();

        Map<String, String> formValues = new HashMap<>();

        if(hasStartFormKey) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            Set<Map.Entry<String, String[]>> entrySet = parameterMap.entrySet();
            for (Map.Entry<String, String[]> entry : entrySet) {
                String key = entry.getKey();
                formValues.put(key, entry.getValue()[0]);
            }
        } else {
            // 动态表单.
            StartFormData startFormData = formService.getStartFormData(formVariables.getProcessDefinitionId());
            Map<String, String> formPropertiesData = formVariables.getFormPropertiesData();
            //
            List<FormProperty> formProperties = startFormData.getFormProperties();
            for (FormProperty formProperty : formProperties) {
                String value = formPropertiesData.get(formProperty.getId());
                formValues.put(formProperty.getId(), value);
            }
        }

        // 获取当前登录的用户
//        User user = UserUtil.getUserFromSession(request.getSession());
        identityService.setAuthenticatedUserId(formVariables.getUserId());

        // 提交表单字段并启动一个新的流程实例.
        ProcessInstance processInstance = formService.submitStartFormData(formVariables.getProcessDefinitionId(), formValues);
        logger.debug("start a process instance: ", processInstance);

        return new ResponseEntity(HttpStatus.OK);

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

    /**
     * 读取流程资源xml。
     *
     * @param processDefinitionId
     * @param resourceName
     * @param response
     * @return
     * @throws IOException
     */
    @GetMapping("/read-resource")
    public ResponseEntity readResource(@RequestParam("pdid") String processDefinitionId,
                                       @RequestParam("resourceName") String resourceName,
                                       HttpServletResponse response) throws IOException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);

        // 输出接口读取资源流
        byte[] b = new byte[1024];
        int len = -1;

        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }

        return ResponseEntity.ok().build();
    }

}
