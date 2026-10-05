/**
 * PawCare Navigation Manager
 * Dynamically builds the navbar for every page based on login state.
 * Include this script AFTER the <nav class="navbar"> element on every page.
 *
 * Usage: just add <script src="nav-manager.js"></script> after your <nav>.
 * The script will automatically populate the nav based on the current user.
 */
(function () {
  'use strict';

  const currentPage = window.location.pathname.split('/').pop() || 'index.html';
  const userStr = localStorage.getItem('pawcare_user');
  let user = null;
  try { user = userStr ? JSON.parse(userStr) : null; } catch (e) { user = null; }

  const isLoggedIn = user && user.success;
  const isStaff = isLoggedIn && user.userType === 'staff';
  const isCustomer = isLoggedIn && user.userType === 'customer';

  /* ── Admin tab bar (shared across ALL admin pages) ────────────── */
  const adminTabs = [
    { label: 'Booking Reservations', href: 'admin.html', icon: '📋' },
    { label: 'Grooming Records',     href: 'grooming_admin.html', icon: '✂️' },
    { label: 'Manage Groomers',      href: 'groomers_admin.html', icon: '💇' },
    { label: 'Boarding',             href: 'boarding_admin.html', icon: '🏠' },
    { label: 'Vet Appointments',     href: 'vet_admin.html', icon: '🩺' },
    { label: 'Global Pet History',   href: 'pet_history_admin.html', icon: '📖' },
    { label: 'Customer Support',     href: 'support_admin.html', icon: '🎧' },
    { label: 'Pet Store Inventory',  href: 'store_admin.html', icon: '🛍️' },
    { label: 'Manage Admins',         href: 'staff_admin.html', icon: '👥' },
    { label: 'Manage Vets',          href: 'vets_admin.html', icon: '🩺' }
  ];

  const adminPages = adminTabs.map(t => t.href).concat(['grooming_service.html', 'vet_service.html']);
  const access = window.PAWCARE_ACCESS;
  const roleName = isStaff ? user.roleName : null;
  const visibleTabs = adminTabs.filter(t => !access || access.canAccess(roleName, t.href));
  const adminHome = access && isStaff ? access.homeFor(roleName) : 'admin.html';

  /* ── Build the nav-links section ─────────────────────────────── */
  const navLinksEl = document.querySelector('.nav-links');
  if (navLinksEl) {
    let links = '';
    links += `<a href="index.html" class="nav-link ${currentPage === 'index.html' ? 'active' : ''}">Home</a>`;
    links += `<a href="booking_main.html" class="nav-link ${currentPage === 'booking_main.html' || currentPage === 'booking.html' ? 'active' : ''}">Grooming</a>`;
    links += `<a href="boarding_main.html" class="nav-link ${currentPage === 'boarding_main.html' || currentPage === 'boarding_service.html' ? 'active' : ''}">Boarding</a>`;
    links += `<a href="vet_main.html" class="nav-link ${currentPage === 'vet_main.html' || currentPage === 'vet_booking.html' ? 'active' : ''}">Veterinary</a>`;
    links += `<a href="store_main.html" class="nav-link ${currentPage === 'store_main.html' ? 'active' : ''}">Pet Store</a>`;
    if (isCustomer) {
      links += `<a href="store_cart.html" class="nav-link ${currentPage === 'store_cart.html' ? 'active' : ''}" style="position:relative;">🛒 Cart <span class="cart-badge" style="background:var(--green-500);color:#fff;border-radius:99px;padding:.1rem .45rem;font-size:.7rem;font-weight:700;display:none;position:absolute;top:-4px;right:-8px;">0</span></a>`;
    }
    if (isStaff) {
      links += `<a href="${adminHome}" class="nav-link ${adminPages.includes(currentPage) ? 'active' : ''}">Admin</a>`;
    }
    navLinksEl.innerHTML = links;
  }

  /* ── Build the right side (login/user/logout) ────────────────── */
  const nav = document.querySelector('.navbar');
  if (!nav) return;

  // Remove any existing right-side elements
  const existingUser = nav.querySelector('.nav-user');
  const existingRight = nav.querySelector('.nav-right');
  const existingAdminBtn = nav.querySelector('.btn-nav-admin');
  if (existingUser) existingUser.remove();
  if (existingRight) existingRight.remove();
  if (existingAdminBtn) existingAdminBtn.remove();
  // Remove inline style divs that contain login/admin buttons
  nav.querySelectorAll('div[style]').forEach(d => {
    if (d.querySelector('a[href="login.html"]') || d.querySelector('.btn-nav-admin')) {
      d.remove();
    }
  });

  const rightDiv = document.createElement('div');
  rightDiv.className = 'nav-right';
  rightDiv.style.cssText = 'display:flex;align-items:center;gap:.75rem;';

  if (isLoggedIn) {
    const initials = isCustomer
      ? ((user.firstName || '?')[0] + (user.lastName || '')[0]).toUpperCase()
      : (user.roleName || 'ADM').substring(0, 3).toUpperCase();
    const displayName = isCustomer
      ? (user.firstName + ' ' + (user.lastName || '')).trim()
      : (user.roleName || 'Staff');

    rightDiv.innerHTML = `
      <a href="${isCustomer ? 'customer_profile.html' : '#'}" class="nav-user-link" style="display:flex;align-items:center;gap:.5rem;text-decoration:none;color:inherit;">
        <div class="user-avatar" style="width:32px;height:32px;border-radius:50%;background:linear-gradient(135deg,var(--green-600),var(--green-400));display:flex;align-items:center;justify-content:center;font-size:.7rem;font-weight:700;color:#fff;">${initials}</div>
        <span style="font-size:.85rem;font-weight:500;color:var(--text-primary);">${displayName}</span>
      </a>
      <button id="nav-logout-btn" style="background:rgba(239,68,68,.1);color:#f87171;border:1px solid rgba(239,68,68,.2);padding:.35rem .8rem;border-radius:6px;font-size:.78rem;font-weight:600;cursor:pointer;font-family:'Inter',sans-serif;">Logout</button>
    `;
    nav.appendChild(rightDiv);

    document.getElementById('nav-logout-btn').addEventListener('click', () => {
      localStorage.removeItem('pawcare_user');
      window.location.href = 'login.html';
    });
  } else {
    rightDiv.innerHTML = `<a href="login.html" class="btn-primary" style="padding:.45rem 1.1rem;font-size:.82rem;">Login / Sign Up</a>`;
    nav.appendChild(rightDiv);
  }

  /* ── Inject admin tabs into admin pages ──────────────────────── */
  if (adminPages.includes(currentPage)) {
    const adminPage = document.querySelector('.admin-page');
    if (adminPage) {
      // Remove any existing admin-tabs
      const existingTabs = adminPage.querySelector('.admin-tabs');
      if (existingTabs) existingTabs.remove();

      const tabBar = document.createElement('div');
      tabBar.className = 'admin-tabs';
      tabBar.style.cssText = 'display:flex;gap:0;border-bottom:1px solid var(--border);margin-bottom:2rem;padding-bottom:0;overflow-x:auto;';

      visibleTabs.forEach(tab => {
        const isActive = tab.href === currentPage;
        const a = document.createElement('a');
        a.href = tab.href;
        a.className = 'admin-tab' + (isActive ? ' active' : '');
        a.style.cssText = `padding:.7rem 1.2rem;font-weight:${isActive ? '700' : '500'};color:${isActive ? 'var(--green-300)' : 'var(--text-secondary)'};border-bottom:${isActive ? '2px solid var(--green-400)' : '2px solid transparent'};white-space:nowrap;text-decoration:none;font-size:.85rem;transition:all .2s;`;
        a.textContent = tab.icon + ' ' + tab.label;

        a.addEventListener('mouseenter', () => { if (!isActive) { a.style.color = 'var(--green-400)'; a.style.borderBottomColor = 'var(--green-600)'; } });
        a.addEventListener('mouseleave', () => { if (!isActive) { a.style.color = 'var(--text-secondary)'; a.style.borderBottomColor = 'transparent'; } });

        tabBar.appendChild(a);
      });

      // Insert AFTER the admin-header
      const adminHeader = adminPage.querySelector('.admin-header');
      if (adminHeader && adminHeader.nextSibling) {
        adminPage.insertBefore(tabBar, adminHeader.nextSibling);
      } else {
        adminPage.prepend(tabBar);
      }
    }
  }
})();
