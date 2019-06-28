package com.lc.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.AbstractTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试用户任务中的用户与组.
 *
 */
public class UserAndGroupInUserTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(UserAndGroupInUserTaskTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mysql.cfg.xml");

    @Before
    public void setUp() {

        IdentityService identityService = activitiRule.getIdentityService();

        // 创建并保存用户组对象.
        Group deptLeaderGroup = identityService.newGroup("deptLeader");
        deptLeaderGroup.setName("部门领导");
        deptLeaderGroup.setType("assignment");
        identityService.saveGroup(deptLeaderGroup);

        User henryyan = identityService.newUser("henryyan");
        henryyan.setFirstName("Henry");
        henryyan.setLastName("yan");
        henryyan.setEmail("Henryyan@163.com");
        identityService.saveUser(henryyan);

        // 把用户加入到组.
        identityService.createMembership("henryyan", "deptLeader");
    }

    /**
     *  用户任务中的候选组.
     */
    @Test
    @Deployment(resources = {"user-group.bpmn20.xml"})
    public void testUserAndGroupInUserTask() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userAndGroupInUserTask");
        logger.info("processInstance = {}", ToStringBuilder.reflectionToString(processInstance, ToStringStyle.JSON_STYLE));

        Assert.assertNotNull(processInstance);

        // 根据角色查询任务.
        TaskService taskService = activitiRule.getTaskService();
        Task henryyanTask = taskService.createTaskQuery().taskCandidateUser("henryyan").singleResult();
        taskService.claim(henryyanTask.getId(), "henryyan");
        taskService.complete(henryyanTask.getId());
    }

    // test git.

    @After
    public void afterInvokeTestMethod() {
        IdentityService identityService = activitiRule.getIdentityService();
        // 删除用户组和用户.
        identityService.deleteMembership("henryyan", "deptLeader");
        identityService.deleteGroup("deptLeader");
        identityService.deleteUser("henryyan");
    }

}
