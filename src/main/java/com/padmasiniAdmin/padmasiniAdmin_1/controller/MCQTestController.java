package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.Collections;

import com.padmasiniAdmin.padmasiniAdmin_1.service.MCQTestService;
import com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;


import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.MCQTestService;

@RestController
public class MCQTestController {
	@Autowired
	MCQTestService mcqTestService;
	
	@GetMapping("/getTest/{parentId}/{testName}")
	public ResponseEntity<?> getTest(
	    @PathVariable("parentId") String parentId,
	    @PathVariable("testName") String testName,
	    @RequestParam String dbname,
	    @RequestParam String subjectName,
	    @RequestParam String rootId) {
	    
	    try {
	        UnitRequest root = mcqTestService.getById(rootId, subjectName, dbname);
	        if (root == null) {
	            return ResponseEntity.badRequest().body("Root not found");
	        }

	        MotherMCQTest test = mcqTestService.findTestRecursive(root, parentId, testName);
	        if (test == null) {
	            return ResponseEntity.badRequest().body("Test not found");
	        }

	        return ResponseEntity.ok(test);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Server Error: " + e.getMessage());
	    }
	}
	
	@PostMapping("/addQuestion/{parentId}")
	public ResponseEntity<?> addQuestion(
	        @PathVariable("parentId") String parentId,
	        @RequestBody WrapperMCQTest question) {
	    try {
	        // enforce parentId from URL
	        question.setParentId(parentId);	

	        // check for rootId
	        if (question.getRootId() == null || question.getRootId().isBlank()) {
	            return ResponseEntity.badRequest().body("❌ rootId is required in request body");
	        }

	        String unitName = mcqTestService.addQuestion(question);
	        return ResponseEntity.ok(Collections.singletonMap("message", "✅ Question added to unit " + unitName));

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("❌ Server Error: " + e.getMessage());
	    }
	}

	@PutMapping("/updateQuestion/{parentId}/{oldName}")
	public ResponseEntity<?> updateQuestion(
	    @PathVariable("parentId") String parentId,
	    @PathVariable("oldName") String oldName,
	    @RequestBody WrapperMCQTest question) {
	    
	    System.out.println("🔄 UPDATE QUESTION ENDPOINT CALLED");
	    System.out.println("📥 Parent ID: " + parentId);
	    System.out.println("📥 Old Test Name: " + oldName);
	    System.out.println("📥 Request Body: " + question);
	    
	    try {
	        if(question.getParentId() == null || question.getParentId().isEmpty()) {
	            return ResponseEntity.badRequest().body("No Unit is selected");
	        }
	        
	        // Use the path variable parentId
	        question.setParentId(parentId);
	        
	        String unitName = mcqTestService.updateQuestion(question, oldName);
	        
	        if(unitName == null || unitName.isEmpty()) {
	            return ResponseEntity.badRequest().body("No Unit present under the provided Unit Name");
	        }
	        
	        return ResponseEntity.ok(Collections.singletonMap("message", "Test updated successfully"));
	        
	    } catch (Exception e) {
	        System.err.println("❌ EXCEPTION in updateQuestion:");
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
	    }
	}
	
	
	
	@DeleteMapping("/deleteQuestion/{parentId}")
	public ResponseEntity<?> deleteQuestion(@PathVariable("parentId") String id,@RequestBody WrapperMCQTest question) {
		System.out.println("inside delete: ");
		if(question.getParentId().isEmpty())return ResponseEntity.badRequest().body("No Unit is selected");
		question.setParentId(id);
		String unitName=mcqTestService.deleteQuestion(question);
		if(!unitName.isEmpty())System.out.println("deleted successfully");
		if(unitName.isEmpty())return ResponseEntity.badRequest().body("No Unit is present under the provided Unit Name");
		return ResponseEntity.ok(Collections.singletonMap("message", "Question added to the unit"));		//System.out.println("Received Question: " + question.getQuestion());
	}
}
