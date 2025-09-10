package com.basketball.referee.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "matches")
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;
    
    @NotBlank(message = "El equipo local es obligatorio")
    @Column(name = "local_team", nullable = false)
    private String localTeam;
    
    @NotBlank(message = "El equipo visitante es obligatorio")
    @Column(name = "visitor_team", nullable = false)
    private String visitorTeam;
    
    @NotNull(message = "La date y hour del match es obligatoria")
    @Column(name = "date_hour", nullable = false)
    private LocalDateTime dateHour;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchState state = MatchState.PROGRAMMED;
    
    @Column(name = "local_result")
    private Integer localResult;
    
    @Column(name = "visitor_result")
    private Integer visitorResult;
    
    @Column(columnDefinition = "TEXT")
    private String observations;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchAssignment> assignments = new ArrayList<>();
    
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
    public enum MatchState {
        PROGRAMMED("Programado"),
        IN_PROGRESS("En Curso"),
        FINISHED("Finalizado"),
        CANCELED("Cancelado"),
        SUSPENDED("Suspendido");
        
        private final String displayName;
        
        MatchState(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public Match() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Tournament getTournament() {
        return tournament;
    }
    
    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }
    
    public Court getCourt() {
        return court;
    }
    
    public void setCourt(Court court) {
        this.court = court;
    }
    
    public String getLocalTeam() {
        return localTeam;
    }
    
    public void setLocalTeam(String localTeam) {
        this.localTeam = localTeam;
    }
    
    public String getVisitorTeam() {
        return visitorTeam;
    }
    
    public void setVisitorTeam(String visitorTeam) {
        this.visitorTeam = visitorTeam;
    }
    
    public LocalDateTime getDateHour() {
        return dateHour;
    }
    
    public void setDateHour(LocalDateTime dateHour) {
        this.dateHour = dateHour;
    }
    
    public MatchState getState() {
        return state;
    }
    
    public void setState(MatchState state) {
        this.state = state;
    }
    
    public Integer getLocalResult() {
        return localResult;
    }
    
    public void setLocalResult(Integer localResult) {
        this.localResult = localResult;
    }
    
    public Integer getVisitorResult() {
        return visitorResult;
    }
    
    public void setVisitorResult(Integer visitorResult) {
        this.visitorResult = visitorResult;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
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
    
    public List<MatchAssignment> getAssignments() {
        return assignments;
    }
    
    public void setAssignments(List<MatchAssignment> assignments) {
        this.assignments = assignments;
    }
    
    public String getMatchDescription() {
        return localTeam + " vs " + visitorTeam;
    }
}
