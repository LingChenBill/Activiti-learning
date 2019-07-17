package com.lc.activiti;

import com.lc.activiti.utils.ActivitiUtils;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程执行过程中流程追踪的节点高亮显示.
 *
 * @author zyz.
 */
public class HightlineTest {

    private static final Logger logger = LoggerFactory.getLogger(HightlineTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg.xml");

    /**
     * 执行流程追踪显示.
     */
    @Test
    @Deployment(resources = {"diagrams/oneTaskProcess.bpmn20.xml"})
    public void testHightlineProcess() throws Exception {

        RuntimeService runtimeService = activitiRule.getRuntimeService();

        HistoryService historyService = activitiRule.getHistoryService();

        RepositoryService repositoryService = activitiRule.getRepositoryService();
        TaskService taskService = activitiRule.getTaskService();

        // 启动流程.
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("extensionOperationProcess");

        // 完成当前任务节点一.
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        // 完成当前任务节点二.
        task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        logger.info("processInstance id = {}, activity id = {}", processInstance.getId(), processInstance.getActivityId());

        String processInstanceId = processInstance.getId();

        // 获取历史流程实例.
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historicProcessInstance == null) {
            logger.error("获取流程实例失败,processInstanceId = {}", processInstanceId);
        }

        // 获取流程定义.
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());

        // 获取流程历史中已执行的节点,并按照节点在流程中执行先后顺序排序.
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceId()
                .desc()
                .list();

        // 已执行的节点ID集合.
        ArrayList<String> executedActivityIdList = new ArrayList<>();

        int index = 1;
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            // executedActivityIdList.add(historicActivityInstance.getActivityId());
            logger.info("第 {} 个已执行的节点", historicActivityInstance.getActivityId());
            index++;
        }

        if (historicActivityInstanceList != null) {
            executedActivityIdList.add(historicActivityInstanceList.get(0).getActivityId());
        }

        // 获取流程图图像字符流.
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        InputStream inputStream = generator.generateDiagram(bpmnModel, "png",
                executedActivityIdList,
                new ArrayList<>(),
                "宋体",
                "宋体",
                "宋体",
                 activitiRule.getProcessEngine().getProcessEngineConfiguration().getClassLoader(),
                1.0 );

        // 输出到本地.
        ActivitiUtils.writeToLocal("C:\\Users\\Administrator\\Downloads\\images\\hightline.png", inputStream);
    }

}
