package com.lc.activiti.patent.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 流程Model.
 *
 * @author zyz.
 */
@Data
public class ProcessModel {

    // 流程名称.
    private String processName;

    // 部门名称.
    private String deptName;

    // 创建时间
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 模型ID.
    private String modelId;

    // 流程模板ID.
    private String processTemplateId;
}
