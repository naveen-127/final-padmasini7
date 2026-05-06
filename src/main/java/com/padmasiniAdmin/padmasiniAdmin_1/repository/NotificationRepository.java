package com.padmasiniAdmin.padmasiniAdmin_1.repository;

import com.padmasiniAdmin.padmasiniAdmin_1.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByTeacherId(String teacherId);
    List<Notification> findByType(String type);
}
