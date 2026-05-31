CREATE TABLE reservations (
                              reservation_id CHAR(36) NOT NULL DEFAULT (UUID()),
                              tenant_id CHAR(36) NOT NULL,
                              guest_id CHAR(36) NOT NULL,
                              room_id CHAR(36) NOT NULL,
                              check_in_date DATE NOT NULL,
                              check_out_date DATE NOT NULL,
                              status_enum VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                              channel_enum VARCHAR(30) NOT NULL DEFAULT 'DIRECT',
                              rate_applied DECIMAL(10,2) NOT NULL,
                              special_requests TEXT,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              deleted_at DATETIME,
                              is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                              PRIMARY KEY (reservation_id),
                              CONSTRAINT fk_res_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
                              CONSTRAINT fk_res_guest FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
                              CONSTRAINT fk_res_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE folios (
                        folio_id CHAR(36) NOT NULL DEFAULT (UUID()),
                        reservation_id CHAR(36) NOT NULL,
                        guest_id CHAR(36) NOT NULL,
                        status_enum VARCHAR(30) NOT NULL DEFAULT 'OPEN',
                        total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        currency CHAR(3) NOT NULL DEFAULT 'USD',
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted_at DATETIME,
                        is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                        PRIMARY KEY (folio_id),
                        CONSTRAINT fk_folio_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
                        CONSTRAINT fk_folio_guest FOREIGN KEY (guest_id) REFERENCES guests(guest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE folio_line_items (
                                  line_item_id CHAR(36) NOT NULL DEFAULT (UUID()),
                                  folio_id CHAR(36) NOT NULL,
                                  description VARCHAR(255) NOT NULL,
                                  amount DECIMAL(10,2) NOT NULL,
                                  charge_type_enum VARCHAR(30) NOT NULL,
                                  posted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  posted_by_user_id CHAR(36),
                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                                  PRIMARY KEY (line_item_id),
                                  CONSTRAINT fk_line_folio FOREIGN KEY (folio_id) REFERENCES folios(folio_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE payments (
                          payment_id CHAR(36) NOT NULL DEFAULT (UUID()),
                          folio_id CHAR(36) NOT NULL,
                          amount DECIMAL(10,2) NOT NULL,
                          currency CHAR(3) NOT NULL DEFAULT 'USD',
                          method_enum VARCHAR(30) NOT NULL,
                          gateway_ref VARCHAR(255),
                          status_enum VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                          paid_at DATETIME,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                          PRIMARY KEY (payment_id),
                          CONSTRAINT fk_payment_folio FOREIGN KEY (folio_id) REFERENCES folios(folio_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
