package com.lc.activiti;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class: 请假流程测试。
 *
 */
public class LeaveDynamicFormTest {


    private static final Logger logger = LoggerFactory.getLogger(UserAndGroupInUserTaskTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mysql.cfg.xml");

    @Test
    @Deployment(resources = "dynamic-form/leave.bpmn")
    public void allApproved() {
        String currentUserId = "henryyan";

        IdentityService identityService = activitiRule.getIdentityService();
        identityService.setAuthenticatedUserId(currentUserId);

        RepositoryService repositoryService = activitiRule.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("leave")
                .singleResult();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        HashMap<String, String> variables = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        String startDate = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        String endDate = sdf.format(calendar.getTime());

        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");

        // 使用FormService的启动流程方法。
        FormService formService = activitiRule.getFormService();
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        logger.info("processInstance = {}", processInstance);
        Assert.assertNotNull(processInstance);

        // 部门领导审批通过。
        TaskService taskService = activitiRule.getTaskService();
        Task deptLeaderTask = taskService.createTaskQuery()
                .taskCandidateGroup("deptLeader")
                .singleResult();

        variables = new HashMap<>();
        // 设置审批通过标志。
        variables.put("deptLeaderApproved", "true");
        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        // 人事审批通过。
        Task hrTask = taskService.createTaskQuery()
                .taskCandidateGroup("hr")
                .singleResult();
        variables = new HashMap<>();
        variables.put("hrApproved", "true");
        formService.submitTaskFormData(hrTask.getId(), variables);

        // 销假（根据申请人的用户ID读取）。
        Task reportBackTask = taskService.createTaskQuery()
                .taskAssignee(currentUserId)
                .singleResult();
        variables = new HashMap<>();
        // 设置销假日期。
        variables.put("reportBackDate", sdf.format(calendar.getTime()));
        formService.submitTaskFormData(reportBackTask.getId(), variables);

        // 验证流程是否已经结束。
        HistoryService historyService = activitiRule.getHistoryService();
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .singleResult();
        Assert.assertNotNull(historicProcessInstance);

        Map<String, Object> historyVariables = packageVariablesMap(processInstance, historyService);

        // 验证执行结果。
        Assert.assertEquals("ok", historyVariables.get("result"));

    }

    private Map<String, Object> packageVariablesMap(ProcessInstance processInstance, HistoryService historyService) {
        Map<String, Object> historyVariables = new HashMap<>();
        List<HistoricDetail> list = historyService.createHistoricDetailQuery()
                .processInstanceId(processInstance.getId())
                .list();
        for (HistoricDetail historicDetail : list) {
            if (historicDetail instanceof HistoricFormProperty) {
                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
                logger.info("form filed: taskId = {}, field propertyId = {}", field.getTaskId(), field.getPropertyId());
            } else if (historicDetail instanceof HistoricVariableUpdate) {
                HistoricVariableUpdate variableUpdate = (HistoricVariableUpdate) historicDetail;
                historyVariables.put(variableUpdate.getVariableName(), variableUpdate.getValue());
                logger.info("variableUpdate name = {}, value = {}", variableUpdate.getVariableName(), variableUpdate.getValue());

            }
        }
        return historyVariables;
    }


}
