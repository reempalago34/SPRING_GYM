package co.sena.adso.fitnation.config;

import co.sena.adso.fitnation.entity.User;
import co.sena.adso.fitnation.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Semilla del administrador — réplica exacta de seed_admin() de Flask:
 *
 *     admin_email = app.config.get('ADMIN_EMAIL')
 *     ...
 *     if not User.query.filter_by(email=admin_email).first():
 *         db.session.add(User(nombre=..., email=..., passwordUser=..., rol='admin'))
 *
 * Se ejecuta una sola vez por arranque y solo si el correo no existe.
 */
@Component
public class AdminSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${fitnation.admin.nombre:Admin}")
    private String adminNombre;

    @Value("${fitnation.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${fitnation.admin.password:admin123}")
    private String adminPassword;

    public AdminSeedRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null) {
            return;
        }
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin {} ya existe, no se siembra nada", adminEmail);
            return;
        }

        User admin = new User(adminNombre, adminEmail, passwordEncoder.encode(adminPassword), "admin");
        userRepository.save(admin);
        log.info("Admin user {} created successfully.", adminEmail);
    }
}
