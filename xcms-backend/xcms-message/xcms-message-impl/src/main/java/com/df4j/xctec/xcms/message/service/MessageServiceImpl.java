package com.df4j.xctec.xcms.message.service;

import com.df4j.xctec.xcms.file.api.FileStorageService;
import com.df4j.xctec.xcms.file.api.dto.FileDTO;
import com.df4j.xctec.xcms.identity.api.UserService;
import com.df4j.xctec.xcms.identity.api.dto.UserBriefDTO;
import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.kernel.context.TenantContext;
import com.df4j.xctec.xcms.kernel.exception.BusinessException;
import com.df4j.xctec.xcms.kernel.exception.ErrorCodes;
import com.df4j.xctec.xcms.message.api.MessageService;
import com.df4j.xctec.xcms.message.api.dto.AttachmentDTO;
import com.df4j.xctec.xcms.message.api.dto.MessageDTO;
import com.df4j.xctec.xcms.message.api.dto.request.MessageInboxQuery;
import com.df4j.xctec.xcms.message.api.dto.request.MessageQuery;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;
import com.df4j.xctec.xcms.message.domain.Message;
import com.df4j.xctec.xcms.message.domain.MessageAttachment;
import com.df4j.xctec.xcms.message.domain.MessageRecipient;
import com.df4j.xctec.xcms.message.repository.MessageAttachmentRepository;
import com.df4j.xctec.xcms.message.repository.MessageRecipientRepository;
import com.df4j.xctec.xcms.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public MessageDTO send(SendMessageCommand command) {
        Long senderId = TenantContext.getCurrentUserId();
        Message message = new Message();
        message.setMsgCode(UUID.randomUUID().toString().replace("-", ""));
        message.setTitle(command.getTitle());
        message.setContent(command.getContent());
        message.setSenderId(senderId);
        message.setMsgType(command.getMsgType() == null ? "NOTICE" : command.getMsgType());
        message.setPriority(command.getPriority() == null ? "NORMAL" : command.getPriority());
        message.setStatus("SENT");
        message.setSendTime(LocalDateTime.now());
        message = messageRepository.save(message);

        if (command.getRecipientIds() != null && !command.getRecipientIds().isEmpty()) {
            for (Long rid : command.getRecipientIds()) {
                MessageRecipient recipient = new MessageRecipient();
                recipient.setMessageId(message.getId());
                recipient.setRecipientId(rid);
                recipient.setRead(false);
                recipient.setStatus("ACTIVE");
                messageRecipientRepository.save(recipient);
            }
        }

        if (command.getAttachmentFileIds() != null) {
            for (Long fileId : command.getAttachmentFileIds()) {
                MessageAttachment attachment = new MessageAttachment();
                attachment.setMessageId(message.getId());
                attachment.setFileId(fileId);
                try {
                    FileDTO file = fileStorageService.getFileInfo(fileId);
                    attachment.setFileName(file.getFileName());
                    attachment.setFileSize(file.getFileSize());
                } catch (Exception ignored) {
                    // 文件不存在时仅保留 id
                }
                messageAttachmentRepository.save(attachment);
            }
        }
        return toDto(message, false, recipientNames(message.getId()), attachments(message.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MessageDTO> inbox(MessageInboxQuery query) {
        Long uid = TenantContext.getCurrentUserId();
        int page = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<MessageRecipient> recipientPage = messageRecipientRepository.findByRecipientId(uid, pageable);
        List<MessageDTO> list = recipientPage.getContent().stream()
                .map(r -> messageRepository.findById(r.getMessageId())
                        .map(m -> toDto(m, r.isRead(), recipientNames(m.getId()), attachments(m.getId())))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
        return PageResult.of(list, recipientPage.getTotalElements());
    }

    @Override
    @Transactional
    public MessageDTO getDetail(Long messageId) {
        Long uid = TenantContext.getCurrentUserId();
        MessageRecipient recipient = messageRecipientRepository.findByMessageIdAndRecipientId(messageId, uid)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "无权查看该消息"));
        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadTime(LocalDateTime.now());
            messageRecipientRepository.save(recipient);
        }
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "消息不存在: " + messageId));
        return toDto(message, true, recipientNames(messageId), attachments(messageId));
    }

    @Override
    @Transactional
    public void markRead(Long messageId) {
        Long uid = TenantContext.getCurrentUserId();
        MessageRecipient recipient = messageRecipientRepository.findByMessageIdAndRecipientId(messageId, uid)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "无权操作该消息"));
        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadTime(LocalDateTime.now());
            messageRecipientRepository.save(recipient);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MessageDTO> sent(MessageQuery query) {
        Long uid = TenantContext.getCurrentUserId();
        int page = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() <= 0 ? 20 : query.getSize();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Message> messagePage = messageRepository.findBySenderId(uid, pageable);
        List<MessageDTO> list = messagePage.getContent().stream()
                .map(m -> toDto(m, false, recipientNames(m.getId()), attachments(m.getId())))
                .toList();
        return PageResult.of(list, messagePage.getTotalElements());
    }

    @Override
    @Transactional
    public void revoke(Long messageId) {
        Long uid = TenantContext.getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCodes.NOT_FOUND, "消息不存在: " + messageId));
        if (!Objects.equals(uid, message.getSenderId())) {
            throw new BusinessException(ErrorCodes.PERMISSION_DENIED, "仅发件人可撤回消息");
        }
        message.setStatus("REVOKED");
        messageRepository.save(message);
        messageRecipientRepository.findByMessageId(messageId).forEach(r -> {
            r.setStatus("REVOKED");
            messageRecipientRepository.save(r);
        });
    }

    private String resolveName(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return userService.getUserById(userId).getRealName();
        } catch (Exception e) {
            return null;
        }
    }

    private Map<Long, UserBriefDTO> resolveUsers(List<Long> ids) {
        try {
            return userService.getUsersByIds(ids).stream()
                    .collect(Collectors.toMap(UserBriefDTO::getId, Function.identity()));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<String> recipientNames(Long messageId) {
        List<Long> ids = messageRecipientRepository.findByMessageId(messageId).stream()
                .map(MessageRecipient::getRecipientId)
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return resolveUsers(ids).values().stream()
                .map(UserBriefDTO::getRealName)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AttachmentDTO> attachments(Long messageId) {
        return messageAttachmentRepository.findByMessageId(messageId).stream().map(a -> {
            AttachmentDTO dto = new AttachmentDTO();
            dto.setFileId(a.getFileId());
            dto.setFileName(a.getFileName());
            return dto;
        }).toList();
    }

    private MessageDTO toDto(Message m, boolean read, List<String> names, List<AttachmentDTO> attachments) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setMsgCode(m.getMsgCode());
        dto.setTitle(m.getTitle());
        dto.setContent(m.getContent());
        dto.setSenderId(m.getSenderId());
        dto.setSenderName(resolveName(m.getSenderId()));
        dto.setMsgType(m.getMsgType());
        dto.setPriority(m.getPriority());
        dto.setStatus(m.getStatus());
        dto.setSendTime(m.getSendTime());
        dto.setRead(read);
        dto.setRecipientNames(names);
        dto.setAttachments(attachments);
        return dto;
    }
}
