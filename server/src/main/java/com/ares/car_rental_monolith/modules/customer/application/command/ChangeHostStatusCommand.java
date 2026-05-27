package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

// Suspend / re-activate a customer's host profile. action comes from the API
// as "suspend"/"activate"; targetStatus() maps it to the persisted value.
public record ChangeHostStatusCommand(UUID customerId, Action action) {

    public enum Action { SUSPEND, ACTIVATE }

    public static ChangeHostStatusCommand from(UUID customerId, String action) {
        if (customerId == null) {
            throw DomainException.validation("customerId is required");
        }
        if (action == null || action.isBlank()) {
            throw DomainException.validation("action is required");
        }
        Action parsed = switch (action.trim().toLowerCase()) {
            case "suspend" -> Action.SUSPEND;
            case "activate" -> Action.ACTIVATE;
            default -> throw DomainException.validation("action must be 'suspend' or 'activate'");
        };
        return new ChangeHostStatusCommand(customerId, parsed);
    }

    public String targetStatus() {
        return action == Action.SUSPEND ? "SUSPENDED" : "ACTIVE";
    }
}
