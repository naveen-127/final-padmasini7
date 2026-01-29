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
import java.util.Objects;

@Service
public class MCQTestService {

    @Autowired
    private MongoClient mongoClient;

    private MongoTemplate getTemplate(String dbName) {
        return new MongoTemplate(mongoClient, dbName);
    }

    // Add / attach questions into existing test or create test if not exist
    public String addQuestion(WrapperMCQTest data) {
        // Defensive checks
        if (data == null) {
            System.out.println("❌ Received null payload");
            return null;
        }
        System.out.println("📥 addQuestion called with wrapper: " + data.getTestName() +
                " parentId: " + data.getParentId() + " db: " + data.getDbname());

        UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
        if (root == null) {
            System.out.println("❌ Root not found for rootId=" + data.getRootId() + " db=" + data.getDbname());
            return null;
        }

        // find test by name under parentId
        MotherMCQTest existingTest = findTestRecursive(root, data.getParentId(), data.getTestName());

        if (existingTest != null) {
            // update existing test: either add questions or update single question if quesId present
            if (data.getQuesId() != null && !data.getQuesId().isEmpty()) {
                // update matching question in this test
                boolean updated = false;
                for (MCQTest q : existingTest.getQuestionsList()) {
                    if (q.getId().equals(data.getQuesId())) {
                        updateQuestionFields(q, data);
                        updated = true;
                        System.out.println("✏️ Updated existing question " + q.getId());
                        break;
                    }
                }
                if (!updated) System.out.println("⚠️ quesId provided but not found in test");
            } else {
                // add new questionsList (if provided)
                if (data.getQuestionsList() != null && !data.getQuestionsList().isEmpty()) {
                    // ensure each question has an id & sanitized lists
                    data.getQuestionsList().forEach(this::sanitizeQuestionBeforeSave);
                    existingTest.getQuestionsList().addAll(data.getQuestionsList());
                    System.out.println("✅ Added " + data.getQuestionsList().size() + " question(s) into existing test " + existingTest.getTestName());
                } else {
                    System.out.println("⚠️ No questionsList provided to add");
                }
            }
        } else {
            // create new test under the parent unit/root
            MotherMCQTest newTest = new MotherMCQTest();
            newTest.setTestName(data.getTestName());
            newTest.setMarks(data.getMarks());
            newTest.setSubjectName(data.getSubjectName());
            newTest.setUnitName(data.getUnitName());
            newTest.setTags(data.getTags() != null ? data.getTags() : new ArrayList<>());

            List<MCQTest> questions = data.getQuestionsList() != null ? data.getQuestionsList() : new ArrayList<>();
            questions.forEach(this::sanitizeQuestionBeforeSave);
            newTest.setQuestionsList(questions);

            attachTestToParent(root, data.getParentId(), newTest);
            System.out.println("🆕 Created and attached new test '" + newTest.getTestName() + "' to parent " + data.getParentId());
        }

        saveRoot(root, data);
        return root.getUnitName();
    }

