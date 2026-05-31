let reservationsData = [];
let editMode = false;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadReservations();

    document.getElementById('btn-add-res').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-res').addEventListener('click', saveReservation);
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

function loadReservations() {
    Auth.authFetch('/api/v1/table-reservations')
        .then(r => r.json())
        .then(data => { reservationsData = data; renderTable(data); })
        .catch(err => showToast('Failed to load reservations: ' + err.message, 'error'));
}

const statusColors = { PENDING: 'bg-warning text-dark', CONFIRMED: 'bg-info', SEATED: 'bg-success', CANCELLED: 'bg-danger' };

function renderTable(data) {
    const tbody = document.getElementById('res-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">No reservations found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(r => {
        const canEdit = ['PENDING', 'CONFIRMED'].includes(r.status);
        return '<tr>' +
            '<td>' + esc(r.tableNumber || '-') + '</td>' +
            '<td>' + esc(r.guestName || 'Walk-in') + '</td>' +
            '<td>' + r.partySize + '</td>' +
            '<td>' + (r.reservationDt ? new Date(r.reservationDt).toLocaleString() : '-') + '</td>' +
            '<td><span class="badge ' + (statusColors[r.status] || 'bg-secondary') + '">' + (r.status || '-') + '</span></td>' +
            '<td class="text-end">' +
                (canEdit ? '<button class="btn btn-sm btn-outline-primary me-1" onclick="editReservation(\'' + r.tableResId + '\')" title="Edit"><i class="bi bi-pencil"></i></button>' : '') +
                (canEdit ? '<button class="btn btn-sm btn-outline-danger" onclick="cancelReservation(\'' + r.tableResId + '\')" title="Cancel"><i class="bi bi-x-lg"></i></button>' : '') +
            '</td>' +
        '</tr>';
    }).join('');
}

function openCreateModal() {
    editMode = false;
    document.getElementById('resModalTitle').textContent = 'New Reservation';
    document.getElementById('res-id').value = '';
    document.getElementById('res-party').value = '2';
    document.getElementById('res-datetime').value = '';
    document.getElementById('res-notes').value = '';

    Promise.all([
        Auth.authFetch('/api/v1/tables/available').then(r => r.json()),
        Auth.authFetch('/api/v1/guests').then(r => r.json())
    ]).then(([tables, guests]) => {
        document.getElementById('res-table').innerHTML = tables.map(t =>
            '<option value="' + t.tableId + '">' + esc(t.tableNumber) + ' (' + t.capacity + ' seats)</option>'
        ).join('');
        document.getElementById('res-guest').innerHTML = '<option value="">Walk-in</option>' +
            guests.map(g => '<option value="' + g.guestId + '">' + esc(g.firstName + ' ' + g.lastName) + '</option>').join('');
    });

    new bootstrap.Modal(document.getElementById('resModal')).show();
}

function editReservation(id) {
    const res = reservationsData.find(r => r.tableResId === id);
    if (!res) return;
    editMode = true;
    document.getElementById('resModalTitle').textContent = 'Edit Reservation';
    document.getElementById('res-id').value = res.tableResId;
    document.getElementById('res-party').value = res.partySize || 2;
    document.getElementById('res-notes').value = res.specialNotes || '';
    if (res.reservationDt) {
        const d = new Date(res.reservationDt);
        document.getElementById('res-datetime').value = d.toISOString().slice(0, 16);
    }

    Promise.all([
        Auth.authFetch('/api/v1/tables').then(r => r.json()),
        Auth.authFetch('/api/v1/guests').then(r => r.json())
    ]).then(([tables, guests]) => {
        document.getElementById('res-table').innerHTML = tables.map(t =>
            '<option value="' + t.tableId + '"' + (t.tableId === (res.tableId || '') ? ' selected' : '') + '>' + esc(t.tableNumber) + ' (' + t.capacity + ' seats)</option>'
        ).join('');
        document.getElementById('res-guest').innerHTML = '<option value="">Walk-in</option>' +
            guests.map(g => '<option value="' + g.guestId + '"' + (g.guestId === (res.guestId || '') ? ' selected' : '') + '>' + esc(g.firstName + ' ' + g.lastName) + '</option>').join('');
    });

    new bootstrap.Modal(document.getElementById('resModal')).show();
}

function saveReservation() {
    const tableId = document.getElementById('res-table').value;
    const dt = document.getElementById('res-datetime').value;
    if (!tableId || !dt) { showToast('Table and date/time are required', 'warning'); return; }

    const body = {
        tableId: tableId,
        guestId: document.getElementById('res-guest').value || null,
        partySize: parseInt(document.getElementById('res-party').value) || 2,
        reservationDt: new Date(dt).toISOString().replace(/\.\d{3}Z$/, ''),
        specialNotes: document.getElementById('res-notes').value.trim() || null
    };

    const id = document.getElementById('res-id').value;
    const url = id ? '/api/v1/table-reservations/' + id : '/api/v1/table-reservations';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); }); return r.json(); })
        .then(() => {
            showToast(editMode ? 'Reservation updated' : 'Reservation created', 'success');
            bootstrap.Modal.getInstance(document.getElementById('resModal')).hide();
            loadReservations();
        })
        .catch(err => showToast(err.message, 'error'));
}

function cancelReservation(id) {
    if (!confirm('Cancel this reservation?')) return;
    Auth.authFetch('/api/v1/table-reservations/' + id, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Cancel failed'); showToast('Reservation cancelled', 'success'); loadReservations(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
