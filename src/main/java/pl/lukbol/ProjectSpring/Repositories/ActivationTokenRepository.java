package pl.lukbol.ProjectSpring.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lukbol.ProjectSpring.Models.ActivationToken;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findOptionalByToken(String token);

    void deleteByUserId(Long userId);
}
