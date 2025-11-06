package com.r2s.user.kafka;

import com.r2s.core.dto.CreateUserProfileDTO;
import com.r2s.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserKafkaConsumer {

    private final UserProfileService userProfileService;

    @KafkaListener(topics = "user.registered", groupId = "user-group")
    public void listen(CreateUserProfileDTO dto) {
        log.info("Received user registration event from Kafka: userId={}, username={}, email={}, fullName={}", 
                dto.getUserId(), dto.getUsername(), dto.getEmail(), dto.getFullName());
        try {
            userProfileService.create(dto);
            log.info("Successfully created user profile for username: {}", dto.getUsername());
        } catch (Exception e) {
            log.error("Error creating user profile for username: {} - {}", dto.getUsername(), e.getMessage(), e);
            throw e;
        }
    }
}




