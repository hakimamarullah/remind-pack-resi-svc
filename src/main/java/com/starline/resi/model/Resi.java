package com.starline.resi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "RESI", uniqueConstraints = {
        @UniqueConstraint(name = "resi_user_unique", columnNames = {"TRACKING_NUMBER", "USER_ID"})
})
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

    @Column(name = "ORIGINAL_CHECKPOINT_TIME")
    @Comment(value = "Original Checkpoint Time Based On Scrapping Result", on = "ORIGINAL_CHECKPOINT_TIME")
    private String originalCheckpointTime;

    @Column(name = "ADDITIONAL_VALUE_1")
    @Comment(value = "Additional Value 1 (it can be anything. e.g. last 5 digits of phone number for JNE verification)", on = "ADDITIONAL_VALUE_1")
    private String additionalValue1;

    @Column(name = "LAST_CHECKPOINT_UPDATE")
    @Comment(value = "Last Checkpoint Update time after being processed by job", on = "LAST_CHECKPOINT_UPDATE")
    private LocalDateTime lastCheckpointUpdate;

    @Column(name = "SUBSCRIPTION_EXPIRY_DATE")
    @Comment(value = "Subscription Expiry Date", on = "SUBSCRIPTION_EXPIRY_DATE")
    private LocalDate subscriptionExpiryDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COURIER_ID")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Courier courier;
}
