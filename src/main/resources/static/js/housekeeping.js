let tasksData = [];
let roomsList = [];
let editMode = false;

document.addEventListener('DOMContentLoaded', function () {
    if (!Auth.requireAuth()) return;
    populateSidebar();
    document.getElementById('filter-date').value = new Date().toISOString().split('T')[0];
    loadTasks();

    document.getElementById('btn-add-task').addEventListener('click', openCreateModal);
    document.getElementById('btn-save-task').addEventListener('click', saveTask);
    document.getElementById('btn-filter').addEventListener('click', loadTasks);
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

function loadTasks() {
    const date = document.getElementById('filter-date').value;
    const url = date ? '/api/v1/housekeeping?date=' + date : '/api/v1/housekeeping';
    Auth.authFetch(url)
        .then(r => r.json())
        .then(data => { tasksData = data; renderTable(data); })
        .catch(err => showToast('Failed to load tasks: ' + err.message, 'error'));
}

const hkStatusColors = { PENDING: 'bg-warning text-dark', IN_PROGRESS: 'bg-info', COMPLETED: 'bg-success' };
const priorityColors = { LOW: 'bg-secondary', NORMAL: 'bg-primary', HIGH: 'bg-warning text-dark', URGENT: 'bg-danger' };

function renderTable(data) {
    const tbody = document.getElementById('tasks-body');
    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-muted text-center">No tasks found.</td></tr>';
        return;
    }
    tbody.innerHTML = data.map(t =>
        '<tr>' +
            '<td>' + esc(t.room ? t.room.roomNumber : '-') + '</td>' +
            '<td>' + esc(t.taskType || '-') + '</td>' +
            '<td>' + esc(t.assignedUserId || 'Unassigned') + '</td>' +
            '<td><span class="badge ' + (priorityColors[t.priority] || 'bg-secondary') + '">' + (t.priority || 'NORMAL') + '</span></td>' +
            '<td><span class="badge ' + (hkStatusColors[t.status] || 'bg-secondary') + '">' + (t.status || '-') + '</span></td>' +
            '<td>' + (t.scheduledDate || '-') + '</td>' +
            '<td class="text-end">' +
                (t.status !== 'COMPLETED' ? '<button class="btn btn-sm btn-outline-success me-1" onclick="completeTask(\'' + t.taskId + '\')" title="Complete"><i class="bi bi-check-lg"></i></button>' : '') +
                '<button class="btn btn-sm btn-outline-primary me-1" onclick="editTask(\'' + t.taskId + '\')"><i class="bi bi-pencil"></i></button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteTask(\'' + t.taskId + '\')"><i class="bi bi-trash"></i></button>' +
            '</td>' +
        '</tr>'
    ).join('');
}

function openCreateModal() {
    editMode = false;
    document.getElementById('taskModalTitle').textContent = 'New Task';
    document.getElementById('task-id').value = '';
    document.getElementById('task-assigned').value = '';
    document.getElementById('task-date').value = new Date().toISOString().split('T')[0];
    document.getElementById('task-status').value = 'PENDING';

    Auth.authFetch('/api/v1/rooms')
        .then(r => r.json())
        .then(rooms => {
            roomsList = rooms;
            document.getElementById('task-room').innerHTML = '<option value="">Select room...</option>' +
                rooms.map(r => '<option value="' + r.roomId + '">' + esc(r.roomNumber) + '</option>').join('');
        });

    new bootstrap.Modal(document.getElementById('taskModal')).show();
}

function editTask(id) {
    const task = tasksData.find(t => t.taskId === id);
    if (!task) return;
    editMode = true;
    document.getElementById('taskModalTitle').textContent = 'Edit Task';
    document.getElementById('task-id').value = task.taskId;
    document.getElementById('task-type').value = task.taskType || 'CLEAN';
    document.getElementById('task-assigned').value = task.assignedUserId || '';
    document.getElementById('task-priority').value = task.priority || 'NORMAL';
    document.getElementById('task-status').value = task.status || 'PENDING';
    document.getElementById('task-date').value = task.scheduledDate || '';

    Auth.authFetch('/api/v1/rooms')
        .then(r => r.json())
        .then(rooms => {
            document.getElementById('task-room').innerHTML = '<option value="">Select room...</option>' +
                rooms.map(r => '<option value="' + r.roomId + '"' + (r.roomId === (task.room ? task.room.roomId : '') ? ' selected' : '') + '>' + esc(r.roomNumber) + '</option>').join('');
        });

    new bootstrap.Modal(document.getElementById('taskModal')).show();
}

function saveTask() {
    const roomId = document.getElementById('task-room').value;
    const scheduledDate = document.getElementById('task-date').value;
    if (!roomId || !scheduledDate) { showToast('Room and date are required', 'warning'); return; }

    const body = {
        roomId: roomId,
        taskType: document.getElementById('task-type').value,
        assignedUserId: document.getElementById('task-assigned').value.trim() || null,
        priority: document.getElementById('task-priority').value,
        status: document.getElementById('task-status').value,
        scheduledDate: scheduledDate
    };

    const id = document.getElementById('task-id').value;
    const url = id ? '/api/v1/housekeeping/' + id : '/api/v1/housekeeping';
    const method = id ? 'PUT' : 'POST';

    Auth.authFetch(url, { method: method, body: JSON.stringify(body) })
        .then(r => { if (!r.ok) return r.json().then(d => { throw new Error(d.message || 'Save failed'); }); return r.json(); })
        .then(() => { showToast(editMode ? 'Task updated' : 'Task created', 'success'); bootstrap.Modal.getInstance(document.getElementById('taskModal')).hide(); loadTasks(); })
        .catch(err => showToast(err.message, 'error'));
}

function completeTask(id) {
    Auth.authFetch('/api/v1/housekeeping/' + id, {
        method: 'PUT',
        body: JSON.stringify({ status: 'COMPLETED' })
    })
    .then(r => { if (!r.ok) throw new Error('Update failed'); return r.json(); })
    .then(() => { showToast('Task completed', 'success'); loadTasks(); })
    .catch(err => showToast(err.message, 'error'));
}

function deleteTask(id) {
    if (!confirm('Delete this task?')) return;
    Auth.authFetch('/api/v1/housekeeping/' + id, { method: 'DELETE' })
        .then(r => { if (!r.ok && r.status !== 204) throw new Error('Delete failed'); showToast('Task deleted', 'success'); loadTasks(); })
        .catch(err => showToast(err.message, 'error'));
}

function esc(s) { const d = document.createElement('div'); d.textContent = s; return d.innerHTML; }
