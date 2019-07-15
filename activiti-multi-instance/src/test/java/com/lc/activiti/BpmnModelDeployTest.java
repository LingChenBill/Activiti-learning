package com.lc.activiti;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti.validation.ValidationError;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * Class: 通过addBpmnModel方式部署流程文件
 *
 * @author zyz.
 */
public class BpmnModelDeployTest {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti.cfg.xml");

    /**
     * Function: BpmnModel方式部署文件。
     */
    @Test
    public void testAddBpmnModel() {
        // 创建BpmnModel.
        BpmnModel bpmnModel = getBpmnModel();

        String resourceName = "shareniu_addBpmnModel";
        Deployment deploy = activitiRule.getRepositoryService()
                .createDeployment()
                .addBpmnModel(resourceName, bpmnModel)
                .deploy();
        logger.info("deploy id = {}", deploy.getId());

    }

    /**
     * Function: 校验BpmnModel实例对象。
     * { 确保BpmnModel实例对象转换成XML格式后是正确的。
     *   List<ValidationError> validate集合长度为0时，说明BpmnModel实例对象已经成功通过校验。 }
     */
    @Test
    public void testProcessValidator() {

        BpmnModel bpmnModel = getBpmnModel();
        ProcessValidatorFactory processValidatorFactory = new ProcessValidatorFactory();
        ProcessValidator defaultProcessValidator = processValidatorFactory.createDefaultProcessValidator();
        List<ValidationError> validate = defaultProcessValidator.validate(bpmnModel);
        logger.info("validate size = {}", validate.size());
    }

    /**
     * Function: 将BpmnModel转换成Xml文档。
     */
    @Test
    public void testConvert2Xml() {
        BpmnModel bpmnModel = getBpmnModel();
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        try {
            String bpmn2xml = new String(xmlConverter.convertToXML(bpmnModel), "UTF-8");
            logger.info("bpmn2xml = {}", bpmn2xml);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function: 将xml流程文档转换成BpmnModel对象。
     *
     * @throws Exception
     */
    @Test
    public void testXml2BpmnModel() throws Exception {
        String resource = "diagrams/userAndGroupInUserTask.bpmn20.xml";
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(resource);
        InputStreamSource inputStreamSource = new InputStreamSource(resourceAsStream);
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        BpmnModel bpmnModel = xmlConverter.convertToBpmnModel(inputStreamSource, true, false, "UTF-8");
        logger.info("bpmnModel = {}", ToStringBuilder.reflectionToString(bpmnModel, ToStringStyle.JSON_STYLE));
    }



    /**
     * Function: 创建BpmnModel.
     *
     * @return
     */
    private BpmnModel getBpmnModel() {
        SequenceFlow flow1 = new SequenceFlow();
        flow1.setId("flow1");
        flow1.setName("开始节点->任务节点1");
        flow1.setSourceRef("start1");
        flow1.setTargetRef("userTask1");

        // flow2的名称为"任务节点1 -> 任务节点2"
        SequenceFlow flow2 = new SequenceFlow();
        flow2.setId("flow2");
        flow2.setName("任务节点1->任务节点2");
        flow2.setSourceRef("userTask1");
        flow2.setTargetRef("userTask2");

        // flow3的名称为"任务节点2 -> 结束节点"
        SequenceFlow flow3 = new SequenceFlow();
        flow3.setId("flow3");
        flow3.setName("任务节点2->结束节点");
        flow3.setSourceRef("userTask2");
        flow3.setTargetRef("endEvent");

        // 实例化BpmnModel类。
        BpmnModel bpmnModel = new BpmnModel();
        Process process = new Process();
        process.setId("process1");
        // 封装开始节点
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start1");
        startEvent.setName("开始节点");
        startEvent.setOutgoingFlows(Arrays.asList(flow1));

        // 任务节点1
        UserTask userTask1 = new UserTask();
        userTask1.setId("userTask1");
        userTask1.setName("任务节点1");
        userTask1.setIncomingFlows(Arrays.asList(flow1));
        userTask1.setOutgoingFlows(Arrays.asList(flow2));

        // 任务节点2
        UserTask userTask2 = new UserTask();
        userTask2.setId("userTask2");
        userTask2.setName("任务节点2");
        userTask2.setIncomingFlows(Arrays.asList(flow2));
        userTask2.setOutgoingFlows(Arrays.asList(flow3));

        // 结束节点。
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("结束节点");
        endEvent.setIncomingFlows(Arrays.asList(flow3));

        // 将所有的FlowElement添加到process中。
        process.addFlowElement(startEvent);
        process.addFlowElement(flow1);
        process.addFlowElement(userTask1);
        process.addFlowElement(flow2);
        process.addFlowElement(userTask2);
        process.addFlowElement(flow3);
        process.addFlowElement(endEvent);

        bpmnModel.addProcess(process);
        return bpmnModel;
    }
}
