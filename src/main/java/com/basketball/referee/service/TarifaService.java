package com.basketball.referee.service;

import com.basketball.referee.entity.*;
import com.basketball.referee.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    public List<Tarifa> findAll() {
        return tarifaRepository.findAll();
    }

    public List<Tarifa> findActive() {
        return tarifaRepository.findByActivaTrue();
    }

    public Optional<Tarifa> findById(Long id) {
        return tarifaRepository.findById(id);
    }

    public List<Tarifa> findByTorneo(Long torneoId) {
        return tarifaRepository.findByTorneoId(torneoId);
    }

    public List<Tarifa> findByEscalafon(Arbitro.Escalafon escalafon) {
        return tarifaRepository.findByEscalafon(escalafon);
    }

    public Optional<Tarifa> findByTorneoEscalafonAndRol(Long torneoId, Arbitro.Escalafon escalafon, AsignacionPartido.RolArbitro rol) {
        return tarifaRepository.findByTorneoIdAndEscalafonAndRol(torneoId, escalafon, rol);
    }

    public Tarifa save(Tarifa tarifa) {
        return tarifaRepository.save(tarifa);
    }

    public Tarifa create(Tarifa tarifa, BigDecimal montoTorneo) {
        tarifa.setActiva(true);
        BigDecimal montoBase;
        if (tarifa.getEscalafon() == Arbitro.Escalafon.FIBA){ montoBase = new BigDecimal("1500000.00"); }
        else if (tarifa.getEscalafon() == Arbitro.Escalafon.PRIMERA){montoBase = new BigDecimal("800000.00");}
        else if (tarifa.getEscalafon() == Arbitro.Escalafon.SEGUNDA){montoBase = new BigDecimal("500000.00");}
        else if (tarifa.getEscalafon() == Arbitro.Escalafon.TERCERA){montoBase = new BigDecimal("300000.00");}
        else if (tarifa.getEscalafon() == Arbitro.Escalafon.FORMACION){montoBase = new BigDecimal("150000.00");}
        else {montoBase = new BigDecimal("100000.00");}
        tarifa.setMonto(montoBase.add(montoTorneo));
        return tarifaRepository.save(tarifa);
    }

    public Tarifa update(Long id, Tarifa tarifaDetails) {
        Optional<Tarifa> tarifaOpt = tarifaRepository.findById(id);
        if (tarifaOpt.isPresent()) {
            Tarifa tarifa = tarifaOpt.get();
            tarifa.setTorneo(tarifaDetails.getTorneo());
            tarifa.setEscalafon(tarifaDetails.getEscalafon());
            tarifa.setRol(tarifaDetails.getRol());
            tarifa.setMonto(tarifaDetails.getMonto());
            tarifa.setDescripcion(tarifaDetails.getDescripcion());
            return tarifaRepository.save(tarifa);
        }
        throw new RuntimeException("Tarifa no encontrada");
    }

    public void toggleStatus(Long id) {
        Optional<Tarifa> tarifaOpt = tarifaRepository.findById(id);
        if (tarifaOpt.isPresent()) {
            Tarifa tarifa = tarifaOpt.get();
            tarifa.setActiva(!tarifa.isActiva());
            tarifaRepository.save(tarifa);
        }
    }

    public void deleteById(Long id) {
        tarifaRepository.deleteById(id);
    }

    public BigDecimal calculatePayment(AsignacionPartido asignacion) {
        Optional<Tarifa> tarifaOpt = findByTorneoEscalafonAndRol(
            asignacion.getPartido().getTorneo().getId(),
            asignacion.getArbitro().getEscalafon(),
            asignacion.getRol()
        );
        
        return tarifaOpt.map(Tarifa::getMonto).orElse(BigDecimal.ZERO);
    }

    public List<Tarifa> findByFilters(String search, String torneo, String escalafon) {
        List<Tarifa> tarifas = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            tarifas = tarifas.stream()
                .filter(t -> (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(search.toLowerCase())) ||
                           t.getTorneo().getNombre().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        if (torneo != null && !torneo.trim().isEmpty()) {
            Long torneoId = Long.parseLong(torneo);
            tarifas = tarifas.stream()
                .filter(t -> t.getTorneo().getId().equals(torneoId))
                .toList();
        }
        
        if (escalafon != null && !escalafon.trim().isEmpty()) {
            Arbitro.Escalafon escalafonEnum = Arbitro.Escalafon.valueOf(escalafon);
            tarifas = tarifas.stream()
                .filter(t -> t.getEscalafon().equals(escalafonEnum))
                .toList();
        }
        
        return tarifas;
    }
}
