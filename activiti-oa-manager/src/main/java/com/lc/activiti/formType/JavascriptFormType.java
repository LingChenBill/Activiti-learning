package com.lc.activiti.formType;

import org.activiti.engine.form.AbstractFormType;

/**
 * Javascript表单字段.
 *
 */
public class JavascriptFormType extends AbstractFormType {
    @Override
    public Object convertFormValueToModelValue(String propertyValue) {
        return propertyValue;
    }

    @Override
    public String convertModelValueToFormValue(Object propertyValue) {
        return (String) propertyValue;
    }

    @Override
    public String getName() {
        return "javascript";
    }
}
