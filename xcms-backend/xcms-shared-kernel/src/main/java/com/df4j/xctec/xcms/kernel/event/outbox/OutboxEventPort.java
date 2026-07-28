package com.df4j.xctec.xcms.kernel.event.outbox;

import com.df4j.xctec.xcms.kernel.event.DomainEvent;

/**
 * Outbox 出站端口（AT-05，拆分预留 SPI）。
 *
 * <p>事务性发件箱模式（Transactional Outbox）的出站端口：在业务事务内将领域事件
 * 持久化到 {@code event_outbox} 表（DDL 见 {@code db/ddl/09-event-outbox.sql}），
 * 由后台转发器（OutboxRelay）轮询未投递记录并发送到 MQ，保证"业务落库"与"事件外发"
 * 的最终一致性（避免双写不一致）。</p>
 *
 * <p>形态切换（业务代码零改动，仅实现替换）：</p>
 * <ul>
 *   <li><b>单体</b>（{@code xcms.event.outbox.enabled=false}，缺省）：装配
 *       {@link NoOpOutboxEventPort}，不写表；事件仍经
 *       {@code InProcessEventPublisher} 同步发布，AFTER_COMMIT 进程内分发，零成本。</li>
 *   <li><b>拆分</b>（{@code xcms.event.outbox.enabled=true}）：由装配层提供
 *       {@code JpaOutboxEventPort}（写 event_outbox 表）+ OutboxRelay（后台转发 MQ，
 *       可复用 {@code MqEventPublisher} 骨架），本端口自动接管事件出站。</li>
 * </ul>
 */
public interface OutboxEventPort {

    /**
     * 随业务事务持久化领域事件（要求调用方处于活动事务中，与业务写库同事务提交）。
     *
     * @param event 领域事件（建议实现 {@link java.io.Serializable}，由实现负责序列化）
     */
    void save(DomainEvent event);
}
