package com.parikshaguru.entity.enums;

public enum AccountStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ACTIVE,
    BLOCKED;

    public boolean canLogin() { return this == ACTIVE || this == APPROVED; }
}
