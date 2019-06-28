package com.lc.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Class:压缩包方式部署流程定义文件.
 *
 */
public class ZipStreamDeploymentTest {


    private static final Logger logger = LoggerFactory.getLogger(ZipStreamDeploymentTest.class);


    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mysql.cfg.xml");

    /**
     * Function: 用classpath的方式部署流程定义文件.
     * { xxx.bar: 用压缩包方式部署可以把多个资源文件打包成一个扩展名为bar的压缩文件. }
     */
    @Test
    public void testClasspathDeployment() {

        InputStream zipStream = this.getClass().getClassLoader()
                .getResourceAsStream("user-group.bar");

        RepositoryService repositoryService = activitiRule.getRepositoryService();

        repositoryService.createDeployment()
                .addZipInputStream(new ZipInputStream(zipStream))
                .deploy();

        // 统计已经部署好的流程定义的个数.
        long processDefinitionCount = repositoryService.createProcessDefinitionQuery().count();
        Assert.assertEquals("流程个数", 2, processDefinitionCount);

        Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
        Assert.assertNotNull(deployment);

        String deploymentId = deployment.getId();

        Assert.assertNotNull("candidateUserInUserTask.bpmn",
                repositoryService.getResourceAsStream(deploymentId, "candidateUserInUserTask.bpmn"));

        Assert.assertNotNull("candidateUserInUserTask.png",
                repositoryService.getResourceAsStream(deploymentId, "candidateUserInUserTask.png"));

        Assert.assertNotNull("userAndGroupInUserTask.bpmn",
                repositoryService.getResourceAsStream(deploymentId, "userAndGroupInUserTask.bpmn"));

        Assert.assertNotNull("userAndGroupInUserTask.png",
                repositoryService.getResourceAsStream(deploymentId, "userAndGroupInUserTask.png"));

    }
}
