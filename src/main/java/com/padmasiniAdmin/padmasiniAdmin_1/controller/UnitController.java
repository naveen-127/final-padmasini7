@RestController
@RequestMapping("/unit")
public class UnitController {

    @Autowired
    private UnitService unitService;

    @GetMapping("/all")
    public List<?> getAllUnits(@RequestParam String db, @RequestParam String collection, @RequestParam String subject) {
        return unitService.getAllUnit(db, collection, subject);
    }

    @PostMapping("/head/add")
    public String addHeadUnit(@RequestBody WrapperUnitRequest wrapper) {
        return unitService.addNewHeadUnit(wrapper); // returns String id
    }

    @PutMapping("/head/update")
    public boolean updateHeadUnit(@RequestBody WrapperUnitRequest wrapper, @RequestParam String oldName) {
        return unitService.updateHeadUnitName(wrapper, oldName);
    }

    @DeleteMapping("/head/delete")
    public boolean deleteHeadUnit(@RequestBody WrapperUnitRequest wrapper) {
        return unitService.deleteHeadUnit(wrapper);
    }

    @DeleteMapping("/delete")
    public boolean deleteUnit(@RequestBody WrapperUnit wrapper) {
        return unitService.deleteUnit(wrapper);
    }

    @PutMapping("/update")
    public boolean updateUnit(@RequestBody WrapperUnit wrapper) {
        return unitService.updateUnit(wrapper);
    }

    @PostMapping("/add")
    public String addUnit(@RequestBody WrapperUnit wrapper) {
        return unitService.addUnit(wrapper);
    }
}
