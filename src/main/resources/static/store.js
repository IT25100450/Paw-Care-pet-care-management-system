/* ════════════════════════════════════════════════════════════════════
   PawCare – Pet Store  (store.js)
   ════════════════════════════════════════════════════════════════════ */
const PROD_API = '/api/products';
function el(id) { return document.getElementById(id); }

async function fetchProducts() { const r = await fetch(PROD_API); return r.ok ? r.json() : []; }
async function createProduct(d) { return fetch(PROD_API, { method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }
async function updateProduct(id,d) { return fetch(`${PROD_API}/${id}`, { method:'PUT', headers:{'Content-Type':'application/json'}, body:JSON.stringify(d) }); }
async function deleteProduct(id) { return fetch(`${PROD_API}/${id}`, { method:'DELETE' }); }

/* ── Normalize API response ── */
function normalizeProduct(p) {
  return {
    id:          p.productId   || p.id,
    name:        p.productName || p.name,
    category:    p.categoryName || p.category,
    categoryId:  p.categoryId,
    price:       Number(p.price  || 0),
    stockQty:    Number(p.quantity !== undefined ? p.quantity : (p.stockQty || 0)),
    description: p.description || '',
    imageUrl:    p.imageUrl || ''
  };
}

/* ═══════════════════════════════════════════════════════════════════
   CART (localStorage)
   ═══════════════════════════════════════════════════════════════════ */
const CART_KEY = 'pawcare_cart';

function getCart() {
  try { return JSON.parse(localStorage.getItem(CART_KEY) || '[]'); } catch { return []; }
}

function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart));
  updateCartBadge();
}

function addToCart(product) {
  const cart = getCart();
  const existing = cart.find(i => i.id === product.id);
  if (existing) {
    existing.qty = Math.min(existing.qty + 1, product.stockQty);
  } else {
    cart.push({ id: product.id, name: product.name, price: product.price, stockQty: product.stockQty, qty: 1 });
  }
  saveCart(cart);
  showCartToast(product.name);
}

function updateCartBadge() {
  const cart  = getCart();
  const total = cart.reduce((s, i) => s + i.qty, 0);
  document.querySelectorAll('.cart-badge').forEach(b => {
    b.textContent = total;
    b.style.display = total > 0 ? 'inline-flex' : 'none';
  });
}

function showCartToast(name) {
  let t = document.getElementById('cart-toast');
  if (!t) { t = document.createElement('div'); t.id='cart-toast'; t.style.cssText='position:fixed;bottom:1.5rem;right:1.5rem;background:var(--green-600);color:#fff;padding:.7rem 1.2rem;border-radius:8px;font-size:.85rem;font-weight:600;z-index:9999;opacity:1;transition:opacity .4s;'; document.body.appendChild(t); }
  t.textContent = `🛒 "${name}" added to cart!`;
  t.style.opacity = '1';
  clearTimeout(t._timer);
  t._timer = setTimeout(() => { t.style.opacity = '0'; }, 2500);
}

/* ═══════════════════════════════════════════════════════════════════
   STOREFRONT (store_main.html)
   ═══════════════════════════════════════════════════════════════════ */
let storeProducts = [];
let storeFilter = { search:'', category:'' };

function initStorefront() {
  if (!el('product-grid')) return;
  loadStorefront();
  el('store-search')?.addEventListener('input', e => { storeFilter.search = e.target.value.toLowerCase(); renderProductGrid(); });
  el('store-cat-filter')?.addEventListener('change', e => { storeFilter.category = e.target.value; renderProductGrid(); });
  updateCartBadge();
}

async function loadStorefront() {
  const raw = await fetchProducts();
  storeProducts = raw.map(normalizeProduct);
  populateCatFilter();
  renderProductGrid();
}

function populateCatFilter() {
  const sel = el('store-cat-filter'); if (!sel) return;
  const cats = [...new Set(storeProducts.map(p => p.category).filter(Boolean))];
  sel.innerHTML = '<option value="">All Categories</option>';
  cats.forEach(c => { const o = document.createElement('option'); o.value=c; o.textContent=c; sel.appendChild(o); });
}

