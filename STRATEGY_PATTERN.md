# PawCare – Strategy Pattern Implementation

This version integrates the Strategy Pattern into the existing PawCare booking flow.

## Pattern roles

- **Strategy interface:** `BookingStrategy`
- **Concrete strategies:**
  - `BoardingBookingStrategy`
  - `GroomingBookingStrategy`
- **Context:** `BookingService`
- **Client/controller:** `BookingController`

## How it works

`BookingService` receives the requested strategy name and selects the corresponding `BookingStrategy` implementation. It validates the common owner/pet information and delegates the service-specific booking creation to the selected strategy.

The two concrete strategies encapsulate the behavior that varies:

- `BoardingBookingStrategy` creates a boarding booking and preserves the existing active-boarding conflict rule.
- `GroomingBookingStrategy` creates a grooming booking and preserves the existing grooming type/default behavior.

The existing API endpoints remain unchanged:

- `POST /api/bookings/boarding`
- `POST /api/bookings/grooming`

Therefore, the existing frontend booking flow does not need to be changed for this Strategy integration.

## Why Strategy fits PawCare

PawCare supports different booking services. The booking creation behavior differs according to the service type. Instead of placing all service-specific creation logic inside `BookingController`, each behavior is encapsulated in its own strategy and selected at runtime by the booking context.

## Lab mapping

| Lab concept | PawCare implementation |
|---|---|
| Strategy Interface | `BookingStrategy` |
| Concrete Strategy | `BoardingBookingStrategy` |
| Concrete Strategy | `GroomingBookingStrategy` |
| Context | `BookingService` |
| Runtime strategy selection | `BookingService.createBooking(strategyName, body)` |
| Client | `BookingController` |

The implementation follows the Strategy Pattern described in the Design Pattern lab: a common strategy contract, concrete interchangeable behaviors, a context that delegates to the selected strategy, and runtime selection of the behavior.
