package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByNombre(String nombre);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNombre(String nombre);

    /** Users por rol (entrenadores, recepcionistas...). */
    List<User> findByRol(String rol);
}