function renderProductGrid() {
  const grid = el('product-grid'); if (!grid) return;
  let f = storeProducts;
  if (storeFilter.search)   f = f.filter(p => (p.name||'').toLowerCase().includes(storeFilter.search) || (p.category||'').toLowerCase().includes(storeFilter.search));
  if (storeFilter.category) f = f.filter(p => p.category === storeFilter.category);
  grid.innerHTML = '';
  const empty = el('store-empty');
  if (f.length === 0) { if (empty) empty.classList.remove('hidden'); return; }
  if (empty) empty.classList.add('hidden');

  f.forEach(p => {
    const inStock = p.stockQty > 0;
    const card = document.createElement('div');
    card.className = 'product-card';
    const isUrl = p.imageUrl && (p.imageUrl.startsWith('http://') || p.imageUrl.startsWith('https://'));
    const imgSrc = isUrl ? p.imageUrl : (p.imageUrl ? `images/${p.imageUrl}` : null);
    const imgHtml = imgSrc ? `<img src="${imgSrc}" alt="${p.name}" style="width:100%;height:100%;object-fit:cover;border-radius:12px;" onerror="this.outerHTML='<div class=\\'pc-img-placeholder\\'>🛍️</div>'">` : `<div class="pc-img-placeholder">🛍️</div>`;
    card.innerHTML = `
      <div class="pc-img">${imgHtml}</div>
      <div class="pc-cat">${p.category || 'Uncategorized'}</div>
      <div class="pc-name">${p.name}</div>
      <div class="pc-desc">${p.description || ''}</div>
      <div class="pc-bottom">
        <div class="pc-price">LKR ${(p.price||0).toLocaleString()}</div>
        <div class="pc-stock">${inStock ? `${p.stockQty} in stock` : '<span style="color:#f87171;">Out of stock</span>'}</div>
      </div>
      <button class="btn-primary pc-cart-btn" style="width:100%;margin-top:.8rem;padding:.55rem;" ${!inStock?'disabled':''} onclick="addToCart(${JSON.stringify(p).replace(/"/g,'&quot;')})">
        ${inStock ? '🛒 Add to Cart' : 'Out of Stock'}
      </button>`;
    grid.appendChild(card);
  });
}

/* ═══════════════════════════════════════════════════════════════════
   ADMIN DASHBOARD (store_admin.html)
   ═══════════════════════════════════════════════════════════════════ */
let adminProducts = [];
let adminFilter   = { search:'', category:'' };
let editingId     = null;

let productCategories = [];

async function loadCategories() {
  const r = await fetch(`${PROD_API}/categories`);
  productCategories = r.ok ? await r.json() : [];
  const opts = productCategories.map(c => `<option value="${c.productCategoryId}">${c.productCategoryName}</option>`).join('');
  if (el('prod-category'))      el('prod-category').innerHTML      = '<option value="">Select category…</option>' + opts;
  if (el('edit-prod-category')) el('edit-prod-category').innerHTML = opts;
}

function closeEditModal() { el('edit-product-panel')?.classList.remove('open'); editingId = null; }

function initStoreAdmin() {
  if (!el('store-admin-tbody')) return;
  loadCategories();
  loadStoreAdmin();

  el('store-admin-search')?.addEventListener('input', e => { adminFilter.search = e.target.value.toLowerCase(); renderStoreAdminTable(); });
  el('store-filter-cat')?.addEventListener('change', e => { adminFilter.category = e.target.value; renderStoreAdminTable(); });
  el('store-clear-filters')?.addEventListener('click', () => { adminFilter={search:'',category:''}; if(el('store-admin-search'))el('store-admin-search').value=''; if(el('store-filter-cat'))el('store-filter-cat').value=''; renderStoreAdminTable(); });

  /* Add product form */
  el('add-product-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name        = el('prod-name').value.trim();
    const categoryId  = parseInt(el('prod-category').value);
    const price       = parseFloat(el('prod-price').value) || 0;
    const quantity    = parseInt(el('prod-stock').value) || 0;
    const description = el('prod-desc')?.value.trim();
    const imageUrl    = el('prod-image')?.value.trim();
    if (!name) { alert('Product name is required'); return; }
    if (!categoryId) { alert('Please select a category'); return; }
    const btn = e.target.querySelector('button[type="submit"]');
    btn.disabled = true; btn.textContent = 'Adding…';
    const res = await createProduct({ productName: name, productCategoryId: categoryId, price, quantity, description, imageUrl });
    btn.disabled = false; btn.textContent = 'Add Product';
    if (res.ok) { e.target.reset(); loadStoreAdmin(); }
    else { const msg = await res.json(); alert(msg.error || 'Failed'); }
  });

  /* Edit form */
  el('edit-product-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!editingId) return;
    const body = {
      productName: el('edit-prod-name').value.trim(),
      productCategoryId: parseInt(el('edit-prod-category').value),
      price: parseFloat(el('edit-prod-price').value) || 0,
      quantity: parseInt(el('edit-prod-stock').value) || 0,
      description: el('edit-prod-desc')?.value.trim(),
      imageUrl: el('edit-prod-image')?.value.trim()
    };
    if (!body.productName) { alert('Product name is required'); return; }
    if (body.price < 0 || body.quantity < 0) { alert('Price and stock cannot be negative'); return; }
    const btn = el('edit-save-btn'); if (btn) { btn.disabled = true; btn.textContent = 'Saving…'; }
    const res = await updateProduct(editingId, body);
    if (btn) { btn.disabled = false; btn.textContent = 'Save Changes'; }
    if (res.ok) { closeEditModal(); loadStoreAdmin(); }
    else { const d = await res.json().catch(() => ({})); alert('Update failed: ' + (d.error || res.status)); }
  });
  el('edit-cancel-btn')?.addEventListener('click', closeEditModal);
  el('edit-product-panel')?.addEventListener('click', e => { if (e.target.id === 'edit-product-panel') closeEditModal(); });
}

