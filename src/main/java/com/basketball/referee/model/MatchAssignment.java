package com.basketball.referee.model;

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
@Table(name = "match_assignments")
public class MatchAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id", nullable = false)
    private Referee referee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefereeRole refereeRole;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentState state = AssignmentState.PENDING;
    
    @Column(name = "response_date")
    private LocalDateTime responseDate;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
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
    public enum RefereeRole {
        FIRST_REFEREE("Primer Árbitro"),
        SECOND_REFEREE("Segundo Árbitro"),
        THIRD_REFEREE("Tercer Árbitro"),
        ANNOTATOR("Anotador"),
        TIMEKEEPER("Cronometrista"),
        OPERATOR_24("Operador 24 segundos");
        
        private final String displayName;
        
        RefereeRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum AssignmentState {
        PENDING("Pendiente"),
        ACCEPTED("Aceptada"),
        REJECTED("Rechazada"),
        COMPLETED("Completada");
        
        private final String displayName;
        
        AssignmentState(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructors
    public MatchAssignment() {}
    
    public MatchAssignment(Match match, Referee referee, RefereeRole refereeRole) {
        this.match = match;
        this.referee = referee;
        this.refereeRole = refereeRole;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Match getMatch() {
        return match;
    }
    
    public void setMatch(Match match) {
        this.match = match;
    }
    
    public Referee getReferee() {
        return referee;
    }
    
    public void setReferee(Referee referee) {
        this.referee = referee;
    }
    
    public RefereeRole getRole() {
        return refereeRole;
    }
    
    public void setRole(RefereeRole refereeRole) {
        this.refereeRole = refereeRole;
    }
    
    public AssignmentState getState() {
        return state;
    }
    
    public void setState(AssignmentState state) {
        this.state = state;
    }
    
    public LocalDateTime getResponseDate() {
        return responseDate;
    }
    
    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
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
