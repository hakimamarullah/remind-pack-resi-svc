package com.starline.resi.dto.rabbit.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum ScrappingType {

    ADD, UPDATE;

    @JsonCreator
    public ScrappingType getType(String type) {
        if (Objects.isNull(type)) {
            return null;
        }
        for (ScrappingType requestType : values()) {
            if (requestType.name().equalsIgnoreCase(type)) {
                return requestType;
            }
        }
        return null;
    }
}
