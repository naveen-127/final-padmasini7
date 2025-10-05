package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class WrapperUnit {

    @Id
    private String id;
    private String dbname;
    private String rootId;
    private String parentId;
    private String subjectName;
    private String unitName;
    private List<Unit> units;

    public WrapperUnit() {}

    public WrapperUnit(String dbname, String rootId, String parentId, String subjectName, String unitName, List<Unit> units) {
        this.dbname = dbname;
        this.rootId = rootId;
        this.parentId = parentId;
        this.subjectName = subjectName;
        this.unitName = unitName;
        this.units = units;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }
}
