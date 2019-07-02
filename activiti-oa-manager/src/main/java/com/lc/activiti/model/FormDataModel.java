package com.lc.activiti.model;

import lombok.Data;

import java.util.List;

/**
 * StartFormDataModel class.
 *
 */
@Data
public class FormDataModel {

    private String formKey;

    private String deploymentId;

    private List<FormPropertyModel> formProperties;
}
