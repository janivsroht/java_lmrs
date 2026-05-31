let charts = {};

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadOccupancy();
    loadChannels();
    loadHousekeeping();
    loadTopMenu();
    loadLowStock();

    document.getElementById('btn-rev').addEventListener('click', loadRevenue);
    document.getElementById('btn-rev-csv').addEventListener('click', downloadRevenueCsv);

    const today = new Date().toISOString().split('T')[0];
    const thirtyAgo = new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0];
    document.getElementById('rev-from').value = thirtyAgo;
    document.getElementById('rev-to').value = today;
    loadRevenue();
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

function loadRevenue() {
    const from = document.getElementById('rev-from').value;
    const to = document.getElementById('rev-to').value;
    if (!from || !to) return;
    Auth.authFetch('/api/v1/reports/revenue?from=' + from + '&to=' + to)
        .then(r => r.json())
        .then(data => {
            if (charts.rev) charts.rev.destroy();
            const ctx = document.getElementById('revChart').getContext('2d');
            charts.rev = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: data.map(d => d.date),
                    datasets: [{ label: 'Revenue (₹)', data: data.map(d => d.amount), borderColor: '#0d6efd', backgroundColor: 'rgba(13,110,253,0.1)', fill: true, tension: 0.3 }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        });
}

function loadOccupancy() {
    Auth.authFetch('/api/v1/reports/occupancy')
        .then(r => r.json())
        .then(data => {
            document.getElementById('occupancy-data').innerHTML =
                '<div class="text-center">' +
                    '<h2 class="text-primary">' + data.occupancyRate + '%</h2>' +
                    '<p class="mb-1">' + data.occupied + ' / ' + data.totalRooms + ' rooms occupied</p>' +
                    '<div class="mt-2">' +
                        Object.entries(data.statusBreakdown || {}).map(([k, v]) =>
                            '<span class="badge bg-secondary me-1">' + k + ': ' + v + '</span>'
                        ).join('') +
                    '</div>' +
                '</div>';
        });
}

function loadChannels() {
    Auth.authFetch('/api/v1/reports/bookings-by-channel')
        .then(r => r.json())
        .then(data => {
            if (charts.channel) charts.channel.destroy();
            const ctx = document.getElementById('channelChart').getContext('2d');
            const colors = ['#0d6efd', '#198754', '#dc3545', '#ffc107', '#0dcaf0', '#6f42c1', '#fd7e14'];
            charts.channel = new Chart(ctx, {
                type: 'pie',
                data: {
                    labels: data.map(d => d.label),
                    datasets: [{ data: data.map(d => d.count), backgroundColor: colors.slice(0, data.length) }]
                },
                options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
            });
        });
}

function loadHousekeeping() {
    Auth.authFetch('/api/v1/reports/housekeeping-summary')
        .then(r => r.json())
        .then(data => {
            document.getElementById('hk-data').innerHTML = data.map(d =>
                '<div class="d-flex justify-content-between mb-1"><span>' + esc(d.label) + '</span><strong>' + d.count + '</strong></div>'
            ).join('') || '<p class="text-muted">No data</p>';
        });
}

function loadTopMenu() {
    Auth.authFetch('/api/v1/reports/top-menu-items')
        .then(r => r.json())
        .then(data => {
            if (charts.menu) charts.menu.destroy();
            const ctx = document.getElementById('menuChart').getContext('2d');
            charts.menu = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.map(d => d.name),
                    datasets: [{ label: 'Ordered', data: data.map(d => d.totalOrdered), backgroundColor: '#198754' }]
                },
                options: { responsive: true, maintainAspectRatio: false, indexAxis: 'y', plugins: { legend: { display: false } } }
            });
        });
}

function downloadRevenueCsv() {
    const from = document.getElementById('rev-from').value;
    const to = document.getElementById('rev-to').value;
    if (!from || !to) { showToast('Select date range first', 'warning'); return; }
    Auth.authFetch('/api/v1/reports/export/revenue?from=' + from + '&to=' + to)
        .then(function(r) { if (!r.ok) throw new Error('Download failed'); return r.blob(); })
        .then(function(blob) {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'revenue-report-' + from + '-to-' + to + '.csv';
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        })
        .catch(function(err) { showToast(err.message, 'error'); });
}

function loadLowStock() {
    Auth.authFetch('/api/v1/reports/low-stock')
        .then(r => r.json())
        .then(data => {
            const tbody = document.getElementById('low-stock-body');
            if (data.length === 0) { tbody.innerHTML = '<tr><td colspan="4" class="text-success">All stocked.</td></tr>'; return; }
            tbody.innerHTML = data.map(i =>
                '<tr class="table-danger"><td>' + esc(i.name) + '</td><td>' + esc(i.unit) + '</td><td class="fw-bold">' + i.currentStock + '</td><td>' + i.reorderThreshold + '</td></tr>'
            ).join('');
        });
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
