package com.padmasiniAdmin.padmasiniAdmin_1.model;

public class WrapperUnitRequest {
    private String dbname;
    private String rootId;
    private String subjectName;
    private String unitName;
    private String standard; // if used anywhere

    // Getters & Setters
    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getRootId() { return rootId; }
    public void setRootId(String rootId) { this.rootId = rootId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
}
