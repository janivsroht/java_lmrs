-- V7: Add performance indexes and missing foreign key constraints

-- ═══════════════════════════════════════════════════════════════════
-- INDEXES
-- ═══════════════════════════════════════════════════════════════════

-- Users: login lookup, tenant filtering
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_tenant_active ON users(tenant_id, is_deleted, is_active);

-- Guests: frequent queries
CREATE INDEX idx_guests_tenant_deleted ON guests(tenant_id, is_deleted);
CREATE INDEX idx_guests_email_tenant ON guests(email, tenant_id);

-- Reservations: conflict detection, dashboard queries
CREATE INDEX idx_res_room_dates ON reservations(room_id, is_deleted, check_in_date, check_out_date);
CREATE INDEX idx_res_tenant_status ON reservations(tenant_id, status_enum, is_deleted);
CREATE INDEX idx_res_tenant_created ON reservations(tenant_id, created_at);

-- Orders: status filter
CREATE INDEX idx_orders_tenant_status ON orders(tenant_id, status_enum, is_deleted);

-- Order items: order lookup
CREATE INDEX idx_order_items_order ON order_items(order_id, is_deleted);

-- Payments: folio lookup, revenue reports
CREATE INDEX idx_payments_folio ON payments(folio_id, is_deleted);
CREATE INDEX idx_payments_folio_status ON payments(folio_id, is_deleted, status_enum);

-- Folio line items: folio lookup
CREATE INDEX idx_folio_line_folio ON folio_line_items(folio_id, is_deleted);

-- Housekeeping: daily task list
CREATE INDEX idx_hk_tenant_date ON housekeeping_tasks(tenant_id, scheduled_date, is_deleted);

-- Inventory: low stock queries
CREATE INDEX idx_inv_tenant_deleted ON inventory_items(tenant_id, is_deleted);

-- Partner API usage: dashboard stats
CREATE INDEX idx_partner_usage_partner ON partner_api_usage(partner_id, created_at);

-- Menu items: tenant listing
CREATE INDEX idx_menu_items_tenant ON menu_items(tenant_id, is_deleted);

-- ═══════════════════════════════════════════════════════════════════
-- MISSING FOREIGN KEY CONSTRAINTS
-- ═══════════════════════════════════════════════════════════════════

-- Orders: guest and server user references
ALTER TABLE orders ADD CONSTRAINT fk_order_guest
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id);
ALTER TABLE orders ADD CONSTRAINT fk_order_server
    FOREIGN KEY (server_user_id) REFERENCES users(user_id);

-- Order items: variant reference
ALTER TABLE order_items ADD CONSTRAINT fk_oi_variant
    FOREIGN KEY (variant_id) REFERENCES menu_item_variants(variant_id);

-- Housekeeping tasks: assigned user reference
ALTER TABLE housekeeping_tasks ADD CONSTRAINT fk_hk_assignee
    FOREIGN KEY (assigned_user_id) REFERENCES users(user_id);

-- Folio line items: posted by user reference
ALTER TABLE folio_line_items ADD CONSTRAINT fk_line_posted_by
    FOREIGN KEY (posted_by_user_id) REFERENCES users(user_id);

-- Audit logs: tenant and user references
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id);
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_user
    FOREIGN KEY (user_id) REFERENCES users(user_id);

-- Fix V6: partner_accounts.deleted_at should default to NULL, not CURRENT_TIMESTAMP
ALTER TABLE partner_accounts ALTER COLUMN deleted_at DROP DEFAULT;
