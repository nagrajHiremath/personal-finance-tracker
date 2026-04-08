package com.finance.tracker.util.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Category {
    EXPENSE,
    INCOME;
    @JsonCreator
    public static Category from(String value) {
        try {
            return Category.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Category must be INCOME or EXPENSE");
        }
    }
}
