package com.lc.activiti.model;

import lombok.Data;

/**
 * 任务动态表单.
 *
 * @author zyz.
 */
@Data
public class TaskDynamicFormModel extends DynamicFormModel {

    // 表单值.
    public String value;

    // 表单元素是否可编辑。
    public boolean isWritable;
}
