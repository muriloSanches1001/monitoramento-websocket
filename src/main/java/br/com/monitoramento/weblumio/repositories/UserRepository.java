package br.com.monitoramento.weblumio.repositories;

import br.com.monitoramento.weblumio.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String email);
}
