/* ════════════════════════════════════════════════════════════════════
   PawCare – Grooming Management  (grooming.js)
   ════════════════════════════════════════════════════════════════════ */
const GROOM_API = '/api/grooming-logs';
function el(id) { return document.getElementById(id); }

async function fetchGroomLogs() { const r = await fetch(GROOM_API); return r.ok ? r.json() : []; }
async function createGroomLog(d) { return fetch(GROOM_API, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }
async function deleteGroomLog(id) { return fetch(`${GROOM_API}/${id}`, { method:'DELETE' }); }

/* ── SERVICE PAGE ──────────────────────────────────────────────── */
function initGroomService() {
  const form = el('groom-form');
  if (!form) return;
  loadRecentGroomLogs();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const ownerEmail = el('groom-owner-email').value.trim();
    const petName = el('groom-pet-name').value.trim();
    const serviceType = el('groom-service-type').value;
    const productsUsed = el('groom-products').value.trim();
    const coatCondition = el('groom-coat').value;
    const skinNotes = el('groom-skin').value.trim();
    const groomer = el('groom-groomer').value.trim();
    const bookingId = el('groom-booking-id').value.trim();

    if (!ownerEmail || !petName || !serviceType || !groomer) {
      showGroomAlert('Please fill in all required fields.', true); return;
    }

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true; btn.textContent = 'Saving…';

    const res = await createGroomLog({ ownerEmail, petName, serviceType, productsUsed, coatCondition, skinNotes, groomer, bookingId: bookingId || null });
    btn.disabled = false; btn.textContent = 'Save Log';

    if (res.ok) { showGroomAlert('✓ Grooming log saved!'); form.reset(); loadRecentGroomLogs(); }
    else { const msg = await res.text(); showGroomAlert(msg || 'Failed to save.', true); }
  });
}

async function loadRecentGroomLogs() {
  const tbody = el('recent-groom-tbody');
  if (!tbody) return;
  const logs = await fetchGroomLogs();
  tbody.innerHTML = '';
  if (logs.length === 0) { tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:2rem;">No logs found</td></tr>'; return; }
  logs.slice(0,20).forEach(g => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${g.petName||'—'}</td><td>${g.ownerName||'—'}</td><td>${g.serviceType||'—'}</td><td>${g.productsUsed||'—'}</td><td>${g.coatCondition||'—'}</td><td>${g.groomer||'—'}</td><td>${g.sessionDate?g.sessionDate.substring(0,10):'—'}</td>`;
    tbody.appendChild(tr);
  });
}

function showGroomAlert(msg, isError=false) {
  const a = el('groom-alert'); if(!a) return;
  a.textContent = msg; a.className = 'vet-alert visible' + (isError?' error':'');
  setTimeout(() => a.className = 'vet-alert', 4000);
}

/* ── ADMIN DASHBOARD ───────────────────────────────────────────── */
let allGroomLogs = [];
let groomFilters = { search:'', groomer:'' };

function initGroomAdmin() {
  if (!el('groom-admin-tbody')) return;
  loadGroomAdmin();
  el('groom-admin-search')?.addEventListener('input', e => { groomFilters.search = e.target.value.toLowerCase(); renderGroomTable(); });
  el('groom-filter-groomer')?.addEventListener('change', e => { groomFilters.groomer = e.target.value; renderGroomTable(); });
  el('groom-clear-filters')?.addEventListener('click', () => { groomFilters={search:'',groomer:''}; if(el('groom-admin-search'))el('groom-admin-search').value=''; if(el('groom-filter-groomer'))el('groom-filter-groomer').value=''; renderGroomTable(); });
}

async function loadGroomAdmin() {
  allGroomLogs = await fetchGroomLogs();
  const total = allGroomLogs.length;
  const month = new Date().toISOString().substring(0,7);
  const thisMonth = allGroomLogs.filter(g => g.sessionDate && g.sessionDate.startsWith(month)).length;
  const types = {}; allGroomLogs.forEach(g => { if(g.serviceType) types[g.serviceType] = (types[g.serviceType]||0)+1; });
  const popular = Object.entries(types).sort((a,b)=>b[1]-a[1])[0];

  if(el('gkpi-total')) el('gkpi-total').textContent = total;
  if(el('gkpi-month')) el('gkpi-month').textContent = thisMonth;
  if(el('gkpi-popular')) el('gkpi-popular').textContent = popular ? popular[0] : '—';

  const select = el('groom-filter-groomer');
  if (select) {
    const groomers = [...new Set(allGroomLogs.map(g=>g.groomer).filter(Boolean))];
    select.innerHTML = '<option value="">All Groomers</option>';
    groomers.forEach(g => { const o=document.createElement('option'); o.value=g; o.textContent=g; select.appendChild(o); });
  }
  renderGroomTable();
}

function renderGroomTable() {
  const tbody = el('groom-admin-tbody'); if(!tbody) return;
  let f = allGroomLogs;
  if (groomFilters.search) f = f.filter(g => (g.petName||'').toLowerCase().includes(groomFilters.search) || (g.ownerName||'').toLowerCase().includes(groomFilters.search));
  if (groomFilters.groomer) f = f.filter(g => g.groomer === groomFilters.groomer);
  tbody.innerHTML = '';
  const empty = el('groom-table-empty');
  if (f.length === 0) { if(empty) empty.classList.remove('hidden'); return; }
  if(empty) empty.classList.add('hidden');
  f.forEach(g => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${g.id}</td><td>${g.petName||'—'}</td><td>${g.ownerName||'—'}</td><td>${g.serviceType||'—'}</td><td>${g.productsUsed||'—'}</td><td>${g.coatCondition||'—'}</td><td>${g.groomer||'—'}</td><td>${g.sessionDate?g.sessionDate.substring(0,10):'—'}</td><td><button class="btn-icon-sm" onclick="delGroom(${g.id})" title="Delete">🗑️</button></td>`;
    tbody.appendChild(tr);
  });
}

async function delGroom(id) { if(!confirm('Delete this grooming log?')) return; await deleteGroomLog(id); loadGroomAdmin(); }

document.addEventListener('DOMContentLoaded', () => { initGroomService(); initGroomAdmin(); });
