package co.sena.adso.fitnation.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Autenticación — réplica de app/routes/auth.py
 *
 *   @bp.route('/', methods=['GET', 'POST'])  -> login (Spring Security procesa el POST)
 *   @bp.route('/dashboard')                  -> panel
 *   @bp.route('/logout')                     -> cierre de sesión
 *   @bp.route('/forgot-password')            -> recuperación
 */
@Controller
public class AuthController {

    /** GET / y GET /login -> pantalla de login (o dashboard si ya hay sesión). */
    @GetMapping({"/", "/login"})
    public String login(HttpServletRequest request) {
        if (estaAutenticado()) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    /**
     * Réplica de auth.dashboard().
     *
     * Los conteos y colecciones llegan en la fase de backend; por ahora se
     * publican vacíos para que la plantilla renderice igual que en Flask con
     * una base recién montada.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalClientes", 0);
        model.addAttribute("totalPlanes", 0);
        model.addAttribute("totalInscripciones", 0);
        model.addAttribute("misHorarios", List.of());
        model.addAttribute("inscripciones", List.of());
        model.addAttribute("asistenciaCliente", null);
        return "dashboard";
    }

    /**
     * Réplica de auth.reset_password() — la plantilla recibe el token de la URL
     * y lo reutiliza en el action del formulario.
     */
    @GetMapping("/reset-password/{token}")
    public String resetPassword(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "reset_password";
    }

    /** Equivale al 404 de Flask, que redirigía al login. */
    @GetMapping("/404")
    public String notFound() {
        return "redirect:/login";
    }

    private boolean estaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}
