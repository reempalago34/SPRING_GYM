package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByIdCliente(Integer idCliente);
}
