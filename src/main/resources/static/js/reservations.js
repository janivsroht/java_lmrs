let resData = [];
let guestsList = [];
let roomsList = [];
let cancelTargetId = null;
let editTargetId = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadReservations();

    document.getElementById('btn-add-res').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-res').addEventListener('click', saveReservation);
    document.getElementById('btn-confirm-cancel').addEventListener('click', confirmCancel);
    document.getElementById('search-form').addEventListener('submit', function(e) { e.preventDefault(); searchReservations(); });
    document.getElementById('btn-clear-search').addEventListener('click', clearSearch);
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
    Auth.authFetch('/api/v1/reservations')
        .then(r => r.json())
        .then(data => { resData = data; renderTable(data); })
        .catch(err => showToast('Failed to load reservations: ' + err.message, 'error'));
}

function searchReservations() {
    const params = new URLSearchParams();
    const guest = document.getElementById('search-guest').value.trim();
    const status = document.getElementById('search-status').value;
    const room = document.getElementById('search-room').value.trim();
    const from = document.getElementById('search-from').value;
    const to = document.getElementById('search-to').value;
    if (guest) params.set('guestName', guest);
    if (status) params.set('status', status);
    if (room) params.set('roomNumber', room);
    if (from) params.set('dateFrom', from);
    if (to) params.set('dateTo', to);
    Auth.authFetch('/api/v1/reservations?' + params.toString())
        .then(r => r.json())
        .then(data => { resData = data; renderTable(data); })
        .catch(err => showToast('Search failed: ' + err.message, 'error'));
}

function clearSearch() {
    document.getElementById('search-guest').value = '';
    document.getElementById('search-status').value = '';
    document.getElementById('search-room').value = '';
    document.getElementById('search-from').value = '';
    document.getElementById('search-to').value = '';
    loadReservations();
}

const statusColors = { PENDING: 'bg-warning text-dark', CONFIRMED: 'bg-info', CHECKED_IN: 'bg-success', CHECKED_OUT: 'bg-secondary', CANCELLED: 'bg-danger', NO_SHOW: 'bg-dark text-white' };

function renderTable(data) {
    const tbody = document.getElementById('res-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-muted text-center">No reservations found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(r => {
        const canCheckIn = r.status === 'CONFIRMED';
        const canCheckOut = r.status === 'CHECKED_IN';
        const canCancel = ['PENDING', 'CONFIRMED'].includes(r.status);
        const canEdit = ['PENDING', 'CONFIRMED'].includes(r.status);
        const canNoShow = ['PENDING', 'CONFIRMED'].includes(r.status);
        return '<tr>' +
            '<td>' + esc(r.guestName || '-') + '</td>' +
            '<td>' + esc(r.roomNumber || '-') + '</td>' +
            '<td>' + (r.checkInDate || '-') + '</td>' +
            '<td>' + (r.checkOutDate || '-') + '</td>' +
            '<td><span class="badge ' + (statusColors[r.status] || 'bg-secondary') + '">' + (r.status || '-') + '</span></td>' +
            '<td>' + esc(r.channel || '-') + '</td>' +
            '<td>₹' + Number(r.rateApplied).toFixed(2) + '</td>' +
            '<td class="text-end">' +
                (canEdit ? '<button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(\'' + r.reservationId + '\')" title="Edit"><i class="bi bi-pencil"></i></button>' : '') +
                (canCheckIn ? '<button class="btn btn-sm btn-outline-success me-1" onclick="doCheckIn(\'' + r.reservationId + '\')" title="Check In"><i class="bi bi-box-arrow-in-right"></i></button>' : '') +
                (canCheckOut ? '<button class="btn btn-sm btn-outline-info me-1" onclick="doCheckOut(\'' + r.reservationId + '\')" title="Check Out"><i class="bi bi-box-arrow-right"></i></button>' : '') +
                (canNoShow ? '<button class="btn btn-sm btn-outline-dark me-1" onclick="doNoShow(\'' + r.reservationId + '\')" title="No Show"><i class="bi bi-person-x"></i></button>' : '') +
                (canCancel ? '<button class="btn btn-sm btn-outline-danger" onclick="cancelReservation(\'' + r.reservationId + '\')" title="Cancel"><i class="bi bi-x-lg"></i></button>' : '') +
            '</td>' +
        '</tr>';
    }).join('');
}

function openCreateModal() {
    editTargetId = null;
    document.getElementById('resModalTitle').textContent = 'New Reservation';
    document.getElementById('res-guest').innerHTML = '<option value="">Loading...</option>';
    document.getElementById('res-room').innerHTML = '<option value="">Loading...</option>';
    document.getElementById('res-checkin').value = '';
    document.getElementById('res-checkout').value = '';
    document.getElementById('res-rate').value = '';
    document.getElementById('res-channel').value = 'DIRECT';
    document.getElementById('res-requests').value = '';

    Promise.all([
        Auth.authFetch('/api/v1/guests').then(r => r.json()),
        Auth.authFetch('/api/v1/rooms').then(r => r.json())
    ]).then(([guests, rooms]) => {
        guestsList = guests;
        roomsList = rooms;
        document.getElementById('res-guest').innerHTML = '<option value="">Select guest...</option>' +
            guests.map(g => '<option value="' + g.guestId + '">' + esc(g.firstName + ' ' + g.lastName) + '</option>').join('');
        document.getElementById('res-room').innerHTML = '<option value="">Select room...</option>' +
            rooms.map(r => '<option value="' + r.roomId + '" data-rate="' + r.baseRate + '">' + esc(r.roomNumber) + ' (' + esc(r.roomTypeName) + ' - ₹' + Number(r.baseRate).toFixed(2) + ')</option>').join('');

        document.getElementById('res-room').addEventListener('change', function() {
            const opt = this.options[this.selectedIndex];
            const rate = opt.getAttribute('data-rate');
            if (rate) document.getElementById('res-rate').value = rate;
        });

        document.getElementById('res-guest').disabled = false;
        document.getElementById('res-room').disabled = false;
    });

    new bootstrap.Modal(document.getElementById('resModal')).show();
}

