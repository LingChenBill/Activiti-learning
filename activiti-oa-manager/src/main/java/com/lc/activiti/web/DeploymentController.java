package com.lc.activiti.web;

import com.lc.activiti.model.ProcessDefinitionModel;
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
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

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

    @RequestMapping(value = "/process-list2")
    public ModelAndView processList2() {
        ModelAndView mv = new ModelAndView("chapter5/process-list");
        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionsList = repositoryService
                .createProcessDefinitionQuery()
                .listPage(0, 100);


        String str = ToStringBuilder.reflectionToString(processDefinitionsList, ToStringStyle.JSON_STYLE);
        mv.addObject("processDefinitionList", str);

        return mv;

    }

    @RequestMapping(value = "/process-list")
    @ResponseBody
    public Object processList() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionsList = repositoryService
                .createProcessDefinitionQuery()
                .listPage(0, 100);

        // 画面Bean转换。
        List<ProcessDefinitionModel> proList = getProcessDefinitionModels(processDefinitionsList);

        return proList;
    }

    /**
     * 画面Bean变换。
     *
     * @param processDefinitionsList
     * @return
     */
    private List<ProcessDefinitionModel> getProcessDefinitionModels(List<ProcessDefinition> processDefinitionsList) {
        List<ProcessDefinitionModel> proList = new ArrayList<>();

        for (ProcessDefinition processDefinition : processDefinitionsList) {
            ProcessDefinitionModel model = new ProcessDefinitionModel();
            model.setId(processDefinition.getId());
            model.setDeploymentId(processDefinition.getDeploymentId());
            model.setName(processDefinition.getName());
            model.setKey(processDefinition.getKey());
            model.setVersion(String.valueOf(processDefinition.getVersion()));
            model.setResourceName(processDefinition.getResourceName());
            model.setDiagramResourceName(processDefinition.getDiagramResourceName());

            proList.add(model);
        }
        return proList;
    }

}
