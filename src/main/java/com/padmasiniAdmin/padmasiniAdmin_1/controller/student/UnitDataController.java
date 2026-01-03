// File: /home/ec2-user/final-padmasini7/src/main/java/com/padmasiniAdmin/padmasiniAdmin_1/controller/student/UnitDataController.java
package com.padmasiniAdmin.padmasiniAdmin_1.controller.student;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperUnit;
import com.padmasiniAdmin.padmasiniAdmin_1.service.StudentDataService; // Correct import

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UnitDataController {
    
    @Autowired
    private StudentDataService service;
    
    @PostMapping("/getUnits")
    public List<UnitRequest> getUnits(@RequestBody WrapperUnit request) {
        return service.getUnitData(request.getSubjectName(), request);
    }
}
