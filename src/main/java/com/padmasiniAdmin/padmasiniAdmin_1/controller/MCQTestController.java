package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import com.padmasiniAdmin.padmasiniAdmin_1.model.MCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;


import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.service.MCQTestService;

@RestController
@RequestMapping("/api")
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
	    
	    System.out.println("🔄 ========== UPDATE QUESTION CONTROLLER CALLED ==========");
	    System.out.println("📥 Parent ID from URL: " + parentId);
	    System.out.println("📥 Old Test Name from URL: " + oldName);
	    System.out.println("📥 Request Body Details:");
	    System.out.println("  - RootId: " + question.getRootId());
	    System.out.println("  - ParentId: " + question.getParentId());
	    System.out.println("  - TestName: " + question.getTestName());
	    System.out.println("  - UnitName: " + question.getUnitName());
	    System.out.println("  - Marks: " + question.getMarks());
	    System.out.println("  - Questions Count: " + (question.getQuestionsList() != null ? question.getQuestionsList().size() : 0));
	    System.out.println("  - Dbname: " + question.getDbname());
	    System.out.println("  - SubjectName: " + question.getSubjectName());

	    // Log first question details
	    if (question.getQuestionsList() != null && !question.getQuestionsList().isEmpty()) {
	        System.out.println("📝 First Question Details:");
	        MCQTest firstQ = question.getQuestionsList().get(0);
	        System.out.println("  - Question: " + firstQ.getQuestion());
	        System.out.println("  - Explanation: " + firstQ.getExplanation());
	        System.out.println("  - Correct Index: " + firstQ.getCorrectIndex());
	        System.out.println("  - Question Images: " + firstQ.getQuestionImages());
	        System.out.println("  - Solution Images: " + firstQ.getSolutionImages());
	    }

	    try {
	        // Enhanced validation
	        if (question.getParentId() == null || question.getParentId().isEmpty()) {
	            System.out.println("❌ ParentId is missing in request body");
	            return ResponseEntity.badRequest().body("ParentId is required in request body");
	        }
	        
	        if (question.getRootId() == null || question.getRootId().isEmpty()) {
	            System.out.println("❌ RootId is missing in request body");
	            return ResponseEntity.badRequest().body("RootId is required in request body");
	        }

	        System.out.println("🔍 Calling service layer...");
	        String unitName = mcqTestService.updateQuestion(question, oldName);
	        
	        if (unitName == null || unitName.isEmpty()) {
	            System.out.println("❌ Service returned null or empty unitName - UPDATE FAILED");
	            return ResponseEntity.badRequest().body("Failed to update test. Test not found or update failed.");
	        }
	        
	        System.out.println("✅ Service returned unitName: " + unitName + " - UPDATE SUCCESSFUL");
	        return ResponseEntity.ok(Collections.singletonMap("message", "Test updated successfully"));
	        
	    } catch (Exception e) {
	        System.err.println("❌ EXCEPTION in updateQuestion controller:");
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
	    }
	}
	
	@PostMapping("/debugUpdate")
	public ResponseEntity<?> debugUpdate(@RequestBody WrapperMCQTest question) {
	    System.out.println("🐛 ========== DEBUG UPDATE ENDPOINT ==========");
	    System.out.println("📥 Full request body:");
	    System.out.println("  - RootId: " + question.getRootId());
	    System.out.println("  - ParentId: " + question.getParentId());
	    System.out.println("  - TestName: " + question.getTestName());
	    System.out.println("  - UnitName: " + question.getUnitName());
	    System.out.println("  - Marks: " + question.getMarks());
	    System.out.println("  - Dbname: " + question.getDbname());
	    System.out.println("  - SubjectName: " + question.getSubjectName());
	    System.out.println("  - Questions Count: " + (question.getQuestionsList() != null ? question.getQuestionsList().size() : 0));
	    
	    if (question.getQuestionsList() != null && !question.getQuestionsList().isEmpty()) {
	        for (int i = 0; i < question.getQuestionsList().size(); i++) {
	            MCQTest q = question.getQuestionsList().get(i);
	            System.out.println("📝 Question " + (i + 1) + ":");
	            System.out.println("    - ID: " + q.getId());
	            System.out.println("    - Question: " + q.getQuestion());
	            System.out.println("    - Explanation: " + q.getExplanation());
	            System.out.println("    - Correct Index: " + q.getCorrectIndex());
	            System.out.println("    - Option1: " + q.getOption1());
	            System.out.println("    - Option2: " + q.getOption2());
	            System.out.println("    - Option3: " + q.getOption3());
	            System.out.println("    - Option4: " + q.getOption4());
	            System.out.println("    - Question Images: " + (q.getQuestionImages() != null ? q.getQuestionImages().size() : 0));
	            System.out.println("    - Solution Images: " + (q.getSolutionImages() != null ? q.getSolutionImages().size() : 0));
	        }
	    }
	    
	    return ResponseEntity.ok(Collections.singletonMap("status", "debug_received"));
	}
	
	@DeleteMapping("/deleteQuestion/{parentId}")
	public ResponseEntity<?> deleteQuestion(
	    @PathVariable("parentId") String parentId,
	    @RequestBody WrapperMCQTest question) {
	    
	    System.out.println("🗑️ ========== DELETE QUESTION CONTROLLER CALLED ==========");
	    System.out.println("📥 Parent ID from URL: " + parentId);
	    System.out.println("📥 Request Body Details:");
	    System.out.println("  - RootId: " + question.getRootId());
	    System.out.println("  - ParentId: " + question.getParentId());
	    System.out.println("  - TestName: " + question.getTestName());
	    System.out.println("  - Dbname: " + question.getDbname());
	    System.out.println("  - SubjectName: " + question.getSubjectName());

	    try {
	        // Enhanced validation
	        if (question.getTestName() == null || question.getTestName().isEmpty()) {
	            System.out.println("❌ TestName is required for deletion");
	            return ResponseEntity.badRequest().body("TestName is required for deletion");
	        }
	        
	        if (question.getRootId() == null || question.getRootId().isEmpty()) {
	            System.out.println("❌ RootId is required for deletion");
	            return ResponseEntity.badRequest().body("RootId is required for deletion");
	        }

	        // Ensure parentId from URL is used
	        question.setParentId(parentId);
	        
	        System.out.println("🔍 Calling service delete method...");
	        String unitName = mcqTestService.deleteQuestion(question);
	        
	        if (unitName == null || unitName.isEmpty()) {
	            System.out.println("❌ Service returned null or empty unitName - DELETE FAILED");
	            return ResponseEntity.badRequest().body("Failed to delete test. Test not found or deletion failed.");
	        }
	        
	        System.out.println("✅ Service returned unitName: " + unitName + " - DELETE SUCCESSFUL");
	        return ResponseEntity.ok(Collections.singletonMap("message", "Test deleted successfully"));
	        
	    } catch (Exception e) {
	        System.err.println("❌ EXCEPTION in deleteQuestion controller:");
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
	    }
	}

	// ✅ ADDITIONAL ENDPOINT: Delete specific question from test
	@DeleteMapping("/deleteSpecificQuestion/{parentId}/{testName}")
	public ResponseEntity<?> deleteSpecificQuestion(
	    @PathVariable("parentId") String parentId,
	    @PathVariable("testName") String testName,
	    @RequestParam String quesId,
	    @RequestParam String dbname,
	    @RequestParam String subjectName,
	    @RequestParam String rootId) {
	    
	    System.out.println("🗑️ ========== DELETE SPECIFIC QUESTION CONTROLLER CALLED ==========");
	    System.out.println("📥 Parent ID: " + parentId);
	    System.out.println("📥 Test Name: " + testName);
	    System.out.println("📥 Question ID: " + quesId);
	    System.out.println("📥 Root ID: " + rootId);

	    try {
	        WrapperMCQTest questionData = new WrapperMCQTest();
	        questionData.setParentId(parentId);
	        questionData.setRootId(rootId);
	        questionData.setTestName(testName);
	        questionData.setDbname(dbname);
	        questionData.setSubjectName(subjectName);
	        questionData.setQuesId(quesId);

	        String unitName = mcqTestService.deleteQuestion(questionData);
	        
	        if (unitName == null || unitName.isEmpty()) {
	            System.out.println("❌ Failed to delete specific question");
	            return ResponseEntity.badRequest().body("Failed to delete question. Question not found.");
	        }
	        
	        System.out.println("✅ Specific question deleted successfully");
	        return ResponseEntity.ok(Collections.singletonMap("message", "Question deleted successfully"));
	        
	    } catch (Exception e) {
	        System.err.println("❌ EXCEPTION in deleteSpecificQuestion controller:");
	        e.printStackTrace();
	        return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
	    }
	}
}
