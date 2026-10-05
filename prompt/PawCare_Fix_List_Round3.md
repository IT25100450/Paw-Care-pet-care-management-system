# PawCare — Fix & Refinement List (Round 3)

Follow-up to `PawCare_Fix_List_Round2.md`. Reconstructed from the annotated screenshots in `error_3.docx`.

---

## 1. Boarding — Kennel Size Field Is Not a Working Dropdown

The "Kennel" field (shown with a value like "Medium") is not behaving as an actual dropdown/select control — fix it so it functions as a proper dropdown.

**Note:** per Round 2 (item 2a), this control was supposed to be **removed from the customer-facing form entirely**, since kennel assignment belongs to the Boarding Services Manager on the admin side. Confirm whether this dropdown is appearing in the wrong place (customer side) or whether this is the admin-side control that just needs to be fixed.

**Acceptance criteria:**
- The kennel field renders and behaves as a functional dropdown wherever it is meant to exist (admin side only).

---

## 2. Pet Store — Add Product Form & Update Action

On the admin "Add New Product" form (Product Name, Category, Price, Stock Qty, Description):

- **2a. Add an Image URL field** so products can have an associated image.
- **2b. Fix the Update button** — updating a product currently doesn't work; this needs to actually save changes.

**Acceptance criteria:**
- The Add/Edit Product form includes an Image URL input.
- Submitting an update via the Update button persists changes to the `Product` table and is reflected in the UI.

---

## 3. Remove "Service Report"

Remove the "Service Report" feature/button from the admin panel.

---

## 4. Grooming Records — Remove Price Column

The grooming records table currently shows a **Price** column that only displays `LKR 0` (broken/meaningless). Remove the Price column from the grooming records UI entirely.

---

## 5. Grooming Services — Replace Tier Options

The current "Choose a Service" options are:
- **Full Grooming** — Bath, haircut, nails & ear clean — LKR 3,500
- **Basic Bath & Dry** — Quick wash, dry, brush — LKR 1,800

Replace these with **three new tiers**:
- **Normal Grooming**
- **Full Grooming**
- **Premium Grooming**

(Exact service descriptions and prices for each new tier need to be defined — see Open Questions below.)

---

## 6. Admin Panel — Remove "New Booking" / "New Appointment" Buttons

Remove the **"+ New Booking"** and **"+ New Appointment"** buttons from the admin panel. Admin staff manage and confirm bookings/appointments that customers create — they should not be creating new bookings/appointments directly from the admin side.

---

## Open Questions

- What are the service descriptions and prices for **Normal Grooming** and **Premium Grooming** (the new tiers), and should **Full Grooming**'s existing description/price (Bath, haircut, nails & ear clean — LKR 3,500) stay as-is or change?
- For Item 1: is the broken kennel dropdown appearing on the **customer** side (which should have been removed already per Round 2) or the **admin** side (where it's supposed to exist but just needs fixing)?

---

## Process Requirements (apply to all of the above)

- **Before implementing:** ask **yes/no clarifying questions** on anything ambiguous rather than assuming.
- **Before implementing:** provide a full **implementation plan** describing exactly what will change for each item above, for review and explicit approval.
