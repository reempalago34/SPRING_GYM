package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    List<Horario> findByIdUser(Integer idUser);
}
