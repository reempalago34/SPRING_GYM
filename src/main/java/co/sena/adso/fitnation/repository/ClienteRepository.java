package co.sena.adso.fitnation.repository;

import co.sena.adso.fitnation.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
