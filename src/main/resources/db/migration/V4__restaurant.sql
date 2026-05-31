CREATE TABLE restaurant_tables (
                                   table_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                   tenant_id CHAR(36) NOT NULL,
                                   table_number VARCHAR(20) NOT NULL,
                                   zone_enum VARCHAR(30),
                                   capacity INT NOT NULL,
                                   status_enum VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
                                   position_x DECIMAL(8,2),
                                   position_y DECIMAL(8,2),
                                   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                   PRIMARY KEY (table_id),
                                   CONSTRAINT fk_rtable_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE table_reservations (
                                    table_res_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                    tenant_id CHAR(36) NOT NULL,
                                    guest_id CHAR(36),
                                    table_id CHAR(36) NOT NULL,
                                    party_size INT NOT NULL,
                                    reservation_dt DATETIME NOT NULL,
                                    status_enum VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                    special_notes TEXT,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                    PRIMARY KEY (table_res_id),
                                    CONSTRAINT fk_tres_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
                                    CONSTRAINT fk_tres_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE menu_categories (
                                 category_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                 tenant_id CHAR(36) NOT NULL,
                                 name VARCHAR(100) NOT NULL,
                                 display_order INT DEFAULT 0,
                                 is_active TINYINT(1) NOT NULL DEFAULT 1,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                 PRIMARY KEY (category_id),
                                 CONSTRAINT fk_mcat_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE menu_items (
                            item_id CHAR(36) NOT NULL DEFAULT (UUID()),
                            tenant_id CHAR(36) NOT NULL,
                            category_id CHAR(36) NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            description TEXT,
                            base_price DECIMAL(10,2) NOT NULL,
                            allergens_json JSON,
                            dietary_flags_json JSON,
                            is_available TINYINT(1) NOT NULL DEFAULT 1,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                            PRIMARY KEY (item_id),
                            CONSTRAINT fk_mitem_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
                            CONSTRAINT fk_mitem_category FOREIGN KEY (category_id) REFERENCES menu_categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE menu_item_variants (
                                    variant_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                    item_id CHAR(36) NOT NULL,
                                    name VARCHAR(100) NOT NULL,
                                    price_modifier DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                    PRIMARY KEY (variant_id),
                                    CONSTRAINT fk_variant_item FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;