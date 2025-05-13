package pl.lukbol.ProjectSpring.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lukbol.ProjectSpring.Models.LoginHistory;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findAllByUsername(String username);
}
