package com.lc.activiti.service.impl;

import com.lc.activiti.mapper.ProcessTemplateMapper;
import com.lc.activiti.pojo.ProcessTemplate;
import com.lc.activiti.service.ProcessTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Class: ProcessTemplate服务实现类.
 *
 * @author zyz.
 */
@Service("processTemplateService")
public class ProcessTemplateServiceImpl implements ProcessTemplateService {

//    @Resource
    @Autowired
    private ProcessTemplateMapper processTemplateMapper;

    /**
     * 按id查询.
     *
     * @param id
     * @return
     */
    @Override
    public ProcessTemplate selectProcessTemplateById(int id) {
        return processTemplateMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询业务流程列表数据.
     *
     * @return
     */
    @Override
    public List<ProcessTemplate> selectProcessTemplateList() {
        return processTemplateMapper.selectProcessTemplateList();
    }

    /**
     * 更新ProcessTemplate.
     *
     * @param processTemplate
     * @return
     */
    @Override
    public int updateProcessTemplate(ProcessTemplate processTemplate) {
        return processTemplateMapper.updateByPrimaryKey(processTemplate);
    }


}
