package com.team.codejam.repository;

import com.team.codejam.entity.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long>, JpaSpecificationExecutor<FuelEntry> {
    List<FuelEntry> findByVehicleIdOrderByDateDesc(Long vehicleId);

    List<FuelEntry> findByVehicleUserIdAndVehicleIdOrderByDateDesc(Long userId, Long vehicleId);

    List<FuelEntry> findByVehicleUserIdAndVehicleIdAndDateBetweenOrderByDateDesc(Long userId, Long vehicleId, LocalDate start, LocalDate end);

    List<FuelEntry> findByVehicleUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
}
