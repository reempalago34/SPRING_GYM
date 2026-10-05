package co.sena.adso.fitnation.api;

import co.sena.adso.fitnation.entity.Plan;
import co.sena.adso.fitnation.repository.PlanRepository;
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
 * API REST de planes — réplica JSON de Plan.to_dict()
 * (app/models/plan.py de Flask).
 */
@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes")
public class ApiPlanController {

    public record PlanDTO(Integer idPlan, String nombrePlan, Double precio,
                          Integer duracionMeses) {
    }

    private final PlanRepository planes;

    public ApiPlanController(PlanRepository planes) {
        this.planes = planes;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PlanDTO> listar() {
        return planes.findAll().stream().map(this::aDTO).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PlanDTO porId(@PathVariable Integer id) {
        return planes.findById(id).map(this::aDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private PlanDTO aDTO(Plan p) {
        return new PlanDTO(p.getIdPlan(), p.getNombrePlan(), p.getPrecio(),
                p.getDuracionMeses());
    }
}
