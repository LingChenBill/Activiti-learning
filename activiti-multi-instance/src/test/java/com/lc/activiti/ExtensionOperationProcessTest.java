package com.lc.activiti;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.impl.util.io.ResourceStreamSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Class: 文档解析xml->BpmnModel。
 *
 * @author zyz.
 */
public class ExtensionOperationProcessTest {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionOperationProcessTest.class);

    /**
     * xml->BpmnModel。
     *
     * @throws Exception
     */
    @Test
    public void testConvertToBpmnModel() {

        // 获取流程文档数据流。
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("diagrams/oneTaskProcess.bpmn20.xml");
        // 包裹文件流：InputStream -> InputStreamSource
        InputStreamSource inputStreamSource = new InputStreamSource(inputStream);

        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(inputStreamSource, false, false, "UTF-8");

        logger.info("BpmnModel = {}", ToStringBuilder.reflectionToString(bpmnModel, ToStringStyle.JSON_STYLE));

    }

    /**
     * ResourceStreamSource读取文档。
     */
    @Test
    public void testResourceStreamSource() {
        ResourceStreamSource resourceStreamSource = new ResourceStreamSource("diagrams/oneTaskProcess.bpmn20.xml");
        InputStream inputStream = resourceStreamSource.getInputStream();
        logger.info("InputStream = {}", inputStream);
    }

    @Test
    public void testGetAttributes() {
        // 获取流程文档数据流。
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("diagrams/customTask.bpmn");
        // 包裹文件流：InputStream -> InputStreamSource
        InputStreamSource inputStreamSource = new InputStreamSource(inputStream);

        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(inputStreamSource, false, false, "UTF-8");

        // 获取id为operationTask任务节点的所有信息。
        FlowElement operationTask = bpmnModel.getProcesses().get(0).getFlowElement("operationTask");

        Map<String, List<ExtensionAttribute>> attributes = operationTask.getAttributes();

        List<ExtensionAttribute> list = attributes.get("id");
        String name = list.get(0).getName();
        String value = list.get(0).getValue();
        logger.info("name = {}, value = {}", name, value);

        List<ExtensionAttribute> nameList = attributes.get("name");
        name = nameList.get(0).getName();
        value = nameList.get(0).getValue();
        logger.info("name = {}, value = {}", name, value);


    }
}
