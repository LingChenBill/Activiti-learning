package com.lc.activiti;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipInputStream;

/**
 * Class: 部署方式测试.
 *
 */
public class DeploymentTest {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg.xml");

    /**
     * Function: addString: 通过字符串方式部署流程文档.
     */
    @Test
    public void testAddString() {

        String resource = "shareniu.bpmn";
        // 读取文件并获取流程文档中的xml信息。
        String text = readText("/Users/zhuyangze/Documents/fork/activiti-spring-boot2-explorer/activiti-multi-instance/src/test/resources/diagrams/userAndGroupInUserTask.bpmn20.xml");

        // 通过addString部署。
        Deployment deploy = activitiRule.getRepositoryService()
                .createDeployment().addString(resource, text)
                .deploy();

        logger.info("deploy id = {}", deploy.getId());

    }

    /**
     * Function: AddClassResource方式部署流程文档。
     */
    @Test
    public void testAddClassResource() {
        String classpath = "diagrams/userAndGroupInUserTask.bpmn20.xml";
        Deployment deploy = activitiRule.getRepositoryService().createDeployment()
                .addClasspathResource(classpath)
                .deploy();
        logger.info("deploy id = {}", deploy.getId());
    }

    /**
     * Function: inputStream部署
     *
     * @throws IOException
     */
    @Test
    public void testAddInputStream() throws IOException {
        InputStream inputStream = DeploymentTest.class.getClassLoader()
                .getResource("diagrams/userAndGroupInUserTask.bpmn20.xml")
                .openStream();
        String resource = "shareniu.bpmn";
        // inputStream部署。
        Deployment deploy = activitiRule.getRepositoryService().createDeployment()
                .addInputStream(resource, inputStream)
                .deploy();
        logger.info("deploy id = {}", deploy.getId());
    }

    /**
     * Function: 通过zipInputStream打包的方式部署流程文件。
     */
    @Test
    public void testAddZipInputStream() {
        InputStream resourceAsStream = DeploymentTest.class.getClassLoader()
                .getResourceAsStream("diagrams/userAndGroupInUserTask-with-bpmn-and-png.zip");
        ZipInputStream zipInputStream = new ZipInputStream(resourceAsStream);
        Deployment deploy = activitiRule.getRepositoryService().createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
        logger.info("deploy id = {}", deploy.getId());
    }




    /**
     * 读取xml文档。
     *
     * @param filePath
     * @return
     */
    private String readText(String filePath) {
        StringBuffer stringBuffer = new StringBuffer();
        InputStreamReader reader = null;

        try {

            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                reader = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String lineText = null;
                while ((lineText = bufferedReader.readLine()) != null) {
                    stringBuffer.append(lineText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return stringBuffer.toString();
    }


}
