package com.nguyeninnov8.authservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private String eventId;
    private String eventType; // REGISTERED, UPDATED, DELETED
    private Long userId;
    private String email;
    private Set<String> roles;
    private LocalDateTime eventTimestamp;
}
