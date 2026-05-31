let ordersData = [];
let menuItemsList = [];

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadOrders();

    document.getElementById('btn-add-order').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-order').addEventListener('click', saveOrder);
    document.getElementById('btn-add-item').addEventListener('click', addOrderItemRow);
    document.getElementById('btn-filter').addEventListener('click', filterOrders);
    document.getElementById('filter-status').addEventListener('change', filterOrders);
    document.getElementById('order-items').addEventListener('click', function(e) {
        if (e.target.closest('.btn-remove-item')) {
            e.target.closest('.order-item-row').remove();
            updateTotal();
        }
    });
    document.getElementById('order-items').addEventListener('input', updateTotal);
});

function populateSidebar() {
    const user = Auth.getUser();
    if (user) {
        const emailEl = document.getElementById('sidebar-user-email');
        const roleEl = document.getElementById('sidebar-user-role');
        if (emailEl) emailEl.textContent = user.email || '';
        if (roleEl) roleEl.textContent = user.role || '';
    }
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) logoutBtn.addEventListener('click', function() { Auth.logout(); });
}

function loadOrders() {
    Auth.authFetch('/api/v1/orders')
        .then(r => r.json())
        .then(data => { ordersData = data; renderTable(data); })
        .catch(err => showToast('Failed to load orders: ' + err.message, 'error'));
}

function filterOrders() {
    const status = document.getElementById('filter-status').value;
    if (!status) { loadOrders(); return; }
    Auth.authFetch('/api/v1/orders/status/' + status)
        .then(r => r.json())
        .then(data => { ordersData = data; renderTable(data); })
        .catch(err => showToast('Filter failed: ' + err.message, 'error'));
}

const orderStatusColors = { OPEN: 'bg-info', SUBMITTED: 'bg-primary', IN_KITCHEN: 'bg-warning text-dark', READY: 'bg-success', SERVED: 'bg-secondary', CLOSED: 'dark', VOIDED: 'bg-danger' };
const nextStatus = { OPEN: 'SUBMITTED', SUBMITTED: 'IN_KITCHEN', IN_KITCHEN: 'READY', READY: 'SERVED', SERVED: 'CLOSED' };

function renderTable(data) {
    const tbody = document.getElementById('orders-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-muted text-center">No orders found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(o => {
        const next = nextStatus[o.status];
        const canAdvance = next && o.status !== 'VOIDED' && o.status !== 'CLOSED';
        const canVoid = o.status !== 'VOIDED' && o.status !== 'CLOSED';
        return '<tr>' +
            '<td><strong>#' + esc(o.orderId.substring(0, 8)) + '</strong></td>' +
            '<td>' + esc(o.tableNumber || '-') + '</td>' +
            '<td>' + esc(o.guestName || '-') + '</td>' +
            '<td><span class="badge ' + (orderStatusColors[o.status] || 'bg-secondary') + '">' + (o.status || '-') + '</span></td>' +
            '<td>₹' + Number(o.totalAmount).toFixed(2) + '</td>' +
            '<td>' + (o.openedAt ? new Date(o.openedAt).toLocaleString() : '-') + '</td>' +
            '<td class="text-end">' +
                (canAdvance ? '<button class="btn btn-sm btn-outline-success me-1" onclick="advanceStatus(\'' + o.orderId + '\',\'' + next + '\')" title="' + next + '"><i class="bi bi-arrow-right"></i></button>' : '') +
                (canVoid ? '<button class="btn btn-sm btn-outline-danger" onclick="voidOrder(\'' + o.orderId + '\')" title="Void"><i class="bi bi-x-lg"></i></button>' : '') +
            '</td>' +
        '</tr>';
    }).join('');
}

function openCreateModal() {
    document.getElementById('order-items').innerHTML = '';
    document.getElementById('order-total').textContent = '0.00';

    Promise.all([
        Auth.authFetch('/api/v1/tables').then(r => r.json()),
        Auth.authFetch('/api/v1/guests').then(r => r.json()),
        Auth.authFetch('/api/v1/menu/items').then(r => r.json())
    ]).then(([tables, guests, items]) => {
        menuItemsList = items;
        document.getElementById('order-table').innerHTML = '<option value="">No table (takeaway)</option>' +
            tables.map(t => '<option value="' + t.tableId + '">' + esc(t.tableNumber) + ' (' + t.capacity + ' seats)</option>').join('');
        document.getElementById('order-guest').innerHTML = '<option value="">No guest</option>' +
            guests.map(g => '<option value="' + g.guestId + '">' + esc(g.firstName + ' ' + g.lastName) + '</option>').join('');

        const itemOpts = '<option value="">Select item...</option>' + items.filter(i => i.available !== false).map(i =>
            '<option value="' + i.itemId + '" data-price="' + i.basePrice + '">' + esc(i.name) + ' (₹' + Number(i.basePrice).toFixed(2) + ')</option>'
        ).join('');

        addOrderItemRow(itemOpts);
    });

    new bootstrap.Modal(document.getElementById('orderModal')).show();
}

