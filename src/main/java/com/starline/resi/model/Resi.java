package com.starline.resi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "RESI")
public class Resi extends BaseEntity {

    @Id
    @Column(name = "TRACKING_NUMBER", length = 60, nullable = false)
    @Comment(value = "Resi Tracking Number", on = "TRACKING_NUMBER")
    private String trackingNumber;

    @Column(name = "USER_ID", nullable = false)
    @Comment(value = "User Id references to users table column ID", on = "USER_ID")
    private Long userId;

    @Column(name = "LAST_CHECKPOINT", length = 800)
    @Comment(value = "Last Checkpoint of the package", on = "LAST_CHECKPOINT")
    private String lastCheckpoint;

    @Column(name = "ADDITIONAL_VALUE_1")
    @Comment(value = "Additional Value 1 (it can be anything. e.g. last 5 digits of phone number for JNE verification)", on = "ADDITIONAL_VALUE_1")
    private String additionalValue1;

    @Column(name = "LAST_CHECKPOINT_UPDATE")
    @Comment(value = "Last Checkpoint Update time after being processed by job", on = "LAST_CHECKPOINT_UPDATE")
    private LocalDateTime lastCheckpointUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COURIER_ID")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Courier courier;
}
