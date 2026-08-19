# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

TARUMT Resorts — a Java console application (BMCS2063 Data Structures and Algorithms group assignment) implementing an integrated resort management system: walk-in booking with FIFO allocation, VIP/loyalty-tier priority allocation, front-desk check-in/out, housekeeping task tracking, and a loyalty rewards program. Five modules, one owner each (see `ReadMe.txt` for the roster) — all integrated through shared in-memory entity collections created once in `TARUMTResorts.main()`.

Data is in-memory only (hard-coded DAO sample data); there is no database or file persistence, and runtime changes are lost on restart.

`ReadMe_AI.txt` is a long, detailed AI-agent context file (rules, per-module business logic, ADT complexity tables, validation rules, known inconsistencies). Treat it as the canonical spec for business rules and read it before making non-trivial changes — the summary below only hits the parts needed to navigate and build.

## Build and run

Java release 26 (`maven.compiler.release` in `pom.xml`), Maven, main class `com.tarumt.resorts.TARUMTResorts`.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' clean compile
& "$env:JAVA_HOME\bin\java.exe" -cp target\classes com.tarumt.resorts.TARUMTResorts
```

`JAVA_HOME` and the `mvn.cmd` path above assume the JDK/Maven bundled with Apache NetBeans (see `.vscode/settings.json`); adjust if a different toolchain is installed. There is no test suite (`src/test` is empty) — verification is `mvn clean compile` succeeding plus manually exercising the console menus.

The app is entirely console/menu-driven: run it, then navigate the numbered `MainMenuUI` menu into one of the five module UIs.

## Architecture

**Entity-Control-Boundary (ECB)**, one package per layer, plus `dao/` for sample data and `adt/` for the custom collection:

- `boundary/` — menus, tables, prompts, input validation messages; reads from one shared `Scanner`; calls into `control/` and owns no business rules.
- `control/` — one class per module (`WalkInRegistrationControl`, `VIPAllocationControl`, `FrontDeskControl`, `HousekeepingControl`, `LoyaltyRewardsControl`); owns searching, filtering, sorting, validation, state transitions, and cross-module calls.
- `entity/` — business records (`Guest`, `Room`, `Booking`, `WalkInRegistration`, `VIPAllocationRequest`, `LoyaltyAccount`, `LoyaltyTransaction`, `RoomStatusLog`, etc.). `Booking` links `Guest` and `Room`; `WalkInRegistration` carries its own `RegistrationChange` history in a `ListQueueInterface`.
- `dao/` — builds the in-memory demonstration dataset (40 guests, 40 rooms, 60 bookings, plus per-module history) and hands it out via `ListQueueInterface<T>`, reusing shared `Guest`/`Room`/`Booking` object references across DAOs so modules observe each other's writes.
- `TARUMTResorts.java` — composition root: initializes DAO data once, builds the shared collections (`sharedRooms`, `sharedGuests`, `sharedBookings`, `sharedRegistrationHistory`, `sharedStatusLogs`, `sharedLoyaltyAccounts`, `sharedLoyaltyTransactions`, `sharedVipRequestHistory`), passes the same references into every relevant `Control`, wires the `Boundary` classes, and starts `MainMenuUI`.
- `util/` — small stateless helpers shared across modules: `LoyaltyClock` (adjustable demo clock for Loyalty's expiry timers) and `RoomScheduleAvailability` (shared room/date overlap rule — see Conventions below).

**No `java.util` collection storage classes** (no `ArrayList`, `LinkedList`, `HashMap`, `Stack`, `java.util.Queue`). All storage goes through the team-built generic ADT:

- `adt/ListQueueInterface<T>` — interface combining queue, deque-like rollback, and search/filter behavior.
- `adt/DoublyLinkedListQueue<T>` — the (only) implementation: generic doubly-linked nodes with front/rear refs. Key ops: `enqueue`/`dequeue`/`peek` (O(1)), `peekLast`/`removeLast` (O(1), used for Housekeeping rollback), `priorityEnqueue(T, Comparator<T>)` (stable ordered insert, used for VIP tier priority and loyalty earliest-expiry redemption), `searchByKey(String, KeyExtractor<T>)`, `filter(Predicate<T>)`, `getIterator()`.

When adding storage anywhere in this codebase, use `ListQueueInterface`/`DoublyLinkedListQueue`, not a `java.util` collection.

## Cross-module data flow

Modules integrate purely through the shared entity references set up in `TARUMTResorts.main()` — there is no event bus or messaging, one control mutates a shared object/collection and another control reads it later:

1. **Walk-In** (strict FIFO `peek`→allocate→`dequeue`) or **VIP** (`priorityEnqueue` by tier: ELITE > DIAMOND > PLATINUM, FIFO within a tier) allocates a room and creates a shared `CONFIRMED`/`UNPAID` `Booking`.
2. **Front-Desk** finds that same `Booking`, checks in (`CONFIRMED`→`ACTIVE`, `UNPAID`→`PAID`, room becomes occupied — rejects early arrival), then checks out (`ACTIVE`→`CHECKED_OUT`, room becomes vacant), and logs a `DIRTY` `RoomStatusLog` for Housekeeping.
3. **Housekeeping** advances the room through `DIRTY → CLEANING → INSPECTED → READY` (invalid transitions rejected; `CLEANING` auto-advances to `INSPECTED` after 1 minute for demo purposes; `peekLast`/`removeLast` support rollback).
4. **Loyalty** scans `sharedBookings` every time its menu opens, turns each unprocessed `PAID`+`CHECKED_OUT` booking into one `EARN` transaction batch (RM1 gross = 1 point, idempotent per confirmation number — never double-awards), and updates the guest's `MembershipTier` on the shared `Guest` object.
5. **VIP** reads that same shared `Guest.tier` on the next allocation to decide priority eligibility, closing the loop.

Because of this shared-reference design: don't change another module's `Control`'s public method signature without checking every caller, and don't duplicate a business rule (pricing, tier thresholds, status transitions) that's already owned by one shared entity/Control — update the one source instead. `MembershipTier.getRoomDiscountRate()` is the single source for room discount rates; `LoyaltyRewardsControl.getRoomDiscountRate()` and `Booking.applyMembershipDiscount()` both delegate to it rather than keeping their own rate tables.

## Conventions

- Booking date ranges are half-open `[check-in, check-out)`.
- A room's schedule stays blocked for `RoomScheduleAvailability.HOUSEKEEPING_TURNAROUND_DAYS` (currently 1 day) past its existing booking's check-out date, so a new stay can't be scheduled to check in on the same day another guest checks out — reserves guaranteed time for Housekeeping's `DIRTY → CLEANING → INSPECTED → READY` cycle before the next guest is due. This only affects which *future* room/date combinations can be booked; it's separate from each module's same-day operational check (`Room.isAvailable()` + `cleaningStatus == READY/UNKNOWN`), which still applies for an immediate/today check-in.
- `WalkInRegistrationControl` calls the shared `RoomScheduleAvailability.isAvailable(...)` for this rule. `VIPAllocationControl`/`FrontDeskControl` currently keep their own local copies of the same overlap logic *without* the turnaround buffer — pending each owner switching their private method to delegate to `RoomScheduleAvailability` too. Until then, Walk-In enforces the 1-day buffer but VIP/Front-Desk do not, so don't assume all three modules currently agree on room/date availability.
- `gross amount = daily rate × nights`; `Booking` stores gross, discount rate, discount amount, and final amount, recalculated whenever the schedule is created or updated.
- Confirmation numbers are unique 8-digit values.
- Cancellations are soft (status set to `CANCELLED`, record retained for audit) — never delete entities.
