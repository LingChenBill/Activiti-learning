package com.lc.activiti.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 任务列表model.
 *
 */
@Data
public class TaskModel {

    // 任务ID.
    private String id;

    // 任务名称.
    private String name;

    // 流程实例ID.
    private String processInstanceId;

    // 流程定义ID.
    private String processDefinitionId;

    // 创建时间.
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 办理人.
    private String assignee;

}
