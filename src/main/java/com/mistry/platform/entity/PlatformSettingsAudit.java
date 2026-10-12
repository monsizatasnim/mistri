package com.mistry.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_settings_audit")
@Getter
@NoArgsConstructor
public class PlatformSettingsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public PlatformSettingsAudit(String fieldName, String oldValue, String newValue,
                                 String changedBy, LocalDateTime changedAt) {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
    }
}