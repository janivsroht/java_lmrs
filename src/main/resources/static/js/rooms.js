let roomsData = [];
let roomTypes = [];
let deleteTargetId = null;
let editMode = false;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadRoomTypes();
    loadRooms();

    document.getElementById('btn-add-room').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-room').addEventListener('click', saveRoom);
    document.getElementById('btn-check-avail').addEventListener('click', checkAvailability);
    document.getElementById('btn-clear-avail').addEventListener('click', loadRooms);
    document.getElementById('btn-confirm-delete').addEventListener('click', confirmDelete);
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

function loadRoomTypes() {
    Auth.authFetch('/api/v1/rooms/types')
        .then(r => r.json())
        .then(data => {
            roomTypes = data;
            const sel = document.getElementById('room-type');
            sel.innerHTML = '<option value="">Select type...</option>' +
                data.map(t => '<option value="' + t.roomTypeId + '">' + esc(t.name) + '</option>').join('');
        })
        .catch(() => {});
}

function loadRooms() {
    document.getElementById('avail-count').textContent = '';
    Auth.authFetch('/api/v1/rooms')
        .then(r => r.json())
        .then(data => { roomsData = data; renderTable(data); })
        .catch(err => showToast('Failed to load rooms: ' + err.message, 'error'));
}

function checkAvailability() {
    const ci = document.getElementById('avail-checkin').value;
    const co = document.getElementById('avail-checkout').value;
    if (!ci || !co) { showToast('Select check-in and check-out dates', 'warning'); return; }
    Auth.authFetch('/api/v1/rooms/availability?checkIn=' + ci + '&checkOut=' + co)
        .then(r => r.json())
        .then(data => {
            document.getElementById('avail-count').textContent = data.length + ' room(s) available';
            renderTable(data.map(r => ({
                roomId: r.roomId, roomNumber: r.roomNumber, roomTypeName: r.roomTypeName,
                floor: r.floor, status: 'AVAILABLE', housekeepingStatus: '-', baseRate: r.baseRate
            })));
        })
        .catch(err => showToast('Availability check failed: ' + err.message, 'error'));
}

const statusColors = { AVAILABLE: 'bg-success', OCCUPIED: 'bg-danger', RESERVED: 'bg-warning text-dark', MAINTENANCE: 'bg-secondary' };
const hkColors = { CLEAN: 'bg-success', DIRTY: 'bg-danger', IN_PROGRESS: 'bg-info', INSPECTED: 'bg-primary' };

function renderTable(data) {
    const tbody = document.getElementById('rooms-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-muted text-center">No rooms found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(r =>
        '<tr>' +
            '<td><strong>' + esc(r.roomNumber) + '</strong></td>' +
            '<td>' + esc(r.roomTypeName || '-') + '</td>' +
            '<td>' + (r.floor != null ? r.floor : '-') + '</td>' +
            '<td><span class="badge ' + (statusColors[r.status] || 'bg-secondary') + '">' + (r.status || '-') + '</span></td>' +
            '<td><span class="badge ' + (hkColors[r.housekeepingStatus] || 'bg-secondary') + '">' + (r.housekeepingStatus || '-') + '</span></td>' +
            '<td>₹' + Number(r.baseRate).toFixed(2) + '</td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editRoom(\'' + r.roomId + '\')"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteRoom(\'' + r.roomId + '\')"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>'
    ).join('');
}

function openCreateModal() {
    editMode = false;
    document.getElementById('roomModalTitle').textContent = 'Add Room';
    document.getElementById('room-id').value = '';
    document.getElementById('room-number').value = '';
    document.getElementById('room-type').value = '';
    document.getElementById('room-floor').value = '';
    document.getElementById('room-rate').value = '';
    new bootstrap.Modal(document.getElementById('roomModal')).show();
}

function editRoom(id) {
    const room = roomsData.find(r => r.roomId === id);
    if (!room) return;
    editMode = true;
    document.getElementById('roomModalTitle').textContent = 'Edit Room';
    document.getElementById('room-id').value = room.roomId;
    document.getElementById('room-number').value = room.roomNumber || '';
    document.getElementById('room-type').value = room.roomTypeId || '';
    document.getElementById('room-floor').value = room.floor || '';
    document.getElementById('room-rate').value = room.baseRate || '';
    new bootstrap.Modal(document.getElementById('roomModal')).show();
}

function saveRoom() {
    const number = document.getElementById('room-number').value.trim();
    const typeId = document.getElementById('room-type').value;
    const rate = document.getElementById('room-rate').value;
    if (!number || !typeId || !rate) { showToast('Room number, type, and rate are required', 'warning'); return; }

    const body = {
        roomNumber: number,
        roomTypeId: typeId,
        floor: document.getElementById('room-floor').value ? parseInt(document.getElementById('room-floor').value) : null,
        baseRate: parseFloat(rate)
    };

    const id = document.getElementById('room-id').value;
    const url = id ? '/api/v1/rooms/' + id : '/api/v1/rooms';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); }); return r.json(); })
        .then(() => {
            showToast(editMode ? 'Room updated' : 'Room created', 'success');
            bootstrap.Modal.getInstance(document.getElementById('roomModal')).hide();
            loadRooms();
        })
        .catch(err => showToast(err.message, 'error'));
}

function deleteRoom(id) { deleteTargetId = id; new bootstrap.Modal(document.getElementById('deleteModal')).show(); }

function confirmDelete() {
    if (!deleteTargetId) return;
    Auth.authFetch('/api/v1/rooms/' + deleteTargetId, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Delete failed'); showToast('Room deleted', 'success'); bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide(); loadRooms(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
