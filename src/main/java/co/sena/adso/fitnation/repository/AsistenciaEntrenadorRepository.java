package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.AsistenciaEntrenador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaEntrenadorRepository extends JpaRepository<AsistenciaEntrenador, Integer> {

    List<AsistenciaEntrenador> findByIdUser(Integer idUser);
}
