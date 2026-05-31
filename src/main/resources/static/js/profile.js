document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadProfile();

    document.getElementById('password-form').addEventListener('submit', changePassword);
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

function loadProfile() {
    const user = Auth.getUser();
    if (user) {
        document.getElementById('profile-email').textContent = user.email || '-';
        document.getElementById('profile-role').textContent = user.role || '-';
    }
}

function changePassword(e) {
    e.preventDefault();

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showToast('Passwords do not match', 'error');
        return;
    }

    const btn = document.getElementById('btn-change-password');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Updating...';

    Auth.authFetch('/api/v1/profile/password', {
        method: 'PUT',
        body: JSON.stringify({ currentPassword, newPassword })
    })
    .then(function (resp) {
        if (!resp.ok) return resp.json().then(function (err) { throw new Error(err.message || 'Failed to update password'); });
        showToast('Password updated successfully', 'success');
        document.getElementById('password-form').reset();
    })
    .catch(function (err) {
        showToast(err.message, 'error');
    })
    .finally(function () {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Update Password';
    });
}
