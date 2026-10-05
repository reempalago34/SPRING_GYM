package co.sena.adso.fitnation.api;

import co.sena.adso.fitnation.entity.Cliente;
import co.sena.adso.fitnation.repository.ClienteRepository;
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
 * API REST de clientes — réplica JSON de Cliente.to_dict()
 * (app/models/cliente.py de Flask).
 */
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes")
public class ApiClienteController {

    public record ClienteDTO(Integer idCliente, String nombre, Integer edad,
                             String telefono, String email) {
    }

    private final ClienteRepository clientes;

    public ApiClienteController(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ClienteDTO> listar() {
        return clientes.findAll().stream().map(this::aDTO).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ClienteDTO porId(@PathVariable Integer id) {
        return clientes.findById(id).map(this::aDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private ClienteDTO aDTO(Cliente c) {
        return new ClienteDTO(c.getIdCliente(), c.getNombre(), c.getEdad(),
                c.getTelefono(), c.getEmail());
    }
}
