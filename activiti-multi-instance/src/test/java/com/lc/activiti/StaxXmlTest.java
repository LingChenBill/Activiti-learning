package com.lc.activiti;

import org.apache.commons.io.input.XmlStreamReader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.Reader;

/**
 * Class: Stax.xml文档解析。
 *
 * @author zyz.
 */
public class StaxXmlTest {

    private static final Logger logger = LoggerFactory.getLogger(StaxXmlTest.class);

    /**
     * Stax方式解析xml文档。
     *
     * @throws Exception
     */
    @Test
    public void testStaxXml() throws Exception {

        // 使用XMLInputFactory工厂。
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // 使用类加载器加载资源文件。
        InputStream inputStream = StaxXmlTest.class.getClassLoader().getResourceAsStream("diagrams/stax.xml");

        // 使用Reader读取资源文件流。
        Reader reader = new XmlStreamReader(inputStream);
        // 根据reader创建XMLStreamReader实例对象。
        XMLStreamReader xtr = xmlInputFactory.createXMLStreamReader(reader);

        while (xtr.hasNext()) {
            int event = xtr.next();
            if (event == XMLStreamConstants.START_DOCUMENT) {
                logger.info("文档开始。");
            } else if (event == XMLStreamConstants.END_DOCUMENT) {
                logger.info("文档结束。");
            } else if (event == XMLStreamConstants.START_ELEMENT) {
                // 文档开始节点。
                logger.info("节点开始解析，name = {}", xtr.getLocalName());

                if ("userTask".equals(xtr.getLocalName()) || "endEvent".equals(xtr.getLocalName())) {
                    for (int i = 0; i < xtr.getAttributeCount(); i++) {
                        if (xtr.getAttributeName(i).toString().startsWith("{")) {
                            logger.info("命名空间--->, {}", xtr.getAttributeValue("http://activiti.org/bpmn", "assignee"));
                        } else {
                            logger.info("xtr.getAttributeName = {}, xtr.getAttributeValue = {}", xtr.getAttributeName(i), xtr.getAttributeValue(i));
                        }
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                logger.info("结束节点");
            } else if (event == XMLStreamConstants.CHARACTERS) {
                String text = xtr.getText();
                logger.info("text string = {}", text);
            }
        }

    }
}
