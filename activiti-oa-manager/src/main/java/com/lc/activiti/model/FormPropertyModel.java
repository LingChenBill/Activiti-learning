package com.lc.activiti.model;

import lombok.Data;

/**
 * FormPropertyModel class.
 *
 */
@Data
public class FormPropertyModel {

    private String id;

    private String name;

    private FormTypeModel formTypeModel;

    private String value;
}