    public String updateQuestion(WrapperMCQTest data, String oldTestName) {
        System.out.println("🔄 ========== UPDATE QUESTION SERVICE CALLED ==========");
        
        if (data == null) {
            System.out.println("❌ Data is null");
            return null;
        }
        
        System.out.println("🔍 Looking for test: '" + oldTestName + "' under parent: " + data.getParentId());
        System.out.println("🔍 New Test Name: " + data.getTestName());
        System.out.println("🔍 New Marks: " + data.getMarks());
        System.out.println("🔍 New Unit Name: " + data.getUnitName());

        try {
            // Get the root document
            UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
            if (root == null) {
                System.out.println("❌ Root not found for rootId: " + data.getRootId());
                return null;
            }

            // Find the test using the OLD test name
            System.out.println("🔍 Searching for test with OLD name: " + oldTestName);
            MotherMCQTest test = findTestRecursiveEnhanced(root, data.getParentId(), oldTestName);
            
            if (test == null) {
                System.out.println("❌ Test not found with old name: " + oldTestName);
                return null;
            }

            System.out.println("✅ Test found with old name: " + test.getTestName());
            System.out.println("📊 Current test state BEFORE update:");
            System.out.println("  - Name: " + test.getTestName());
            System.out.println("  - Marks: " + test.getMarks());
            System.out.println("  - Unit: " + test.getUnitName());
            System.out.println("  - Questions: " + (test.getQuestionsList() != null ? test.getQuestionsList().size() : 0));

            // ✅ CRITICAL FIX: Update ALL fields, not just the name
            boolean updated = false;

            // Update test name if changed
            if (data.getTestName() != null && !data.getTestName().equals(test.getTestName())) {
                System.out.println("📝 Updating test name: '" + test.getTestName() + "' → '" + data.getTestName() + "'");
                test.setTestName(data.getTestName());
                updated = true;
            }

            // ✅ FIX: Always update marks (pass percentage)
            if (data.getMarks() != test.getMarks()) {
                System.out.println("📊 Updating marks: " + test.getMarks() + " → " + data.getMarks());
                test.setMarks(data.getMarks());
                updated = true;
            }

            // ✅ FIX: Always update unit name
            if (data.getUnitName() != null && !data.getUnitName().equals(test.getUnitName())) {
                System.out.println("📁 Updating unit name: '" + test.getUnitName() + "' → '" + data.getUnitName() + "'");
                test.setUnitName(data.getUnitName());
                updated = true;
            }

            if (data.getTags() != null) {
               System.out.println("🏷️ Updating tags: " + data.getTags());
               test.setTags(data.getTags());
               updated = true;
            }

            // ✅ CRITICAL FIX: Always update questions list if provided
            if (data.getQuestionsList() != null && !data.getQuestionsList().isEmpty()) {
                System.out.println("❓ UPDATING QUESTIONS LIST:");
                System.out.println("  - Current questions count: " + (test.getQuestionsList() != null ? test.getQuestionsList().size() : 0));
                System.out.println("  - New questions count: " + data.getQuestionsList().size());
                
                // ✅ FIX: Clear existing questions and add new ones
                test.getQuestionsList().clear();
                
                // Add all new questions with proper processing
                for (MCQTest newQuestion : data.getQuestionsList()) {
                    if (newQuestion != null) {
                        // ✅ FIX: Use the existing MCQTest object directly instead of creating new one
                        // This preserves all the fields that are already properly set
                        MCQTest processedQuestion = newQuestion;
                        
                        // ✅ FIX: Ensure the question has an ID if missing
                        if (processedQuestion.getId() == null || processedQuestion.getId().trim().isEmpty()) {
                            processedQuestion.setId(new ObjectId().toHexString());
                        }
                        
                        // ✅ FIX: Sanitize the question to ensure all fields are properly set
                        sanitizeQuestionBeforeSave(processedQuestion);
                        
                        test.getQuestionsList().add(processedQuestion);
                        
                        System.out.println("✅ Added question: " + processedQuestion.getQuestion());
                        System.out.println("  - Explanation: " + processedQuestion.getExplanation());
                        System.out.println("  - Correct Index: " + processedQuestion.getCorrectIndex());
                        System.out.println("  - Question Images: " + (processedQuestion.getQuestionImages() != null ? processedQuestion.getQuestionImages().size() : 0));
                        System.out.println("  - Solution Images: " + (processedQuestion.getSolutionImages() != null ? processedQuestion.getSolutionImages().size() : 0));
                        System.out.println("  - Table Data: " + (processedQuestion.getTableData() != null ? processedQuestion.getTableData().size() : 0));
                        System.out.println("  - Tags: " + (processedQuestion.getTags() != null ? processedQuestion.getTags().size() : 0));
                    }
                }
                updated = true;
                System.out.println("✅ Questions list completely replaced with new data");
            }

            // ✅ FIX: Always save if any field was updated
            if (updated) {
                System.out.println("💾 SAVING CHANGES TO DATABASE...");
                saveRoot(root, data);
                System.out.println("✅ All test fields updated and saved to database");
                
                // Enhanced verification after save
                System.out.println("🔍 Verifying update after save...");
                UnitRequest verifiedRoot = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
                if (verifiedRoot != null) {
                    MotherMCQTest verifiedTest = findTestRecursiveEnhanced(verifiedRoot, data.getParentId(), data.getTestName());
                    if (verifiedTest != null) {
                        System.out.println("✅ VERIFICATION - Test found after update:");
                        System.out.println("  - Name: " + verifiedTest.getTestName());
                        System.out.println("  - Marks: " + verifiedTest.getMarks());
                        System.out.println("  - Questions: " + (verifiedTest.getQuestionsList() != null ? verifiedTest.getQuestionsList().size() : 0));
                        if (verifiedTest.getQuestionsList() != null && !verifiedTest.getQuestionsList().isEmpty()) {
                            MCQTest firstQ = verifiedTest.getQuestionsList().get(0);
                            System.out.println("  - First Question: " + firstQ.getQuestion());
                            System.out.println("  - First Question Explanation: " + firstQ.getExplanation());
                            System.out.println("  - First Question Correct Index: " + firstQ.getCorrectIndex());
                        }
                    }
                }
                
                return root.getUnitName();
            } else {
                System.out.println("⚠️ No changes detected to save");
                return root.getUnitName();
            }
            
        } catch (Exception e) {
            System.err.println("❌ EXCEPTION in updateQuestion service:");
            e.printStackTrace();
            return null;
        }
    }
    // ✅ FIXED DELETE METHOD
    public String deleteQuestion(WrapperMCQTest data) {
        System.out.println("🗑️ ========== DELETE QUESTION SERVICE CALLED ==========");
        System.out.println("📥 Data received - TestName: " + data.getTestName() + 
                         ", ParentId: " + data.getParentId() + 
                         ", RootId: " + data.getRootId() +
                         ", Dbname: " + data.getDbname() +
                         ", Subject: " + data.getSubjectName());

        try {
            UnitRequest root = getById(data.getRootId(), data.getSubjectName(), data.getDbname());
            if (root == null) {
                System.out.println("❌ Root not found for rootId=" + data.getRootId());
                return null;
            }

            // Enhanced deletion with better debugging
            boolean deleted = deleteTestRecursiveEnhanced(root, data.getParentId(), data.getTestName());

            if (deleted) {
                System.out.println("✅ Test deleted successfully, saving changes...");
                saveRoot(root, data);
                return root.getUnitName();
            } else {
                System.out.println("❌ Test not found for deletion");
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ EXCEPTION in deleteQuestion service:");
            e.printStackTrace();
            return null;
        }
    }

    // ✅ ENHANCED DELETION METHOD
    private boolean deleteTestRecursiveEnhanced(UnitRequest root, String parentId, String testName) {
        System.out.println("🔍 Searching for test to delete: '" + testName + "' in parent: " + parentId);
        
        // Check root level
        if (root.getId().equals(parentId)) {
            System.out.println("✅ Found matching parent in root");
            if (root.getTest() != null && !root.getTest().isEmpty()) {
                int initialSize = root.getTest().size();
                boolean removed = root.getTest().removeIf(t -> {
                    boolean match = t.getTestName() != null && t.getTestName().equals(testName);
                    System.out.println("🔍 Checking test: '" + t.getTestName() + "' vs '" + testName + "' -> " + match);
                    return match;
                });
                System.out.println("📊 Root tests - Initial: " + initialSize + ", After: " + 
                                 (root.getTest() != null ? root.getTest().size() : 0) + ", Removed: " + removed);
                if (removed) {
                    System.out.println("✅ Successfully deleted test from root");
                    return true;
                }
            } else {
                System.out.println("⚠️ No tests found in root");
            }
        }
        
        // Search in child units
        if (root.getUnits() != null && !root.getUnits().isEmpty()) {
            for (Unit u : root.getUnits()) {
                boolean deleted = deleteTestRecursiveInUnitEnhanced(u, parentId, testName);
                if (deleted) return true;
            }
        } else {
            System.out.println("⚠️ No child units found in root");
        }
        
        System.out.println("❌ Test '" + testName + "' not found for deletion in parent: " + parentId);
        return false;
    }

    private boolean deleteTestRecursiveInUnitEnhanced(Unit unit, String parentId, String testName) {
        System.out.println("🔍 Searching in unit: " + unit.getId() + " for parent: " + parentId);
        
        // Check if this unit is the parent
        if (unit.getId().equals(parentId)) {
            System.out.println("✅ Found matching parent in unit: " + unit.getUnitName());
            if (unit.getTest() != null && !unit.getTest().isEmpty()) {
                int initialSize = unit.getTest().size();
                boolean removed = unit.getTest().removeIf(t -> {
                    boolean match = t.getTestName() != null && t.getTestName().equals(testName);
                    System.out.println("🔍 Checking test: '" + t.getTestName() + "' vs '" + testName + "' -> " + match);
                    return match;
                });
                System.out.println("📊 Unit tests - Initial: " + initialSize + ", After: " + 
                                 (unit.getTest() != null ? unit.getTest().size() : 0) + ", Removed: " + removed);
                if (removed) {
                    System.out.println("✅ Successfully deleted test from unit: " + unit.getUnitName());
                    return true;
                }
            } else {
                System.out.println("⚠️ No tests found in unit: " + unit.getUnitName());
            }
        }
        
        // Search in nested units
        if (unit.getUnits() != null && !unit.getUnits().isEmpty()) {
            for (Unit child : unit.getUnits()) {
                boolean deleted = deleteTestRecursiveInUnitEnhanced(child, parentId, testName);
                if (deleted) return true;
            }
        }
        
        return false;
    }

    // Helper method to list tests in parent
    private void listTestsInParent(UnitRequest root, String parentId) {
        System.out.println("📋 Listing all tests in parent: " + parentId);
        
        if (root.getId().equals(parentId)) {
            if (root.getTest() != null && !root.getTest().isEmpty()) {
                for (MotherMCQTest t : root.getTest()) {
                    System.out.println("  - " + t.getTestName() + " (questions: " + 
                                     (t.getQuestionsList() != null ? t.getQuestionsList().size() : 0) + ")");
                }
            } else {
                System.out.println("  No tests found in root");
            }
            return;
        }
        
        if (root.getUnits() != null) {
            for (Unit unit : root.getUnits()) {
                if (unit.getId().equals(parentId)) {
                    if (unit.getTest() != null && !unit.getTest().isEmpty()) {
                        for (MotherMCQTest t : unit.getTest()) {
                            System.out.println("  - " + t.getTestName() + " (questions: " + 
                                             (t.getQuestionsList() != null ? t.getQuestionsList().size() : 0) + ")");
                        }
                    } else {
                        System.out.println("  No tests found in unit: " + unit.getUnitName());
                    }
                    return;
                }
            }
        }
        System.out.println("  Parent not found");
    }

    // Enhanced test finding method with better debugging
    private MotherMCQTest findTestRecursiveEnhanced(UnitRequest root, String parentId, String testName) {
        System.out.println("🔍 Searching in root: " + root.getId() + " for parent: " + parentId + ", test: " + testName);
        
        if (root.getId().equals(parentId)) {
            System.out.println("✅ Found matching parent in root");
            if (root.getTest() != null) {
                for (MotherMCQTest t : root.getTest()) {
                    System.out.println("🔍 Checking test: '" + t.getTestName() + "' vs '" + testName + "'");
                    if (t.getTestName() != null && t.getTestName().equals(testName)) {
                        System.out.println("✅ Found test in root!");
                        return t;
                    }
                }
            }
        }
        
        // Search in child units
        if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                MotherMCQTest found = findTestRecursiveInUnitEnhanced(u, parentId, testName);
                if (found != null) return found;
            }
        }
        
