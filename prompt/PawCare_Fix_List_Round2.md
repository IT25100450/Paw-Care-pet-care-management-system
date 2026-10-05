# PawCare — Fix & Refinement List (Round 2)

Follow-up to `PawCare_Fix_List.md`. Reorganized and expanded from the raw notes into clear, actionable items.

---

## 1. Grooming Records — Scheduling & Conflict Rules

**Current state:** A grooming record can only be added/logged *after* its booking has been confirmed. This gating behavior is correct and should stay.

**Required fixes:**
- **1a. Groomer conflict prevention:** Two grooming bookings must not be assigned the same groomer for overlapping date/time slots. When a Grooming Centre Supervisor tries to assign a groomer who already has a confirmed session at that time, the system should block it (similar to the existing vet double-booking rule).
- **1b. Groomer reassignment:** The supervisor must be able to change the groomer already assigned to a confirmed grooming record (not just assign once and lock it).

**Acceptance criteria:**
- Attempting to assign an already-busy groomer to an overlapping slot is rejected or flagged.
- An assigned groomer can be swapped for another available groomer at any time before the session is completed.

---

## 2. Boarding — Kennel Assignment & Duplicate Prevention

**Current state:** The customer-facing boarding form still has a "Kennel Size" selector, but per the Round 1 fix, kennel selection/confirmation belongs to the **Boarding Services Manager (admin side)**, not the customer.

**Required fixes:**
- **2a. Remove customer kennel selection:** Delete the "Kennel Size" dropdown (and any related kennel-choice UI) from the customer booking form. The customer should only submit check-in/check-out dates and pet details; the admin assigns the actual kennel afterward.
- **2b. Prevent double-boarding a pet:** If a pet already has an active or pending boarding booking (not yet checked out), the system must not allow a new boarding booking to be created for that same pet until the existing one is completed/cancelled.
- **2c. Admin update capability:** The Boarding Services Manager must be able to **update** existing boarding records from the admin panel (e.g., change kennel, dates, status) — not just create/view them.

**Acceptance criteria:**
- Customer boarding form no longer shows a kennel-size field.
- Submitting a second boarding request for a pet that already has an open boarding record is blocked with a clear message.
- Admin panel has a working Edit/Update action on boarding records.

---

## 3. Vet Appointments — Duplicate Booking Indicator

**Required fix:**
- In the customer's vet appointment view, if the customer already has an existing vet appointment booked for a **specific pet**, that existing booking should be visually flagged **in red** so they're aware before booking another one for the same pet.

**Open question this raises:** Is this purely a visual warning (customer can still proceed to double-book), or should it also **block** a second vet appointment for the same pet the way boarding now will? *(Flagged for the implementation plan / yes-no questions.)*

**Acceptance criteria:**
- An existing appointment for a pet that already has one shows a distinct red status/highlight in the customer's appointment list.

---

## 4. Pet Store Inventory — Fix "Update" Action

**Current state:** In the admin Pet Store Inventory table, the **Update/Edit** action does not work (Create, Read, and Delete context aside — Update specifically is broken here).

**Required fix:**
- Debug and fix the Update action so staff (per role permissions) can successfully edit product fields (name, category, price, stock, description) and have changes persist to the `Product` table.

**Acceptance criteria:**
- Editing a product via the admin UI correctly updates the corresponding row in the database and reflects immediately in the table view.

---

## 5. Roles Reference (confirmed)

The five staff roles are confirmed as a reference table matching the `Roles` table:

| Role_ID | Role_Name |
|---|---|
| 1 | Operations Manager |
| 2 | Veterinary Coordinator |
| 3 | Boarding Services Manager |
| 4 | Grooming Centre Supervisor |
| 5 | Customer Care Supervisor |

**Access rule (reconfirmed):** Every role **except Operations Manager** should only be able to see and act on pages belonging to their own service. The Operations Manager retains full visibility across all services and staff.

---

## 6. Database ↔ Webpage Field Alignment

**Instruction:** Use the existing **PawCare database** (as defined in the SQL folder) as the single source of truth.
- **Do not rename the database** or restructure its core naming.
- Go through every page (customer-facing and admin-facing) and make sure its form fields and displayed columns **match the actual schema fields** for that entity — no missing fields, no mismatched names, no orphaned UI fields that don't map to a column.
- Every piece of data a webpage collects should actually be written to and fully populate the corresponding database table/columns — no partial inserts, no fields silently left blank in the database when the UI collected them.

**Acceptance criteria:**
- For each entity (Pet_Owner, Pet, Booking + subtypes, Staff + role subtypes, Product, Vet_Appointment, Medical_Records, etc.), the webpage fields are a 1:1 match with the schema columns relevant to that page.
- Submitting any form results in all relevant columns being populated in the database (no `NULL`/blank gaps from fields the UI already captured).

---

## Process Requirements (apply to all of the above)

- **Before implementing:** ask **yes/no clarifying questions** on anything ambiguous (e.g., the open question under Item 3) rather than assuming.
- **Before implementing:** provide a full **implementation plan** describing exactly what will change for each item above, for review and explicit approval.
