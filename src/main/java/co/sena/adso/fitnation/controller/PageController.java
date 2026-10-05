package co.sena.adso.fitnation.controller;

import co.sena.adso.fitnation.entity.Cliente;
import co.sena.adso.fitnation.entity.Horario;
import co.sena.adso.fitnation.entity.Plan;
import co.sena.adso.fitnation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controlador único de páginas — réplica de los blueprints de Flask.
 *
 * Flask (app/routes/*.py) con sus url_prefix:
 *   /clientes        cliente_routes   -> index, add, edit/<id>, delete, asistencia/historial
 *   /planes          plan_routes      -> index, add, edit/<id>, delete
 *   /inscripciones   inscripcion_routes -> index, add, delete
 *   /pagos           pago_routes      -> index, add
 *   /horarios        horario_routes   -> index, add, edit/<id>, historial, ...
 *   /User            users_route      -> index, add, edit/<id>, detail/<id>, delete
 *   /UserAsync       users_route_async -> API JSON (se agregará en la fase de backend)
 *
 * Fase de frontend: las colecciones vienen vacías (igual que una base nueva de
 * Flask) y los objetos de edición llegan como placeholders para poder renderizar
 * el formulario. La fase de backend las sustituye por repositorios reales.
 */
@Controller
public class PageController {

    /** Réplica de horario_routes.py: dias = ['Lunes', ..., 'Domingo']. */
    private static final List<String> DIAS_SEMANA = List.of(
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");

    private final UserRepository usuarios;

    public PageController(UserRepository usuarios) {
        this.usuarios = usuarios;
    }

    // ---------------------------------------------------------------- clientes
    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("clientes", List.of());
        return "clientes/index";
    }

    @GetMapping("/clientes/add")
    public String clienteAdd() {
        return "clientes/add";
    }

    @GetMapping("/clientes/edit/{id}")
    public String clienteEdit(@PathVariable int id, Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/edit";
    }

    @GetMapping("/clientes/asistencia/historial")
    public String clienteHistorialAsistencia(Model model) {
        model.addAttribute("asistencias", List.of());
        return "clientes/historial_asistencia";
    }

    // ------------------------------------------------------------------ planes
    @GetMapping("/planes")
    public String planes(Model model) {
        model.addAttribute("planes", List.of());
        return "planes/index";
    }

    @GetMapping("/planes/add")
    public String planAdd() {
        return "planes/add";
    }

    @GetMapping("/planes/edit/{id}")
    public String planEdit(@PathVariable int id, Model model) {
        model.addAttribute("plan", new Plan());
        return "planes/edit";
    }

    // ----------------------------------------------------------- inscripciones
    @GetMapping("/inscripciones")
    public String inscripciones(Model model) {
        model.addAttribute("inscripciones", List.of());
        model.addAttribute("hasActiveInscription", false);
        return "inscripciones/index";
    }

    @GetMapping("/inscripciones/add")
    public String inscripcionAdd(Model model) {
        model.addAttribute("planes", List.of());
        model.addAttribute("horarios", List.of());
        return "inscripciones/add";
    }

    // ------------------------------------------------------------------- pagos
    @GetMapping("/pagos")
    public String pagos(Model model) {
        model.addAttribute("pagos", List.of());
        return "pagos/index";
    }

    @GetMapping("/pagos/add")
    public String pagoAdd(Model model) {
        model.addAttribute("clientes", List.of());
        return "pagos/add";
    }

    // ---------------------------------------------------------------- horarios
    @GetMapping("/horarios")
    public String horarios(Model model) {
        model.addAttribute("horarios", List.of());
        model.addAttribute("asistenciaActiva", null);
        return "horarios/index";
    }

    @GetMapping("/horarios/add")
    public String horarioAdd(Model model) {
        model.addAttribute("trainers", usuarios.findByRol("entrenador"));
        model.addAttribute("dias", DIAS_SEMANA);
        return "horarios/add";
    }

    @GetMapping("/horarios/edit/{id}")
    public String horarioEdit(@PathVariable int id, Model model) {
        model.addAttribute("horario", new Horario());
        model.addAttribute("trainers", usuarios.findByRol("entrenador"));
        model.addAttribute("dias", DIAS_SEMANA);
        return "horarios/edit";
    }

    @GetMapping("/horarios/historial")
    public String horarioHistorial(Model model) {
        model.addAttribute("asistencias", List.of());
        return "horarios/historial";
    }

    // ----------------------------------------------------------------- usuarios
    @GetMapping("/User")
    public String usuarios(Model model) {
        model.addAttribute("data", usuarios.findAll());
        return "users/index";
    }

    @GetMapping("/User/add")
    public String usuarioAdd() {
        return "users/add";
    }

    @GetMapping("/User/edit/{id}")
    public String usuarioEdit(@PathVariable int id, Model model) {
        model.addAttribute("user", buscarUsuario(id));
        return "users/edit";
    }

    @GetMapping("/User/detail/{id}")
    public String usuarioDetail(@PathVariable int id, Model model) {
        model.addAttribute("user", buscarUsuario(id));
        return "users/detail";
    }

    // ------------------------------------------------------- recuperación
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot_password";
    }

    /** Health check (Coolify/Docker), igual que el de Flask. */
    @GetMapping("/health")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "healthy");
    }

    /** Equivalente a <code>Model.query.get_or_404(id)</code> de Flask. */
    private co.sena.adso.fitnation.entity.User buscarUsuario(int id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
