# PawCare System: Core Module Architecture

## 1. Grooming Management Module

**Overview**  
This module focuses on the end-to-end management of grooming services within the PawCare system[cite: 5, 6]. It is designed specifically for Grooming Centre Supervisors to track operations and for Pet Owners to review their pet's grooming history[cite: 6]. 

**Core Features & Capabilities**
*   **Schedule Management:** Implementation of grooming schedule interfaces, allowing staff to view and manage daily grooming bookings[cite: 5].
*   **Session Logging:** Development of post-session logging capabilities, enabling staff to record specific details of completed grooming appointments[cite: 5].
*   **Health Tracking:** Integration of health tracking features to note specific coat or skin observations (e.g., skin irritation, ticks, matting) identified during the grooming process[cite: 5, 6].
*   **Data Maintenance:** Addition of CRUD (Create, Read, Update, Delete) operations, allowing supervisors to update or delete historical grooming records to maintain data accuracy[cite: 5].

**Database Implementation**
The module is built upon a normalized schema utilizing a disjoint subclass architecture[cite: 35]:
*   `Grooming_Booking`: The primary subclass handling scheduling details[cite: 35].
*   `Grooming_Record`: A weak entity tracking issues identified and groomer recommendations[cite: 35].
*   `Grooming_Record_Services`: A junction table managing multivalued attributes for specific services rendered (e.g., Bathing, Nail Clipping)[cite: 35].
*   `Grooming_Record_ProductsUsed`: A junction table logging inventory utilized during sessions (e.g., Moisturizing Shampoo)[cite: 35].

---

## 2. Veterinary, Administration & Store Management Module

**Overview**  
This cross-functional module serves as the administrative backbone of PawCare, handling medical records, system-wide access control, and retail inventory management[cite: 5, 6]. It caters to Veterinary Coordinators, Operations Managers, and Customer Care Supervisors[cite: 6].

**Core Features & Capabilities**
*   **Access Control:** Development of secure staff account and role management systems, providing the Operations Manager with oversight over system personnel[cite: 5].
*   **Medical Histories:** Creation of the medical history logging interface for post-check-up data entry, including vaccination record management (tracking vaccine types and next due dates)[cite: 5, 6].
*   **Centralized Dashboards:** A consolidated dashboard for staff to view complete medical, grooming, and boarding histories for any registered pet, alongside global booking visibility for administrative oversight[cite: 5].
*   **Store & Support:** Deployment of the pet store product management catalog, automated service report generation, and customer inquiry response mechanisms[cite: 5].

**Database Implementation**
The architecture handles strict relational mapping and complex entity relationships[cite: 35]:
*   **Medical & Vet:** `Vet_Appointment` and `Medical_Records` are linked via a strict 1:1 `UNIQUE` foreign key constraint to ensure data integrity per consultation[cite: 35]. `Medical_Record_Vaccination` handles recurring data points like administration and due dates[cite: 35].
*   **Access Control:** The `Staff` superclass and `Roles` table manage the disjoint total specialization required for role-based system authentication[cite: 35].
*   **Inventory:** The `Product` and `Product_Category` tables utilize a `Manage_Product` junction table to resolve the M:N (many-to-many) relationship between staff members and store inventory[cite: 35].