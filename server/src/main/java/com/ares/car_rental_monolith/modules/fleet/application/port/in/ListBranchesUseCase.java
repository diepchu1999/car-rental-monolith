package com.ares.car_rental_monolith.modules.fleet.application.port.in;

import com.ares.car_rental_monolith.modules.fleet.domain.BranchSummary;
import java.util.List;

@FunctionalInterface
public interface ListBranchesUseCase {
    List<BranchSummary> handle();
}
