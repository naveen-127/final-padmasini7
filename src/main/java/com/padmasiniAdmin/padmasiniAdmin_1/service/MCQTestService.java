package com.padmasiniAdmin.padmasiniAdmin_1.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.padmasiniAdmin.padmasiniAdmin_1.model.MCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.MotherMCQTest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Unit;
import com.padmasiniAdmin.padmasiniAdmin_1.model.UnitRequest;
import com.padmasiniAdmin.padmasiniAdmin_1.model.WrapperMCQTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MCQTestService {

    @Autowired
    private MongoClient mongoClient;

    // ------------------- Helper: MongoTemplate -------------------
    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    // ------------------- Add or Update Questions -------------------
    public String addQuestion(WrapperMCQTest data) {
        UnitRequest root = getById(data.getRootId(), "units", data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found for RootId: " + data.getRootId());
            return null;
        }

        MotherMCQTest existingTest = findTestRecursive(root, data.getParentId(), data.getTestName());

        if (existingTest != null) {
            // Update existing question
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                for (MCQTest q : existingTest.getQuestionsList()) {
                    if (q.getId().equals(data.getQuesId())) {
                        updateQuestionFields(q, data);
                        System.out.println("✏️ Updated existing question: " + q.getId());
                        break;
                    }
                }
            }
            // Add new questions
            if (data.getQuestionsList() != null && !data.getQuestionsList().isEmpty()) {
                data.getQuestionsList().forEach(this::sanitizeImages);
                existingTest.getQuestionsList().addAll(data.getQuestionsList());
                System.out.println("✅ Added " + data.getQuestionsList().size() +
                        " new question(s) to test: " + existingTest.getTestName());
            }
        } else {
            // Create new test
            MotherMCQTest mcq = new MotherMCQTest();
            mcq.setTestName(data.getTestName());
            mcq.setMarks(data.getMarks());
            mcq.setSubjectName(data.getSubjectName());
            mcq.setQuestionsList(data.getQuestionsList() != null ? data.getQuestionsList() : new ArrayList<>());
            mcq.getQuestionsList().forEach(this::sanitizeImages);

            // Attach test to parent
            attachTestToParent(root, data.getParentId(), mcq);
            System.out.println("✅ Created new test: " + mcq.getTestName());
        }

        saveRoot(root, data);
        return root.getUnitName();
    }

    // ------------------- Update Single Question -------------------
    public String updateQuestion(WrapperMCQTest data, String oldTestName) {
        UnitRequest root = getById(data.getRootId(), "units", data.getDbname());
        if (root == null) {
            System.out.println("❌ Root unit not found for RootId: " + data.getRootId());
            return null;
        }

        MotherMCQTest test = findTestRecursive(root, data.getParentId(), oldTestName);
        if (test == null) {
            System.out.println("⚠️ Test not found: " + oldTestName);
            return null;
        }

        // Update test info
        if (data.getTestName() != null && !data.getTestName().trim().isEmpty()) {
            test.setTestName(data.getTestName());
        }
        if (data.getMarks() > 0) {
            test.setMarks(data.getMarks());
        }

        // Update question
        boolean updated = false;
        for (MCQTest ques : test.getQuestionsList()) {
            if (ques.getId().equals(data.getQuesId())) {
                updateQuestionFields(ques, data);
                updated = true;
                System.out.println("✏️ Updated question ID: " + ques.getId());
                break;
            }
        }

        if (updated) {
            saveRoot(root, data);
            return root.getUnitName();
        } else {
            System.out.println("⚠️ Question not found for update");
            return null;
        }
    }

    // ------------------- Delete Question or Test -------------------
    public String deleteQuestion(WrapperMCQTest data) {
        UnitRequest root = getById(data.getRootId(), "units", data.getDbname());
        if (root == null) return null;

        boolean deleted = deleteTestRecursive(root, data.getParentId(), data);

        if (deleted) {
            saveRoot(root, data);
            return root.getUnitName();
        } else {
            System.out.println("⚠️ No matching question/test found");
            return null;
        }
    }

    // ------------------- Helpers -------------------
    private void updateQuestionFields(MCQTest q, WrapperMCQTest data) {
        q.setQuestion(data.getQuestion());
        q.setQuestionImages(sanitizeList(data.getQuestionImages(), "NO_QUESTION_IMAGE"));
        q.setOption1(data.getOption1());
        q.setOption1Image(data.getOption1Image());
        q.setOption2(data.getOption2());
        q.setOption2Image(data.getOption2Image());
        q.setOption3(data.getOption3());
        q.setOption3Image(data.getOption3Image());
        q.setOption4(data.getOption4());
        q.setOption4Image(data.getOption4Image());
        q.setSolution(data.getSolution());
        q.setSolutionImages(sanitizeList(data.getSolutionImages(), "NO_SOLUTION_IMAGE"));
    }

    private void sanitizeImages(MCQTest q) {
        q.setQuestionImages(sanitizeList(q.getQuestionImages(), "NO_QUESTION_IMAGE"));
        q.setSolutionImages(sanitizeList(q.getSolutionImages(), "NO_SOLUTION_IMAGE"));
    }

    private List<String> sanitizeList(List<String> input, String defaultValue) {
        if (input == null || input.isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add(defaultValue);
            return list;
        }
        return input;
    }

    // ------------------- Recursive Test Search -------------------
    private MotherMCQTest findTestRecursive(UnitRequest root, String parentId, String testName) {
        if (root.getId().equals(parentId)) {
            return root.getTest().stream()
                    .filter(t -> t.getTestName().equals(testName))
                    .findFirst()
                    .orElse(null);
        }
        for (Unit u : root.getUnits()) {
            MotherMCQTest res = findTestRecursiveInUnit(u, parentId, testName);
            if (res != null) return res;
        }
        return null;
    }

    private MotherMCQTest findTestRecursiveInUnit(Unit unit, String parentId, String testName) {
        if (unit.getId().equals(parentId)) {
            return unit.getTest().stream()
                    .filter(t -> t.getTestName().equals(testName))
                    .findFirst()
                    .orElse(null);
        }
        for (Unit child : unit.getUnits()) {
            MotherMCQTest res = findTestRecursiveInUnit(child, parentId, testName);
            if (res != null) return res;
        }
        return null;
    }

    // ------------------- Attach Test to Parent -------------------
    private void attachTestToParent(UnitRequest root, String parentId, MotherMCQTest test) {
        if (root.getId().equals(parentId)) {
            root.getTest().add(test);
            return;
        }
        for (Unit u : root.getUnits()) {
            attachTestToParentRecursive(u, parentId, test);
        }
    }

    private void attachTestToParentRecursive(Unit unit, String parentId, MotherMCQTest test) {
        if (unit.getId().equals(parentId)) {
            unit.getTest().add(test);
            return;
        }
        for (Unit child : unit.getUnits()) {
            attachTestToParentRecursive(child, parentId, test);
        }
    }

    // ------------------- Delete Recursive -------------------
    private boolean deleteTestRecursive(UnitRequest root, String parentId, WrapperMCQTest data) {
        if (root.getId().equals(parentId)) {
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                Optional<MotherMCQTest> test = root.getTest().stream()
                        .filter(t -> t.getTestName().equals(data.getTestName()))
                        .findFirst();
                return test.map(t -> t.getQuestionsList().removeIf(q -> q.getId().equals(data.getQuesId()))).orElse(false);
            } else {
                return root.getTest().removeIf(t -> t.getTestName().equals(data.getTestName()));
            }
        }
        for (Unit u : root.getUnits()) {
            if (deleteTestRecursiveInUnit(u, parentId, data)) return true;
        }
        return false;
    }

    private boolean deleteTestRecursiveInUnit(Unit unit, String parentId, WrapperMCQTest data) {
        if (unit.getId().equals(parentId)) {
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                Optional<MotherMCQTest> test = unit.getTest().stream()
                        .filter(t -> t.getTestName().equals(data.getTestName()))
                        .findFirst();
                return test.map(t -> t.getQuestionsList().removeIf(q -> q.getId().equals(data.getQuesId()))).orElse(false);
            } else {
                return unit.getTest().removeIf(t -> t.getTestName().equals(data.getTestName()));
            }
        }
        for (Unit child : unit.getUnits()) {
            if (deleteTestRecursiveInUnit(child, parentId, data)) return true;
        }
        return false;
    }

    // ------------------- Save Root -------------------
    private void saveRoot(UnitRequest root, WrapperMCQTest data) {
        MongoTemplate template = getTemplate(data.getDbname());
        template.save(root, "units"); // Fixed collection name
        System.out.println("✅ Saved successfully to DB: " + data.getDbname());
    }

    // ------------------- Get UnitRequest by ID -------------------
    public UnitRequest getById(String id, String collectionName, String dbname) {
        MongoTemplate mongoTemplate = getTemplate(dbname);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
}
