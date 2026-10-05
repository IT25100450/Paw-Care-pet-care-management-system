# PawCare — Fix & Rebuild List

Reorganized from the raw notes into clear feature groups (the original order was jumbled).

## 1. Boarding System — Overhaul

- User flow: customer books a kennel by providing check-in and check-out dates.
- On submission, the booking goes into **Pending** status (not confirmed automatically).
- The relevant stakeholder (Boarding Services Manager) picks the actual kennel type and confirms the booking.
- **Remove** the current customer-facing "Kennel Management" panel entirely — kennel management belongs only on the **admin side**.
- The customer should be able to see their current boarding status on their **user profile**.

## 2. Veterinary Appointments & Medical Records — Overhaul

- Veterinary Coordinator gets an interface to add/manage vets.
- Customer flow: user books a **pending** vet appointment by selecting their pet, a date, and a time slot.
- Once booked, the admin (Veterinary Coordinator) can see the appointment from their dashboard.
- To confirm a booking, the Veterinary Coordinator must select a vet who has **no ongoing work** at that time slot; if a vet is already booked, show them as **"Not available"** instead of letting them be selected.

## 3. Booking & Pricing Bug

- Prices currently show as undefined because services aren't being pulled from a proper pricing/services table.
- Fix: pull price per service from a real services/pricing table so every booking row shows a correct price instead of `LKR 0` / `LKR NaN`.

## 4. Data Integrity Bugs — Confirm & Refresh

- **Bug A:** After confirming a booking, its record fields (owner, pet, service, date, time slot, price) get wiped out and show as `undefined` / `NaN`.
- **Bug B:** After refreshing the page, a confirmed booking reverts back to **Pending** status.
- Both need to be fixed so confirmed bookings persist correctly with all their data intact.

## 5. Pet Store Inventory — Admin Table Not Populating

- The admin Pet Store Inventory table shows `undefined` for ID, Name, and Category columns (Price, Stock, Description do show).
- Fix so the product ID, name, and category populate correctly from the database.

## 6. Signup — Data Not Saving Correctly

- New account signup form (First Name, Last Name, Email, Phone, Password) is not writing the correct values into the `Pet_Owner` table — inspect and fix the insert/mapping logic so the right data lands in the right columns.

## 7. Customer Pet Store Page — Needs a Working Shopping Cart

- Build a normal e-commerce-style shopping cart flow (add to cart, view cart, adjust quantities) similar to a standard online store.
- Since the project has **no online payment integration**, checkout ends with the customer **confirming their basket** (the list of items they picked) rather than paying online.

## 8. Pet Profile — Enable Update, Not Just Cancel

- On the user profile, pets/bookings/appointments currently only support **Cancel**.
- Add the ability for the customer to actually **update** their pet's profile details (not just cancel bookings).

## 9. CRUD Completion — Delete (and remaining Update gaps)

Create, Read, and Update are working (once the above fixes are in) — **Delete is missing everywhere**. Implement delete for:
- Booking records (from the admin side)
- A user's pet details (full removal, from the user side)
- Veterinary appointments
- Vet (staff) details
- Boarding records

Also confirm/complete this Update capability:
- Stakeholders (relevant staff) can update pet store product/item details.

## 10. Role-Based Page Access

- Each staff role should only see the pages/sections relevant to their own service — no visibility into other roles' areas.
- The **Operations Manager** is the exception: they have full oversight of every operation, including assigning/managing staff roles.

---

## Process Requirements (apply to all of the above)

- **Before implementing:** provide a full **implementation plan** describing exactly what will change for each item above, for review.
- **Before implementing:** ask **yes/no clarifying questions** on anything ambiguous or underspecified, rather than assuming.
