let usersData = [];
let deleteTargetId = null;
let editMode = false;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadUsers();

    document.getElementById('btn-add-user').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-user').addEventListener('click', saveUser);
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

function loadUsers() {
    Auth.authFetch('/api/v1/users')
        .then(r => r.json())
        .then(data => { usersData = data; renderTable(data); })
        .catch(err => showToast('Failed to load users: ' + err.message, 'error'));
}

function renderTable(data) {
    const tbody = document.getElementById('users-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center">No users found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(u =>
        '<tr>' +
            '<td><strong>' + esc(u.email) + '</strong></td>' +
            '<td><span class="badge bg-primary">' + esc(u.role) + '</span></td>' +
            '<td><span class="badge ' + (u.active ? 'bg-success' : 'bg-secondary') + '">' + (u.active ? 'Active' : 'Inactive') + '</span></td>' +
            '<td>' + (u.lastLogin || 'Never') + '</td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(\'' + u.userId + '\')" title="Edit"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-warning me-1" onclick="toggleActive(\'' + u.userId + '\')" title="Toggle Active"><i class="bi bi-power"></i></button>' +
                '<button class="btn btn-sm btn-outline-info me-1" onclick="changeRole(\'' + u.userId + '\', \'' + esc(u.role) + '\')" title="Change Role"><i class="bi bi-person-gear"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteUser(\'' + u.userId + '\')" title="Delete"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>'
    ).join('');
}

function openCreateModal() {
    editMode = false;
    document.getElementById('userModalTitle').textContent = 'Add User';
    document.getElementById('user-id').value = '';
    document.getElementById('user-email').value = '';
    document.getElementById('user-password').value = '';
    document.getElementById('user-role').value = 'FRONT_DESK';
    document.getElementById('password-field-group').style.display = '';
    document.getElementById('user-email').disabled = false;
    new bootstrap.Modal(document.getElementById('userModal')).show();
}

function openEditModal(userId) {
    const user = usersData.find(u => u.userId === userId);
    if (!user) return;

    editMode = true;
    document.getElementById('userModalTitle').textContent = 'Edit User';
    document.getElementById('user-id').value = user.userId;
    document.getElementById('user-email').value = user.email;
    document.getElementById('user-role').value = user.role;
    document.getElementById('password-field-group').style.display = 'none';
    document.getElementById('user-email').disabled = false;
    new bootstrap.Modal(document.getElementById('userModal')).show();
}

function saveUser() {
    const userId = document.getElementById('user-id').value;
    const email = document.getElementById('user-email').value.trim();
    const password = document.getElementById('user-password').value;
    const role = document.getElementById('user-role').value;
    if (!email) { showToast('Email is required', 'warning'); return; }
    if (!editMode && !password) { showToast('Password is required', 'warning'); return; }

    if (editMode) {
        Auth.authFetch('/api/v1/users/' + userId, {
            method: 'PUT',
            body: JSON.stringify({ email: email, role: role })
        })
        .then(r => {
            if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Update failed'); });
            return r.json();
        })
        .then(() => {
            showToast('User updated successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
            loadUsers();
        })
        .catch(err => showToast(err.message, 'error'));
    } else {
        Auth.authFetch('/api/v1/users', {
            method: 'POST',
            body: JSON.stringify({ email: email, password: password, role: role })
        })
        .then(r => {
            if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); });
            return r.json();
        })
        .then(() => {
            showToast('User created successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
            loadUsers();
        })
        .catch(err => showToast(err.message, 'error'));
    }
}

function toggleActive(userId) {
    Auth.authFetch('/api/v1/users/' + userId + '/toggle-active', { method: 'PUT' })
        .then(r => {
            if (!r.ok) throw new Error('Toggle failed');
            return r.json();
        })
        .then(() => { showToast('User status updated', 'success'); loadUsers(); })
        .catch(err => showToast(err.message, 'error'));
}

function changeRole(userId, currentRole) {
    const roles = ['FRONT_DESK', 'MANAGER', 'PROPERTY_ADMIN', 'HOUSEKEEPER', 'SERVER', 'KITCHEN', 'FINANCE'];
    const newRole = prompt('Enter new role:\nAvailable: ' + roles.join(', '), currentRole);
    if (!newRole || newRole === currentRole) return;

    Auth.authFetch('/api/v1/users/' + userId + '/role?role=' + encodeURIComponent(newRole), { method: 'PUT' })
        .then(r => {
            if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Update failed'); });
            return r.json();
        })
        .then(() => { showToast('Role updated', 'success'); loadUsers(); })
        .catch(err => showToast(err.message, 'error'));
}

function deleteUser(userId) {
    deleteTargetId = userId;
    new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

function confirmDelete() {
    if (!deleteTargetId) return;
    Auth.authFetch('/api/v1/users/' + deleteTargetId, { method: 'DELETE' })
        .then(r => {
            if (!r.ok && r.status !== 204) throw new Error('Delete failed');
            showToast('User deleted successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
            loadUsers();
        })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
