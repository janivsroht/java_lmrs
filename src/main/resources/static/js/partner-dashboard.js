let successChart = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadPartnerList();

    document.getElementById('partner-selector').addEventListener('change', function () {
        const id = this.value;
        if (id) loadUsageDashboard(id);
    });
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

function loadPartnerList() {
    Auth.authFetch('/api/v1/admin/partners')
        .then(r => r.json())
        .then(partners => {
            const sel = document.getElementById('partner-selector');
            if (partners.length === 0) {
                sel.innerHTML = '<option value="">No partners found</option>';
                return;
            }
            sel.innerHTML = partners.map(p =>
                '<option value="' + p.partnerId + '">' + esc(p.name) + ' (' + esc(p.providerType) + ')</option>'
            ).join('');

            // Check URL for preselected partner
            const pathParts = window.location.pathname.split('/');
            const preselectedId = pathParts[pathParts.length - 1];
            if (preselectedId && preselectedId !== 'partner-dashboard') {
                sel.value = preselectedId;
            }
            loadUsageDashboard(sel.value);
        })
        .catch(err => showToast('Failed to load partners: ' + err.message, 'error'));
}

function loadUsageDashboard(partnerId) {
    Auth.authFetch('/api/v1/admin/partners/' + partnerId + '/usage')
        .then(r => r.json())
        .then(data => {
            updateKPIs(data);
            updatePartnerInfo(data);
            renderEndpointTable(data.endpointStats);
            renderSuccessChart(data);
        })
        .catch(err => showToast('Failed to load usage data: ' + err.message, 'error'));
}

function updateKPIs(data) {
    document.getElementById('kpi-total-calls').textContent = data.totalCalls.toLocaleString();
    document.getElementById('kpi-success').textContent = data.successCalls.toLocaleString();
    document.getElementById('kpi-errors').textContent = data.errorCalls.toLocaleString();
    document.getElementById('kpi-avg-ms').textContent = data.avgResponseMs.toFixed(0) + 'ms';
}

function updatePartnerInfo(data) {
    document.getElementById('partner-name').textContent = data.partnerName || '-';
    document.getElementById('partner-provider').textContent = data.providerType || '-';
    document.getElementById('partner-id').innerHTML = '<code>' + esc(data.partnerId) + '</code>';
    document.getElementById('partner-total').textContent = data.totalCalls.toLocaleString();
    const successRate = data.totalCalls > 0 ? ((data.successCalls / data.totalCalls) * 100).toFixed(1) : 0;
    document.getElementById('partner-success-rate').textContent = successRate + '%';
}

function renderEndpointTable(stats) {
    const tbody = document.getElementById('endpoint-body');
    if (!stats || stats.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-muted text-center">No usage data available yet.</td></tr>';
        return;
    }
    tbody.innerHTML = stats.map(s => {
        const health = s.avgResponseMs < 100
            ? '<span class="badge bg-success">Fast</span>'
            : s.avgResponseMs < 300
                ? '<span class="badge bg-warning text-dark">Moderate</span>'
                : '<span class="badge bg-danger">Slow</span>';
        return '<tr>' +
            '<td><code>' + esc(s.endpoint) + '</code></td>' +
            '<td>' + s.callCount.toLocaleString() + '</td>' +
            '<td>' + s.avgResponseMs.toFixed(0) + 'ms</td>' +
            '<td>' + health + '</td>' +
        '</tr>';
    }).join('');
}

function renderSuccessChart(data) {
    const ctx = document.getElementById('successChart').getContext('2d');
    if (successChart) successChart.destroy();

    const success = data.successCalls || 0;
    const errors = data.errorCalls || 0;

    if (success === 0 && errors === 0) {
        successChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['No Data'],
                datasets: [{ data: [1], backgroundColor: ['#e9ecef'] }]
            },
            options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
        });
        return;
    }

    successChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Success (' + success + ')', 'Errors (' + errors + ')'],
            datasets: [{
                data: [success, errors],
                backgroundColor: ['#198754', '#dc3545']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const pct = total > 0 ? ((context.raw / total) * 100).toFixed(1) : 0;
                            return context.label + ' - ' + pct + '%';
                        }
                    }
                }
            }
        }
    });
}
