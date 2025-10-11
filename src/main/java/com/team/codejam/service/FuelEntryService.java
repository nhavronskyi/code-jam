package com.team.codejam.service;

import com.team.codejam.entity.FuelEntry;
import com.team.codejam.repository.FuelEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public FuelEntry addFuelEntry(FuelEntry entry) {
        validateEntry(entry);
        return fuelEntryRepository.save(entry);
    }

    public void deleteEntry(Long id) {
        fuelEntryRepository.deleteById(id);
    }

    private void validateEntry(FuelEntry entry) {
        if (entry.getLiters() == null || entry.getLiters() <= 0) {
            throw new IllegalArgumentException("Liters must be positive");
        }
        if (entry.getTotalAmount() == null || entry.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
        if (entry.getDate() == null || entry.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date must not be in the future");
        }
        // Additional odometer monotonicity validation to be added in controller/service
    }
}
