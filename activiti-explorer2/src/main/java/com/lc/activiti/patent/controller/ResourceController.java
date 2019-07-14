package com.lc.activiti.patent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 资源文件控制器。
 *
 * @author zyz。
 */
@Controller
public class ResourceController {

    @Autowired
    private ProcessEngine processEngine;

    /**
     * 根据模型导出xml资源文件。
     *
     * @param modelId
     * @param response
     */
    @GetMapping("/process/outputProcessXml")
    public void outputProcessXml(@RequestParam("modelId") String modelId,
                                 HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");

        RepositoryService repositoryService = processEngine.getRepositoryService();

        try {
            Model model = repositoryService.getModel(modelId);

            byte[] modelEditorSource = repositoryService.getModelEditorSource(model.getId());
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode jsonNode = new ObjectMapper().readTree(modelEditorSource);

            // 将节点信息转换成xml。
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(jsonNode);

            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);

            IOUtils.copy(in, response.getOutputStream());
            String fileName = model.getName() + ".bpmn20.xml";
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            response.flushBuffer();

        } catch (Exception e) {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            out.write("未找到对应数据");
            e.printStackTrace();
        }
    }

    /**
     * 发布模型流程。
     *
     * @param modelId
     */
    @GetMapping("/process/deploy")
    public void deploy(@RequestParam("modelId") String modelId) {

        RepositoryService repositoryService = processEngine.getRepositoryService();


        try {

            Model model = repositoryService.getModel(modelId);
            JsonNode jsonNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(model.getId()));

            byte[] bpmnBytes = null;
            BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);
            bpmnBytes = new BpmnXMLConverter().convertToXML(bpmnModel);

            String processName = model.getName() + ".bpmn20.xml";
            repositoryService.createDeployment().name(model.getName()).addString(processName, new String(bpmnBytes, "UTF-8")).deploy();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
