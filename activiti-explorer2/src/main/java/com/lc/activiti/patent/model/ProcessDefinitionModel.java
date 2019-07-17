package com.lc.activiti.patent.model;

import lombok.Data;

/**
 * Class: processDefinition model bean.
 *
 * @author zyz.
 */
@Data
public class ProcessDefinitionModel {

    // 流程定义ID
    private String id;
    // 部署ID
    private String deploymentId;
    // 流程定义名称
    private String name;
    // 流程定义KEY
    private String key;
    // 版本号
    private String version;
    // XML资源名称
    private String resourceName;
    // 图片资源名称
    private String diagramResourceName;
    // 流程实例.
    private String processInstanceId;
    // 执行流程节点数.
    private String taskCount;
}
