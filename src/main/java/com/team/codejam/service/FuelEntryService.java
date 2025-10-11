package com.team.codejam.service;

import com.team.codejam.entity.FuelEntry;
import com.team.codejam.repository.FuelEntryRepository;
import com.team.codejam.specification.FuelEntrySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FuelEntryService {
    @Autowired
    private FuelEntryRepository fuelEntryRepository;

    public List<FuelEntry> getEntriesForVehicle(Long vehicleId) {
        return fuelEntryRepository.findByVehicleIdOrderByDateDesc(vehicleId);
    }

    public void deleteEntry(Long entryId) {
        fuelEntryRepository.deleteById(entryId);
    }

    public Page<FuelEntry> getFilteredEntries(Long userId, Long vehicleId, String brand, String grade, String station, LocalDate startDate, LocalDate endDate, int page) {
        Pageable pageable = PageRequest.of(page, 25);
        System.out.println("fetching filtered entries "+ startDate + " | " + endDate + " | ");
        return fuelEntryRepository.findAll(
            FuelEntrySpecification.filter(vehicleId, brand, grade, station, startDate, endDate, userId),
            pageable
        );
    }

    public FuelEntry addFuelEntry(FuelEntry entry) {
        validateEntry(entry);
        // Odometer integrity: must be greater than previous entry for the same vehicle
        List<FuelEntry> entries = fuelEntryRepository.findByVehicleIdOrderByDateDesc(entry.getVehicle().getId());
        if (!entries.isEmpty()) {
            FuelEntry latest = entries.get(0);
            if (entry.getDate().isAfter(latest.getDate()) || entry.getDate().isEqual(latest.getDate())) {
                if (entry.getOdometer() <= latest.getOdometer()) {
                    throw new IllegalArgumentException("Odometer must be greater than previous entry for this vehicle");
                }
            } else {
                // Inserted out of order: must check odometer against previous and next entries
                for (int i = 0; i < entries.size(); i++) {
                    FuelEntry e = entries.get(i);
                    if (e.getDate().isBefore(entry.getDate())) {
                        if (entry.getOdometer() <= e.getOdometer()) {
                            throw new IllegalArgumentException("Odometer must be greater than previous entry for this vehicle");
                        }
                        if (i > 0 && entry.getOdometer() >= entries.get(i - 1).getOdometer()) {
                            throw new IllegalArgumentException("Odometer must be less than next entry for this vehicle");
                        }
                        break;
                    }
                }
            }
        }
        FuelEntry saved = fuelEntryRepository.save(entry);
        return saved;
    }


    private void validateEntry(FuelEntry entry) {
        if (entry.getLiters() == null || entry.getLiters() <= 0) {
            throw new IllegalArgumentException("Liters must be positive");
        }
        if (entry.getTotalAmount() == null || entry.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
        if (entry.getOdometer() == null || entry.getOdometer() <= 0) {
            throw new IllegalArgumentException("Odometer must be positive");
        }
        if (entry.getDate() == null || entry.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be in the future");
        }
    }
}
