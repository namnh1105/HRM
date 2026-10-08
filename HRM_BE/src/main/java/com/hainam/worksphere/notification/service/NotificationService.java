package com.hainam.worksphere.notification.service;

import com.hainam.worksphere.notification.domain.Notification;
import com.hainam.worksphere.notification.domain.NotificationType;
import com.hainam.worksphere.notification.dto.NotificationResponse;
import com.hainam.worksphere.notification.mapper.NotificationMapper;
import com.hainam.worksphere.notification.repository.NotificationRepository;
import com.hainam.worksphere.user.domain.User;
import com.hainam.worksphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;

    // Track active SSE connections per user
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        // Set timeout to 30 minutes
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(userId, emitter);

        log.info("User {} subscribed to notifications. Total connections: {}", userId, emitters.size());

        emitter.onCompletion(() -> {
            log.info("SSE completed for user {}", userId);
            emitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.info("SSE timeout for user {}", userId);
            emitters.remove(userId);
        });
        emitter.onError((e) -> {
            log.error("SSE error for user {}: {}", userId, e.getMessage());
            emitters.remove(userId);
        });

        // Send an initial event to establish connection successfully
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }

        return emitter;
    }

    @Transactional
    public void sendNotification(UUID userId, NotificationType type, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toResponse(saved);

        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("NOTIFICATION")
                        .data(response));
            } catch (IOException e) {
                log.error("Error sending notification to user {}: {}", userId, e.getMessage());
                emitters.remove(userId);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                });
    }
}
