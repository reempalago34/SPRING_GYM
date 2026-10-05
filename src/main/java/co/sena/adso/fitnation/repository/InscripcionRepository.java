package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    List<Inscripcion> findByIdCliente(Integer idCliente);
}
