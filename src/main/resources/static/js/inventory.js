let invData = [];
let editMode = false;

let stockTargetId = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadInventory();

    document.getElementById('btn-add-item').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-item').addEventListener('click', saveItem);
    document.getElementById('btn-low-stock').addEventListener('click', loadLowStock);
    document.getElementById('btn-show-all').addEventListener('click', loadInventory);
    document.getElementById('btn-confirm-stock').addEventListener('click', confirmStockAdjustment);
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

function loadInventory() {
    document.getElementById('low-stock-badge').textContent = '';
    Auth.authFetch('/api/v1/inventory')
        .then(r => r.json())
        .then(data => { invData = data; renderTable(data); })
        .catch(err => showToast('Failed to load inventory: ' + err.message, 'error'));
}

function loadLowStock() {
    Auth.authFetch('/api/v1/inventory/low-stock')
        .then(r => r.json())
        .then(data => {
            document.getElementById('low-stock-badge').textContent = data.length + ' item(s) low';
            renderTable(data);
        })
        .catch(err => showToast('Failed to load low stock: ' + err.message, 'error'));
}

function renderTable(data) {
    const tbody = document.getElementById('inv-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-muted text-center">No items found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(i => {
        const isLow = parseFloat(i.currentStock) <= parseFloat(i.reorderThreshold);
        return '<tr' + (isLow ? ' class="table-danger"' : '') + '>' +
            '<td><strong>' + esc(i.name) + '</strong></td>' +
            '<td>' + esc(i.unit) + '</td>' +
            '<td>' + i.currentStock + '</td>' +
            '<td>' + i.reorderThreshold + '</td>' +
            '<td>₹' + Number(i.costPerUnit).toFixed(2) + '</td>' +
            '<td>' + (isLow ? '<span class="badge bg-danger">Low</span>' : '<span class="badge bg-success">OK</span>') + '</td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editItem(\'' + i.inventoryId + '\')" title="Edit"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-info me-1" onclick="openStockModal(\'' + i.inventoryId + '\')" title="Adjust Stock"><i class="bi bi-plus-minus"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteItem(\'' + i.inventoryId + '\')" title="Delete"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>';
    }).join('');
}

function openCreateModal() {
    editMode = false;
    document.getElementById('itemModalTitle').textContent = 'Add Item';
    document.getElementById('item-id').value = '';
    document.getElementById('item-name').value = '';
    document.getElementById('item-unit').value = 'KG';
    document.getElementById('item-stock').value = '';
    document.getElementById('item-threshold').value = '';
    document.getElementById('item-cost').value = '';
    new bootstrap.Modal(document.getElementById('itemModal')).show();
}

function editItem(id) {
    const item = invData.find(i => i.inventoryId === id);
    if (!item) return;
    editMode = true;
    document.getElementById('itemModalTitle').textContent = 'Edit Item';
    document.getElementById('item-id').value = item.inventoryId;
    document.getElementById('item-name').value = item.name || '';
    document.getElementById('item-unit').value = item.unit || 'KG';
    document.getElementById('item-stock').value = item.currentStock || '';
    document.getElementById('item-threshold').value = item.reorderThreshold || '';
    document.getElementById('item-cost').value = item.costPerUnit || '';
    new bootstrap.Modal(document.getElementById('itemModal')).show();
}

function saveItem() {
    const name = document.getElementById('item-name').value.trim();
    const stock = document.getElementById('item-stock').value;
    const threshold = document.getElementById('item-threshold').value;
    const cost = document.getElementById('item-cost').value;
    if (!name || !stock || !threshold || !cost) { showToast('All fields are required', 'warning'); return; }

    const body = {
        name: name,
        unit: document.getElementById('item-unit').value,
        currentStock: parseFloat(stock),
        reorderThreshold: parseFloat(threshold),
        costPerUnit: parseFloat(cost)
    };

    const id = document.getElementById('item-id').value;
    const url = id ? '/api/v1/inventory/' + id : '/api/v1/inventory';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); }); return r.json(); })
        .then(() => { showToast(editMode ? 'Item updated' : 'Item created', 'success'); bootstrap.Modal.getInstance(document.getElementById('itemModal')).hide(); loadInventory(); })
        .catch(err => showToast(err.message, 'error'));
}

function deleteItem(id) {
    if (!confirm('Delete this inventory item?')) return;
    Auth.authFetch('/api/v1/inventory/' + id, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Delete failed'); showToast('Item deleted', 'success'); loadInventory(); })
        .catch(err => showToast(err.message, 'error'));
}

function openStockModal(id) {
    stockTargetId = id;
    document.getElementById('stock-quantity').value = '';
    document.getElementById('stock-reason').value = '';
    const item = invData.find(i => i.inventoryId === id);
    document.getElementById('stock-current').textContent = item ? item.currentStock : '-';
    new bootstrap.Modal(document.getElementById('stockModal')).show();
}

function confirmStockAdjustment() {
    const quantity = parseInt(document.getElementById('stock-quantity').value);
    if (isNaN(quantity) || quantity === 0) { showToast('Enter a valid quantity (positive to add, negative to deduct)', 'warning'); return; }
    const reason = document.getElementById('stock-reason').value.trim();

    const btn = document.getElementById('btn-confirm-stock');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Adjusting...';

    Auth.authFetch('/api/v1/inventory/' + stockTargetId + '/stock', {
        method: 'PUT',
        body: JSON.stringify({ quantity: quantity, reason: reason || null })
    })
    .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Adjustment failed'); }); return r.json(); })
    .then(() => {
        showToast('Stock adjusted', 'success');
        bootstrap.Modal.getInstance(document.getElementById('stockModal')).hide();
        loadInventory();
    })
    .catch(err => showToast(err.message, 'error'))
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = 'Confirm';
    });
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
