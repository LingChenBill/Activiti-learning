package com.lc.activiti.configurator;

import org.activiti.engine.cfg.AbstractProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * 自定义配置器。
 *
 * @author zyz.
 */
public class MyConfigurator extends AbstractProcessEngineConfigurator {

    public int getPriority() {
        return 1;
    }

    public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
       System.out.println("A: beforeInit");
       processEngineConfiguration.setDatabaseSchemaUpdate("true");
    }

    public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
        System.out.println("A: configure");
    }
}
