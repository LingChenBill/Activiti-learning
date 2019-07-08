package com.lc.activiti.model;

import lombok.Data;

import java.util.Map;

/**
 * 流程表单提交model.
 *
 * @author zyz.
 *
 */
@Data
public class FormVariables {

    // 流程定义ID.
    private String processDefinitionId;

    // 任务ID.
    private String taskId;

    // 表单键值对.
    private Map<String, String> formPropertiesData;

    // 用户ID.
    private String userId;
}
