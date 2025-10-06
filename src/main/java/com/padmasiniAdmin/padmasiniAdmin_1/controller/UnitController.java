package com.padmasiniAdmin.padmasiniAdmin_1.model;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public class WrapperUnit {

    private Unit unit;  // ✅ changed from String to Unit

    @NotBlank(message = "Parent ID is required")
    private String parentId;

    @NotBlank(message = "Standard is required")
    private String standard;

    private List<String> keepAudioFileIds;
    private String dbname;
    private String rootUnitId;

    private String subjectName;

    // --- Getters & Setters ---
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public List<String> getKeepAudioFileIds() { return keepAudioFileIds; }
    public void setKeepAudioFileIds(List<String> keepAudioFileIds) { this.keepAudioFileIds = keepAudioFileIds; }

    public String getDbname() { return dbname; }
    public void setDbname(String dbname) { this.dbname = dbname; }

    public String getRootUnitId() { return rootUnitId; }
    public void setRootUnitId(String rootUnitId) { this.rootUnitId = rootUnitId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getParentId() {
        if ((parentId == null || parentId.isEmpty()) && unit != null) {
            return unit.getId();  // ✅ fallback to unit's ID
        }
        return parentId;
    }
    public void setParentId(String parentId) { this.parentId = parentId; }

    @Override
    public String toString() {
        return "WrapperUnit [parentId=" + parentId + ", unit=" + unit + "]";
    }
}
