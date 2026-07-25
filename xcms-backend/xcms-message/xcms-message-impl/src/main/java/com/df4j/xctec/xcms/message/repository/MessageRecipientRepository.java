package com.df4j.xctec.xcms.message.repository;

import com.df4j.xctec.xcms.message.domain.MessageRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Long> {

    Page<MessageRecipient> findByRecipientId(Long recipientId, Pageable pageable);

    Optional<MessageRecipient> findByMessageIdAndRecipientId(Long messageId, Long recipientId);

    List<MessageRecipient> findByMessageId(Long messageId);
}
