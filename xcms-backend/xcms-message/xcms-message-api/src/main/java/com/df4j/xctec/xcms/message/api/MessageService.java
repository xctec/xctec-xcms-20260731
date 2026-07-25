package com.df4j.xctec.xcms.message.api;

import com.df4j.xctec.xcms.kernel.common.PageResult;
import com.df4j.xctec.xcms.message.api.dto.MessageDTO;
import com.df4j.xctec.xcms.message.api.dto.request.MessageInboxQuery;
import com.df4j.xctec.xcms.message.api.dto.request.MessageQuery;
import com.df4j.xctec.xcms.message.api.dto.request.SendMessageCommand;

/**
 * 消息服务。发送消息（含收件人、附件）、收件箱、已发、已读、撤回。
 * 依赖 identity（解析收件人/发件人）与 file-storage（附件）。
 */
public interface MessageService {

    MessageDTO send(SendMessageCommand command);

    PageResult<MessageDTO> inbox(MessageInboxQuery query);

    MessageDTO getDetail(Long messageId);

    void markRead(Long messageId);

    PageResult<MessageDTO> sent(MessageQuery query);

    void revoke(Long messageId);
}
