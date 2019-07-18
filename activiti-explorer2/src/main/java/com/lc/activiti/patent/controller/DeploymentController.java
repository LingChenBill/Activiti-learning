package com.lc.activiti.patent.controller;

import com.lc.activiti.patent.model.ProcessDefinitionModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * Class: 部署流程控制层.
 *
 * @author zyz.
 */
@RestController
@RequestMapping(value = "/deploy")
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @GetMapping(value = "/process-list")
    @ResponseBody
    public Object processList() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionsList = repositoryService
                .createProcessDefinitionQuery()
                .orderByDeploymentId()
                .desc()
                .listPage(0, 100);


        // 画面Bean转换。
        List<ProcessDefinitionModel> proList = getProcessDefinitionModels(processDefinitionsList);

        return proList;
    }

    /**
     * 文件上传，并发布流程。
     * { 文件格式为zip， bar， xml }
     *
     * @param file
     * @return
     */
    @PostMapping(value = "uploadDeploy")
    public ResponseEntity uploadDeploy(@RequestParam(value = "file") MultipartFile file) {
        String filename = file.getOriginalFilename();

        try {
            InputStream inputStream = file.getInputStream();
            String extension = FilenameUtils.getExtension(filename);

            RepositoryService repositoryService = processEngine.getRepositoryService();
            DeploymentBuilder deployment = repositoryService.createDeployment();

            if ("zip".equals(extension) || "bar".equals(extension)) {
                deployment.addZipInputStream(new ZipInputStream(inputStream));
            } else if ("xml".equals(extension) || "bpmn".equals(extension)) {
                deployment.addInputStream(filename, inputStream);
            } else {
                logger.info("文件格式有问题，请重新选择！文件后缀为 = {}", extension);
                return new ResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }

            deployment.deploy();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 读取流程资源(包括xml和图片文件都可以读取)。
     * { http://localhost:8082/chapter05/read-resource?pdid=userAndGroupInUserTask:4:7506&resourceName=userAndGroupInUserTask.bpmn }
     *
     * @param processDefinitionId
     * @param resouceName
     * @param response
     */
    @RequestMapping(value = "/read-resource")
    public void readResource(@RequestParam("pdid") String processDefinitionId,
                             @RequestParam("resourceName") String resouceName,
                             HttpServletResponse response) throws IOException {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        // 通过接口读取资源流.
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resouceName);


        // 输出资源内容到相应对象。
        byte[] b = new byte[1024];
        int len = -1;

        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }

    }

    /**
     * 删除部署.
     *
     * @param deploymentId
     * @return
     */
    @GetMapping(value = "/delete-deployment")
    public void deleteProcessDefinition(@RequestParam("deploymentId") String deploymentId) {

        repositoryService.deleteDeployment(deploymentId, true);

    }

    /**
     * 启动流程.
     *
     * @param processDefinitionKey
     */
    @GetMapping("/startProcess")
    public ResponseEntity<Map<String, Object>> startProcess(@RequestParam("processDefinitionKey") String processDefinitionKey) {
        // 启动流程.
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);

        logger.info("processInstanceId = {}", processInstance.getId());

        // 流程实例ID.
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("processInstanceId", processInstance.getId());

        return new ResponseEntity<>(resMap, HttpStatus.OK);
    }

    /**
     * 完成任务.
     *
     * @param processInstanceId
     */
    @GetMapping("/completeTask")
    public void startTask(@RequestParam("processInstanceId") String processInstanceId) {

        // 完成当前任务节点.
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        if ("译腾".equals(task.getName())) {
            Map<String, Object> variables = new HashMap<>();
            // 默认走第一个分支.
            variables.put("pass", 1);
            taskService.complete(task.getId(), variables);
        } else {
            taskService.complete(task.getId());
        }

        logger.info("task id = {}", task.getId());
    }

    /**
     * 画面Bean变换。
     *
     * @param processDefinitionsList
     * @return
     */
    private List<ProcessDefinitionModel> getProcessDefinitionModels(List<ProcessDefinition> processDefinitionsList) {
        List<ProcessDefinitionModel> proList = new ArrayList<>();

        for (ProcessDefinition processDefinition : processDefinitionsList) {
            ProcessDefinitionModel model = new ProcessDefinitionModel();
            model.setId(processDefinition.getId());
            model.setDeploymentId(processDefinition.getDeploymentId());
            model.setName(processDefinition.getName());
            model.setKey(processDefinition.getKey());
            model.setVersion(String.valueOf(processDefinition.getVersion()));
            model.setResourceName(processDefinition.getResourceName());
            model.setDiagramResourceName(processDefinition.getDiagramResourceName());

            // 获取流程实例.
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(processDefinition.getKey())
                    .singleResult();

            if (processInstance != null) {
                // 设置流程实例ID.
                model.setProcessInstanceId(processInstance.getId());

                // 获取流程历史中已执行的节点,并按照节点在流程中执行先后顺序排序.
                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .orderByHistoricActivityInstanceId()
                        .desc()
                        .list();

                // 执行流程节点个数.
                model.setTaskCount(String.valueOf(historicActivityInstanceList.size()));
            }

            proList.add(model);
        }
        return proList;
    }

}
