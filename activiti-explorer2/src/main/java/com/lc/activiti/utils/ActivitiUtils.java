package com.lc.activiti.utils;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.List;

/**
 * Activiti流程处理工具类。
 *
 * @author zyz.
 */
public class ActivitiUtils {

    /**
     * 创建任务。
     *
     * @param id
     * @param name
     * @param assignee
     * @return
     */
    public static UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setId(id);
        userTask.setName(name);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /**
     * 创建顺序流。
     *
     * @param from
     * @param to
     * @return
     */
    public static SequenceFlow createSequenceFlow(String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    /**
     * 创建排它网关。
     *
     * @param id
     * @return
     */
    public static ExclusiveGateway createExclusiveGateway(String id) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        return exclusiveGateway;
    }

    /**
     * 流程资源文件导出。
     *
     * @param destination
     * @param input
     * @throws IOException
     */
    public static void writeToLocal(String destination, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        downloadFile.close();
        input.close();
    }

    /**
     * 流程图形信息。
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public static GraphicInfo generateGraphicInfo(double x, double y,
                                                     double width, double height) {
        GraphicInfo graphicInfo1 = new GraphicInfo();
        graphicInfo1.setWidth(width);
        graphicInfo1.setHeight(height);
        graphicInfo1.setX(x);
        graphicInfo1.setY(y);
        return graphicInfo1;
    }

    /**
     * 修正流程图的绘图信息(起始点信息).
     * @param model
     * @return
     */
    public static BpmnModel fixGraphicInfo(BpmnModel model) {
        for(org.activiti.bpmn.model.Process p : model.getProcesses()) {
            for(FlowElement e : p.getFlowElements()) {
                if (e instanceof SequenceFlow) {
                    /**
                     * 数据加工任务的自定义组件 与顺序流箭头连接后，箭头的起始位置没有被保存。导致图片导出时报错（数组越界）
                     * 在此手动补充添加箭头的起始位置
                     */
                    List<GraphicInfo> infos = model.getFlowLocationGraphicInfo(e.getId());
                    GraphicInfo incomingGI = model.getGraphicInfo(((SequenceFlow) e).getSourceRef());
                    GraphicInfo outgoingGI = model.getGraphicInfo(((SequenceFlow) e).getTargetRef());
                    if (infos.size() < 2) {
                        infos.clear();
                        infos.add(generateGraphicInfo(incomingGI.getX(), incomingGI.getY(), 30, 30));
                        infos.add(generateGraphicInfo(outgoingGI.getX(), outgoingGI.getY(), 30, 30));
                    }
                }
            }
        }
        return model;
    }

    /**
     * 将流程图模型 转换为xml再转换回Model
     * 应对图片导出时的瑕疵
     * @param model
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLStreamException
     */
    public static BpmnModel refreshXml(BpmnModel model) throws UnsupportedEncodingException, XMLStreamException {
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        String bpmn20Xml = new String(bpmnXMLConverter.convertToXML(model), "UTF-8");

        InputStream stream = new ByteArrayInputStream(bpmn20Xml.getBytes("UTF-8"));
        InputStreamReader in = new InputStreamReader(stream, "UTF-8");

        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xtr = xif.createXMLStreamReader(in);

        return bpmnXMLConverter.convertToBpmnModel(xtr);
    }
}
