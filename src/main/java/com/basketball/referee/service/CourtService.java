package com.basketball.referee.service;

import com.basketball.referee.model.Court;
import com.basketball.referee.repository.CourtRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourtService {

    @Autowired
    private CourtRepository courtRepository;

    public List<Court> findAll() {
        return courtRepository.findAll();
    }

    public List<Court> findAllActive() {
        return courtRepository.findByActiveTrue();
    }

    public List<Court> findActive() {
        return findAllActive();
    }

    public List<Court> findAllActiveOrdered() {
        return courtRepository.findByActiveTrueOrderByName();
    }

    public Optional<Court> findById(Long id) {
        return courtRepository.findById(id);
    }

    public List<Court> findByCity(String city) {
        return courtRepository.findByCityIgnoreCase(city);
    }

    public List<Court> searchByName(String name) {
        return courtRepository.findByNameContainingIgnoreCase(name);
    }

    public Court save(Court court) {
        return courtRepository.save(court);
    }

    public Court create(Court court) {
        court.setActive(true);
        return courtRepository.save(court);
    }

    public Court update(Long id, Court courtDetails) {
        Optional<Court> courtOpt = courtRepository.findById(id);
        if (courtOpt.isPresent()) {
            Court court = courtOpt.get();
            court.setName(courtDetails.getName());
            court.setCity(courtDetails.getCity()); // Fixed field name to match model
            court.setAddress(courtDetails.getAddress()); // Added capacity field
            return courtRepository.save(court);
        }
        throw new RuntimeException("Court no encontrada");
    }

    public void toggleStatus(Long id) {
        Optional<Court> courtOpt = courtRepository.findById(id);
        if (courtOpt.isPresent()) {
            Court court = courtOpt.get();
            court.setActive(!court.isActive());
            courtRepository.save(court);
        }
    }

    public void deleteById(Long id) {
        courtRepository.deleteById(id);
    }

    public long countActive() {
        return courtRepository.findByActiveTrue().size();
    }

    public List<Court> findByFilters(String search, String active) {
        List<Court> courts = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            courts = courts.stream()
                .filter(c -> c.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (active != null && !active.trim().isEmpty()) {
            boolean isActive = Boolean.parseBoolean(active);
            courts = courts.stream()
                .filter(c -> c.isActive() == isActive)
                .collect(Collectors.toList());
        }
        
        return courts;
    }
}
