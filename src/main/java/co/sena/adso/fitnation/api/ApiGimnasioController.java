package co.sena.adso.fitnation.api;

import co.sena.adso.fitnation.entity.Horario;
import co.sena.adso.fitnation.entity.Inscripcion;
import co.sena.adso.fitnation.entity.Pago;
import co.sena.adso.fitnation.repository.HorarioRepository;
import co.sena.adso.fitnation.repository.InscripcionRepository;
import co.sena.adso.fitnation.repository.PagoRepository;
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
 * API REST de inscripciones, pagos y horarios (solo GET).
 *
 * Réplicas JSON de Flask:
 * - Inscripcion.to_dict() -> {idInscripcion, fecha, idCliente, idPlan}
 * - Pago.to_dict()        -> {idPago, monto, fecha, metodo_pago, cliente}
 * - Horario no tiene to_dict en Flask; se expone el mismo grano
 *   (id, día, horas, entrenador) que muestra horarios/index.html.
 */
@RestController
@Tag(name = "Inscripciones, pagos y horarios")
public class ApiGimnasioController {

    public record InscripcionDTO(Integer idInscripcion, String fecha,
                                 Integer idCliente, String cliente,
                                 Integer idPlan, String plan) {
    }

    public record PagoDTO(Integer idPago, Double monto, String fecha,
                          String metodo_pago, Integer idCliente, String cliente,
                          Integer idInscripcion) {
    }

    public record HorarioDTO(Integer idHorario, String diaSemana, String horaInicio,
                             String horaFin, Integer idUser, String entrenador,
                             int inscritos) {
    }

    private final InscripcionRepository inscripciones;
    private final PagoRepository pagos;
    private final HorarioRepository horarios;

    public ApiGimnasioController(InscripcionRepository inscripciones,
                                 PagoRepository pagos,
                                 HorarioRepository horarios) {
        this.inscripciones = inscripciones;
        this.pagos = pagos;
        this.horarios = horarios;
    }

    // ---------------------------------------------------------- inscripciones
    @GetMapping("/api/inscripciones")
    @Transactional(readOnly = true)
    public List<InscripcionDTO> listarInscripciones() {
        return inscripciones.findAll().stream().map(this::aInscripcionDTO).toList();
    }

    @GetMapping("/api/inscripciones/{id}")
    @Transactional(readOnly = true)
    public InscripcionDTO inscripcionPorId(@PathVariable Integer id) {
        return inscripciones.findById(id).map(this::aInscripcionDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------------ pagos
    @GetMapping("/api/pagos")
    @Transactional(readOnly = true)
    public List<PagoDTO> listarPagos() {
        return pagos.findAll().stream().map(this::aPagoDTO).toList();
    }

    @GetMapping("/api/pagos/{id}")
    @Transactional(readOnly = true)
    public PagoDTO pagoPorId(@PathVariable Integer id) {
        return pagos.findById(id).map(this::aPagoDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // --------------------------------------------------------------- horarios
    @GetMapping("/api/horarios")
    @Transactional(readOnly = true)
    public List<HorarioDTO> listarHorarios() {
        return horarios.findAll().stream().map(this::aHorarioDTO).toList();
    }

    @GetMapping("/api/horarios/{id}")
    @Transactional(readOnly = true)
    public HorarioDTO horarioPorId(@PathVariable Integer id) {
        return horarios.findById(id).map(this::aHorarioDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // ------------------------------------------------------------------ mapeo
    private InscripcionDTO aInscripcionDTO(Inscripcion i) {
        return new InscripcionDTO(i.getIdInscripcion(),
                i.getFecha() != null ? i.getFecha().toString() : null,
                i.getIdCliente(),
                i.getCliente() != null ? i.getCliente().getNombre() : null,
                i.getIdPlan(),
                i.getPlan() != null ? i.getPlan().getNombrePlan() : null);
    }

    private PagoDTO aPagoDTO(Pago p) {
        return new PagoDTO(p.getIdPago(), p.getMonto(),
                p.getFecha() != null ? p.getFecha().toString() : null,
                p.getMetodoPago(), p.getIdCliente(),
                p.getCliente() != null ? p.getCliente().getNombre() : null,
                p.getIdInscripcion());
    }

    private HorarioDTO aHorarioDTO(Horario h) {
        return new HorarioDTO(h.getIdHorario(), h.getDiaSemana(),
                h.getHoraInicio(), h.getHoraFin(), h.getIdUser(),
                h.getTrainer() != null ? h.getTrainer().getNombre() : null,
                h.getUsuariosInscritos() != null ? h.getUsuariosInscritos().size() : 0);
    }
}
