/* ════════════════════════════════════════════════════════════════════
   PawCare – Application Logic (app.js)
   UC02: Book a Grooming Service
   ════════════════════════════════════════════════════════════════════ */

'use strict';

/* ──────────────────────────────────────────────────────────────────────
   VIDEO BACKGROUND – hides the placeholder once the real video plays
   ────────────────────────────────────────────────────────────────────── */
(function initVideoBg() {
  const video       = document.getElementById('hero-bg-video');
  const placeholder = document.getElementById('hero-video-placeholder');
  if (!video || !placeholder) return;

  const src = video.querySelector('source')?.getAttribute('src') || '';

  /* If still the placeholder value, keep the placeholder visible */
  if (!src || src === 'YOUR_VIDEO_FILE.mp4') return;

  /* Hide placeholder as soon as the video can play */
  video.addEventListener('canplay', () => {
    placeholder.style.transition = 'opacity .8s ease';
    placeholder.style.opacity    = '0';
    setTimeout(() => { placeholder.style.display = 'none'; }, 800);
  });

  /* If video fails to load, keep placeholder with an error hint */
  video.addEventListener('error', () => {
    const hint = placeholder.querySelector('.placeholder-hint');
    if (hint) hint.textContent = '⚠️ Video file not found – check the file path in booking_main.html';
  });
})();

/* ──────────────────────────────────────────────────────────────────────
   SHARED DATA STORE (Connected to MS SQL Server via Spring Boot API)
   ────────────────────────────────────────────────────────────────────── */
let globalBookings = [];

function getBookings() {
  return globalBookings;
}

/* Normalize an API booking object into the shape the admin table expects */
function normalizeBooking(b) {
  /* bookingDateTime is e.g. "2026-10-01T09:00:00" */
  const dt = b.bookingDateTime || b.date || '';
  const datePart = dt.split('T')[0] || '';
  const timePart = dt.includes('T') ? dt.split('T')[1].substring(0,5) : '';

  return {
    id:       String(b.bookingId || b.id || ''),
    owner:    b.ownerName    || b.owner    || '',
    email:    b.ownerEmail   || b.email    || '',
    ownerId:  b.ownerId      || null,
    pet:      b.petName      || b.pet      || '',
    petId:    b.petId        || null,
    service:  b.service      || b.type     || 'Grooming',
    date:     datePart,
    time:     timePart ? (timePart + ' – ' + (parseInt(timePart)+1).toString().padStart(2,'0') + ':00') : (b.time || ''),
    price:    b.price        || 0,
    status:   b.status       || 'pending',
    notes:    b.notes        || b.specialCareInstructions || '',
    created:  b.created      || Date.now()
  };
}

async function loadBookingsFromServer() {
  try {
    const res = await fetch('/api/bookings');
    if (res.ok) {
      const raw = await res.json();
      globalBookings = raw.map(normalizeBooking);
    }
  } catch (e) {
    console.error("Failed to load bookings from API", e);
  }
}

async function saveBookingToServer(booking) {
  try {
    /* Map frontend state to backend API format */
    const user = JSON.parse(localStorage.getItem('pawcare_user') || '{}');
    const ownerId = user.ownerId ? parseInt(user.ownerId) : null;
    const petId = booking.petId ? parseInt(booking.petId) : null;

    if (!ownerId) { console.error("No ownerId in session"); return booking; }
    if (!petId)   { console.error("No petId selected");     return booking; }

    /* Build ISO datetime from date + time slot (e.g. "2026-10-01" + "09:00 – 10:00") */
    const startHour = booking.time ? booking.time.split(/\s*[–-]\s*/)[0].trim() : '09:00';
    const bookingDateTime = booking.date + 'T' + startHour + ':00';

    /* Map service label to Grooming_Booking.Type values */
    const typeMap = {
      'Normal Grooming': 'Normal Grooming',
      'Full Grooming':   'Full Grooming',
      'Premium Grooming':'Premium Grooming'
    };
    const bookingType = typeMap[booking.service] || booking.service || 'Normal Grooming';

    const payload = {
      ownerId: ownerId,
      petId:   petId,
      bookingDateTime: bookingDateTime,
      type: bookingType,
      specialCareInstructions: booking.notes || null
    };

    const res = await fetch('/api/bookings/grooming', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      const saved = await res.json();
      globalBookings.push(saved);
      return saved;
    } else {
      const err = await res.json();
      console.error("Booking API error:", err);
    }
  } catch (e) {
    console.error("Failed to save booking to API", e);
  }
  return booking;
}

