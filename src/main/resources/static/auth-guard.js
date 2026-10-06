/**
 * PawCare Access Guard
 * Include this script on protected pages to enforce login.
 * Public pages (index.html, login.html) should NOT include this.
 *
 * Role rule: Operations Manager sees everything; every other staff role
 * only sees the admin pages that belong to its own service.
 */
(function() {
  const OPS = 'Operations Manager';

  /* Single source of truth for staff page access (also used by nav-manager.js) */
  const STAFF_PAGE_ROLES = {
    'admin.html':             [OPS],                                   // Booking reservations (all services)
    'staff_admin.html':       [OPS],                                   // Manage admins
    'grooming_admin.html':    [OPS, 'Grooming Centre Supervisor'],
    'grooming_service.html':  [OPS, 'Grooming Centre Supervisor'],
    'groomers_admin.html':    [OPS, 'Grooming Centre Supervisor'],
    'boarding_admin.html':    [OPS, 'Boarding Services Manager'],
    'vet_admin.html':         [OPS, 'Veterinary Coordinator'],
    'vet_service.html':       [OPS, 'Veterinary Coordinator'],
    'vets_admin.html':        [OPS, 'Veterinary Coordinator'],
    'pet_history_admin.html': [OPS, 'Veterinary Coordinator'],
    'support_admin.html':     [OPS, 'Customer Care Supervisor'],
    'store_admin.html':       [OPS, 'Customer Care Supervisor']
  };

  /* Landing page per role (where the "Admin" link and access-denied redirects go) */
  const ROLE_HOME = {
    'Operations Manager':         'admin.html',
    'Grooming Centre Supervisor': 'grooming_admin.html',
    'Boarding Services Manager':  'boarding_admin.html',
    'Veterinary Coordinator':     'vet_admin.html',
    'Customer Care Supervisor':   'store_admin.html'
  };

  window.PAWCARE_ACCESS = {
    STAFF_PAGE_ROLES,
    ROLE_HOME,
    canAccess(roleName, page) {
      const allowed = STAFF_PAGE_ROLES[page];
      return !allowed || allowed.includes(roleName);
    },
    homeFor(roleName) { return ROLE_HOME[roleName] || 'index.html'; }
  };

  const publicPages = ['index.html', 'login.html', ''];
  const currentPage = window.location.pathname.split('/').pop() || '';

  if (publicPages.includes(currentPage)) return;

  const user = localStorage.getItem('pawcare_user');
  if (!user) {
    window.location.href = 'login.html';
    return;
  }

  try {
    const parsed = JSON.parse(user);
    if (!parsed.success) {
      localStorage.removeItem('pawcare_user');
      window.location.href = 'login.html';
      return;
    }

    /* Staff RBAC: restrict admin pages to correct roles */
    if (STAFF_PAGE_ROLES[currentPage]) {
      if (parsed.userType !== 'staff') {
        window.location.href = 'index.html';
        return;
      }
      if (!STAFF_PAGE_ROLES[currentPage].includes(parsed.roleName)) {
        // Send them to their own service dashboard instead of logging them out
        window.location.replace(window.PAWCARE_ACCESS.homeFor(parsed.roleName));
        return;
      }
    }
  } catch (e) {
    localStorage.removeItem('pawcare_user');
    window.location.href = 'login.html';
  }
})();
