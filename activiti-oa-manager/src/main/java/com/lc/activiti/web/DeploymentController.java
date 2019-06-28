package com.lc.activiti.web;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;

/**
 * Class: 部署流程控制层.
 *
 */
//@RestController
@Controller
@RequestMapping(value = "/chapter05")
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

    @Autowired
    private ProcessEngine processEngine;

    @RequestMapping(value = "/process-list")
    public ModelAndView processList() {
        ModelAndView mv = new ModelAndView("chapter5/process-list");
        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionsList = repositoryService
                .createProcessDefinitionQuery()
                .listPage(0, 100);


        String str = ToStringBuilder.reflectionToString(processDefinitionsList, ToStringStyle.JSON_STYLE);
        mv.addObject("processDefinitionList", str);

        return mv;

    }

    @RequestMapping(value = "/process-list2")
    @ResponseBody
//    @JsonSerialize(include = NON_NULL)
    public Object processList2() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionsList = repositoryService
                .createProcessDefinitionQuery()
                .listPage(0, 100);

        String str = ToStringBuilder.reflectionToString(processDefinitionsList, ToStringStyle.JSON_STYLE);
        logger.info("processDefinitionsList = {}", str);

        return str;
//        return "hello activiti";
    }

}
