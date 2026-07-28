package com.df4j.xctec.xcms.datapermission;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据权限模块自动装配（AT-16）。
 *
 * <p>无自有实体/仓储：规则来源经 SPI（DataPermissionRuleProvider / ColumnMaskRuleProvider）
 * 由提供方模块注册，本模块仅装配门面实现与缓存端口。</p>
 */
@ComponentScan("com.df4j.xctec.xcms.datapermission")
@AutoConfiguration
public class DataPermissionAutoConfiguration {
}
