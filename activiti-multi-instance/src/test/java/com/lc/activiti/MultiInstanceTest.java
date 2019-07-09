package com.lc.activiti;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class:Java Service任务多实例.
 *
 */
public class MultiInstanceTest {

    private static final Logger logger = LoggerFactory.getLogger(MultiInstanceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mysql.cfg.xml");

    /**
     * Function: 多实例任务.
     */
    @Test
    @Deployment(resources = {"diagrams/testMultiInstanceFixedNumbers.bpmn"})
    public void testMultiInstance() {

        Map<String, Object> variables = new HashMap<>();

        long loop = 3;

        variables.put("loop", loop);
        variables.put("counter", 0);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMultiInstanceFixedNumbers", variables);

        logger.info("processInstance = {}", ToStringBuilder.reflectionToString(processInstance, ToStringStyle.JSON_STYLE));

        Object counter = runtimeService.getVariable(processInstance.getId(), "counter");
        Assert.assertEquals("counter equals", loop, counter);
    }

    /**
     * 顺序执行用户多实例。
     */
    @Test
    @Deployment(resources = {"diagrams/testMultiInstanceForUserTask.users.sequential.bpmn"})
    public void testMultiSequentialInstance() {

        // 设置会签的3个用户。
        Map<String, Object> variables = new HashMap<>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);

        // 启动流程实例。
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);

        TaskService taskService = activitiRule.getTaskService();
        for (String userId : users) {
            // 遍历用户任务。
            Task task = taskService.createTaskQuery().taskAssignee(userId).singleResult();
            logger.info("task = {}", ToStringBuilder.reflectionToString(task, ToStringStyle.JSON_STYLE));
            taskService.complete(task.getId());
        }

    }

    /**
     * 并行方式执行用户多实例。
     */
    @Test
    @Deployment(resources = {"diagrams/testMultiInstanceForUserTask.users.nosequential.bpmn"})
    public void testMultiNoSequentialInstance() {

        // 设置会签的3个用户。
        Map<String, Object> variables = new HashMap<>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);

        // 启动流程实例。
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);

        TaskService taskService = activitiRule.getTaskService();
        for (String userId : users) {
            // 遍历验证每个用户都有一个任务。
            long count = taskService.createTaskQuery().taskAssignee(userId).count();
            Assert.assertEquals("user task nums", 1, count);
        }

    }

    /**
     * 设置结束条件的顺序多实例--现实中投票例子。
     * { 如：比率 >= 60% 即结束 }
     */
    @Test
    @Deployment(resources = {"diagrams/testMultiInstanceForUserTask.users.sequential.with.complete.conditon.bpmn"})
    public void testMultiConditionSequentialInstance() {

        // 设置会签的3个用户。
        Map<String, Object> variables = new HashMap<>();
        List<String> users = Arrays.asList("user1", "user2", "user3");
        variables.put("users", users);
        // 设置比率
        variables.put("rate", 0.6d);

        // 启动流程实例。
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        runtimeService.startProcessInstanceByKey("testMultiInstanceForUserTask", variables);

        TaskService taskService = activitiRule.getTaskService();

        // 第一个用户完成任务。
        Task user1Task = taskService.createTaskQuery().taskAssignee("user1").singleResult();
        taskService.complete(user1Task.getId());

        // 第二个用户完成任务。
        Task user2Task = taskService.createTaskQuery().taskAssignee("user2").singleResult();
        taskService.complete(user2Task.getId());

        // 此时该流程用户任务应该符合结束条件，验证是否已经结束。
        long count = activitiRule.getHistoryService().createHistoricProcessInstanceQuery().finished().count();
        Assert.assertEquals("流程任务结束的个数：", 1, count);
    }


}
