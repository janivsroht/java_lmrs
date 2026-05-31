CREATE TABLE orders (
                        order_id CHAR(36) NOT NULL DEFAULT (UUID()),
                        tenant_id CHAR(36) NOT NULL,
                        table_id CHAR(36),
                        server_user_id CHAR(36),
                        guest_id CHAR(36),
                        status_enum VARCHAR(30) NOT NULL DEFAULT 'OPEN',
                        opened_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        closed_at DATETIME,
                        total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                        PRIMARY KEY (order_id),
                        CONSTRAINT fk_order_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
                        CONSTRAINT fk_order_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE order_items (
                             order_item_id CHAR(36) NOT NULL DEFAULT (UUID()),
                             order_id CHAR(36) NOT NULL,
                             item_id CHAR(36) NOT NULL,
                             variant_id CHAR(36),
                             quantity INT NOT NULL DEFAULT 1,
                             unit_price DECIMAL(10,2) NOT NULL,
                             modifiers_json JSON,
                             status_enum VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                             fired_at DATETIME,
                             completed_at DATETIME,
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                             PRIMARY KEY (order_item_id),
                             CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
                             CONSTRAINT fk_oi_item FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE inventory_items (
                                 inventory_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                 tenant_id CHAR(36) NOT NULL,
                                 name VARCHAR(255) NOT NULL,
                                 unit_enum VARCHAR(30) NOT NULL,
                                 current_stock DECIMAL(10,3) NOT NULL DEFAULT 0,
                                 reorder_threshold DECIMAL(10,3) NOT NULL DEFAULT 0,
                                 cost_per_unit DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                 PRIMARY KEY (inventory_id),
                                 CONSTRAINT fk_inv_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE housekeeping_tasks (
                                    task_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                    tenant_id CHAR(36) NOT NULL,
                                    room_id CHAR(36) NOT NULL,
                                    assigned_user_id CHAR(36),
                                    task_type_enum VARCHAR(50) NOT NULL,
                                    status_enum VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                    priority_enum VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                                    scheduled_date DATE NOT NULL,
                                    completed_at DATETIME,
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                    PRIMARY KEY (task_id),
                                    CONSTRAINT fk_hk_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
                                    CONSTRAINT fk_hk_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE loyalty_transactions (
                                      loyalty_tx_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                      guest_id CHAR(36) NOT NULL,
                                      transaction_type_enum VARCHAR(30) NOT NULL,
                                      points INT NOT NULL,
                                      reference_id CHAR(36),
                                      reference_type_enum VARCHAR(50),
                                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (loyalty_tx_id),
                                      CONSTRAINT fk_loyalty_guest FOREIGN KEY (guest_id) REFERENCES guests(guest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE audit_logs (
                            log_id CHAR(36) NOT NULL DEFAULT (UUID()),
                            tenant_id CHAR(36),
                            user_id CHAR(36),
                            action VARCHAR(100) NOT NULL,
                            entity_type VARCHAR(100) NOT NULL,
                            entity_id CHAR(36),
                            old_value_json JSON,
                            new_value_json JSON,
                            ip_address VARCHAR(45),
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
