let tablesData = [];
let editMode = false;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadTables();

    document.getElementById('btn-add-table').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-table').addEventListener('click', saveTable);
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

function loadTables() {
    Auth.authFetch('/api/v1/tables')
        .then(r => r.json())
        .then(data => { tablesData = data; renderTable(data); })
        .catch(err => showToast('Failed to load tables: ' + err.message, 'error'));
}

const statusColors = {
    AVAILABLE: 'bg-success', OCCUPIED: 'bg-danger',
    RESERVED: 'bg-warning text-dark', MAINTENANCE: 'bg-secondary'
};

function renderTable(data) {
    const tbody = document.getElementById('tables-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center">No tables found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(t =>
        '<tr>' +
            '<td><strong>' + esc(t.tableNumber) + '</strong></td>' +
            '<td>' + esc(t.zone || '-') + '</td>' +
            '<td>' + t.capacity + '</td>' +
            '<td><span class="badge ' + (statusColors[t.status] || 'bg-secondary') + '">' + (t.status || '-') + '</span></td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editTable(\'' + t.tableId + '\')" title="Edit"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteTable(\'' + t.tableId + '\')" title="Delete"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>'
    ).join('');
}

function openCreateModal() {
    editMode = false;
    document.getElementById('tableModalTitle').textContent = 'Add Table';
    document.getElementById('table-id').value = '';
    document.getElementById('table-number').value = '';
    document.getElementById('table-zone').value = 'INDOOR';
    document.getElementById('table-capacity').value = '4';
    document.getElementById('table-status').value = 'AVAILABLE';
    new bootstrap.Modal(document.getElementById('tableModal')).show();
}

function editTable(id) {
    const table = tablesData.find(t => t.tableId === id);
    if (!table) return;
    editMode = true;
    document.getElementById('tableModalTitle').textContent = 'Edit Table';
    document.getElementById('table-id').value = table.tableId;
    document.getElementById('table-number').value = table.tableNumber || '';
    document.getElementById('table-zone').value = table.zone || 'INDOOR';
    document.getElementById('table-capacity').value = table.capacity || 4;
    document.getElementById('table-status').value = table.status || 'AVAILABLE';
    new bootstrap.Modal(document.getElementById('tableModal')).show();
}

function saveTable() {
    const number = document.getElementById('table-number').value.trim();
    if (!number) { showToast('Table number is required', 'warning'); return; }

    const body = {
        tableNumber: number,
        zone: document.getElementById('table-zone').value,
        capacity: parseInt(document.getElementById('table-capacity').value) || 4,
        status: document.getElementById('table-status').value
    };

    const id = document.getElementById('table-id').value;
    const url = id ? '/api/v1/tables/' + id : '/api/v1/tables';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) throw new Error('Save failed'); return r.json(); })
        .then(() => {
            showToast(editMode ? 'Table updated' : 'Table created', 'success');
            bootstrap.Modal.getInstance(document.getElementById('tableModal')).hide();
            loadTables();
        })
        .catch(err => showToast(err.message, 'error'));
}

function deleteTable(id) {
    if (!confirm('Delete this table?')) return;
    Auth.authFetch('/api/v1/tables/' + id, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Delete failed'); showToast('Table deleted', 'success'); loadTables(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
