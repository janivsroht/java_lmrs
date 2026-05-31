let guestsData = [];
let deleteTargetId = null;
let editMode = false;
let currentPage = 0;
const PAGE_SIZE = 20;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadGuests(0);

    document.getElementById('btn-add-guest').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-guest').addEventListener('click', saveGuest);
    document.getElementById('btn-search').addEventListener('click', searchGuests);
    document.getElementById('btn-clear').addEventListener('click', function() {
        document.getElementById('search-lastname').value = '';
        loadGuests(0);
    });
    document.getElementById('search-lastname').addEventListener('keydown', function(e) {
        if (e.key === 'Enter') searchGuests();
    });
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

function loadGuests(page) {
    page = page || 0;
    currentPage = page;
    Auth.authFetch('/api/v1/guests/page?page=' + page + '&size=' + PAGE_SIZE + '&sortBy=lastName&sortDir=asc')
        .then(r => r.json())
        .then(data => {
            guestsData = data.content;
            renderTable(data.content);
            renderPagination(data);
        })
        .catch(err => showToast('Failed to load guests: ' + err.message, 'error'));
}

function searchGuests() {
    const name = document.getElementById('search-lastname').value.trim();
    if (!name) { loadGuests(0); return; }
    Auth.authFetch('/api/v1/guests/search?lastName=' + encodeURIComponent(name))
        .then(r => r.json())
        .then(data => { guestsData = data; renderTable(data); document.getElementById('guests-pagination').innerHTML = ''; })
        .catch(err => showToast('Search failed: ' + err.message, 'error'));
}

function renderTable(data) {
    const tbody = document.getElementById('guests-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">No guests found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(g =>
        '<tr>' +
            '<td><strong>' + esc(g.firstName) + ' ' + esc(g.lastName) + '</strong></td>' +
            '<td>' + esc(g.email || '-') + '</td>' +
            '<td>' + esc(g.phone || '-') + '</td>' +
            '<td>' + esc(g.nationality || '-') + '</td>' +
            '<td><span class="badge bg-secondary">' + (g.loyaltyTier || 'BRONZE') + '</span></td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editGuest(\'' + g.guestId + '\')"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteGuest(\'' + g.guestId + '\')"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>'
    ).join('');
}

function renderPagination(pageData) {
    const container = document.getElementById('guests-pagination');
    if (!container) return;
    if (pageData.totalPages <= 1) { container.innerHTML = ''; return; }
    let html = '<nav><ul class="pagination pagination-sm mb-0">';
    html += '<li class="page-item' + (pageData.first ? ' disabled' : '') + '"><a class="page-link" href="#" onclick="loadGuests(' + (currentPage - 1) + '); return false;">Prev</a></li>';
    for (let i = 0; i < pageData.totalPages; i++) {
        html += '<li class="page-item' + (i === currentPage ? ' active' : '') + '"><a class="page-link" href="#" onclick="loadGuests(' + i + '); return false;">' + (i + 1) + '</a></li>';
    }
    html += '<li class="page-item' + (pageData.last ? ' disabled' : '') + '"><a class="page-link" href="#" onclick="loadGuests(' + (currentPage + 1) + '); return false;">Next</a></li>';
    html += '</ul></nav>';
    container.innerHTML = html;
}

function openCreateModal() {
    editMode = false;
    document.getElementById('guestModalTitle').textContent = 'Add Guest';
    document.getElementById('guest-id').value = '';
    document.getElementById('guest-firstname').value = '';
    document.getElementById('guest-lastname').value = '';
    document.getElementById('guest-email').value = '';
    document.getElementById('guest-phone').value = '';
    document.getElementById('guest-dob').value = '';
    document.getElementById('guest-nationality').value = '';
    document.getElementById('guest-idtype').value = '';
    document.getElementById('guest-idnumber').value = '';
    new bootstrap.Modal(document.getElementById('guestModal')).show();
}

function editGuest(id) {
    const guest = guestsData.find(g => g.guestId === id);
    if (!guest) return;
    editMode = true;
    document.getElementById('guestModalTitle').textContent = 'Edit Guest';
    document.getElementById('guest-id').value = guest.guestId;
    document.getElementById('guest-firstname').value = guest.firstName || '';
    document.getElementById('guest-lastname').value = guest.lastName || '';
    document.getElementById('guest-email').value = guest.email || '';
    document.getElementById('guest-phone').value = guest.phone || '';
    document.getElementById('guest-dob').value = guest.dob || '';
    document.getElementById('guest-nationality').value = guest.nationality || '';
    document.getElementById('guest-idtype').value = guest.idDocType || '';
    document.getElementById('guest-idnumber').value = guest.idDocNumber || '';
    new bootstrap.Modal(document.getElementById('guestModal')).show();
}

function saveGuest() {
    const firstName = document.getElementById('guest-firstname').value.trim();
    const lastName = document.getElementById('guest-lastname').value.trim();
    if (!firstName || !lastName) { showToast('First and Last name are required', 'warning'); return; }

    const body = {
        firstName: firstName,
        lastName: lastName,
        email: document.getElementById('guest-email').value.trim() || null,
        phone: document.getElementById('guest-phone').value.trim() || null,
        dob: document.getElementById('guest-dob').value || null,
        nationality: document.getElementById('guest-nationality').value.trim() || null,
        idDocType: document.getElementById('guest-idtype').value || null,
        idDocNumber: document.getElementById('guest-idnumber').value.trim() || null
    };

    const id = document.getElementById('guest-id').value;
    const url = id ? '/api/v1/guests/' + id : '/api/v1/guests';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => {
            if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); });
            return r.json();
        })
        .then(() => {
            showToast(editMode ? 'Guest updated successfully' : 'Guest created successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('guestModal')).hide();
            loadGuests(currentPage);
        })
        .catch(err => showToast(err.message, 'error'));
}

function deleteGuest(id) {
    deleteTargetId = id;
    new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

function confirmDelete() {
    if (!deleteTargetId) return;
    Auth.authFetch('/api/v1/guests/' + deleteTargetId, { method: 'DELETE' })
        .then(r => {
            if (!r.ok && r.status !== 204) throw new Error('Delete failed');
            showToast('Guest deleted successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
            loadGuests(currentPage);
        })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
