package com.padmasiniAdmin.padmasiniAdmin_1.model;

public class WrapperUnit {
    private String dbname;
    private String rootId;
    private String subjectName;
    private String unitId;
    private String parentUnitId; // optional for nested units
    private String unitName;

    // Getters & Setters
    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getRootId() { return rootId; }
    public void setRootId(String rootId) { this.rootId = rootId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getUnitId() { return unitId; }
    public void setUnitId(String unitId) { this.unitId = unitId; }

    public String getParentUnitId() { return parentUnitId; }
    public void setParentUnitId(String parentUnitId) { this.parentUnitId = parentUnitId; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
}
