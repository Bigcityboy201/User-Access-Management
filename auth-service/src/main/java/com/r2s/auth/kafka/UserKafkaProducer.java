package com.r2s.auth.kafka;

import com.r2s.core.dto.CreateUserProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserRegistered(CreateUserProfileDTO user) {
        log.info("Sending user registration event to Kafka: userId={}, username={}, email={}, fullName={}", 
                user.getUserId(), user.getUsername(), user.getEmail(), user.getFullName());
        try {
            kafkaTemplate.send("user.registered", user);
            log.info("Successfully sent user registration event to Kafka topic: user.registered");
        } catch (Exception e) {
            log.error("Failed to send user registration event to Kafka: {}", e.getMessage(), e);
        }
    }
}



