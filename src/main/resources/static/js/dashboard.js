let charts = {};

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();

    Auth.authFetch('/api/v1/dashboard/summary')
        .then(r => r.json())
        .then(data => {
            updateKpis(data.kpis);
            renderRevenueChart(data.revenueTrend);
            renderReservationChart(data.reservationStatuses);
            renderChannelChart(data.bookingChannels);
            renderRoomStatusChart(data.roomStatuses);
            renderRoomTypeChart(data.roomsByType);
            renderMenuChart(data.topMenuItems);
            renderHKChart(data.housekeepingStatuses);
            renderLowStockTable(data.lowStockItems);
        })
        .catch(err => {
            document.querySelector('#page-content-wrapper').innerHTML =
                '<div class="alert alert-danger m-4">Failed to load dashboard: ' + err.message + '</div>';
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
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() { Auth.logout(); });
    }
}

function updateKpis(kpis) {
    document.getElementById('kpi-guests').textContent = kpis.totalGuests;
    document.getElementById('kpi-rooms').textContent = kpis.totalRooms;
    document.getElementById('kpi-active').textContent = kpis.activeReservations;
    document.getElementById('kpi-occupancy').textContent = kpis.occupancyRate.toFixed(1) + '%';
    document.getElementById('kpi-revenue').textContent = '₹' + Number(kpis.totalRevenue).toLocaleString();
    document.getElementById('kpi-hk').textContent = kpis.pendingHousekeeping;
}

function renderRevenueChart(data) {
    const ctx = document.getElementById('revenueChart').getContext('2d');
    if (charts.revenue) charts.revenue.destroy();
    charts.revenue = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.map(d => d.date),
            datasets: [{
                label: 'Revenue (₹)',
                data: data.map(d => d.amount),
                borderColor: '#0d6efd',
                backgroundColor: 'rgba(13,110,253,0.1)',
                fill: true,
                tension: 0.3
            }]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });
}

function renderReservationChart(data) {
    const ctx = document.getElementById('reservationChart').getContext('2d');
    if (charts.reservation) charts.reservation.destroy();
    const colors = ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#6c757d', '#0dcaf0'];
    charts.reservation = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.label),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: colors.slice(0, data.length)
            }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
}

function renderChannelChart(data) {
    const ctx = document.getElementById('channelChart').getContext('2d');
    if (charts.channel) charts.channel.destroy();
    const colors = ['#0d6efd', '#198754', '#dc3545', '#ffc107', '#0dcaf0', '#6f42c1', '#fd7e14'];
    charts.channel = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: data.map(d => d.label),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: colors.slice(0, data.length)
            }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
}

function renderRoomStatusChart(data) {
    const ctx = document.getElementById('roomStatusChart').getContext('2d');
    if (charts.roomStatus) charts.roomStatus.destroy();
    const colors = { AVAILABLE: '#198754', OCCUPIED: '#dc3545', RESERVED: '#ffc107', MAINTENANCE: '#6c757d', OUT_OF_ORDER: '#212529' };
    charts.roomStatus = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.label),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: data.map(d => colors[d.label] || '#0d6efd')
            }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
}

function renderRoomTypeChart(data) {
    const ctx = document.getElementById('roomTypeChart').getContext('2d');
    if (charts.roomType) charts.roomType.destroy();
    const colors = ['#0d6efd', '#198754', '#dc3545', '#ffc107', '#0dcaf0', '#6f42c1', '#fd7e14'];
    charts.roomType = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.label),
            datasets: [{
                label: 'Rooms',
                data: data.map(d => d.count),
                backgroundColor: colors.slice(0, data.length)
            }]
        },
        options: {
            responsive: true,
            indexAxis: 'y',
            plugins: { legend: { display: false } }
        }
    });
}

function renderMenuChart(data) {
    const ctx = document.getElementById('menuChart').getContext('2d');
    if (charts.menu) charts.menu.destroy();
    const top5 = data.slice(0, 8);
    const colors = ['#198754', '#0d6efd', '#ffc107', '#dc3545', '#0dcaf0', '#6f42c1', '#fd7e14', '#20c997'];
    charts.menu = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: top5.map(d => d.name),
            datasets: [{
                label: 'Times Ordered',
                data: top5.map(d => d.totalOrdered),
                backgroundColor: colors.slice(0, top5.length)
            }]
        },
        options: {
            responsive: true,
            indexAxis: 'y',
            plugins: { legend: { display: false } }
        }
    });
}

function renderHKChart(data) {
    const ctx = document.getElementById('hkChart').getContext('2d');
    if (charts.hk) charts.hk.destroy();
    const colors = { PENDING: '#ffc107', IN_PROGRESS: '#0d6efd', COMPLETED: '#198754' };
    charts.hk = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.label),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: data.map(d => colors[d.label] || '#6c757d')
            }]
        },
        options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
}

function renderLowStockTable(items) {
    const tbody = document.getElementById('low-stock-body');
    document.getElementById('low-stock-count').textContent = items.length;
    if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-success">All items are well-stocked.</td></tr>';
        return;
    }
    tbody.innerHTML = items.map(item =>
        `<tr>
            <td>${esc(item.name)}</td>
            <td>${esc(item.unit)}</td>
            <td class="text-danger fw-bold">${esc(String(item.currentStock))}</td>
            <td>${esc(String(item.reorderThreshold))}</td>
        </tr>`
    ).join('');
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
