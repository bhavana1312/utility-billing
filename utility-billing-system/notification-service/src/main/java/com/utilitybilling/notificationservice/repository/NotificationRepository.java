package com.utilitybilling.notificationservice.repository;

import com.utilitybilling.notificationservice.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification,String>{
}
