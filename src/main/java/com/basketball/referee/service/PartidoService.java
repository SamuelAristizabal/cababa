package com.basketball.referee.service;

import com.basketball.referee.entity.Partido;
import com.basketball.referee.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PartidoService {

    @Autowired
    private PartidoRepository partidoRepository;

    public List<Partido> findAll() {
        return partidoRepository.findAll();
    }

    public Optional<Partido> findById(Long id) {
        return partidoRepository.findById(id);
    }

    public List<Partido> findByTorneo(Long torneoId) {
        return partidoRepository.findByTorneoId(torneoId);
    }

    public List<Partido> findByCancha(Long canchaId) {
        return partidoRepository.findByCanchaId(canchaId);
    }

    public List<Partido> findByEstado(Partido.EstadoPartido estado) {
        return partidoRepository.findByEstado(estado);
    }

    public List<Partido> findUpcomingMatches() {
        return partidoRepository.findUpcomingMatches(LocalDateTime.now());
    }

    public List<Partido> findByDateRange(LocalDateTime inicio, LocalDateTime fin) {
        return partidoRepository.findByFechaHoraBetween(inicio, fin);
    }

    public List<Partido> findByArbitro(Long arbitroId) {
        return partidoRepository.findByArbitroId(arbitroId);
    }

    public Partido save(Partido partido) {
        return partidoRepository.save(partido);
    }

    public Partido create(Partido partido) {
        partido.setEstado(Partido.EstadoPartido.PROGRAMADO);
        return partidoRepository.save(partido);
    }

    public Partido update(Long id, Partido partidoDetails) {
        Optional<Partido> partidoOpt = partidoRepository.findById(id);
        if (partidoOpt.isPresent()) {
            Partido partido = partidoOpt.get();
            partido.setTorneo(partidoDetails.getTorneo());
            partido.setCancha(partidoDetails.getCancha());
            partido.setEquipoLocal(partidoDetails.getEquipoLocal());
            partido.setEquipoVisitante(partidoDetails.getEquipoVisitante());
            partido.setFechaHora(partidoDetails.getFechaHora());
            partido.setObservaciones(partidoDetails.getObservaciones());
            return partidoRepository.save(partido);
        }
        throw new RuntimeException("Partido no encontrado");
    }

    public void updateEstado(Long id, Partido.EstadoPartido nuevoEstado) {
        Optional<Partido> partidoOpt = partidoRepository.findById(id);
        if (partidoOpt.isPresent()) {
            Partido partido = partidoOpt.get();
            partido.setEstado(nuevoEstado);
            partidoRepository.save(partido);
        }
    }

    public void updateResultado(Long id, Integer resultadoLocal, Integer resultadoVisitante) {
        Optional<Partido> partidoOpt = partidoRepository.findById(id);
        if (partidoOpt.isPresent()) {
            Partido partido = partidoOpt.get();
            partido.setResultadoLocal(resultadoLocal);
            partido.setResultadoVisitante(resultadoVisitante);
            partido.setEstado(Partido.EstadoPartido.FINALIZADO);
            partidoRepository.save(partido);
        }
    }

    public void deleteById(Long id) {
        partidoRepository.deleteById(id);
    }

    public long countByEstado(Partido.EstadoPartido estado) {
        return partidoRepository.countByEstado(estado);
    }

    public List<Partido> findByFilters(String search, String torneo, String estado, String fecha) {
        // This would typically use Criteria API or custom repository methods
        // For now, implementing basic filtering
        List<Partido> partidos = findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            partidos = partidos.stream()
                .filter(p -> p.getEquipoLocal().toLowerCase().contains(search.toLowerCase()) ||
                           p.getEquipoVisitante().toLowerCase().contains(search.toLowerCase()) ||
                           p.getTorneo().getNombre().toLowerCase().contains(search.toLowerCase()))
                .toList();
        }
        
        if (torneo != null && !torneo.trim().isEmpty()) {
            Long torneoId = Long.parseLong(torneo);
            partidos = partidos.stream()
                .filter(p -> p.getTorneo().getId().equals(torneoId))
                .toList();
        }
        
        if (estado != null && !estado.trim().isEmpty()) {
            Partido.EstadoPartido estadoEnum = Partido.EstadoPartido.valueOf(estado);
            partidos = partidos.stream()
                .filter(p -> p.getEstado().equals(estadoEnum))
                .toList();
        }
        
        if (fecha != null && !fecha.trim().isEmpty()) {
            LocalDateTime fechaFiltro = LocalDateTime.parse(fecha + "T00:00:00");
            LocalDateTime fechaFin = fechaFiltro.plusDays(1);
            partidos = partidos.stream()
                .filter(p -> p.getFechaHora().isAfter(fechaFiltro) && p.getFechaHora().isBefore(fechaFin))
                .toList();
        }
        
        return partidos;
    }
}
