package com.df4j.xctec.xcms.identity.repository;

import com.df4j.xctec.xcms.identity.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByToken(String token);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    Optional<UserSession> findBySessionId(String sessionId);

    List<UserSession> findByUserId(Long userId);

    void deleteByToken(String token);
}
