/* ════════════════════════════════════════════════════════════════════
   PawCare – Veterinary / Medical Management  (vet.js)
   ════════════════════════════════════════════════════════════════════ */

const VET_API = '/api/medical-records';

/* ── Fetch helpers ──────────────────────────────────────────────── */
async function fetchRecords() {
  const res = await fetch(VET_API);
  return res.ok ? res.json() : [];
}

async function createRecord(data) {
  const res = await fetch(VET_API, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return res;
}

async function updateRecord(id, data) {
  const res = await fetch(`${VET_API}/${id}`, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return res;
}

async function deleteRecord(id) {
  return fetch(`${VET_API}/${id}`, { method: 'DELETE' });
}

/* ── DOM helper ─────────────────────────────────────────────────── */
function el(id) { return document.getElementById(id); }

/* ════════════════════════════════════════════════════════════════════
   SERVICE PAGE  (vet_service.html)
   ════════════════════════════════════════════════════════════════════ */
function initVetService() {
  const form = el('vet-record-form');
  if (!form) return;

  loadRecentRecords();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const ownerEmail = el('vet-owner-email').value.trim();
    const petName    = el('vet-pet-name').value.trim();
    const diagnosis  = el('vet-diagnosis').value.trim();
    const treatment  = el('vet-treatment').value.trim();
    const vetStaff   = el('vet-staff').value.trim();
    const vacDate    = el('vet-vac-date').value;
    const nextDue    = el('vet-next-due').value;
    const notes      = el('vet-notes').value.trim();

    /* Client-side validation */
    if (!ownerEmail || !petName || !diagnosis || !vetStaff) {
      showVetAlert('Please fill in all required fields (Owner Email, Pet Name, Diagnosis, Vet Staff).', true);
      return;
    }

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Saving…';

    const res = await createRecord({
      ownerEmail, petName: petName, diagnosis, treatment,
      vaccinationDate: vacDate || null, nextDueDate: nextDue || null,
      vetStaff, notes
    });

    btn.disabled = false;
    btn.textContent = 'Save Record';

    if (res.ok) {
      showVetAlert('✓ Medical record saved successfully!');
      form.reset();
      loadRecentRecords();
    } else {
      const msg = await res.text();
      showVetAlert(msg || 'Failed to save record.', true);
    }
  });
}

async function loadRecentRecords() {
  const tbody = el('recent-records-tbody');
  if (!tbody) return;

  const records = await fetchRecords();
  tbody.innerHTML = '';

  if (records.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:2rem;">No records found</td></tr>';
    return;
  }

  records.slice(0, 20).forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.petName || '—'}</td>
      <td>${r.ownerName || '—'}</td>
      <td>${r.diagnosis || '—'}</td>
      <td>${r.treatment || '—'}</td>
      <td>${r.vetStaff || '—'}</td>
      <td>${r.vaccinationDate || '—'}</td>
      <td>${r.recordDate ? r.recordDate.substring(0, 10) : '—'}</td>
    `;
    tbody.appendChild(tr);
  });
}

function showVetAlert(msg, isError = false) {
  const alert = el('vet-alert');
  if (!alert) return;
  alert.textContent = msg;
  alert.className = 'vet-alert visible' + (isError ? ' error' : '');
  setTimeout(() => alert.className = 'vet-alert', 4000);
}

/* ════════════════════════════════════════════════════════════════════
   ADMIN DASHBOARD  (vet_admin.html)
   ════════════════════════════════════════════════════════════════════ */
let allVetRecords = [];
let vetFilters = { search: '', vet: '' };

function initVetAdmin() {
  if (!el('vet-admin-tbody')) return;
  loadVetAdmin();

  el('vet-admin-search')?.addEventListener('input', (e) => {
    vetFilters.search = e.target.value.toLowerCase();
    renderVetAdminTable();
  });
  el('vet-filter-vet')?.addEventListener('change', (e) => {
    vetFilters.vet = e.target.value;
    renderVetAdminTable();
  });
  el('vet-clear-filters')?.addEventListener('click', () => {
    vetFilters = { search: '', vet: '' };
    if (el('vet-admin-search')) el('vet-admin-search').value = '';
    if (el('vet-filter-vet')) el('vet-filter-vet').value = '';
    renderVetAdminTable();
  });
}

async function loadVetAdmin() {
  allVetRecords = await fetchRecords();
  updateVetKPIs();
  populateVetFilter();
  renderVetAdminTable();
}

function updateVetKPIs() {
  const total = allVetRecords.length;
  const today = new Date().toISOString().substring(0, 7); // YYYY-MM
  const thisMonth = allVetRecords.filter(r => r.recordDate && r.recordDate.startsWith(today)).length;
  const upcomingVax = allVetRecords.filter(r => {
    if (!r.nextDueDate) return false;
    return new Date(r.nextDueDate) >= new Date();
  }).length;
  const withVax = allVetRecords.filter(r => r.vaccinationDate).length;

  if (el('kpi-total-val')) el('kpi-total-val').textContent = total;
  if (el('kpi-month-val')) el('kpi-month-val').textContent = thisMonth;
  if (el('kpi-upcoming-val')) el('kpi-upcoming-val').textContent = upcomingVax;
  if (el('kpi-vaccinated-val')) el('kpi-vaccinated-val').textContent = withVax;
}

function populateVetFilter() {
  const select = el('vet-filter-vet');
  if (!select) return;
  const vets = [...new Set(allVetRecords.map(r => r.vetStaff).filter(Boolean))];
  select.innerHTML = '<option value="">All Vets</option>';
  vets.forEach(v => {
    const opt = document.createElement('option');
    opt.value = v; opt.textContent = v;
    select.appendChild(opt);
  });
}

function renderVetAdminTable() {
  const tbody = el('vet-admin-tbody');
  if (!tbody) return;

  let filtered = allVetRecords;
  if (vetFilters.search) {
    filtered = filtered.filter(r =>
      (r.petName || '').toLowerCase().includes(vetFilters.search) ||
      (r.ownerName || '').toLowerCase().includes(vetFilters.search) ||
      (r.diagnosis || '').toLowerCase().includes(vetFilters.search)
    );
  }
  if (vetFilters.vet) {
    filtered = filtered.filter(r => r.vetStaff === vetFilters.vet);
  }

  tbody.innerHTML = '';
  const empty = el('vet-table-empty');

  if (filtered.length === 0) {
    if (empty) empty.classList.remove('hidden');
    return;
  }
  if (empty) empty.classList.add('hidden');

  filtered.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.id}</td>
      <td>${r.petName || '—'}</td>
      <td>${r.ownerName || '—'}</td>
      <td>${r.diagnosis || '—'}</td>
      <td>${r.treatment || '—'}</td>
      <td>${r.vetStaff || '—'}</td>
      <td>${r.vaccinationDate || '—'}</td>
      <td>${r.nextDueDate || '—'}</td>
      <td>${r.recordDate ? r.recordDate.substring(0, 10) : '—'}</td>
      <td>
        <button class="btn-icon-sm" onclick="deleteVetRecord(${r.id})" title="Delete">🗑️</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function deleteVetRecord(id) {
  if (!confirm('Delete this medical record?')) return;
  await deleteRecord(id);
  loadVetAdmin();
}

/* ── Auto-init ─────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  initVetService();
  initVetAdmin();
});
