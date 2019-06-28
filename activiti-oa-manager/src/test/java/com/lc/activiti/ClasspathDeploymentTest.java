package com.lc.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Class:部署流程定义文件.
 *
 */
public class ClasspathDeploymentTest {


    private static final Logger logger = LoggerFactory.getLogger(ClasspathDeploymentTest.class);


    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mysql.cfg.xml");

    /**
     * Function: 用classpath的方式部署流程定义文件.
     */
    @Test
    public void testClasspathDeployment() {
        // 定义path.
        String bpmnClassPath = "user-group.bpmn20.xml";
        String pngClassPath = "user-group.bpmn20.png";

        RepositoryService repositoryService = activitiRule.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource(bpmnClassPath);
        deploymentBuilder.addClasspathResource(pngClassPath);

        deploymentBuilder.deploy();

        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("userAndGroupInUserTask")
                .count();

        Assert.assertEquals("部署的流程定义个数", 1, count);

        String diagramResourceName = repositoryService.createProcessDefinitionQuery()
                .singleResult()
                .getDiagramResourceName();

        Assert.assertEquals("图片文件名称", "user-group.userAndGroupInUserTask.png" , diagramResourceName);

    }

    /**
     * Function: 用InputStream的方式部署流程资源文件.
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testInputStreamFromAbsoluteFilePath() throws FileNotFoundException {
        String filePath = "D:\\A_patent\\github\\activiti-spring-boot2-explorer\\activiti-oa-manager\\src\\test\\resources\\user-group.bpmn";
        // 读取classpath的资源作为
        FileInputStream fileInputStream = new FileInputStream(filePath);

        RepositoryService repositoryService = activitiRule.getRepositoryService();
        repositoryService.createDeployment()
                .addInputStream("user-group.bpmn", fileInputStream)
                .deploy();

        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("userAndGroupInUserTask")
                .count();

        Assert.assertEquals("流程个数", 1, count);
    }

}
