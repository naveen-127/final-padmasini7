package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.padmasiniAdmin.padmasiniAdmin_1.model.ClassAssignment;
import com.padmasiniAdmin.padmasiniAdmin_1.model.Notification;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.AssignmentRepository;
import com.padmasiniAdmin.padmasiniAdmin_1.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Notification>> getNotificationsForTeacher(@PathVariable String teacherId) {
        return ResponseEntity.ok(notificationRepository.findByTeacherId(teacherId));
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Notification notification) {
        notification.setStatus("PENDING");
        return ResponseEntity.ok(notificationRepository.save(notification));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptReschedule(@PathVariable String id) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isPresent()) {
            Notification notification = notifOpt.get();
            if ("RESCHEDULE_REQUEST".equals(notification.getType())) {
                Optional<ClassAssignment> assignmentOpt = assignmentRepository.findById(notification.getClassAssignmentId());
                if (assignmentOpt.isPresent()) {
                    ClassAssignment assignment = assignmentOpt.get();
                    List<String> dates = assignment.getSelectedDates();
                    if (dates != null) {
                        List<String> mutableDates = new ArrayList<>(dates);
                        mutableDates.remove(notification.getOldDate());
                        if (!mutableDates.contains(notification.getNewDate())) {
                            mutableDates.add(notification.getNewDate());
                        }
                        assignment.setSelectedDates(mutableDates);
                        
                        // Add to rescheduledSlots tracking
                        List<Map<String, String>> slots = assignment.getRescheduledSlots();
                        if (slots == null) {
                            slots = new ArrayList<>();
                        }
                        
                        Map<String, String> newSlot = new HashMap<>();
                        newSlot.put("date", notification.getNewDate());
                        newSlot.put("startTime", notification.getNewStartTime());
                        newSlot.put("endTime", notification.getNewEndTime());
                        
                        // Prevent duplicates in rescheduledSlots if same date is rescheduled multiple times (keep latest)
                        slots.removeIf(s -> s.get("date").equals(notification.getNewDate()));
                        slots.add(newSlot);
                        
                        assignment.setRescheduledSlots(slots);

                        assignmentRepository.save(assignment);
                    }
                }
            }
            notification.setStatus("ACCEPTED");
            notification.setMessage("Your request to reschedule " + notification.getBatchName() + " to " + notification.getNewDate() + " was accepted.");
            notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        }
        return ResponseEntity.badRequest().body("Notification not found");
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<?> declineReschedule(@PathVariable String id) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        if (notifOpt.isPresent()) {
            Notification notification = notifOpt.get();
            notification.setStatus("DECLINED");
            notification.setMessage("Your request to reschedule " + notification.getBatchName() + " to " + notification.getNewDate() + " was declined.");
            notificationRepository.save(notification);
            return ResponseEntity.ok(notification);
        }
        return ResponseEntity.badRequest().body("Notification not found");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable String id) {
        notificationRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }
}