async function patchBookingStatus(id, status) {
  try {
    const res = await fetch(`/api/bookings/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status })
    });
    if (res.ok) {
      const updated = await res.json();
      const normalized = normalizeBooking(updated);
      const idx = globalBookings.findIndex(b => b.id === String(id));
      if (idx > -1) globalBookings[idx] = normalized;
      else globalBookings.push(normalized);
      return normalized;
    }
  } catch (e) {
    console.error('Failed to update booking status', e);
  }
}



/* Normalize time string – strips surrounding spaces, collapses any dash variant to a simple hyphen */
function normalizeTime(t) {
  if (!t) return '';
  return t.replace(/[\u2013\u2014]/g, '-').replace(/\s+/g, ' ').trim();
}

/* Booked slots: { "2026-09-15": ["09:00 - 10:00","11:00 - 12:00"], ... } */
function getBookedSlots() {
  const bookings = getBookings();
  const map = {};
  bookings.forEach(b => {
    if (b.status === 'cancelled') return;
    if (!b.date || !b.time) return;   // skip null-time entries (legacy rows)
    if (!map[b.date]) map[b.date] = [];
    map[b.date].push(normalizeTime(b.time));
  });
  return map;
}

/* Generate a unique booking ID */
function generateId() {
  const n = String(getBookings().length + 1).padStart(4,'0');
  return `BK-${new Date().getFullYear()}-${n}`;
}


/* All time slots for a day */
const ALL_SLOTS = [
  '08:00 – 09:00','09:00 – 10:00','10:00 – 11:00',
  '11:00 – 12:00','12:00 – 13:00','13:00 – 14:00',
  '14:00 – 15:00','15:00 – 16:00','16:00 – 17:00','17:00 – 18:00',
];

/* ════════════════════════════════════════════════════════════════════
   UTILITY HELPERS
   ════════════════════════════════════════════════════════════════════ */
function el(id) { return document.getElementById(id); }
function fmt(date) {
  return new Date(date).toLocaleDateString('en-GB', { day:'numeric', month:'long', year:'numeric' });
}
function fmtCurrency(n) { return 'LKR ' + Number(n).toLocaleString(); }

/* ════════════════════════════════════════════════════════════════════
   PAGE DETECTION
   ════════════════════════════════════════════════════════════════════ */
const page = document.body.querySelector('.booking-page') ? 'booking'
           : document.body.querySelector('.admin-page')   ? 'admin'
           : 'home';

if (page === 'booking') initBookingPage();
if (page === 'admin')   initAdminPage();

/* ════════════════════════════════════════════════════════════════════
   BOOKING PAGE
   ════════════════════════════════════════════════════════════════════ */
function initBookingPage() {
  /* State */
  let state = {
    pet: null,
    service: null,
    serviceLabel: null,
    date: null,
    time: null,
    step: 1,
    calYear: new Date().getFullYear(),
    calMonth: new Date().getMonth(),
  };

  /* Pre-select from URL param */
  const params = new URLSearchParams(location.search);
  const preService = params.get('service');
  if (preService) {
    const map = { grooming:'svc-grooming', 'basic-groom':'svc-basic' };
    if (map[preService]) setTimeout(() => el(map[preService]).click(), 100);
  }

  /* ── Step Navigation ─────────────────────────────────────────────── */
  function goTo(n) {
    document.querySelectorAll('.booking-step').forEach(s => s.classList.add('hidden'));
    el(`step-${n}`).classList.remove('hidden');
    state.step = n;
    updateProgress(n);
    window.scrollTo({ top: 100, behavior: 'smooth' });
  }

  function updateProgress(n) {
    for (let i = 1; i <= 4; i++) {
      const step = el(`prog-${i}`);
      step.classList.remove('active','done');
      if (i < n)  step.classList.add('done');
      if (i === n) step.classList.add('active');
    }
    for (let i = 1; i <= 3; i++) {
      const line = el(`line-${i}`);
      if (line) line.classList.toggle('done', i < n);
    }
  }

  /* ── STEP 1: Pet & Service ─────────────────────────────────────── */
  /* Pet selection dynamic loading */
  async function loadUserPets() {
    const grid = el('pet-grid');
    const userStr = localStorage.getItem('pawcare_user');
    if (!userStr) {
      grid.innerHTML = '<div class="field-error">Please log in to view your pets.</div>';
      return;
    }
    const user = JSON.parse(userStr);
    try {
      const res = await fetch(`/api/pets/by-email?email=${encodeURIComponent(user.email)}`);
      if (!res.ok) throw new Error('Network response was not ok');
      const pets = await res.json();
      if (pets.length === 0) {
        grid.innerHTML = '<div class="field-error">No pets found. Please register a pet first.</div>';
        return;
      }
      grid.innerHTML = '';
      pets.forEach(pet => {
        const div = document.createElement('div');
        div.className = 'pet-option';
        div.dataset.pet = pet.petId;
        div.dataset.petname = pet.name;
        div.tabIndex = 0;
        
        // determine avatar based on species
        let avatar = '🐾';
        if (pet.specieName) {
          const l = pet.specieName.toLowerCase();
          if (l.includes('dog')) avatar = '🐕';
          else if (l.includes('cat')) avatar = '🐈';
          else if (l.includes('bird')) avatar = '🦜';
        }
        
        div.innerHTML = `
          <div class="pet-opt-avatar">${avatar}</div>
          <div class="pet-opt-info">
            <div class="pet-opt-name">${pet.name}</div>
            <div class="pet-opt-breed">${pet.specieName || 'Unknown Species'}</div>
          </div>
          <div class="pet-check"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg></div>
        `;
        
        div.addEventListener('click', () => {
          document.querySelectorAll('.pet-option').forEach(o => o.classList.remove('selected'));
          div.classList.add('selected');
          state.pet = div.dataset.pet;
          state.petName = div.dataset.petname;
          el('pet-error').classList.add('hidden');
        });
        div.addEventListener('keydown', e => { if (e.key==='Enter'||e.key===' ') div.click(); });
        
        grid.appendChild(div);
      });
    } catch(e) {
      grid.innerHTML = '<div class="field-error">Error loading pets.</div>';
    }
  }
  
  loadUserPets();

  const svcLabels = {
    'Normal Grooming':  'Normal Grooming',
    'Full Grooming':    'Full Grooming',
    'Premium Grooming': 'Premium Grooming'
  };
  document.querySelectorAll('.service-option').forEach(opt => {
    opt.addEventListener('click', () => {
      document.querySelectorAll('.service-option').forEach(o => o.classList.remove('selected'));
      opt.classList.add('selected');
      state.service = opt.dataset.service;
      state.serviceLabel = svcLabels[opt.dataset.service];
      el('service-error').classList.add('hidden');
    });
    opt.addEventListener('keydown', e => { if (e.key==='Enter'||e.key===' ') opt.click(); });
  });

  el('step1-next').addEventListener('click', () => {
    let valid = true;
    if (!state.pet)     { el('pet-error').classList.remove('hidden'); valid = false; }
    if (!state.service) { el('service-error').classList.remove('hidden'); valid = false; }
    if (valid) { renderCalendar(); goTo(2); }
  });

  /* ── STEP 2: Calendar & Time Slots ─────────────────────────────── */
  function renderCalendar() {
    const y = state.calYear, m = state.calMonth;
    const monthNames = ['January','February','March','April','May','June',
                        'July','August','September','October','November','December'];
    el('cal-month-label').textContent = `${monthNames[m]} ${y}`;

    const grid = el('cal-grid');
    grid.innerHTML = '';

    const today = new Date(); today.setHours(0,0,0,0);
    const firstDay = new Date(y, m, 1).getDay();
    const daysInMonth = new Date(y, m+1, 0).getDate();
    const bookedSlots = getBookedSlots();

    /* blanks for first row */
    for (let i = 0; i < firstDay; i++) {
      const blank = document.createElement('div');
      blank.className = 'cal-day other-month';
      const prev = new Date(y, m, -firstDay + i + 1).getDate();
      blank.textContent = prev;
      grid.appendChild(blank);
    }

    for (let d = 1; d <= daysInMonth; d++) {
      const dayEl = document.createElement('div');
      const thisDate = new Date(y, m, d); thisDate.setHours(0,0,0,0);
      const dateStr = `${y}-${String(m+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
      dayEl.textContent = d;
      dayEl.className = 'cal-day';

      if (thisDate.getTime() === today.getTime()) dayEl.classList.add('today');

      if (thisDate < today) {
        dayEl.classList.add('past');
      } else {
        const booked = (bookedSlots[dateStr] || []).length;
        if (booked >= ALL_SLOTS.length) {
          dayEl.classList.add('booked');
        } else {
          dayEl.classList.add('available');
          if (booked > 0) dayEl.classList.add('has-slots');
          if (state.date === dateStr) dayEl.classList.add('selected');
          dayEl.addEventListener('click', () => {
            state.date = dateStr;
            el('date-error').classList.add('hidden');
            renderCalendar();
            renderTimeSlots(dateStr);
          });
        }
      }
      grid.appendChild(dayEl);
    }

    /* Fill remainder of last row */
    const total = firstDay + daysInMonth;
    const remainder = total % 7 === 0 ? 0 : 7 - (total % 7);
    for (let i = 1; i <= remainder; i++) {
      const blank = document.createElement('div');
      blank.className = 'cal-day other-month';
      blank.textContent = i;
      grid.appendChild(blank);
    }
  }

  el('cal-prev').addEventListener('click', () => {
    state.calMonth--;
    if (state.calMonth < 0) { state.calMonth = 11; state.calYear--; }
    renderCalendar();
  });
  el('cal-next').addEventListener('click', () => {
    state.calMonth++;
    if (state.calMonth > 11) { state.calMonth = 0; state.calYear++; }
    renderCalendar();
  });

  function renderTimeSlots(dateStr) {
    const bookedSlots = getBookedSlots();
    const takenSlots = bookedSlots[dateStr] || [];
    const grid = el('time-slot-grid');
    const ph = el('time-slot-placeholder');

    grid.innerHTML = '';
    grid.classList.remove('hidden');
    ph.classList.add('hidden');

    ALL_SLOTS.forEach(slot => {
      const div = document.createElement('div');
      div.className = 'time-slot';
      const isTaken = takenSlots.includes(normalizeTime(slot));

      if (isTaken) {
        div.classList.add('unavailable');
        div.innerHTML = `<span class="ts-time">${slot}</span><span class="ts-status busy">Unavailable</span>`;
      } else {
        div.innerHTML = `<span class="ts-time">${slot}</span><span class="ts-status">Available</span>`;
        if (state.time === slot) div.classList.add('selected');
        div.addEventListener('click', () => {
          document.querySelectorAll('.time-slot').forEach(t => t.classList.remove('selected'));
          div.classList.add('selected');
          state.time = slot;
          el('time-error').classList.add('hidden');
        });
      }
      grid.appendChild(div);
    });
  }

  el('step2-back').addEventListener('click', () => goTo(1));
  el('step2-next').addEventListener('click', () => {
    let valid = true;
    if (!state.date) { el('date-error').classList.remove('hidden'); valid = false; }
    if (!state.time) { el('time-error').classList.remove('hidden'); valid = false; }
    if (valid) { populateStep3(); goTo(3); }
  });

  /* ── STEP 3: Details ────────────────────────────────────────────── */
  function populateStep3() {
    el('sum-pet').textContent = state.petName || state.pet;
    el('sum-service').textContent = '✂️ ' + state.serviceLabel;
    el('sum-date').textContent = '📅 ' + fmt(state.date);
    el('sum-time').textContent = '⏰ ' + state.time;
  }

  el('step3-back').addEventListener('click', () => goTo(2));
  el('step3-next').addEventListener('click', () => {
    // No required fields to validate here since notes are optional
    el('details-error').classList.add('hidden');
    populateConfirm();
    goTo(4);
  });

  /* ── STEP 4: Confirm ────────────────────────────────────────────── */
  function populateConfirm() {
    const refId = generateId();
    el('confirm-ref').textContent = 'Ref: #' + refId;
    el('cr-pet').textContent      = state.petName || state.pet;
    el('cr-service').textContent  = state.serviceLabel;
    el('cr-date').textContent     = fmt(state.date);
    el('cr-time').textContent     = state.time;
    const notes = el('special-notes').value.trim();
    el('cr-notes').textContent    = notes || 'None';
  }

  el('step4-back').addEventListener('click', () => goTo(3));

  /* Guard against double-submit */
  let submitting = false;

  el('confirm-btn').addEventListener('click', () => {
    if (submitting) return;  /* block spam clicks */
    submitting = true;

    const btnText = el('confirm-btn-text');
    const spinner = el('confirm-spinner');
    btnText.classList.add('hidden');
    spinner.classList.remove('hidden');
    el('confirm-btn').disabled = true;

    /* Simulate concurrency check (Step 6 from UC02) */
    setTimeout(async () => {
      /* Re-check if slot is still available */
      const bookedSlots = getBookedSlots();
      const taken = (bookedSlots[state.date] || []).includes(normalizeTime(state.time));

      if (taken) {
        /* Step 6a: Slot Taken – re-enable button so user can try again */
        btnText.classList.remove('hidden');
        spinner.classList.add('hidden');
        el('confirm-btn').disabled = false;
        submitting = false;

        el('slot-taken-modal').classList.remove('hidden');
        state.time = null;
        goTo(2);
        renderCalendar();
        renderTimeSlots(state.date);
        return;
      }

      /* Step 7: Save booking */
      const user = JSON.parse(localStorage.getItem('pawcare_user') || '{}');
      const newBooking = {
        id:         generateId(),
        owner:      user.firstName ? (user.firstName + ' ' + (user.lastName || '')) : (user.name || 'Unknown'),
        phone:      user.contactNo || '',
        email:      user.email || '',
        petId:      state.pet,
        pet:        (state.petName || '').replace(/\s*[🐕🐈🐩🦜]/g,'').trim(),
        service:    state.serviceLabel,
        date:       state.date,
        time:       state.time,
        status:     'pending',
        notes:      el('special-notes').value.trim()
      };

      const saved = await saveBookingToServer(newBooking);
      const bookingId = (saved && saved.bookingId) ? saved.bookingId : newBooking.id;

      /* Step 8: Show success modal – button stays disabled (booking done) */
      el('modal-booking-id').textContent = `Booking Confirmed! ID: #${bookingId}`;
      el('modal-details').innerHTML = `
        <div style="background:rgba(34,197,94,.06);border:1px solid rgba(34,197,94,.15);border-radius:10px;padding:1rem;text-align:left;font-size:.85rem;color:var(--text-secondary);line-height:1.9">
          <b style="color:var(--text-primary)">Pet:</b> ${escHtml(newBooking.pet)}<br>
          <b style="color:var(--text-primary)">Service:</b> ${escHtml(newBooking.service)}<br>
          <b style="color:var(--text-primary)">Date:</b> ${fmt(newBooking.date)}<br>
          <b style="color:var(--text-primary)">Time:</b> ${escHtml(newBooking.time)}<br>
          <b style="color:#fbbf24">Status:</b> <span style="color:#fbbf24">⏳ Pending – awaiting admin confirmation</span>
        </div>`;
      el('success-modal').classList.remove('hidden');
      /* DO NOT re-enable button here – booking is already submitted */
    }, 1800);
  });

  el('slot-taken-close').addEventListener('click', () => {
    el('slot-taken-modal').classList.add('hidden');
  });
}


/* ════════════════════════════════════════════════════════════════════
   ADMIN PAGE
   ════════════════════════════════════════════════════════════════════ */
function initAdminPage() {
  let currentPage = 1;
  const PER_PAGE = 8;
  let cancelTarget = null;
  let detailTarget = null;

  /* ── KPI Update ─────────────────────────────────────────────────── */
  function updateKPIs() {
    const bookings = getBookings();
    el('kpi-total-val').textContent     = bookings.length;
    el('kpi-confirmed-val').textContent = bookings.filter(b => b.status==='confirmed').length;
    el('kpi-pending-val').textContent   = bookings.filter(b => b.status==='pending').length;
    el('kpi-cancelled-val').textContent = bookings.filter(b => b.status==='cancelled').length;
    const revenue = bookings
      .filter(b => b.status === 'confirmed' || b.status === 'completed')
      .reduce((s, b) => s + (Number(b.price) || 0), 0);
    el('kpi-revenue-val').textContent = fmtCurrency(revenue);
  }

  /* ── Render Table ────────────────────────────────────────────────── */
  function renderTable() {
    const searchQ  = el('admin-search').value.toLowerCase();
    const stFilter = el('filter-status').value;
    const svFilter = el('filter-service').value;
    const sort     = el('filter-sort').value;

    let bookings = getBookings().filter(b => {
      const owner   = (b.owner   || '').toLowerCase();
      const pet     = (b.pet     || '').toLowerCase();
      const id      = (b.id      || '').toLowerCase();
      const email   = (b.email   || '').toLowerCase();
      const service = (b.service || '').toLowerCase();
      const matchQ = !searchQ || owner.includes(searchQ) || pet.includes(searchQ)
                               || id.includes(searchQ)   || email.includes(searchQ);
      const matchS  = !stFilter  || b.status  === stFilter;
      const matchSv = !svFilter  || service.includes(svFilter.toLowerCase());
      return matchQ && matchS && matchSv;
    });

    if (sort === 'newest') bookings.sort((a,b) => b.created - a.created);
    else if (sort === 'oldest') bookings.sort((a,b) => a.created - b.created);
    else if (sort === 'name') bookings.sort((a,b) => a.owner.localeCompare(b.owner));

    const tbody = el('bookings-tbody');
    tbody.innerHTML = '';

    const total = bookings.length;
    const totalPages = Math.max(1, Math.ceil(total / PER_PAGE));
    if (currentPage > totalPages) currentPage = 1;

    const paged = bookings.slice((currentPage-1)*PER_PAGE, currentPage*PER_PAGE);

    if (!paged.length) {
      el('table-empty').classList.remove('hidden');
      el('pagination').innerHTML = '';
      return;
    }
    el('table-empty').classList.add('hidden');

    paged.forEach(b => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td class="booking-id-cell">#${b.id}</td>
        <td>${escHtml(b.owner)}</td>
        <td>
          <div class="pet-cell">
            <span class="pet-emoji">${getPetEmoji(b.pet)}</span>
            <span>${escHtml(b.pet)}</span>
          </div>
        </td>
        <td>${escHtml(b.service)}</td>
        <td>${fmt(b.date)}</td>
        <td>${escHtml(b.time)}</td>
        <td><span class="status-badge ${b.status}">${capitalize(b.status)}</span></td>
        <td style="display:flex;gap:.4rem;flex-wrap:wrap">
          <button class="table-action-btn view-btn" data-id="${b.id}">View</button>
          ${b.status === 'pending'
            ? `<button class="table-action-btn confirm-btn" data-id="${b.id}">Confirm</button>` : ''}
          ${b.status !== 'cancelled' && b.status !== 'completed'
            ? `<button class="table-action-btn danger cancel-btn" data-id="${b.id}">Cancel</button>` : ''}
          <button class="table-action-btn danger delete-btn" data-id="${b.id}" title="Delete">🗑️</button>
        </td>`;
      tbody.appendChild(tr);
    });

    /* Event delegation */
    tbody.querySelectorAll('.view-btn').forEach(btn => {
      btn.addEventListener('click', () => openDetail(btn.dataset.id));
    });
    tbody.querySelectorAll('.confirm-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        await patchBookingStatus(btn.dataset.id, 'confirmed');
        updateKPIs();
        renderTable();
      });
    });
    tbody.querySelectorAll('.cancel-btn').forEach(btn => {
      btn.addEventListener('click', () => openCancelConfirm(btn.dataset.id));
    });
    tbody.querySelectorAll('.delete-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (!confirm('Delete booking #' + btn.dataset.id + '? This cannot be undone.')) return;
        try {
          await fetch(`/api/bookings/${btn.dataset.id}`, { method: 'DELETE' });
          globalBookings = globalBookings.filter(b => b.id !== btn.dataset.id);
          updateKPIs();
          renderTable();
        } catch(e) { alert('Delete failed'); }
      });
    });

    renderPagination(totalPages);
  }

  function renderPagination(total) {
    const pg = el('pagination');
    pg.innerHTML = '';
    if (total <= 1) return;
    for (let i = 1; i <= total; i++) {
      const btn = document.createElement('button');
      btn.className = 'page-btn' + (i === currentPage ? ' active' : '');
      btn.textContent = i;
      btn.addEventListener('click', () => { currentPage = i; renderTable(); });
      pg.appendChild(btn);
    }
  }

  /* ── Detail Modal ────────────────────────────────────────────────── */
  function openDetail(id) {
    const b = getBookings().find(x => x.id === id);
    if (!b) return;
    detailTarget = id;
    const grid = el('detail-grid');
    grid.innerHTML = ['owner','pet','service','date','time','phone','email','notes','status']
      .map(k => {
        let v = b[k] || '—';
        if (k === 'date')   v = fmt(b.date);
        if (k === 'status') v = `<span class="status-badge ${b.status}">${capitalize(b.status)}</span>`;
        if (k === 'notes' && !b.notes) v = 'None';
        return `<div class="confirm-row"><span class="cr-label">${capitalize(k)}</span><span class="cr-value">${k==='status' ? v : escHtml(String(v))}</span></div>`;
      }).join('');
    el('detail-cancel-btn').style.display = (b.status==='cancelled'||b.status==='completed') ? 'none' : '';
    el('detail-modal').classList.remove('hidden');
  }

  el('detail-modal-close').addEventListener('click', () => el('detail-modal').classList.add('hidden'));
  el('detail-close-btn').addEventListener('click', () => el('detail-modal').classList.add('hidden'));
  el('detail-cancel-btn').addEventListener('click', () => {
    el('detail-modal').classList.add('hidden');
    openCancelConfirm(detailTarget);
  });

  /* ── Cancel Modal ────────────────────────────────────────────────── */
  function openCancelConfirm(id) {
    cancelTarget = id;
    el('cancel-modal').classList.remove('hidden');
  }

  el('cancel-no-btn').addEventListener('click', () => el('cancel-modal').classList.add('hidden'));
  el('cancel-yes-btn').addEventListener('click', async () => {
    if (!cancelTarget) return;
    await patchBookingStatus(cancelTarget, 'cancelled');
    el('cancel-modal').classList.add('hidden');
    cancelTarget = null;
    updateKPIs();
    renderTable();
  });

  /* ── Filters ─────────────────────────────────────────────────────── */
  ['admin-search','filter-status','filter-service','filter-sort'].forEach(id => {
    el(id).addEventListener('input', () => { currentPage = 1; renderTable(); });
    el(id).addEventListener('change', () => { currentPage = 1; renderTable(); });
  });
  el('clear-filters').addEventListener('click', () => {
    el('admin-search').value = '';
    el('filter-status').value = '';
    el('filter-service').value = '';
    el('filter-sort').value = 'newest';
    currentPage = 1;
    renderTable();
  });

  /* ── Init ────────────────────────────────────────────────────────── */
  updateKPIs();
  renderTable();
}

/* ════════════════════════════════════════════════════════════════════
   HELPERS
   ════════════════════════════════════════════════════════════════════ */
function capitalize(s) { return s ? s.charAt(0).toUpperCase() + s.slice(1) : ''; }
function escHtml(str) {
  return String(str)
    .replace(/&/g,'&amp;').replace(/</g,'&lt;')
    .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function fmt(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-GB', { month:'short', day:'numeric', year:'numeric' });
}

function getPetEmoji(petName) {
  if (!petName) return '🐾';
  const lower = petName.toLowerCase();
  if (lower.includes('dog') || lower.includes('buddy')) return '🐕';
  if (lower.includes('cat') || lower.includes('luna')) return '🐈';
  if (lower.includes('poodle') || lower.includes('max')) return '🐩';
  return '🐾';
}

/* ── Bootstrap ─────────────────────────────────────────────────────── */
(async function bootstrap() {
  await loadBookingsFromServer();
  if (document.getElementById('step-1')) initBookingPage();
  if (document.getElementById('admin-search')) initAdminPage();
})();
