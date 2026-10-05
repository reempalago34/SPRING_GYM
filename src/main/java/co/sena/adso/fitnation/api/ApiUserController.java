package co.sena.adso.fitnation.api;

import co.sena.adso.fitnation.entity.User;
import co.sena.adso.fitnation.repository.UserRepository;
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
 * API REST de usuarios — réplica JSON del blueprint Flask
 * {@code userAsync} (app/routes/users_route_async.py).
 *
 * Flask: GET /UserAsync/index -> jsonify([user.to_dict() for user in users])
 * Aquí:  GET /api/users      -> [{idUser, nombre, email, telefono, rol, creado_en}]
 *        GET /api/users/{id}
 *
 * El hash de contraseña NUNCA se expone (igual que to_dict de Flask).
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios")
public class ApiUserController {

    /** Réplica exacta de las claves de User.to_dict() en Flask. */
    public record UserDTO(Integer idUser, String nombre, String email,
                          String telefono, String rol, String creado_en) {
    }

    private final UserRepository usuarios;

    public ApiUserController(UserRepository usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<UserDTO> listar() {
        return usuarios.findAll().stream().map(this::aDTO).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public UserDTO porId(@PathVariable Integer id) {
        return usuarios.findById(id).map(this::aDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private UserDTO aDTO(User u) {
        return new UserDTO(u.getIdUser(), u.getNombre(), u.getEmail(),
                u.getTelefono(), u.getRol(),
                u.getCreadoEn() != null ? u.getCreadoEn().toString() : null);
    }
}
