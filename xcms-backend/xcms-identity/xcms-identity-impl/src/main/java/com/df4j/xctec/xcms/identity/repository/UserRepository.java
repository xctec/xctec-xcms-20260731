package com.df4j.xctec.xcms.identity.repository;

import com.df4j.xctec.xcms.identity.api.enums.UserStatus;
import com.df4j.xctec.xcms.identity.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            select u from User u
            where u.deletedAt is null
              and (:keyword is null or u.username like %:keyword%
                   or u.realName like %:keyword% or u.phone like %:keyword%)
              and (:status is null or u.status = :status)
            """)
    Page<User> search(@Param("keyword") String keyword,
                      @Param("status") UserStatus status,
                      Pageable pageable);
}
