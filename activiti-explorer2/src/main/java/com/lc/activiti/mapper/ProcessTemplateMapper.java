package com.lc.activiti.mapper;

import com.lc.activiti.pojo.ProcessTemplate;

public interface ProcessTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProcessTemplate record);

    int insertSelective(ProcessTemplate record);

    ProcessTemplate selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProcessTemplate record);

    int updateByPrimaryKeyWithBLOBs(ProcessTemplate record);

    int updateByPrimaryKey(ProcessTemplate record);
}