        System.out.println("❌ Test not found in recursive search");
        return null;
    }

    private MotherMCQTest findTestRecursiveInUnitEnhanced(Unit unit, String parentId, String testName) {
        System.out.println("🔍 Searching in unit: " + unit.getId() + " for parent: " + parentId);
        
        if (unit.getId().equals(parentId)) {
            System.out.println("✅ Found matching parent in unit: " + unit.getUnitName());
            if (unit.getTest() != null) {
                for (MotherMCQTest t : unit.getTest()) {
                    System.out.println("🔍 Checking test: '" + t.getTestName() + "' vs '" + testName + "'");
                    if (t.getTestName() != null && t.getTestName().equals(testName)) {
                        System.out.println("✅ Found test in unit!");
                        return t;
                    }
                }
            }
        }
        
        // Search in child units
        if (unit.getUnits() != null) {
            for (Unit child : unit.getUnits()) {
                MotherMCQTest found = findTestRecursiveInUnitEnhanced(child, parentId, testName);
                if (found != null) return found;
            }
        }
        
        return null;
    }

    private void updateQuestionFields(MCQTest q, WrapperMCQTest data) {
        System.out.println("🔧 Updating question fields for question: " + q.getId());
        
        if (data.getQuestion() != null && !data.getQuestion().equals(q.getQuestion())) {
            System.out.println("  - Updating question: " + data.getQuestion());
            q.setQuestion(data.getQuestion());
        }
        
        if (data.getQuestionImages() != null) {
            System.out.println("  - Updating question images: " + data.getQuestionImages().size());
            q.setQuestionImages(sanitizeList(data.getQuestionImages(), "NO_QUESTION_IMAGE"));
        }
        
        if (data.getOption1() != null && !data.getOption1().equals(q.getOption1())) {
            System.out.println("  - Updating option1: " + data.getOption1());
            q.setOption1(data.getOption1());
        }
        
        if (data.getOption1Image() != null) {
            System.out.println("  - Updating option1 image");
            q.setOption1Image(data.getOption1Image());
        }
        
        if (data.getOption2() != null && !data.getOption2().equals(q.getOption2())) {
            System.out.println("  - Updating option2: " + data.getOption2());
            q.setOption2(data.getOption2());
        }
        
        if (data.getOption2Image() != null) {
            System.out.println("  - Updating option2 image");
            q.setOption2Image(data.getOption2Image());
        }
        
        if (data.getOption3() != null && !data.getOption3().equals(q.getOption3())) {
            System.out.println("  - Updating option3: " + data.getOption3());
            q.setOption3(data.getOption3());
        }
        
        if (data.getOption3Image() != null) {
            System.out.println("  - Updating option3 image");
            q.setOption3Image(data.getOption3Image());
        }
        
        if (data.getOption4() != null && !data.getOption4().equals(q.getOption4())) {
            System.out.println("  - Updating option4: " + data.getOption4());
            q.setOption4(data.getOption4());
        }
        
        if (data.getOption4Image() != null) {
            System.out.println("  - Updating option4 image");
            q.setOption4Image(data.getOption4Image());
        }
        
        if (data.getExplanation() != null && !data.getExplanation().equals(q.getExplanation())) {
            System.out.println("  - Updating explanation: " + data.getExplanation());
            q.setExplanation(data.getExplanation());
        }
        
        if (data.getTags() != null) {
            System.out.println("  - Updating tags: " + data.getTags());
            q.setTags(data.getTags());
        }
        
        if (data.getSolutionImages() != null) {
            System.out.println("  - Updating solution images: " + data.getSolutionImages().size());
            q.setSolutionImages(sanitizeList(data.getSolutionImages(), "NO_SOLUTION_IMAGE"));
        }
        
        // Update correct index if provided
        if (data.getCorrectIndex() != null) {
            System.out.println("  - Updating correct index: " + data.getCorrectIndex());
            q.setCorrectIndex(data.getCorrectIndex());
        }
        
        System.out.println("✅ Question fields updated successfully");
    }

    private void sanitizeQuestionBeforeSave(MCQTest q) {
        if (q == null) {
            System.out.println("⚠️ Question is null in sanitizeQuestionBeforeSave");
            return;
        }
        
        try {
            System.out.println("🔧 Sanitizing question: " + (q.getQuestion() != null ? q.getQuestion().substring(0, Math.min(30, q.getQuestion().length())) : "null"));
            System.out.println("  - Current tags: " + q.getTags());
            
            if (q.getId() == null || q.getId().trim().isEmpty()) {
                String newId = new ObjectId().toHexString();
                q.setId(newId);
                System.out.println("  - Generated new ID: " + newId);
            } else {
                System.out.println("  - Using existing ID: " + q.getId());
            }
            
            // Safe null handling for lists
            if (q.getQuestionImages() == null) {
                q.setQuestionImages(new ArrayList<>());
                System.out.println("  - Initialized empty questionImages");
            }
            if (q.getSolutionImages() == null) {
                q.setSolutionImages(new ArrayList<>());
                System.out.println("  - Initialized empty solutionImages");
            }
            if (q.getTableData() == null) {
                q.setTableData(new ArrayList<>());
                System.out.println("  - Initialized empty tableData");
            }
            
            if (q.getTags() == null) {
                q.setTags(new ArrayList<>());
                System.out.println("⚠️  Warning: Tags was null, initialized empty");
            }
            
            // Safe null handling for strings
            if (q.getExplanation() == null) {
                q.setExplanation("");
                System.out.println("  - Set empty explanation");
            }
            if (q.getQuestion() == null) {
                q.setQuestion("");
                System.out.println("  - Set empty question");
            }
            if (q.getOption1() == null) {
                q.setOption1("");
                System.out.println("  - Set empty option1");
            }
            if (q.getOption2() == null) {
                q.setOption2("");
                System.out.println("  - Set empty option2");
            }
            if (q.getOption3() == null) {
                q.setOption3("");
                System.out.println("  - Set empty option3");
            }
            if (q.getOption4() == null) {
                q.setOption4("");
                System.out.println("  - Set empty option4");
            }
            
            // Log final state
            System.out.println("✅ Sanitized question - ID: " + q.getId() + 
                    ", Tags: " + q.getTags() + 
                    ", Options: " + q.getOption1() + ", " + q.getOption2() + ", " + q.getOption3() + ", " + q.getOption4());
        } catch (Exception e) {
            System.err.println("❌ Error in sanitizeQuestionBeforeSave:");
            e.printStackTrace();
        }
    }
    private List<String> sanitizeList(List<String> input, String defaultValue) {
        if (input == null || input.isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add(defaultValue);
            return list;
        }
        return input;
    }

    public MotherMCQTest findTestRecursive(UnitRequest root, String parentId, String testName) {
        if (root == null) return null;
        if (root.getId().equals(parentId)) {
            if (root.getTest() != null) {
                return root.getTest().stream()
                        .filter(t -> t.getTestName() != null && t.getTestName().equals(testName))
                        .findFirst().orElse(null);
            }
        }
        if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                MotherMCQTest found = findTestRecursiveInUnit(u, parentId, testName);
                if (found != null) return found;
            }
        }
        return null;
    }

    private MotherMCQTest findTestRecursiveInUnit(Unit unit, String parentId, String testName) {
        if (unit.getId().equals(parentId)) {
            if (unit.getTest() != null) {
                return unit.getTest().stream()
                        .filter(t -> t.getTestName() != null && t.getTestName().equals(testName))
                        .findFirst()
                        .orElse(null);
            }
        }
        if (unit.getUnits() != null) {
            for (Unit child : unit.getUnits()) {
                MotherMCQTest r = findTestRecursiveInUnit(child, parentId, testName);
                if (r != null) return r;
            }
        }
        return null;
    }

    private void attachTestToParent(UnitRequest root, String parentId, MotherMCQTest test) {
        if (root.getId().equals(parentId)) {
            if (root.getTest() == null) {
                root.setTest(new ArrayList<>());
            }
            root.getTest().add(test);
            return;
        }
        if (root.getUnits() != null) {
            for (Unit u : root.getUnits()) {
                if (u.getId().equals(parentId)) {
                    if (u.getTest() == null) {
                        u.setTest(new ArrayList<>());
                    }
                    u.getTest().add(test);
                    return;
                }
                attachTestToParentRecursive(u, parentId, test);
            }
        }
    }

    private void attachTestToParentRecursive(Unit unit, String parentId, MotherMCQTest test) {
        if (unit.getId().equals(parentId)) {
            if (unit.getTest() == null) {
                unit.setTest(new ArrayList<>());
            }
            unit.getTest().add(test);
            return;
        }
        if (unit.getUnits() != null) {
            for (Unit child : unit.getUnits()) {
                attachTestToParentRecursive(child, parentId, test);
            }
        }
    }

    private void saveRoot(UnitRequest root, WrapperMCQTest data) {
        System.out.println("💾 ========== SAVE ROOT CALLED ==========");
        System.out.println("  - Database: " + data.getDbname());
        System.out.println("  - Collection: " + data.getSubjectName());
        System.out.println("  - Root ID: " + root.getId());
        System.out.println("  - Root Name: " + root.getUnitName());
        
        try {
            MongoTemplate mongoTemplate = getTemplate(data.getDbname());
            
            System.out.println("🔍 Root structure BEFORE save:");
            debugRootStructure(root);
            
            // Save to the specific subject collection
            System.out.println("💽 Executing mongoTemplate.save...");
            UnitRequest saved = mongoTemplate.save(root, data.getSubjectName());
            
            System.out.println("✅ Save operation completed");
            System.out.println("🔍 Saved object: " + (saved != null ? saved.getId() : "null"));
            
            // Enhanced verification
            System.out.println("🔍 Enhanced verification...");
            UnitRequest verified = mongoTemplate.findById(root.getId(), UnitRequest.class, data.getSubjectName());
            if (verified != null) {
                System.out.println("✅ Save verification: Document exists in database");
                System.out.println("🔍 Verified root structure:");
                debugRootStructure(verified);
                
                // Check if our specific test exists in the verified document
                MotherMCQTest verifiedTest = findTestRecursiveEnhanced(verified, data.getParentId(), 
                    data.getTestName() != null ? data.getTestName() : data.getTestName());
                if (verifiedTest != null) {
                    System.out.println("✅ Test verification: Test found in saved document");
                    System.out.println("  - Test name: " + verifiedTest.getTestName());
                    System.out.println("  - Questions: " + (verifiedTest.getQuestionsList() != null ? verifiedTest.getQuestionsList().size() : 0));
                } else {
                    System.out.println("❌ Test verification: Test NOT found in saved document");
                }
            } else {
                System.out.println("❌ Save verification: Document NOT found in database!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error saving to database:");
            e.printStackTrace();
            throw e;
        }
    }

    // Enhanced debug method
    private void debugRootStructure(UnitRequest root) {
        if (root == null) {
            System.out.println("  - Root is null");
            return;
        }
        System.out.println("  - Root ID: " + root.getId());
        System.out.println("  - Root Name: " + root.getUnitName());
        System.out.println("  - Has tests: " + (root.getTest() != null ? root.getTest().size() : 0));
        if (root.getTest() != null && !root.getTest().isEmpty()) {
            for (MotherMCQTest test : root.getTest()) {
                System.out.println("    📝 Test: " + test.getTestName() + 
                                 " (questions: " + (test.getQuestionsList() != null ? test.getQuestionsList().size() : 0) + ")");
                if (test.getQuestionsList() != null && !test.getQuestionsList().isEmpty()) {
                    MCQTest firstQ = test.getQuestionsList().get(0);
                    System.out.println("      - First question: " + firstQ.getQuestion());
                    System.out.println("      - First question explanation: " + firstQ.getExplanation());
                }
            }
        }
        System.out.println("  - Has units: " + (root.getUnits() != null ? root.getUnits().size() : 0));
        if (root.getUnits() != null && !root.getUnits().isEmpty()) {
            for (Unit unit : root.getUnits()) {
                System.out.println("    📁 Unit: " + unit.getUnitName() + " (ID: " + unit.getId() + ")");
                if (unit.getTest() != null && !unit.getTest().isEmpty()) {
                    for (MotherMCQTest test : unit.getTest()) {
                        System.out.println("      📝 Test: " + test.getTestName() + 
                                         " (questions: " + (test.getQuestionsList() != null ? test.getQuestionsList().size() : 0) + ")");
                    }
                }
            }
        }
    }


    public UnitRequest getById(String id, String collectionName, String dbname) {
        MongoTemplate mongoTemplate = getTemplate(dbname);
        try {
            return mongoTemplate.findById(new ObjectId(id), UnitRequest.class, collectionName);
        } catch (IllegalArgumentException e) {
            return mongoTemplate.findById(id, UnitRequest.class, collectionName);
        }
    }
}
