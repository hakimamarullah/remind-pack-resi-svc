package com.starline.resi.dto.proxy.subscriptions;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum SubscriptionStatus {

    PENDING, ACTIVE, CANCELLED, EXPIRED;

    @JsonCreator
    public static SubscriptionStatus getType(String type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (SubscriptionStatus requestType : values()) {
            if (requestType.name().equalsIgnoreCase(type)) {
                return requestType;
            }
        }
        return null;
    }
}
