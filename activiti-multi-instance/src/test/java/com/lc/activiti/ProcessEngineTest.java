package com.lc.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Map;

/**
 * Spring风格配置风格的测试类。
 *
 * @author zyz.
 */
public class ProcessEngineTest {

    @Test
    public void testBean() {

        // 实例化ApplicationContext类。
        ApplicationContext applicationContext = new GenericXmlApplicationContext("activiti-content.xml");
        // 获取ProcessEngine类型的实例对象。
        Map<String, ProcessEngine> beansOfType = applicationContext.getBeansOfType(ProcessEngine.class);
        // 取出第一个ProcessEngine实例对象。
        ProcessEngine pe = beansOfType.values().iterator().next();
        // 获取任务服务类TaskService实例对象。
        TaskService taskService = pe.getTaskService();
        Assert.assertNotNull("not null", taskService);
        Assert.assertNotNull("not null", pe);

    }

    @Test
    public void getDefaultProcessEngine() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Assert.assertNotNull("not null", taskService);
    }

}
