// LMRS Toast Notification Utility

function showToast(message, type) {
    type = type || 'info';
    const container = document.getElementById('toast-container');
    if (!container) return;

    const icons = { success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };
    const colors = { success: 'text-bg-success', error: 'text-bg-danger', warning: 'text-bg-warning', info: 'text-bg-primary' };

    const toast = document.createElement('div');
    toast.className = 'toast align-items-center border-0 ' + (colors[type] || colors.info);
    toast.setAttribute('role', 'alert');
    toast.innerHTML =
        '<div class="d-flex">' +
            '<div class="toast-body"><i class="bi ' + (icons[type] || icons.info) + ' me-2"></i>' + message + '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>' +
        '</div>';

    container.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast, { delay: 4000 });
    bsToast.show();
    toast.addEventListener('hidden.bs.toast', function() { toast.remove(); });
}
