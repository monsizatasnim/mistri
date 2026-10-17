package com.mistry.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_log")
@Getter
@NoArgsConstructor
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_name", nullable = false)
    private String adminName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActionCategory category;

    @Column(nullable = false)
    private String action;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(length = 1000)
    private String detail;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    public AdminActionLog(String adminName, AdminActionCategory category, String action,
                          String targetType, Long targetId, String detail, LocalDateTime performedAt) {
        this.adminName = adminName;
        this.category = category;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.detail = detail;
        this.performedAt = performedAt;
    }
}
