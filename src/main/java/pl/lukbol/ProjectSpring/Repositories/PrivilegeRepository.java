package pl.lukbol.ProjectSpring.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lukbol.ProjectSpring.Models.Privilege;

public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByName(String name);
}