function addOrderItemRow(opts) {
    if (!opts) {
        const firstRow = document.querySelector('.order-item-row .item-select');
        if (firstRow) opts = firstRow.innerHTML;
        else return;
    }
    const row = document.createElement('div');
    row.className = 'row g-2 mb-2 order-item-row';
    row.innerHTML =
        '<div class="col-md-5"><select class="form-select form-select-sm item-select">' + opts + '</select></div>' +
        '<div class="col-md-2"><input type="number" class="form-control form-control-sm item-qty" value="1" min="1"></div>' +
        '<div class="col-md-3"><input type="number" class="form-control form-control-sm item-price" placeholder="Price" step="0.01"></div>' +
        '<div class="col-md-2"><button class="btn btn-sm btn-outline-danger btn-remove-item"><i class="bi bi-x"></i></button></div>';

    row.querySelector('.item-select').addEventListener('change', function() {
        const price = this.options[this.selectedIndex].getAttribute('data-price');
        if (price) row.querySelector('.item-price').value = price;
        updateTotal();
    });

    document.getElementById('order-items').appendChild(row);
}

function updateTotal() {
    let total = 0;
    document.querySelectorAll('.order-item-row').forEach(row => {
        const qty = parseInt(row.querySelector('.item-qty').value) || 0;
        const price = parseFloat(row.querySelector('.item-price').value) || 0;
        total += qty * price;
    });
    document.getElementById('order-total').textContent = total.toFixed(2);
}

function saveOrder() {
    const items = [];
    document.querySelectorAll('.order-item-row').forEach(row => {
        const itemId = row.querySelector('.item-select').value;
        const qty = parseInt(row.querySelector('.item-qty').value) || 0;
        const price = parseFloat(row.querySelector('.item-price').value) || 0;
        if (itemId && qty > 0) {
            items.push({ menuItemId: itemId, quantity: qty, unitPrice: price });
        }
    });

    if (items.length === 0) { showToast('Add at least one item', 'warning'); return; }

    const body = {
        tableId: document.getElementById('order-table').value || null,
        guestId: document.getElementById('order-guest').value || null,
        serverUserId: Auth.getUser()?.userId || null,
        items: items
    };

    Auth.authFetch('/api/v1/orders', { method: 'POST', body: JSON.stringify(body) })
        .then(r => {
            if (!r.ok) {
                return r.json()
                    .then(d => { throw new Error(d.message || 'Create failed'); })
                    .catch(parseErr => {
                        if (parseErr.message && parseErr.message !== 'Create failed') throw parseErr;
                        throw new Error('Create failed (HTTP ' + r.status + ')');
                    });
            }
            return r.json();
        })
        .then(() => {
            showToast('Order created', 'success');
            bootstrap.Modal.getInstance(document.getElementById('orderModal')).hide();
            loadOrders();
        })
        .catch(err => showToast(err.message, 'error'));
}

function advanceStatus(id, status) {
    Auth.authFetch('/api/v1/orders/' + id + '/status?status=' + status, { method: 'PUT' })
        .then(r => {
            if (!r.ok) {
                return r.json()
                    .then(d => { throw new Error(d.message || 'Update failed'); })
                    .catch(parseErr => {
                        if (parseErr.message && parseErr.message !== 'Update failed') throw parseErr;
                        throw new Error('Update failed (HTTP ' + r.status + ')');
                    });
            }
            return r.json();
        })
        .then(() => { showToast('Status updated to ' + status, 'success'); loadOrders(); })
        .catch(err => showToast(err.message, 'error'));
}

function voidOrder(id) {
    if (!confirm('Void this order?')) return;
    Auth.authFetch('/api/v1/orders/' + id + '/void', { method: 'PUT' })
        .then(r => {
            if (!r.ok) {
                return r.json()
                    .then(d => { throw new Error(d.message || 'Void failed'); })
                    .catch(parseErr => {
                        if (parseErr.message && parseErr.message !== 'Void failed') throw parseErr;
                        throw new Error('Void failed (HTTP ' + r.status + ')');
                    });
            }
            showToast('Order voided', 'success');
            loadOrders();
        })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
