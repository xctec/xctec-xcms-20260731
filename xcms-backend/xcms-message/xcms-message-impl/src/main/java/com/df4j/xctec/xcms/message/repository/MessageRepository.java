package com.df4j.xctec.xcms.message.repository;

import com.df4j.xctec.xcms.message.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByMsgCode(String msgCode);

    Page<Message> findBySenderId(Long senderId, Pageable pageable);
}
