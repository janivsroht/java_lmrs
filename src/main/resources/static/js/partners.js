let partnersData = [];

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadPartners();

    document.getElementById('btn-add-partner').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-partner').addEventListener('click', savePartner);
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

function loadPartners() {
    Auth.authFetch('/api/v1/admin/partners')
        .then(r => r.json())
        .then(data => {
            partnersData = data;
            renderTable(data);
            const active = data.filter(p => p.active).length;
            document.getElementById('active-count').textContent = active + '/' + data.length;
        })
        .catch(err => showToast('Failed to load partners: ' + err.message, 'error'));
}

function renderTable(data) {
    const tbody = document.getElementById('partner-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-muted text-center">No partner accounts found. Add one to enable third-party API access.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(p => {
        const apiKeyDisplay = p.apiKey ? p.apiKey.substring(0, 16) + '...' : '-';
        return '<tr>' +
            '<td><strong>' + esc(p.name) + '</strong></td>' +
            '<td><span class="badge bg-info text-dark">' + esc(p.providerType) + '</span></td>' +
            '<td><code>' + esc(apiKeyDisplay) + '</code>' +
                ' <button class="btn btn-sm btn-outline-secondary py-0 px-1" onclick="copyFullKey(\'' + esc(p.apiKey) + '\')" title="Copy full key"><i class="bi bi-clipboard"></i></button></td>' +
            '<td>' + (p.active
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-secondary">Inactive</span>') + '</td>' +
            '<td>' + (p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '-') + '</td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-info me-1" onclick="viewDashboard(\'' + p.partnerId + '\')" title="Usage Dashboard"><i class="bi bi-bar-chart"></i></button>' +
                '<button class="btn btn-sm ' + (p.active ? 'btn-outline-warning' : 'btn-outline-success') + ' me-1" onclick="toggleStatus(\'' + p.partnerId + '\', ' + p.active + ')" title="' + (p.active ? 'Deactivate' : 'Activate') + '">' +
                    (p.active ? '<i class="bi bi-pause-circle"></i>' : '<i class="bi bi-play-circle"></i>') +
                '</button>' +
            '</td>' +
        '</tr>';
    }).join('');
}

function openCreateModal() {
    document.getElementById('partnerModalLabel').textContent = 'Add Partner';
    document.getElementById('partner-name').value = '';
    document.getElementById('partner-type').value = 'CUSTOM';
    document.getElementById('partner-key-display').classList.add('d-none');
    document.getElementById('partner-api-key').value = '';
    document.getElementById('btn-save-partner').dataset.editId = '';
    new bootstrap.Modal(document.getElementById('partnerModal')).show();
}

function savePartner() {
    const name = document.getElementById('partner-name').value.trim();
    const providerType = document.getElementById('partner-type').value;
    if (!name) { showToast('Please enter a partner name', 'error'); return; }

    Auth.authFetch('/api/v1/admin/partners', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: name, providerType: providerType })
    })
    .then(r => {
        if (!r.ok) throw new Error('Failed to create partner');
        return r.json();
    })
    .then(partner => {
        bootstrap.Modal.getInstance(document.getElementById('partnerModal')).hide();
        showToast('Partner created successfully!', 'success');
        document.getElementById('partner-api-key').value = partner.apiKey;
        document.getElementById('partner-key-display').classList.remove('d-none');
        loadPartners();
    })
    .catch(err => showToast('Error: ' + err.message, 'error'));
}

function copyFullKey(key) {
    navigator.clipboard.writeText(key).then(() => {
        showToast('API key copied to clipboard', 'success');
    }).catch(() => {
        showToast('Failed to copy', 'error');
    });
}

function copyApiKey() {
    const keyInput = document.getElementById('partner-api-key');
    keyInput.select();
    navigator.clipboard.writeText(keyInput.value).then(() => {
        showToast('API key copied to clipboard', 'success');
    }).catch(() => {
        showToast('Failed to copy', 'error');
    });
}

function toggleStatus(partnerId, currentActive) {
    const newStatus = !currentActive;
    Auth.authFetch('/api/v1/admin/partners/' + partnerId + '/status', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ isActive: newStatus })
    })
    .then(r => {
        if (!r.ok) throw new Error('Failed to update status');
        showToast('Partner ' + (newStatus ? 'activated' : 'deactivated'), 'success');
        loadPartners();
    })
    .catch(err => showToast('Error: ' + err.message, 'error'));
}

function viewDashboard(partnerId) {
    window.location.href = '/dashboard/partners/' + partnerId;
}
