package com.lc.activiti.pojo;

import java.util.Date;

public class ProcessTemplate {
    private Integer id;

    private String deptname;

    private String processname;

    private String modelid;

    private Date createtime;

    private Date updatetime;

    private String flowchartid;

    private Integer ishavenew;

    private Integer istemplate;

    private String alternatefield1;

    private String alternatefield2;

    private String style;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeptname() {
        return deptname;
    }

    public void setDeptname(String deptname) {
        this.deptname = deptname == null ? null : deptname.trim();
    }

    public String getProcessname() {
        return processname;
    }

    public void setProcessname(String processname) {
        this.processname = processname == null ? null : processname.trim();
    }

    public String getModelid() {
        return modelid;
    }

    public void setModelid(String modelid) {
        this.modelid = modelid == null ? null : modelid.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getFlowchartid() {
        return flowchartid;
    }

    public void setFlowchartid(String flowchartid) {
        this.flowchartid = flowchartid == null ? null : flowchartid.trim();
    }

    public Integer getIshavenew() {
        return ishavenew;
    }

    public void setIshavenew(Integer ishavenew) {
        this.ishavenew = ishavenew;
    }

    public Integer getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(Integer istemplate) {
        this.istemplate = istemplate;
    }

    public String getAlternatefield1() {
        return alternatefield1;
    }

    public void setAlternatefield1(String alternatefield1) {
        this.alternatefield1 = alternatefield1 == null ? null : alternatefield1.trim();
    }

    public String getAlternatefield2() {
        return alternatefield2;
    }

    public void setAlternatefield2(String alternatefield2) {
        this.alternatefield2 = alternatefield2 == null ? null : alternatefield2.trim();
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style == null ? null : style.trim();
    }
}