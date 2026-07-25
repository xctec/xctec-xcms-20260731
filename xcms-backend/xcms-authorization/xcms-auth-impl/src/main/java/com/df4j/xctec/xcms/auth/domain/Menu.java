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
@Table(name = "perm_menu")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_code", unique = true, nullable = false, length = 128)
    private String menuCode;

    @Column(name = "menu_name", nullable = false, length = 128)
    private String menuName;

    @Column(name = "menu_type", nullable = false, length = 20)
    private String menuType;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "path", length = 256)
    private String path;

    @Column(name = "icon", length = 64)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "visible", nullable = false)
    private Boolean visible;

    @Column(name = "scope", nullable = false, length = 20)
    private String scope;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}
