package pl.lukbol.ProjectSpring.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lukbol.ProjectSpring.Models.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
