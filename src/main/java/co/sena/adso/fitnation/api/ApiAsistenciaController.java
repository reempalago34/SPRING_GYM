package co.sena.adso.fitnation.api;

import co.sena.adso.fitnation.entity.AsistenciaCliente;
import co.sena.adso.fitnation.entity.AsistenciaEntrenador;
import co.sena.adso.fitnation.repository.AsistenciaClienteRepository;
import co.sena.adso.fitnation.repository.AsistenciaEntrenadorRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * API REST de asistencias (solo GET) — réplica de los historiales de Flask:
 * - cliente_routes.historial()  -> asistencias_clientes
 * - horario_routes.historial()  -> asistencias_entrenadores
 */
@RestController
@RequestMapping("/api/asistencias")
@Tag(name = "Asistencias")
public class ApiAsistenciaController {

    public record AsistenciaClienteDTO(Integer idAsistencia, Integer idUser, String usuario,
                                       String fecha, String horaEntrada, String horaSalida,
                                       String observaciones) {
    }

    public record AsistenciaEntrenadorDTO(Integer idAsistencia, Integer idUser, String entrenador,
                                         Integer idHorario, String fecha, String horaEntrada,
                                         String horaSalida, String observaciones) {
    }

    private final AsistenciaClienteRepository asistenciasClientes;
    private final AsistenciaEntrenadorRepository asistenciasEntrenadores;

    public ApiAsistenciaController(AsistenciaClienteRepository asistenciasClientes,
                                   AsistenciaEntrenadorRepository asistenciasEntrenadores) {
        this.asistenciasClientes = asistenciasClientes;
        this.asistenciasEntrenadores = asistenciasEntrenadores;
    }

    // ---------------------------------------------------------------- clientes
    @GetMapping("/clientes")
    @Transactional(readOnly = true)
    public List<AsistenciaClienteDTO> listarClientes() {
        return asistenciasClientes.findAll().stream().map(this::aClienteDTO).toList();
    }

    @GetMapping("/clientes/{id}")
    @Transactional(readOnly = true)
    public AsistenciaClienteDTO clientePorId(@PathVariable Integer id) {
        return asistenciasClientes.findById(id).map(this::aClienteDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------ entrenadores
    @GetMapping("/entrenadores")
    @Transactional(readOnly = true)
    public List<AsistenciaEntrenadorDTO> listarEntrenadores() {
        return asistenciasEntrenadores.findAll().stream().map(this::aEntrenadorDTO).toList();
    }

    @GetMapping("/entrenadores/{id}")
    @Transactional(readOnly = true)
    public AsistenciaEntrenadorDTO entrenadorPorId(@PathVariable Integer id) {
        return asistenciasEntrenadores.findById(id).map(this::aEntrenadorDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------------ mapeo
    private AsistenciaClienteDTO aClienteDTO(AsistenciaCliente a) {
        return new AsistenciaClienteDTO(a.getIdAsistencia(), a.getIdUser(),
                a.getUsuario() != null ? a.getUsuario().getNombre() : null,
                a.getFecha() != null ? a.getFecha().toString() : null,
                a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : null,
                a.getHoraSalida() != null ? a.getHoraSalida().toString() : null,
                a.getObservaciones());
    }

    private AsistenciaEntrenadorDTO aEntrenadorDTO(AsistenciaEntrenador a) {
        return new AsistenciaEntrenadorDTO(a.getIdAsistencia(), a.getIdUser(),
                a.getEntrenador() != null ? a.getEntrenador().getNombre() : null,
                a.getIdHorario(),
                a.getFecha() != null ? a.getFecha().toString() : null,
                a.getHoraEntrada() != null ? a.getHoraEntrada().toString() : null,
                a.getHoraSalida() != null ? a.getHoraSalida().toString() : null,
                a.getObservaciones());
    }
}
