-- Partner registry
CREATE TABLE partner_accounts (
                                  partner_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                  tenant_id CHAR(36) NOT NULL,
                                  name VARCHAR(255) NOT NULL,
                                  provider_type VARCHAR(50) NOT NULL, -- ZOMATO, SWIGGY, CUSTOM
                                  api_key VARCHAR(64) NOT NULL UNIQUE,
                                  is_active TINYINT(1) NOT NULL DEFAULT 1,
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  deleted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                  PRIMARY KEY (partner_id),
                                  CONSTRAINT fk_partner_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- Per-endpoint usage tracking
CREATE TABLE partner_api_usage (
                                   usage_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                   partner_id CHAR(36) NOT NULL,
                                   endpoint VARCHAR(255) NOT NULL,
                                   http_method VARCHAR(10) NOT NULL,
                                   status_code INT NOT NULL,
                                   response_ms INT NOT NULL DEFAULT 0,
                                   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (usage_id),
                                   CONSTRAINT fk_usage_partner FOREIGN KEY (partner_id)
                                       REFERENCES partner_accounts(partner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;