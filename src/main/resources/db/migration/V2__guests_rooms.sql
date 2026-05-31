CREATE TABLE guests (
                        guest_id CHAR(36) NOT NULL DEFAULT (UUID()),
                        tenant_id CHAR(36) NOT NULL,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        email VARCHAR(255),
                        phone VARCHAR(50),
                        dob DATE,
                        nationality VARCHAR(100),
                        id_doc_type VARCHAR(50),
                        id_doc_number VARCHAR(100),
                        loyalty_tier VARCHAR(20) DEFAULT 'BRONZE',
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted_at DATETIME,
                        is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                        PRIMARY KEY (guest_id),
                        CONSTRAINT fk_guests_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE room_types (
                            room_type_id CHAR(36) NOT NULL DEFAULT (UUID()),
                            tenant_id CHAR(36) NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            max_occupancy INT NOT NULL,
                            description TEXT,
                            amenities_json JSON,
                            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            deleted_at DATETIME,
                            is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                            PRIMARY KEY (room_type_id),
                            CONSTRAINT fk_room_types_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE rooms (
                       room_id CHAR(36) NOT NULL DEFAULT (UUID()),
                       tenant_id CHAR(36) NOT NULL,
                       room_number VARCHAR(20) NOT NULL,
                       room_type_id CHAR(36) NOT NULL,
                       floor INT,
                       status_enum VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
                       housekeeping_status_enum VARCHAR(30) NOT NULL DEFAULT 'CLEAN',
                       base_rate DECIMAL(10,2) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at DATETIME,
                       is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                       PRIMARY KEY (room_id),
                       CONSTRAINT fk_rooms_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
                       CONSTRAINT fk_rooms_room_type FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
