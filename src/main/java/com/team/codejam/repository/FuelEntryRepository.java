package com.team.codejam.repository;

import com.team.codejam.entity.FuelEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {
    List<FuelEntry> findByVehicleIdOrderByDateDesc(Long vehicleId);

    List<FuelEntry> findByVehicleIdAndDateBetweenOrderByDateDesc(Long vehicleId, java.time.LocalDate start, java.time.LocalDate end);
}
