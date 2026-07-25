package com.df4j.xctec.xcms.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "perm_operation")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "perm_code", unique = true, nullable = false, length = 128)
    private String permCode;

    @Column(name = "perm_name", nullable = false, length = 128)
    private String permName;

    @Column(name = "module", nullable = false, length = 64)
    private String module;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "description", length = 512)
    private String description;
}
