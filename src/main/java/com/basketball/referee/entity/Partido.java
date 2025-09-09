package com.basketball.referee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partidos")
public class Partido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;
    
    @NotBlank(message = "El equipo local es obligatorio")
    @Column(name = "equipo_local", nullable = false)
    private String equipoLocal;
    
    @NotBlank(message = "El equipo visitante es obligatorio")
    @Column(name = "equipo_visitante", nullable = false)
    private String equipoVisitante;
    
    @NotNull(message = "La fecha y hora del partido es obligatoria")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;
    
    @Column(name = "resultado_local")
    private Integer resultadoLocal;
    
    @Column(name = "resultado_visitante")
    private Integer resultadoVisitante;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "partido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AsignacionPartido> asignaciones = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Enum
    public enum EstadoPartido {
        PROGRAMADO("Programado"),
        EN_CURSO("En Curso"),
        FINALIZADO("Finalizado"),
        CANCELADO("Cancelado"),
        SUSPENDIDO("Suspendido");
        
        private final String displayName;
        
        EstadoPartido(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Partido() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Torneo getTorneo() {
        return torneo;
    }
    
    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }
    
    public Cancha getCancha() {
        return cancha;
    }
    
    public void setCancha(Cancha cancha) {
        this.cancha = cancha;
    }
    
    public String getEquipoLocal() {
        return equipoLocal;
    }
    
    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }
    
    public String getEquipoVisitante() {
        return equipoVisitante;
    }
    
    public void setEquipoVisitante(String equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public EstadoPartido getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoPartido estado) {
        this.estado = estado;
    }
    
    public Integer getResultadoLocal() {
        return resultadoLocal;
    }
    
    public void setResultadoLocal(Integer resultadoLocal) {
        this.resultadoLocal = resultadoLocal;
    }
    
    public Integer getResultadoVisitante() {
        return resultadoVisitante;
    }
    
    public void setResultadoVisitante(Integer resultadoVisitante) {
        this.resultadoVisitante = resultadoVisitante;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<AsignacionPartido> getAsignaciones() {
        return asignaciones;
    }
    
    public void setAsignaciones(List<AsignacionPartido> asignaciones) {
        this.asignaciones = asignaciones;
    }
    
    public String getPartidoDescripcion() {
        return equipoLocal + " vs " + equipoVisitante;
    }
}
