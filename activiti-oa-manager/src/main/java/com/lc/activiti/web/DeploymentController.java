package com.lc.activiti.web;

import com.lc.activiti.model.ProcessDefinitionModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
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
 */
@RestController
@RequestMapping(value = "/chapter05")
public class DeploymentController {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentController.class);

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping(value = "/process-list")
    @ResponseBody
    public Object processList() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionsList = repositoryService
                .createProcessDefinitionQuery()
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

            proList.add(model);
        }
        return proList;
    }

}
