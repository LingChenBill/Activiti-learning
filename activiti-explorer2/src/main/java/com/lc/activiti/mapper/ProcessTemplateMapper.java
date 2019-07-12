package com.lc.activiti.mapper;

import com.lc.activiti.pojo.ProcessTemplate;

import java.util.List;

public interface ProcessTemplateMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProcessTemplate record);

    int insertSelective(ProcessTemplate record);

    ProcessTemplate selectByPrimaryKey(Integer id);

    List<ProcessTemplate> selectProcessTemplateList();

    int updateByPrimaryKeySelective(ProcessTemplate record);

    int updateByPrimaryKeyWithBLOBs(ProcessTemplate record);

    int updateByPrimaryKey(ProcessTemplate record);
}