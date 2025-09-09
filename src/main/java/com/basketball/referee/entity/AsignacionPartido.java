package com.basketball.referee.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignaciones_partido")
public class AsignacionPartido {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arbitro_id", nullable = false)
    private Arbitro arbitro;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolArbitro rol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAsignacion estado = EstadoAsignacion.PENDIENTE;
    
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    
    @Column(columnDefinition = "TEXT")
    private String comentarios;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Enums
    public enum RolArbitro {
        PRIMER_ARBITRO("Primer Árbitro"),
        SEGUNDO_ARBITRO("Segundo Árbitro"),
        TERCER_ARBITRO("Tercer Árbitro"),
        ANOTADOR("Anotador"),
        CRONOMETRISTA("Cronometrista"),
        OPERADOR_24("Operador 24 segundos");
        
        private final String displayName;
        
        RolArbitro(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum EstadoAsignacion {
        PENDIENTE("Pendiente"),
        ACEPTADA("Aceptada"),
        RECHAZADA("Rechazada"),
        COMPLETADA("Completada");
        
        private final String displayName;
        
        EstadoAsignacion(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public AsignacionPartido() {}
    
    public AsignacionPartido(Partido partido, Arbitro arbitro, RolArbitro rol) {
        this.partido = partido;
        this.arbitro = arbitro;
        this.rol = rol;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Partido getPartido() {
        return partido;
    }
    
    public void setPartido(Partido partido) {
        this.partido = partido;
    }
    
    public Arbitro getArbitro() {
        return arbitro;
    }
    
    public void setArbitro(Arbitro arbitro) {
        this.arbitro = arbitro;
    }
    
    public RolArbitro getRol() {
        return rol;
    }
    
    public void setRol(RolArbitro rol) {
        this.rol = rol;
    }
    
    public EstadoAsignacion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoAsignacion estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }
    
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
    
    public String getComentarios() {
        return comentarios;
    }
    
    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
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
}
