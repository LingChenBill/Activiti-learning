package com.lc.activiti.utils;

import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
    public static GraphicInfo gegenerateGraphicInfo(double x, double y,
                                                     double width, double height) {
        GraphicInfo graphicInfo1 = new GraphicInfo();
        graphicInfo1.setWidth(width);
        graphicInfo1.setHeight(height);
        graphicInfo1.setX(x);
        graphicInfo1.setY(y);
        return graphicInfo1;
    }
}
