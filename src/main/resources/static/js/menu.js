let categories = [];
let menuItems = [];
let editCatMode = false;
let editItemMode = false;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    loadCategories();
    loadMenuItems();

    document.getElementById('btn-add-cat').addEventListener('click', openCatModal);
    document.getElementById('btn-save-cat').addEventListener('click', saveCategory);
    document.getElementById('btn-add-item').addEventListener('click', openItemModal);
    document.getElementById('btn-save-item').addEventListener('click', saveItem);
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

function loadCategories() {
    Auth.authFetch('/api/v1/menu/categories')
        .then(r => r.json())
        .then(data => { categories = data; renderCategories(data); })
        .catch(err => showToast('Failed to load categories: ' + err.message, 'error'));
}

function renderCategories(data) {
    const list = document.getElementById('cat-list');
    if (data.length === 0) {
        list.innerHTML = '<div class="list-group-item text-muted text-center">No categories</div>';
        return;
    }
    list.innerHTML = data.map(c =>
        '<div class="list-group-item d-flex justify-content-between align-items-center">' +
            '<div><strong>' + esc(c.name) + '</strong><br><small class="text-muted">Order: ' + c.displayOrder + '</small></div>' +
            '<div>' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editCategory(\'' + c.categoryId + '\')"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteCategory(\'' + c.categoryId + '\')"><i class="bi bi-trash"></i></button>' +
            '</div>' +
        '</div>'
    ).join('');
}

function openCatModal() {
    editCatMode = false;
    document.getElementById('catModalTitle').textContent = 'Add Category';
    document.getElementById('cat-id').value = '';
    document.getElementById('cat-name').value = '';
    document.getElementById('cat-order').value = '0';
    new bootstrap.Modal(document.getElementById('catModal')).show();
}

function editCategory(id) {
    const cat = categories.find(c => c.categoryId === id);
    if (!cat) return;
    editCatMode = true;
    document.getElementById('catModalTitle').textContent = 'Edit Category';
    document.getElementById('cat-id').value = cat.categoryId;
    document.getElementById('cat-name').value = cat.name || '';
    document.getElementById('cat-order').value = cat.displayOrder || 0;
    new bootstrap.Modal(document.getElementById('catModal')).show();
}

function saveCategory() {
    const name = document.getElementById('cat-name').value.trim();
    if (!name) { showToast('Name is required', 'warning'); return; }
    const body = { name: name, displayOrder: parseInt(document.getElementById('cat-order').value) || 0, active: true };
    const id = document.getElementById('cat-id').value;
    const url = id ? '/api/v1/menu/categories/' + id : '/api/v1/menu/categories';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) throw new Error('Save failed'); return r.json(); })
        .then(() => { showToast(editCatMode ? 'Category updated' : 'Category created', 'success'); bootstrap.Modal.getInstance(document.getElementById('catModal')).hide(); loadCategories(); loadMenuItems(); })
        .catch(err => showToast(err.message, 'error'));
}

function deleteCategory(id) {
    if (!confirm('Delete this category?')) return;
    Auth.authFetch('/api/v1/menu/categories/' + id, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Delete failed'); showToast('Category deleted', 'success'); loadCategories(); loadMenuItems(); })
        .catch(err => showToast(err.message, 'error'));
}

function loadMenuItems() {
    Auth.authFetch('/api/v1/menu/items')
        .then(r => r.json())
        .then(data => { menuItems = data; renderItems(data); })
        .catch(err => showToast('Failed to load menu items: ' + err.message, 'error'));
}

function renderItems(data) {
    const tbody = document.getElementById('items-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-muted text-center">No menu items.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(i =>
        '<tr>' +
            '<td><strong>' + esc(i.name) + '</strong>' + (i.description ? '<br><small class="text-muted">' + esc(i.description.substring(0, 60)) + '</small>' : '') + '</td>' +
            '<td>' + esc(i.categoryName || '-') + '</td>' +
            '<td>₹' + Number(i.basePrice).toFixed(2) + '</td>' +
            '<td>' + (i.isAvailable !== false ? '<span class="badge bg-success">Yes</span>' : '<span class="badge bg-danger">No</span>') + '</td>' +
            '<td class="text-end">' +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editItem(\'' + i.itemId + '\')"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteItem(\'' + i.itemId + '\')"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>'
    ).join('');
}

function openItemModal() {
    editItemMode = false;
    document.getElementById('itemModalTitle').textContent = 'Add Menu Item';
    document.getElementById('item-id').value = '';
    document.getElementById('item-name').value = '';
    document.getElementById('item-price').value = '';
    document.getElementById('item-desc').value = '';
    document.getElementById('item-allergens').value = '';
    document.getElementById('item-dietary').value = '';
    document.getElementById('item-available').value = 'true';
    populateCategoryDropdown();
    new bootstrap.Modal(document.getElementById('itemModal')).show();
}

function editItem(id) {
    const item = menuItems.find(i => i.itemId === id);
    if (!item) return;
    editItemMode = true;
    document.getElementById('itemModalTitle').textContent = 'Edit Menu Item';
    document.getElementById('item-id').value = item.itemId;
    document.getElementById('item-name').value = item.name || '';
    document.getElementById('item-price').value = item.basePrice || '';
    document.getElementById('item-desc').value = item.description || '';
    document.getElementById('item-allergens').value = (item.allergens || []).join(', ');
    document.getElementById('item-dietary').value = (item.dietaryFlags || []).join(', ');
    document.getElementById('item-available').value = item.isAvailable !== false ? 'true' : 'false';
    populateCategoryDropdown(item.categoryId);
    new bootstrap.Modal(document.getElementById('itemModal')).show();
}

function populateCategoryDropdown(selectedId) {
    const sel = document.getElementById('item-category');
    sel.innerHTML = '<option value="">Select...</option>' +
        categories.map(c => '<option value="' + c.categoryId + '"' + (c.categoryId === selectedId ? ' selected' : '') + '>' + esc(c.name) + '</option>').join('');
}

function saveItem() {
    const name = document.getElementById('item-name').value.trim();
    const catId = document.getElementById('item-category').value;
    const price = document.getElementById('item-price').value;
    if (!name || !catId || !price) { showToast('Name, category, and price are required', 'warning'); return; }

    const allergens = document.getElementById('item-allergens').value.trim();
    const dietary = document.getElementById('item-dietary').value.trim();

    const availStr = document.getElementById('item-available').value;
    const body = {
        categoryId: catId,
        name: name,
        description: document.getElementById('item-desc').value.trim() || null,
        basePrice: parseFloat(price),
        allergens: allergens ? allergens.split(',').map(s => s.trim()).filter(Boolean) : [],
        dietaryFlags: dietary ? dietary.split(',').map(s => s.trim()).filter(Boolean) : [],
        isAvailable: availStr === 'true'
    };

    const id = document.getElementById('item-id').value;
    const url = id ? '/api/v1/menu/items/' + id : '/api/v1/menu/items';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); }); return r.json(); })
        .then(() => { showToast(editItemMode ? 'Item updated' : 'Item created', 'success'); bootstrap.Modal.getInstance(document.getElementById('itemModal')).hide(); loadMenuItems(); })
        .catch(err => showToast(err.message, 'error'));
}

function deleteItem(id) {
    if (!confirm('Delete this menu item?')) return;
    Auth.authFetch('/api/v1/menu/items/' + id, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Delete failed'); showToast('Item deleted', 'success'); loadMenuItems(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
