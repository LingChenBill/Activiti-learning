package com.lc.activiti;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 测试用户管理功能.
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:activiti_mysql.cfg.xml"})
public class IdentifyServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(IdentifyServiceTest.class);

    @Rule
//    @Autowired
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mysql.cfg.xml");

    /**
     * 添加用户,删除用户.
     *
     */
    @Test
    public void testUser() {
        IdentityService identityService = activitiRule.getIdentityService();

        User user = identityService.newUser("henryyan");
        user.setFirstName("Henry");
        user.setLastName("Yan");
        user.setEmail("yanhonglei@126.com");
        identityService.saveUser(user);

        User henryyan = identityService.createUserQuery().userId("henryyan").singleResult();

        Assert.assertNotNull(henryyan);

        identityService.deleteUser("henryyan");

        henryyan = identityService.createUserQuery().userId("henryyan").singleResult();

        Assert.assertNull(henryyan);
    }

    /**
     *  用户组CURD.
     */
    @Test
    public void testGroup() {
        IdentityService identityService = activitiRule.getIdentityService();

        // 新建用户组.
        Group deptLeader = identityService.newGroup("deptLeader");
        deptLeader.setName("部门领导");
        deptLeader.setType("assignment");
        identityService.saveGroup(deptLeader);

        // 查询组.
        List<Group> groupList = identityService.createGroupQuery().groupId("deptLeader").list();
        Assert.assertEquals(1, groupList.size());

        // 删除用户组.
        identityService.deleteGroup("deptLeader");
        groupList = identityService.createGroupQuery().groupId("deptLeader").list();
        Assert.assertEquals(0, groupList.size());

    }

    /**
     * 用户与用户组的关系,相互查询.
     *
     */
    @Test
    public void testUserAndGroupMemembership() {
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

        // 查询属于用户组的用户.
        User user = identityService.createUserQuery().memberOfGroup("deptLeader").singleResult();
        // logger.info("user = {}", user);
        logger.info("user = {}", ToStringBuilder.reflectionToString(user, ToStringStyle.JSON_STYLE));

        Assert.assertNotNull(user);
        Assert.assertEquals("用户ID", "henryyan", user.getId());

        // 查询用户所属组.
        Group henryyanGroup = identityService.createGroupQuery().groupMember("henryyan").singleResult();
        Assert.assertNotNull(henryyanGroup);
        Assert.assertEquals("用户组ID", "deptLeader", henryyanGroup.getId());

        // 删除用户组和用户.
        identityService.deleteMembership("henryyan", "deptLeader");
        identityService.deleteGroup("deptLeader");
        identityService.deleteUser("henryyan");

    }



}
