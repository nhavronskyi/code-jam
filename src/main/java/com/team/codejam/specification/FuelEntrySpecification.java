package com.team.codejam.specification;

import com.team.codejam.entity.FuelEntry;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;

public class FuelEntrySpecification {
    public static Specification<FuelEntry> filter(
            Long vehicleId,
            String brand,
            String grade,
            String station,
            LocalDate startDate,
            LocalDate endDate,
            Long userId
    ) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("user").get("id"), userId));
            }
            if (vehicleId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("vehicle").get("id"), vehicleId));
            }
            if (brand != null) {
                predicate = cb.and(predicate, cb.equal(root.get("fuelBrand"), brand));
            }
            if (grade != null) {
                predicate = cb.and(predicate, cb.equal(root.get("fuelGrade"), grade));
            }
            if (station != null) {
                predicate = cb.and(predicate, cb.equal(root.get("stationName"), station));
            }
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("date"), endDate));
            }
            query.orderBy(cb.desc(root.get("date")));
            return predicate;
        };
    }
}
