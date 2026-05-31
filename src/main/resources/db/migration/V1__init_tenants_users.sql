CREATE TABLE tenants (
                         tenant_id CHAR(36) NOT NULL DEFAULT (UUID()),
                         name VARCHAR(255) NOT NULL,
                         subdomain VARCHAR(100) NOT NULL UNIQUE,
                         config_json JSON,
                         is_active TINYINT(1) NOT NULL DEFAULT 1,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         deleted_at DATETIME,
                         is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                         PRIMARY KEY (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE users (
                       user_id CHAR(36) NOT NULL DEFAULT (UUID()),
                       tenant_id CHAR(36) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role_enum VARCHAR(50) NOT NULL,
                       is_active TINYINT(1) NOT NULL DEFAULT 1,
                       last_login DATETIME,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at DATETIME,
                       is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                       PRIMARY KEY (user_id),
                       CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;