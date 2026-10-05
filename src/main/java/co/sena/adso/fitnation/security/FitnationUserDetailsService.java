package co.sena.adso.fitnation.security;

import co.sena.adso.fitnation.entity.User;
import co.sena.adso.fitnation.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Equivale al user_loader de Flask-Login:
 *
 *     @login_manager.user_loader
 *     def load_user(idUser):
 *         return User.query.get(int(idUser))
 *
 * Aquí el "username" de Spring Security es el nombre de usuario (nombre),
 * que es lo que el formulario de Flask llama "nameUser".
 */
@Service
public class FitnationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public FitnationUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByNombre(username)
            .or(() -> userRepository.findByEmail(username))
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado: " + username));

        return new FitnationUserDetails(user);
    }
}
