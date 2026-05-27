package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

// Block / unblock a customer. action comes from the API as "block"/"unblock";
// targetStatus() maps it to the persisted status value.
public record ChangeCustomerStatusCommand(UUID customerId, Action action) {

    public enum Action { BLOCK, UNBLOCK }

    public static ChangeCustomerStatusCommand from(UUID customerId, String action) {
        if (customerId == null) {
            throw DomainException.validation("customerId is required");
        }
        if (action == null || action.isBlank()) {
            throw DomainException.validation("action is required");
        }
        Action parsed = switch (action.trim().toLowerCase()) {
            case "block" -> Action.BLOCK;
            case "unblock" -> Action.UNBLOCK;
            default -> throw DomainException.validation("action must be 'block' or 'unblock'");
        };
        return new ChangeCustomerStatusCommand(customerId, parsed);
    }

    public String targetStatus() {
        return action == Action.BLOCK ? "BLOCKED" : "ACTIVE";
    }
}
