package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.AsistenciaCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaClienteRepository extends JpaRepository<AsistenciaCliente, Integer> {

    List<AsistenciaCliente> findByIdUser(Integer idUser);
}
