package com.basketball.referee.service;

import com.basketball.referee.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LiquidacionService {

    @Autowired
    private AsignacionPartidoService asignacionService;

    @Autowired
    private TarifaService tarifaService;

    public LiquidacionData calculateLiquidacion(Long arbitroId, YearMonth yearMonth) {
        List<AsignacionPartido> asignaciones = asignacionService.findByArbitroAndMonth(arbitroId, yearMonth);
        
        // Filter only completed assignments
        List<AsignacionPartido> completedAssignments = asignaciones.stream()
            .filter(a -> a.getEstado() == AsignacionPartido.EstadoAsignacion.COMPLETADA)
            .collect(Collectors.toList());

        LiquidacionData liquidacion = new LiquidacionData();
        liquidacion.setArbitroId(arbitroId);
        liquidacion.setYearMonth(yearMonth);
        liquidacion.setAsignaciones(completedAssignments);

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, Integer> partidosPorRol = new HashMap<>();
        Map<String, BigDecimal> montosPorRol = new HashMap<>();

        for (AsignacionPartido asignacion : completedAssignments) {
            BigDecimal payment = tarifaService.calculatePayment(asignacion);
            totalAmount = totalAmount.add(payment);

            String rol = asignacion.getRol().getDisplayName();
            partidosPorRol.put(rol, partidosPorRol.getOrDefault(rol, 0) + 1);
            montosPorRol.put(rol, montosPorRol.getOrDefault(rol, BigDecimal.ZERO).add(payment));
        }

        liquidacion.setTotalAmount(totalAmount);
        liquidacion.setPartidosPorRol(partidosPorRol);
        liquidacion.setMontosPorRol(montosPorRol);
        liquidacion.setTotalPartidos(completedAssignments.size());

        return liquidacion;
    }

    public List<LiquidacionSummary> generateMonthlySummary(YearMonth yearMonth) {
        // This would typically be implemented with a custom repository query
        // For now, we'll use a simplified approach
        List<LiquidacionSummary> summaries = new ArrayList<>();
        
        // This is a placeholder - in a real implementation, you'd query all arbitros
        // and calculate their liquidations
        
        return summaries;
    }

    // Inner classes for data transfer
    public static class LiquidacionData {
        private Long arbitroId;
        private YearMonth yearMonth;
        private List<AsignacionPartido> asignaciones;
        private BigDecimal totalAmount;
        private Map<String, Integer> partidosPorRol;
        private Map<String, BigDecimal> montosPorRol;
        private int totalPartidos;

        // Getters and setters
        public Long getArbitroId() { return arbitroId; }
        public void setArbitroId(Long arbitroId) { this.arbitroId = arbitroId; }
        
        public YearMonth getYearMonth() { return yearMonth; }
        public void setYearMonth(YearMonth yearMonth) { this.yearMonth = yearMonth; }
        
        public List<AsignacionPartido> getAsignaciones() { return asignaciones; }
        public void setAsignaciones(List<AsignacionPartido> asignaciones) { this.asignaciones = asignaciones; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Map<String, Integer> getPartidosPorRol() { return partidosPorRol; }
        public void setPartidosPorRol(Map<String, Integer> partidosPorRol) { this.partidosPorRol = partidosPorRol; }
        
        public Map<String, BigDecimal> getMontosPorRol() { return montosPorRol; }
        public void setMontosPorRol(Map<String, BigDecimal> montosPorRol) { this.montosPorRol = montosPorRol; }
        
        public int getTotalPartidos() { return totalPartidos; }
        public void setTotalPartidos(int totalPartidos) { this.totalPartidos = totalPartidos; }
    }

    public static class LiquidacionSummary {
        private Long arbitroId;
        private String arbitroName;
        private int totalPartidos;
        private BigDecimal totalAmount;

        // Getters and setters
        public Long getArbitroId() { return arbitroId; }
        public void setArbitroId(Long arbitroId) { this.arbitroId = arbitroId; }
        
        public String getArbitroName() { return arbitroName; }
        public void setArbitroName(String arbitroName) { this.arbitroName = arbitroName; }
        
        public int getTotalPartidos() { return totalPartidos; }
        public void setTotalPartidos(int totalPartidos) { this.totalPartidos = totalPartidos; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
}
