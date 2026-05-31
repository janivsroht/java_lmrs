document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();

    // Concierge
    document.getElementById('btn-concierge').addEventListener('click', function () {
        const query = document.getElementById('concierge-query').value.trim();
        if (!query) { alert('Please enter a query.'); return; }

        const btn = this;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Thinking...';

        Auth.authFetch('/api/v1/ai/concierge', {
            method: 'POST',
            body: JSON.stringify({
                query: query,
                guestId: document.getElementById('concierge-guest').value.trim() || null,
                reservationId: document.getElementById('concierge-reservation').value.trim() || null
            })
        })
        .then(r => r.json())
        .then(data => {
            document.getElementById('concierge-result').textContent = data.reply || 'No response.';
            document.getElementById('concierge-meta').textContent =
                'Model: ' + (data.model || 'N/A') + ' | Tokens: ' + (data.tokensUsed || 0);
        })
        .catch(err => {
            document.getElementById('concierge-result').textContent = 'Error: ' + err.message;
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-send me-1"></i>Get AI Response';
        });
    });

    // Feedback Analysis
    document.getElementById('btn-feedback').addEventListener('click', function () {
        const text = document.getElementById('feedback-text').value.trim();
        if (!text) { alert('Please enter feedback text.'); return; }

        const btn = this;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Analyzing...';

        Auth.authFetch('/api/v1/ai/feedback-analyze', {
            method: 'POST',
            body: JSON.stringify({
                feedbackText: text,
                guestName: document.getElementById('feedback-guest').value.trim() || null
            })
        })
        .then(r => r.json())
        .then(data => {
            const sentimentEl = document.getElementById('feedback-sentiment');
            const sentiment = (data.sentiment || 'NEUTRAL').toUpperCase();
            const badgeClass = sentiment === 'POSITIVE' ? 'bg-success' :
                               sentiment === 'NEGATIVE' ? 'bg-danger' : 'bg-warning text-dark';
            sentimentEl.innerHTML = '<span class="badge ' + badgeClass + ' sentiment-badge">' + sentiment + '</span>';

            document.getElementById('feedback-summary').textContent = data.summary || '-';
            document.getElementById('feedback-reply').textContent = data.suggestedReply || '-';
            document.getElementById('feedback-meta').textContent =
                'Model: ' + (data.model || 'N/A') + ' | Tokens: ' + (data.tokensUsed || 0);
        })
        .catch(err => {
            document.getElementById('feedback-sentiment').innerHTML = 'Error';
            document.getElementById('feedback-summary').textContent = err.message;
            document.getElementById('feedback-reply').textContent = '';
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-search me-1"></i>Analyze Feedback';
        });
    });

    // Menu Description
    document.getElementById('btn-menu').addEventListener('click', function () {
        const name = document.getElementById('menu-name').value.trim();
        if (!name) { alert('Please enter an item name.'); return; }

        const btn = this;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Generating...';

        const ingredients = document.getElementById('menu-ingredients').value.trim();
        const dietary = document.getElementById('menu-dietary').value.trim();

        Auth.authFetch('/api/v1/ai/menu-description', {
            method: 'POST',
            body: JSON.stringify({
                itemName: name,
                categoryName: document.getElementById('menu-category').value.trim() || '',
                ingredients: ingredients ? ingredients.split(',').map(s => s.trim()).filter(Boolean) : [],
                dietaryFlags: dietary ? dietary.split(',').map(s => s.trim()).filter(Boolean) : [],
                basePrice: parseFloat(document.getElementById('menu-price').value) || 0
            })
        })
        .then(r => r.json())
        .then(data => {
            document.getElementById('menu-result').textContent = data.description || 'No description generated.';
            document.getElementById('menu-meta').textContent =
                'Model: ' + (data.model || 'N/A') + ' | Tokens: ' + (data.tokensUsed || 0);
        })
        .catch(err => {
            document.getElementById('menu-result').textContent = 'Error: ' + err.message;
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-magic me-1"></i>Generate Description';
        });
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
