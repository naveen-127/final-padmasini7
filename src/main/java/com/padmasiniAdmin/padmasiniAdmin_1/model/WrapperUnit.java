package com.padmasiniAdmin.padmasiniAdmin_1.model;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class WrapperUnit {
    private Unit unit;
    private List<MultipartFile> files;

    public Unit getUnit() {
        return unit;
    }
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }
    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }
}
