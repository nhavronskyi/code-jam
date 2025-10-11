package com.team.codejam.repository;

import com.team.codejam.entity.FuelEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FuelEntryRepository extends JpaRepository<FuelEntry, Long> {
    List<FuelEntry> findByVehicleIdOrderByDateDesc(Long vehicleId);

    List<FuelEntry> findByVehicleIdAndDateBetweenOrderByDateDesc(Long vehicleId, LocalDate start, LocalDate end);

    List<FuelEntry> findByVehicleUserIdAndVehicleIdOrderByDateDesc(Long userId, Long vehicleId);

    List<FuelEntry> findByVehicleUserIdAndVehicleIdAndDateBetweenOrderByDateDesc(Long userId, Long vehicleId, LocalDate start, LocalDate end);

    List<FuelEntry> findByVehicleUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);
    List<FuelEntry> findByVehicleIdAndDateBetweenOrderByDateDesc(Long vehicleId, java.time.LocalDate start, java.time.LocalDate end);

    @Query("SELECT f FROM FuelEntry f WHERE (:vehicleId IS NULL OR f.vehicle.id = :vehicleId) " +
           "AND (:brand IS NULL OR f.fuelBrand = :brand) " +
           "AND (:grade IS NULL OR f.fuelGrade" + "= :grade) " +
           "AND (:station IS NULL OR f.stationName = :station) " +
           "AND (:startDate IS NULL OR f.date >= :startDate) " +
           "AND (:endDate IS NULL OR f.date <= :endDate) " +
           "ORDER BY f.date DESC")
    Page<FuelEntry> findFiltered(
        @Param("vehicleId") Long vehicleId,
        @Param("brand") String brand,
        @Param("grade") String grade,
        @Param("station") String station,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate,
        Pageable pageable
    );
}
