package com.lc.activiti.service;

import com.lc.activiti.pojo.ProcessTemplate;

import java.util.List;

/**
 * Interface: ProcessTemplate服务接口.
 *
 * @author zyz.
 */
public interface ProcessTemplateService {

    /**
     * 根据ID查询.
     *
     * @param id
     * @return
     */
    ProcessTemplate selectProcessTemplateById(int id);

    int updateProcessTemplate(ProcessTemplate processTemplate);


    List<ProcessTemplate> selectProcessTemplateList();
}
