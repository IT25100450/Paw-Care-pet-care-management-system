/* ════════════════════════════════════════════════════════════════════
   PawCare – Pet Boarding Management  (boarding.js)
   ════════════════════════════════════════════════════════════════════ */
const BOARD_API = '/api/boarding';
function el(id) { return document.getElementById(id); }

async function fetchBoardings() { const r = await fetch(BOARD_API); return r.ok ? r.json() : []; }
async function fetchKennels() { const r = await fetch(`${BOARD_API}/kennels`); return r.ok ? r.json() : []; }
async function createBoarding(d) { return fetch(BOARD_API, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }
async function patchBoarding(id,d) { return fetch(`${BOARD_API}/${id}`, { method:'PATCH', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }
async function addKennel(d) { return fetch(`${BOARD_API}/kennels`, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }
async function patchKennel(id,d) { return fetch(`${BOARD_API}/kennels/${id}`, { method:'PATCH', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }

function showBoardAlert(msg, isError=false) {
  const a = el('board-alert'); if(!a) return;
  a.textContent = msg; a.className = 'vet-alert visible' + (isError?' error':'');
  setTimeout(() => a.className = 'vet-alert', 4000);
}

/* ── SERVICE PAGE ──────────────────────────────────────────────── */
function initBoardService() {
  const form = el('board-form');
  if (!form) return;

  loadKennelGrid();
  loadRecentBoardings();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const ownerEmail = el('board-owner-email').value.trim();
    const petName = el('board-pet-name').value.trim();
    const kennelId = el('board-kennel').value;
    const dietNotes = el('board-diet').value.trim();
    const specialInstructions = el('board-instructions').value.trim();
    const checkInTime = el('board-checkin').value;

    if (!ownerEmail || !petName || !kennelId) {
      showBoardAlert('Please fill in Owner Email, Pet Name, and select a Kennel.', true); return;
    }

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true; btn.textContent = 'Reserving…';

    const res = await createBoarding({
      ownerEmail, petName, kennelId: parseInt(kennelId),
      checkInTime: checkInTime ? checkInTime + ':00' : null,
      dietNotes, specialInstructions
    });
    btn.disabled = false; btn.textContent = 'Reserve Kennel';

    if (res.ok) {
      showBoardAlert('✓ Kennel reserved successfully!');
      form.reset(); loadKennelGrid(); loadRecentBoardings();
    } else {
      const msg = await res.text();
      showBoardAlert(msg || 'Reservation failed — kennel may be occupied.', true);
    }
  });
}

async function loadKennelGrid() {
  const grid = el('kennel-grid');
  if (!grid) return;
  const kennels = await fetchKennels();
  grid.innerHTML = '';

  if (kennels.length === 0) {
    grid.innerHTML = '<p style="color:var(--text-muted);grid-column:1/-1;text-align:center;">No kennels configured yet.</p>';
    return;
  }

  const select = el('board-kennel');
  if (select) {
    select.innerHTML = '<option value="">Select kennel…</option>';
    kennels.filter(k => k.status === 'AVAILABLE').forEach(k => {
      const o = document.createElement('option');
      o.value = k.id; o.textContent = `${k.kennelNumber} (${k.size})`;
      select.appendChild(o);
    });
  }

  kennels.forEach(k => {
    const card = document.createElement('div');
    card.className = 'kennel-card ' + k.status.toLowerCase();
    card.innerHTML = `
      <div class="kc-number">${k.kennelNumber}</div>
      <div class="kc-size">${k.size}</div>
      <div class="kc-status">${k.status}</div>
    `;
    grid.appendChild(card);
  });
}

async function loadRecentBoardings() {
  const tbody = el('recent-board-tbody');
  if (!tbody) return;
  const records = await fetchBoardings();
  tbody.innerHTML = '';
  if (records.length === 0) { tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-muted);padding:2rem;">No boarding records</td></tr>'; return; }
  records.slice(0,15).forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${r.petName||'—'}</td><td>${r.ownerName||'—'}</td><td>${r.kennelNumber||'—'}</td><td>${r.status||'—'}</td><td>${r.checkInTime?r.checkInTime.substring(0,16).replace('T',' '):'—'}</td><td>${r.checkOutTime?r.checkOutTime.substring(0,16).replace('T',' '):'—'}</td>
      <td>${r.status==='CHECKED_IN'?`<button class="btn-ghost" style="font-size:.75rem;padding:.3rem .6rem;" onclick="checkOut(${r.id})">Check Out</button>`:(r.status==='RESERVED'?`<button class="btn-ghost" style="font-size:.75rem;padding:.3rem .6rem;" onclick="checkIn(${r.id})">Check In</button>`:'—')}</td>`;
    tbody.appendChild(tr);
  });
}

async function checkIn(id) { await patchBoarding(id, { status: 'CHECKED_IN' }); loadRecentBoardings(); loadKennelGrid(); }
async function checkOut(id) { await patchBoarding(id, { status: 'CHECKED_OUT' }); loadRecentBoardings(); loadKennelGrid(); }

/* ── ADMIN DASHBOARD ───────────────────────────────────────────── */
let allBoardings = [];
let allKennels = [];
let boardFilters = { search:'', status:'' };

function initBoardAdmin() {
  if (!el('board-admin-tbody')) return;
  loadBoardAdmin();

  el('board-admin-search')?.addEventListener('input', e => { boardFilters.search = e.target.value.toLowerCase(); renderBoardTable(); });
  el('board-filter-status')?.addEventListener('change', e => { boardFilters.status = e.target.value; renderBoardTable(); });
  el('board-clear-filters')?.addEventListener('click', () => { boardFilters={search:'',status:''}; if(el('board-admin-search'))el('board-admin-search').value=''; if(el('board-filter-status'))el('board-filter-status').value=''; renderBoardTable(); });

  /* Add kennel form */
  el('add-kennel-btn')?.addEventListener('click', async () => {
    const num = el('new-kennel-num')?.value.trim();
    const size = el('new-kennel-size')?.value;
    if (!num) { alert('Kennel number required'); return; }
    const res = await addKennel({ kennelNumber: num, size: size || 'MEDIUM' });
    if (res.ok) { el('new-kennel-num').value = ''; loadBoardAdmin(); }
    else { const msg = await res.text(); alert(msg); }
  });
}

async function loadBoardAdmin() {
  allBoardings = await fetchBoardings();
  allKennels = await fetchKennels();
  updateBoardKPIs();
  renderKennelAdmin();
  renderBoardTable();
}

function updateBoardKPIs() {
  const total = allBoardings.length;
  const active = allBoardings.filter(b => b.status==='CHECKED_IN' || b.status==='RESERVED').length;
  const available = allKennels.filter(k => k.status==='AVAILABLE').length;
  const occ = allKennels.length > 0 ? Math.round((allKennels.filter(k=>k.status==='OCCUPIED').length / allKennels.length)*100) : 0;

  if(el('bkpi-total')) el('bkpi-total').textContent = total;
  if(el('bkpi-active')) el('bkpi-active').textContent = active;
  if(el('bkpi-available')) el('bkpi-available').textContent = available;
  if(el('bkpi-occ')) el('bkpi-occ').textContent = occ + '%';
}

function renderKennelAdmin() {
  const grid = el('admin-kennel-grid');
  if (!grid) return;
  grid.innerHTML = '';
  allKennels.forEach(k => {
    const card = document.createElement('div');
    card.className = 'kennel-card ' + k.status.toLowerCase();
    card.innerHTML = `<div class="kc-number">${k.kennelNumber}</div><div class="kc-size">${k.size}</div><div class="kc-status">${k.status}</div>`;
    if (k.status === 'AVAILABLE') {
      card.innerHTML += `<button class="btn-ghost" style="font-size:.7rem;margin-top:.4rem;padding:.2rem .5rem;" onclick="setKennelMaint(${k.id})">→ Maintenance</button>`;
    } else if (k.status === 'MAINTENANCE') {
      card.innerHTML += `<button class="btn-ghost" style="font-size:.7rem;margin-top:.4rem;padding:.2rem .5rem;" onclick="setKennelAvail(${k.id})">→ Available</button>`;
    }
    grid.appendChild(card);
  });
}

async function setKennelMaint(id) { await patchKennel(id, { status:'MAINTENANCE' }); loadBoardAdmin(); }
async function setKennelAvail(id) { await patchKennel(id, { status:'AVAILABLE' }); loadBoardAdmin(); }

function renderBoardTable() {
  const tbody = el('board-admin-tbody'); if(!tbody) return;
  let f = allBoardings;
  if (boardFilters.search) f = f.filter(b => (b.petName||'').toLowerCase().includes(boardFilters.search) || (b.ownerName||'').toLowerCase().includes(boardFilters.search));
  if (boardFilters.status) f = f.filter(b => b.status === boardFilters.status);
  tbody.innerHTML = '';
  const empty = el('board-table-empty');
  if (f.length === 0) { if(empty) empty.classList.remove('hidden'); return; }
  if(empty) empty.classList.add('hidden');
  f.forEach(b => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${b.id}</td><td>${b.petName||'—'}</td><td>${b.ownerName||'—'}</td><td>${b.kennelNumber||'—'}</td><td><span class="status-badge ${b.status.toLowerCase()}">${b.status}</span></td><td>${b.checkInTime?b.checkInTime.substring(0,16).replace('T',' '):'—'}</td><td>${b.checkOutTime?b.checkOutTime.substring(0,16).replace('T',' '):'—'}</td>
      <td>${b.status==='CHECKED_IN'?`<button class="btn-ghost" style="font-size:.75rem;padding:.3rem .5rem;" onclick="checkOutAdmin(${b.id})">Check Out</button>`:(b.status==='RESERVED'?`<button class="btn-ghost" style="font-size:.75rem;padding:.3rem .5rem;" onclick="checkInAdmin(${b.id})">Check In</button>`:'—')}</td>`;
    tbody.appendChild(tr);
  });
}

async function checkInAdmin(id) { await patchBoarding(id, { status:'CHECKED_IN' }); loadBoardAdmin(); }
async function checkOutAdmin(id) { await patchBoarding(id, { status:'CHECKED_OUT' }); loadBoardAdmin(); }

document.addEventListener('DOMContentLoaded', () => { initBoardService(); initBoardAdmin(); });
