package org.ostech.gtdcardsbackend.enums;

public enum StatusReason {
    LOST_CARD("Lost Card", "Customer reported card as lost"),
    STOLEN_CARD("Stolen Card", "Card was stolen"),
    FRAUD_SUSPECTED("Fraud Suspected", "Suspicious activity detected"),
    CUSTOMER_REQUEST("Customer Request", "Status change requested by customer"),
    EXPIRED("Expired", "Card has expired"),
    DAMAGED("Damaged", "Physical card damage reported");

    private final String displayName;
    private final String description;

    StatusReason(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