async function loadStoreAdmin() {
  const raw = await fetchProducts();
  adminProducts = raw.map(normalizeProduct);

  if(el('skpi-total'))   el('skpi-total').textContent   = adminProducts.length;
  if(el('skpi-instock')) el('skpi-instock').textContent = adminProducts.filter(p => p.stockQty > 0).length;
  if(el('skpi-outstock'))el('skpi-outstock').textContent= adminProducts.filter(p => p.stockQty === 0).length;
  if(el('skpi-cats'))    el('skpi-cats').textContent    = new Set(adminProducts.map(p=>p.category).filter(Boolean)).size;

  const sel = el('store-filter-cat');
  if (sel) {
    const cats = [...new Set(adminProducts.map(p=>p.category).filter(Boolean))];
    sel.innerHTML = '<option value="">All Categories</option>';
    cats.forEach(c => { const o=document.createElement('option'); o.value=c; o.textContent=c; sel.appendChild(o); });
  }
  renderStoreAdminTable();
}

function renderStoreAdminTable() {
  const tbody = el('store-admin-tbody'); if (!tbody) return;
  let f = adminProducts;
  if (adminFilter.search)   f = f.filter(p => (p.name||'').toLowerCase().includes(adminFilter.search));
  if (adminFilter.category) f = f.filter(p => p.category === adminFilter.category);
  tbody.innerHTML = '';
  const empty = el('store-table-empty');
  if (f.length === 0) { if(empty) empty.classList.remove('hidden'); return; }
  if (empty) empty.classList.add('hidden');

  f.forEach(p => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${p.id}</td><td>${p.name||'—'}</td><td>${p.category||'—'}</td><td>LKR ${(p.price||0).toLocaleString()}</td><td>${p.stockQty||0}</td><td style="max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${p.description||'—'}</td>
      <td>
        <button class="btn-icon-sm" onclick="startEdit(${p.id})" title="Edit">✏️</button>
        <button class="btn-icon-sm" onclick="delProduct(${p.id})" title="Delete" style="margin-left:.3rem;">🗑️</button>
      </td>`;
    tbody.appendChild(tr);
  });
}

function startEdit(id) {
  const p = adminProducts.find(x => x.id === id); if (!p) return;
  editingId = id;
  if(el('edit-prod-name'))  el('edit-prod-name').value  = p.name;
  if(el('edit-prod-price')) el('edit-prod-price').value = p.price;
  if(el('edit-prod-stock')) el('edit-prod-stock').value = p.stockQty;
  if(el('edit-prod-desc'))  el('edit-prod-desc').value  = p.description;
  if(el('edit-prod-image')) el('edit-prod-image').value = p.imageUrl;
  if(el('edit-prod-category') && p.categoryId) el('edit-prod-category').value = String(p.categoryId);
  el('edit-product-panel')?.classList.add('open');
  el('edit-prod-name')?.focus();
}

async function delProduct(id) {
  if (!confirm('Delete this product?')) return;
  await deleteProduct(id);
  loadStoreAdmin();
}

document.addEventListener('DOMContentLoaded', () => { initStorefront(); initStoreAdmin(); });
