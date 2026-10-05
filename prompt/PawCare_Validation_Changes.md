# PawCare — Validation Changes & UI Fix

## Context

- **Create Account form** (First Name, Last Name, Email Address, Phone Number, Password, Confirm Password) needs stronger field validation.
- The **"Confirm Booking"** button shown on the Select Pet & Services step needs to be removed.

---

## 1. Validation Changes

### 1a. Customer Email — Must Be a Genuine Gmail Address
- On signup, the **Email Address** field (customer/Pet Owner accounts) must validate that the address is a real, properly formatted Gmail account (i.e. ends in `@gmail.com` and follows valid email syntax).
- Reject signups with non-Gmail addresses or malformed emails.

### 1b. Admin/Staff Email — Must End With @pawcare.com
- For admin/staff accounts (Operations Manager, Veterinary Coordinator, Boarding Services Manager, Grooming Centre Supervisor, Customer Care Supervisor), the email must end with **`@pawcare.com`**.
- Reject staff account creation/login if the email domain doesn't match.

### 1c. Phone Number — Must Be Exactly 10 Digits
- The **Phone Number** field (format shown as `07X XXX XXXX`) must validate to exactly **10 digits**.
- Reject submissions with fewer or more digits, or non-numeric characters.

**Acceptance criteria:**
- Signup/forms reject invalid entries for all three rules above with a clear inline error message, rather than allowing bad data into the database.
- Existing `Pet_Owner.Contact_No` / `Staff.Contact_No` fields (`VARCHAR(10)`) stay consistent with the 10-digit rule.

---

## 2. Remove "Confirm Booking" Button (Select Pet & Services step)

Remove the **"Confirm Booking"** button that currently appears on the **Select Pet & Services** step of the booking flow.

*(Per earlier fixes, booking confirmation is meant to happen on the admin/staff side — e.g. Boarding Services Manager or Veterinary Coordinator confirming after assignment — not as a customer-facing button at the pet/service selection stage.)*

---

## Open Question

- Should the customer-facing flow still end with some kind of **"Submit Request"** action (that creates a Pending booking) after removing "Confirm Booking," or should selecting a pet/service auto-submit without any button at all?

---

## Process Requirements

- **Before implementing:** ask **yes/no clarifying questions** on anything ambiguous rather than assuming.
- **Before implementing:** provide a full **implementation plan** describing exactly what will change, for review and explicit approval.
