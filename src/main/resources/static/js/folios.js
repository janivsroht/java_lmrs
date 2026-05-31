let currentFolioId = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();

    document.getElementById('btn-lookup').addEventListener('click', lookupFolio);
    document.getElementById('btn-post-charge').addEventListener('click', function() { new bootstrap.Modal(document.getElementById('chargeModal')).show(); });
    document.getElementById('btn-process-payment').addEventListener('click', function() { new bootstrap.Modal(document.getElementById('paymentModal')).show(); });
    document.getElementById('btn-save-charge').addEventListener('click', postCharge);
    document.getElementById('btn-save-payment').addEventListener('click', processPayment);
    document.getElementById('btn-close-folio').addEventListener('click', closeFolio);
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

function lookupFolio() {
    const resId = document.getElementById('lookup-res-id').value.trim();
    if (!resId) { showToast('Enter a reservation ID', 'warning'); return; }

    Auth.authFetch('/api/v1/folios/reservation/' + resId)
        .then(r => {
            if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Folio not found'); });
            return r.json();
        })
        .then(data => {
            currentFolioId = data.folioId;
            document.getElementById('folio-guest').textContent = data.guestName || '-';
            document.getElementById('folio-status').innerHTML = '<span class="badge ' + (data.status === 'OPEN' ? 'bg-success' : 'bg-secondary') + '">' + data.status + '</span>';
            document.getElementById('folio-total').textContent = Number(data.totalAmount).toFixed(2);
            document.getElementById('folio-currency').textContent = data.currency;

            document.getElementById('charges-body').innerHTML = (data.lineItems || []).map(li =>
                '<tr><td>' + esc(li.description) + '</td><td>' + esc(li.chargeType) + '</td><td>₹' + Number(li.amount).toFixed(2) + '</td><td>' + (li.postedAt || '-') + '</td></tr>'
            ).join('') || '<tr><td colspan="4" class="text-muted">No charges</td></tr>';

            document.getElementById('payments-body').innerHTML = (data.payments || []).map(p =>
                '<tr><td>' + esc(p.method) + '</td><td>₹' + Number(p.amount).toFixed(2) + '</td><td>' + esc(p.status) + '</td><td>' + (p.paidAt || '-') + '</td></tr>'
            ).join('') || '<tr><td colspan="4" class="text-muted">No payments</td></tr>';

            document.getElementById('folio-section').style.display = 'block';
        })
        .catch(err => showToast(err.message, 'error'));
}

function postCharge() {
    if (!currentFolioId) return;
    const desc = document.getElementById('charge-desc').value.trim();
    const amount = document.getElementById('charge-amount').value;
    if (!desc || !amount) { showToast('Description and amount required', 'warning'); return; }

    Auth.authFetch('/api/v1/folios/' + currentFolioId + '/charges', {
        method: 'POST',
        body: JSON.stringify({ description: desc, amount: parseFloat(amount), chargeType: document.getElementById('charge-type').value })
    })
    .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Failed'); }); return r.json(); })
    .then(() => { showToast('Charge posted', 'success'); bootstrap.Modal.getInstance(document.getElementById('chargeModal')).hide(); lookupFolio(); })
    .catch(err => showToast(err.message, 'error'));
}

function processPayment() {
    if (!currentFolioId) return;
    const amount = document.getElementById('pay-amount').value;
    if (!amount) { showToast('Amount required', 'warning'); return; }

    Auth.authFetch('/api/v1/payments/folio/' + currentFolioId, {
        method: 'POST',
        body: JSON.stringify({ amount: parseFloat(amount), method: document.getElementById('pay-method').value })
    })
    .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Failed'); }); return r.json(); })
    .then(() => { showToast('Payment processed', 'success'); bootstrap.Modal.getInstance(document.getElementById('paymentModal')).hide(); lookupFolio(); })
    .catch(err => showToast(err.message, 'error'));
}

function closeFolio() {
    if (!currentFolioId) return;
    if (!confirm('Close this folio?')) return;
    Auth.authFetch('/api/v1/folios/' + currentFolioId + '/close', { method: 'PUT' })
        .then(r => { if (!r.ok) throw new Error('Close failed'); showToast('Folio closed', 'success'); lookupFolio(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