function openEditModal(reservationId) {
    const r = resData.find(x => x.reservationId === reservationId);
    if (!r) return;

    editTargetId = reservationId;
    document.getElementById('resModalTitle').textContent = 'Edit Reservation';

    Promise.all([
        Auth.authFetch('/api/v1/guests').then(r => r.json()),
        Auth.authFetch('/api/v1/rooms').then(r => r.json())
    ]).then(([guests, rooms]) => {
        guestsList = guests;
        roomsList = rooms;

        document.getElementById('res-guest').innerHTML = '<option value="">Select guest...</option>' +
            guests.map(g => '<option value="' + g.guestId + '">' + esc(g.firstName + ' ' + g.lastName) + '</option>').join('');
        document.getElementById('res-room').innerHTML = '<option value="">Select room...</option>' +
            rooms.map(rm => '<option value="' + rm.roomId + '" data-rate="' + rm.baseRate + '">' + esc(rm.roomNumber) + ' (' + esc(rm.roomTypeName) + ' - ₹' + Number(rm.baseRate).toFixed(2) + ')</option>').join('');

        document.getElementById('res-guest').value = r.guestId || '';
        document.getElementById('res-room').value = r.roomId || '';
        document.getElementById('res-checkin').value = r.checkInDate || '';
        document.getElementById('res-checkout').value = r.checkOutDate || '';
        document.getElementById('res-rate').value = r.rateApplied || '';
        document.getElementById('res-channel').value = r.channel || 'DIRECT';
        document.getElementById('res-requests').value = r.specialRequests || '';

        document.getElementById('res-guest').disabled = true;
        document.getElementById('res-room').disabled = true;
    });

    new bootstrap.Modal(document.getElementById('resModal')).show();
}

function saveReservation() {
    const guestId = document.getElementById('res-guest').value;
    const roomId = document.getElementById('res-room').value;
    const checkIn = document.getElementById('res-checkin').value;
    const checkOut = document.getElementById('res-checkout').value;
    const rate = document.getElementById('res-rate').value;

    if (!guestId || !roomId || !checkIn || !checkOut || !rate) {
        showToast('All required fields must be filled', 'warning'); return;
    }

    const body = {
        guestId: guestId,
        roomId: roomId,
        checkInDate: checkIn,
        checkOutDate: checkOut,
        rateApplied: parseFloat(rate),
        channel: document.getElementById('res-channel').value,
        specialRequests: document.getElementById('res-requests').value.trim() || null
    };

    const isEdit = editTargetId !== null;
    const url = isEdit ? '/api/v1/reservations/' + editTargetId : '/api/v1/reservations';
    const method = isEdit ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
    .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || (isEdit ? 'Update failed' : 'Create failed')); }); return r.json(); })
    .then(() => {
        showToast(isEdit ? 'Reservation updated' : 'Reservation created', 'success');
        bootstrap.Modal.getInstance(document.getElementById('resModal')).hide();
        editTargetId = null;
        loadReservations();
    })
    .catch(err => showToast(err.message, 'error'));
}

function doCheckIn(id) {
    Auth.authFetch('/api/v1/reservations/' + id + '/check-in', { method: 'PUT' })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Check-in failed'); }); return r.json(); })
        .then(() => { showToast('Guest checked in', 'success'); loadReservations(); })
        .catch(err => showToast(err.message, 'error'));
}

function doCheckOut(id) {
    Auth.authFetch('/api/v1/reservations/' + id + '/check-out', { method: 'PUT' })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Check-out failed'); }); return r.json(); })
        .then(() => { showToast('Guest checked out', 'success'); loadReservations(); })
        .catch(err => showToast(err.message, 'error'));
}

function doNoShow(id) {
    if (!confirm('Mark this reservation as no-show?')) return;
    Auth.authFetch('/api/v1/reservations/' + id + '/no-show', { method: 'PUT' })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Failed to mark no-show'); }); showToast('Reservation marked as no-show', 'success'); loadReservations(); })
        .catch(err => showToast(err.message, 'error'));
}

function cancelReservation(id) { cancelTargetId = id; new bootstrap.Modal(document.getElementById('deleteModal')).show(); }

function confirmCancel() {
    if (!cancelTargetId) return;
    Auth.authFetch('/api/v1/reservations/' + cancelTargetId + '/cancel', { method: 'PUT' })
        .then(r => { if (!r.ok) throw new Error('Cancel failed'); showToast('Reservation cancelled', 'success'); bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide(); loadReservations(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
