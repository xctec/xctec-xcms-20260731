package com.df4j.xctec.xcms.identity.repository;

import com.df4j.xctec.xcms.identity.domain.SsoBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SsoBindingRepository extends JpaRepository<SsoBinding, Long> {

    Optional<SsoBinding> findByProviderIdAndIdpOpenId(Long providerId, String idpOpenId);

    List<SsoBinding> findByUserId(Long userId);
}
