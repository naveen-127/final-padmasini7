@RestController
public class MCQTestController {

    @Autowired
    private MCQTestService mcqTestService;

    @PostMapping("/addQuestion/{parentId}")
    public ResponseEntity<?> addQuestion(
            @PathVariable("parentId") String parentId,
            @RequestBody WrapperMCQTest question) {
        try {
            question.setParentId(parentId);

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

        question.setParentId(parentId);
        String unitName = mcqTestService.updateQuestion(question, oldName);

        if (unitName == null || unitName.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ No unit found with the provided parentId");
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "✅ Question updated in unit " + unitName));
    }

    @DeleteMapping("/deleteQuestion/{parentId}")
    public ResponseEntity<?> deleteQuestion(
            @PathVariable("parentId") String parentId,
            @RequestBody WrapperMCQTest question) {

        question.setParentId(parentId);
        String unitName = mcqTestService.deleteQuestion(question);

        if (unitName == null || unitName.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ No unit found with the provided parentId");
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "✅ Question deleted from unit " + unitName));
    }
}
