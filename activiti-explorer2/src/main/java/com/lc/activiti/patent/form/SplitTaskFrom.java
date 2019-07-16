package com.lc.activiti.patent.form;

import lombok.Data;

/**
 * 拆分项目画面formBean.
 *
 * @author zyz.
 */
@Data
public class SplitTaskFrom {

    // 模型ID.
    private String modelId;

    // 拆分个数.
    private String splitNum;
}
