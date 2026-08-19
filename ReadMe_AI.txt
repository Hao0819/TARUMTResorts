TARUMT RESORTS - COMPLETE AI AGENT CONTEXT
BMCS2063 Data Structures and Algorithms Group Assignment

PURPOSE OF THIS FILE

This file is written for an AI coding/review agent that needs to understand the
complete integrated system before suggesting or making changes. Read the entire
file before modifying code. Treat the source code as the final authority if the
README and code ever differ.

AI AGENT RULES

1. Inspect the latest source files before giving code changes.
2. Preserve the Entity-Control-Boundary and DAO package responsibilities.
3. Preserve shared Guest, Room, and Booking object references from Main.
4. Do not use Java collection storage classes such as ArrayList, LinkedList,
   HashMap, Stack, or java.util.Queue.
5. Use ListQueueInterface<T> and DoublyLinkedListQueue<T> for collections.
6. Do not change another member's public method without explaining the impact.
7. Do not duplicate business rules across modules when one shared entity or
   Control is already responsible for them.
8. Maintain strict FIFO for standard Walk-In allocation.
9. Maintain tier priority plus FIFO ties for VIP allocation.
10. Run Maven clean compile after code changes.
11. Clearly distinguish confirmed behaviour, assumptions, and known issues.
12. Keep important source comments in English.

======================================================================
1. PROJECT INFORMATION
======================================================================

System type:
Java console application using Entity-Control-Boundary (ECB), DAO sample
data, and a team-developed generic collection ADT.

Team members and modules:

1. Lim Jun Hao
   Walk-In Booking Requests & Standard Allocation
2. Gan Koh Jun
   Housekeeping & Task Log
3. Tan Keng Ting
   Front-Desk Service
4. Gary Khor Wei Qi
   Loyalty & Rewards Service
5. Brian Lee Kit Mun
   VIP & Loyalty Tier Priority Allocation

Main class:
com.tarumt.resorts.TARUMTResorts

Java version:
JDK 26 (configured by <maven.compiler.release>26</maven.compiler.release>
in pom.xml)

Data storage:
In-memory hard-coded DAO data. Runtime changes are not written to a database
or file. Restarting the application restores the initial DAO data.


======================================================================
2. HOW TO RUN
======================================================================

Method A - Apache NetBeans

1. Open the TARUMTResorts project folder in Apache NetBeans.
2. Confirm that NetBeans uses a JDK supporting Java release 26.
3. Clean and Build the project.
4. Run TARUMTResorts.java or run the Maven project.
5. Use the numbered console menus to operate each module.

Method B - PowerShell with the JDK and Maven bundled with NetBeans

Run from the project root:

$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' clean compile
& "$env:JAVA_HOME\bin\java.exe" -cp target\classes com.tarumt.resorts.TARUMTResorts

Expected compile result:

[INFO] BUILD SUCCESS


======================================================================
3. PROJECT PACKAGE STRUCTURE
======================================================================

src/main/java/com/tarumt/resorts/

adt/
    ListQueueInterface.java
    DoublyLinkedListQueue.java

boundary/
    MainMenuUI.java
    WalkInRegistrationUI.java
    VIPAllocationUI.java
    HouseKeepingUI.java
    FrontDeskUI.java
    LoyaltyRewardsUI.java

control/
    WalkInRegistrationControl.java
    VIPAllocationControl.java
    HousekeepingControl.java
    FrontDeskControl.java
    LoyaltyRewardsControl.java

dao/
    GuestDAO.java
    RoomDAO.java
    BookingDAO.java
    WalkInRegistrationDAO.java
    VIPAllocationDAO.java
    RoomStatusLogDAO.java
    LoyaltyDAO.java

entity/
    Guest.java
    Room.java
    Booking.java
    WalkInRegistration.java
    RegistrationChange.java
    VIPAllocationRequest.java
    RoomStatusLog.java
    StageDuration.java
    MembershipTier.java
    LoyaltyAccount.java
    LoyaltyTransaction.java
    RedemptionRequest.java
    RewardPackage.java


======================================================================
4. ARCHITECTURE
======================================================================

Boundary classes:
- Display menus, tables, reports, prompts, and validation messages.
- Read console input through one shared Scanner.
- Call Control methods and do not own the main business rules.

Control classes:
- Implement module business logic.
- Operate on entity objects stored in the custom ADT.
- Perform searching, filtering, sorting, validation, state changes, and
  cross-entity processing.

Entity classes:
- Represent the business records and their relationships.
- Booking links Guest and Room.
- WalkInRegistration stores its RegistrationChange history in the custom ADT.

DAO classes:
- Create in-memory sample entity objects.
- Return collections through ListQueueInterface<T>.
- Reuse shared Guest, Room, and Booking objects.

TARUMTResorts.java:
- Initializes DAO data once.
- Creates shared collections.
- Passes the same collection references into related Controls.
- Creates each Boundary and starts MainMenuUI.


======================================================================
5. SHARED STATE AND MODULE INTEGRATION
======================================================================

The following collections are initialized once in TARUMTResorts.main():

- sharedRooms
- sharedGuests
- sharedBookings
- sharedRegistrationHistory
- sharedStatusLogs
- sharedLoyaltyAccounts
- sharedLoyaltyTransactions
- sharedVipRequestHistory

Walk-In/VIP -> Booking -> Front-Desk

1. Walk-In or VIP creates a Booking in sharedBookings.
2. Front-Desk can immediately search the same Booking object.
3. Check-in changes CONFIRMED/UNPAID to ACTIVE/PAID.
4. Check-out changes ACTIVE to CHECKED_OUT.

Front-Desk -> Housekeeping

1. Check-out makes the Room physically available.
2. Cleaning status becomes DIRTY.
3. FrontDeskControl calls the shared HousekeepingControl to log DIRTY.
4. A same-day allocation waits until the room is Housekeeping-ready.

Front-Desk -> Loyalty through shared data

1. Front-Desk records a Booking as PAID and CHECKED_OUT.
2. Front-Desk does not create Loyalty points.
3. When the Loyalty menu opens, Loyalty scans sharedBookings.
4. An eligible unprocessed Booking creates one EARN batch.
5. If the Guest has no LoyaltyAccount, Loyalty creates one first.
6. The confirmation number prevents duplicate point awards.

Loyalty -> VIP

- Loyalty recalculates MembershipTier on the shared Guest object.
- VIP reads the same Guest tier for priority allocation.
- PLATINUM, DIAMOND, and ELITE are priority tiers.


======================================================================
6. MAIN MENU
======================================================================

1. Walk-In Booking Requests & Standard Allocation
2. Housekeeping & Task Log
3. Front-Desk Service
4. Loyalty & Rewards Service
5. VIP & Loyalty Tier Priority Allocation
0. Exit


======================================================================
7. WALK-IN BOOKING REQUESTS & STANDARD ALLOCATION
======================================================================

Module owner: Lim Jun Hao

Purpose:
Manage standard booking requests chronologically with strict FIFO behaviour,
date-range availability, allocation, update, cancellation, search, and reports.

Menu:

1. Submit standard room booking request
2. Process next standard booking request
3. View standard booking request queue
4. Walk-In Registration Analysis Report
5. Room-Type Demand and Availability Report
6. Search standard booking requests
7. Update waiting booking request
8. Cancel waiting booking request
0. Back to main menu

Submission flow:

1. Search an existing Guest by Malaysian mobile number.
2. Reuse the Guest or create a new Guest profile.
3. Enter room type, stay duration, and booking month.
4. The calendar checks whether one room can cover the complete stay.
5. A number is a selectable check-in day.
6. X means no single room can cover the complete stay.
7. - means a past date.
8. Add a WAITING WalkInRegistration to history and the FIFO queue.

Date range:

The system uses [check-in date, check-out date). A check-in on 14 August for
3 nights occupies 14, 15, and 16 August and checks out on 17 August.

Housekeeping turnaround buffer (Walk-In only so far):

WalkInRegistrationControl additionally blocks the room's schedule for one
extra day past the existing booking's check-out date, so a new stay cannot
be scheduled to check in on the same calendar day another guest checks out.
This reserves guaranteed time for Housekeeping to complete the
DIRTY -> CLEANING -> INSPECTED -> READY cycle before the next guest arrives.
The rule lives in the shared static helper
com.tarumt.resorts.util.RoomScheduleAvailability.isAvailable(...), used by
WalkInRegistrationControl.isRoomAvailableForSchedule(). VIPAllocationControl
and FrontDeskControl still use their own local copies of the overlap check
WITHOUT this buffer, so as of this writing Walk-In and VIP/Front-Desk can
disagree about whether a room is bookable on its previous guest's check-out
day. Each owner should switch their private overlap method to delegate to
RoomScheduleAvailability.isAvailable(...) so all three modules agree.

Strict FIFO allocation:

1. peek() the front request.
2. Search for a matching room for its complete date range.
3. If no room matches, leave the request at the front.
4. Later standard requests cannot bypass it.
5. Only dequeue() after the Booking is saved successfully.

Successful allocation:

- Creates a CONFIRMED/UNPAID Booking.
- Stores schedule, duration, amount, and confirmation number.
- Changes the registration from WAITING to ASSIGNED.
- Records the change in RegistrationChange history.
- Removes the successful front request.

Update:

- Only WAITING requests can be updated.
- Staff may change room type, check-in date, or stay duration.
- Schedule changes require full-stay availability.
- Successful changes are recorded in the entity's change-history ADT.

Cancellation:

- Uses Registration ID + Guest ID.
- Only WAITING records can be cancelled.
- Status becomes CANCELLED and the active queue entry is removed.
- Other entries keep their FIFO order.
- ASSIGNED records must be handled as Bookings by Front-Desk.

Reports:

- Walk-In Registration Analysis Report filters room type/status and sorts by
  registration time.
- Room-Type Demand and Availability Report summarizes requests, statuses,
  supply, availability, percentages, and console graphs.


======================================================================
8. VIP & LOYALTY TIER PRIORITY ALLOCATION
======================================================================

Module owner: Brian Lee Kit Mun

Purpose:
Let eligible high-tier members bypass the standard FIFO queue while preserving
arrival order among members of the same tier.

Eligible priority:

ELITE > DIAMOND > PLATINUM

Menu:

1. Register new VIP allocation request
2. Allocate next VIP guest
3. View VIP priority queue
4. Update waiting VIP request
5. Cancel waiting VIP request
6. VIP Priority Queue Report
7. VIP Allocation History Report
8. Search Request ID and Guest ID
0. Back to main menu

Priority behaviour:

- priorityEnqueue() inserts higher tiers before lower tiers.
- Equal tiers keep first-come-first-served arrival order.
- Allocation peeks at the highest-priority front request.
- It checks room type, full date range, and same-day operational readiness.
- If no room matches, the request remains at the front.
- Success creates a CONFIRMED/UNPAID Booking and marks the request ASSIGNED.


======================================================================
9. FRONT-DESK SERVICE
======================================================================

Module owner: Tan Keng Ting

Purpose:
Search bookings, answer availability enquiries, perform check-in/check-out,
cancel confirmed bookings, and generate booking and billing reports.

Menu:

1. Search bookings
2. Check room availability now
3. Room availability calendar by month
4. Check in guest
5. Check out guest
6. Cancel booking
7. Booking / Occupancy Report
8. Billing Summary Report
0. Back to main menu

Search:

- Supports confirmation-number and free-text keyword searching.
- Searches Guest, membership, room, status, payment, date, and amount fields.
- searchByKey() performs key-based confirmation lookup.

Check-in:

- Only CONFIRMED Bookings can check in.
- Arrival before the scheduled check-in date is rejected.
- Success records actual check-in time and sets ACTIVE/PAID.
- The Room becomes physically occupied.

Check-out:

- Only ACTIVE Bookings can check out.
- Success records check-out time and sets CHECKED_OUT.
- The Room becomes physically vacant but DIRTY.
- DIRTY is logged in Housekeeping.
- Loyalty later detects the PAID + CHECKED_OUT Booking.

Cancellation:

- Only CONFIRMED Bookings can be cancelled.
- It is a soft cancel: status becomes CANCELLED and history remains.
- ACTIVE Bookings must be checked out instead.
- Availability ignores CANCELLED schedule ranges.

Reports:

- Booking / Occupancy Report filters status and room type.
- Billing Summary Report filters payment/room data, sorts amounts, and totals
  the selected records.


======================================================================
10. HOUSEKEEPING & TASK LOG
======================================================================

Module owner: Gan Koh Jun

Purpose:
Track sequential room-cleaning status, support rollback, synchronize check-out,
and report current status and stage duration.

Menu:

1. Log new room status change
2. View current status of a room
3. Rollback last status change
4. View full history of a room
5. Report 1: Rooms by current status
6. Report 2: Average time per stage
7. Summary Report (Room + Status Log)
0. Back to main menu

Normal cycle:

DIRTY -> CLEANING -> INSPECTED -> READY

- Invalid sequence changes are rejected.
- Front-Desk check-out begins a new DIRTY cycle.
- CLEANING schedules an automatic INSPECTED log after one minute for demo.
- READY ends the cleaning cycle.
- peekLast()/removeLast() provide stack-like rollback using the shared ADT.


======================================================================
11. LOYALTY & REWARDS SERVICE
======================================================================

Module owner: Gary Khor Wei Qi

Purpose:
Manage accounts, automatically award completed-stay points, process redemption,
track independent expiry batches, update tiers, and generate reports.

Menu:

1. Find Loyalty Member
2. Create Loyalty Account
3. View Completed-Stay Point Processing
4. Redeem Rewards
5. Tier and Points Report
6. Activate / Deactivate Account
7. Expiring Points Report
8. Expiring Points Notifications
9. Point Transaction History
0. Return to Main Menu

Automatic synchronization:

- Runs whenever the Loyalty menu is displayed.
- Finds PAID + CHECKED_OUT shared Bookings.
- Ignores Bookings that already have an EARN transaction.
- Creates an account automatically when required.
- Creates one independent EARN batch per eligible Booking.
- Displays previous/new points and tier plus batch/expiry details.

Points:

- RM1 of original gross Booking amount earns 1 point.
- A discount does not reduce points earned.
- One Booking can receive points only once.

Transaction types:

- EARN: points earned from a completed paid Booking.
- REDEEM: points used for a reward.
- EXPIRE: audit record for expired unused points.
- ADJUST: verified imported historical points without a current Booking.

Independent batches:

- Each EARN/eligible ADJUST stores points, remainingPoints, transactionTime,
  and expiryTime.
- expiryTime = transactionTime + 1 year.
- Only the expired batch's unused points are removed.
- Redemption consumes the earliest-expiring batch first using
  priorityEnqueue() and a Comparator.
- Pending requests reserve points to prevent over-submission.

Tier thresholds:

- NONE:       0 - 1,999
- SILVER:     2,000 - 4,999
- GOLD:       5,000 - 9,999
- PLATINUM:  10,000 - 14,999
- DIAMOND:   15,000 - 19,999
- ELITE:     20,000+

- Redemption/expiry reduce redeemable balance, not lifetime qualifying points.
- Inactive accounts cannot earn or redeem.
- Demo inactivity changes tier to NONE after ten minutes; reactivation derives
  the tier from retained qualifying points.

Prepared demo:

- L002 begins at 4,800 SILVER.
- Booking 20260010 is initially unprocessed, CHECKED_OUT, and PAID.
- Entering Loyalty awards 200 points automatically.
- L002 becomes 5,000 GOLD.
- Re-entering does not award the same points again.


======================================================================
12. BOOKING, ROOM, PAYMENT, AND MEMBERSHIP RULES
======================================================================

Room inventory and nightly rate:

- 16 Standard rooms at RM200.00
- 14 Deluxe rooms at RM350.00
- 10 Suite rooms at RM500.00
- Total: 40 rooms

gross amount = daily rate x stay duration in nights

Booking statuses:

- CONFIRMED: allocated but not checked in.
- ACTIVE: checked in and currently in-house.
- CHECKED_OUT: stay completed.
- CANCELLED: soft-cancelled and retained for audit.

Request statuses:

- WAITING: waiting for allocation.
- ASSIGNED: allocation succeeded and a Booking exists.
- CANCELLED: pending request cancelled.

Payment statuses:

- UNPAID: payment has not been collected.
- PAID: payment was collected at Front-Desk check-in.

Membership priority:

- NONE, SILVER, GOLD: standard path.
- PLATINUM, DIAMOND, ELITE: VIP priority path.

Booking room discounts currently defined by MembershipTier:

- NONE 0%, SILVER 5%, GOLD 8%, PLATINUM 10%, DIAMOND 15%, ELITE 20%.

Booking stores gross amount, discount rate, discount amount, and final amount.
Pricing is recalculated when the Booking schedule is created or updated.


======================================================================
13. CUSTOM GENERIC ADT
======================================================================

Interface:      ListQueueInterface<T>
Implementation: DoublyLinkedListQueue<T>

Internal structure:

- Generic doubly linked nodes
- front and rear references
- numberOfEntries counter
- Each node stores data, previous, and next

No Java collection storage classes are used: no ArrayList, LinkedList, HashMap,
Stack, or java.util.Queue.

Iterator, Comparator, Predicate, and NoSuchElementException support algorithms;
they are not used as collection storage.

Methods:

- enqueue(T): add at rear.
- dequeue(): remove front.
- peek(): read front.
- getEntry(int): indexed access from the nearer end.
- getIterator(): front-to-rear traversal.
- contains(T): equality search.
- getNumberOfEntries(): collection size.
- isEmpty()/isFull(): capacity state.
- clear(): reset collection.
- priorityEnqueue(T, Comparator<T>): ordered stable insertion.
- removeLast()/peekLast(): rear rollback behaviour.
- searchByKey(String, KeyExtractor<T>): generic key search.
- filter(Predicate<T>): matching collection with order preserved.


======================================================================
14. ADT USE BY MODULE
======================================================================

Walk-In:
- enqueue(), peek(), dequeue() implement strict FIFO.
- getIterator(), searchByKey(), and filter() support search/reports.

VIP:
- priorityEnqueue() implements tier order.
- peek()/dequeue() process the highest-priority request.

Housekeeping:
- enqueue() appends status logs.
- peekLast()/removeLast() implement rollback.

Front-Desk:
- searchByKey() finds confirmation numbers.
- Iterator traversal supports availability, free-text search, and reports.

Loyalty:
- filter() supports account, tier, expiry, and history reports.
- priorityEnqueue() supports earliest-expiry redemption.
- searchByKey() finds accounts, Guests, Bookings, and EARN batches.
- enqueue() stores account, transaction, and redemption-request records.

Entity-level use:

WalkInRegistration contains:

ListQueueInterface<RegistrationChange> changeHistory =
        new DoublyLinkedListQueue<>();

This demonstrates ADT usage in both Entity and Control layers.


======================================================================
15. ADT TIME AND SPACE COMPLEXITY
======================================================================

Operation                              Complexity
------------------------------------------------------------------
enqueue(), dequeue(), peek()            O(1)
peekLast(), removeLast()                O(1)
getNumberOfEntries(), isEmpty()         O(1)
isFull()                                O(1)
clear()                                 O(1) reference reset
getEntry(position)                      O(min(position, n-position))
contains()                              O(n)
searchByKey()                           O(n), best case O(1)
filter()                                O(n) time, O(k) result nodes
priorityEnqueue()                       O(n), best case O(1)
complete Iterator traversal             O(n) time, O(1) extra space

Collection storage space is O(n).


======================================================================
16. ENTITY RELATIONSHIPS
======================================================================

Guest -> WalkInRegistration -> Booking -> Room
Guest -> VIPAllocationRequest -> Booking -> Room
Guest -> LoyaltyAccount -> LoyaltyTransaction -> Booking
Room -> RoomStatusLog

These dependencies allow reports to combine multiple entity classes instead of
showing isolated static values.


======================================================================
17. SAMPLE DAO DATA
======================================================================

- 40 Guests
- 40 Rooms
- 60 Bookings
- 20 Walk-In registration-history records
- 7 VIP allocation-history records
- 20 initial Loyalty accounts
- Multiple Loyalty EARN/ADJUST/EXPIRE batches
- Seeded Housekeeping status logs

Booking samples include CONFIRMED, ACTIVE, CHECKED_OUT, and CANCELLED records so
reports, overlap checks, Front-Desk, and Loyalty can be demonstrated directly.


======================================================================
18. RECOMMENDED INTEGRATED DEMO
======================================================================

Walk-In:
1. View the queue before changes.
2. Submit a request and show the full-stay calendar.
3. Search by Registration ID.
4. Update a field and display change history.
5. Process the front and display the CONFIRMED Booking.
6. Show queue order after success or strict FIFO blocking.
7. Display both reports.

VIP:
1. Add requests from different tiers.
2. Show ELITE > DIAMOND > PLATINUM and FIFO ties.
3. Allocate the front request.
4. Display both reports.

Front-Desk:
1. Search an eight-digit confirmation number.
2. Show room/calendar availability.
3. Check in a CONFIRMED Booking and show ACTIVE/PAID.
4. Check it out and show CHECKED_OUT.
5. Show the Room as DIRTY in Housekeeping.
6. Display both reports.

Housekeeping:
1. Display a DIRTY room received from Front-Desk.
2. Log CLEANING and demonstrate automatic INSPECTED.
3. Complete READY or demonstrate rollback.
4. Display status/duration/summary reports.

Loyalty:
1. Enter Loyalty and show Booking 20260010 automatic processing.
2. Show L002: 4,800 SILVER -> 5,000 GOLD.
3. Use Option 3 to view the EARN without adding points again.
4. Re-enter and prove duplicate prevention.
5. Show separate expiry batches and earliest-expiry redemption.
6. Display Loyalty reports and transaction history.


======================================================================
19. VALIDATION AND BUSINESS SAFETY
======================================================================

- Guest names cannot be blank.
- Malaysian mobile numbers use configured digit/start validation.
- Email uses basic name@domain validation.
- Room types are Standard, Deluxe, or Suite.
- Stay duration is normally 1-30 nights.
- Availability checks the entire stay.
- Confirmation numbers are unique eight-digit values.
- Walk-In cancellation verifies Registration ID + Guest ID.
- Booking cancellation is restricted by status.
- A Booking cannot receive Loyalty points twice.
- Invalid Housekeeping transitions are rejected.
- Queue rebuilds preserve unaffected entry order.


======================================================================
20. IMPORTANT NOTES AND CURRENT LIMITATIONS
======================================================================

1. In-memory data
   Runtime changes disappear after exit. This is expected for the assignment.

2. Strict FIFO blocking
   A front standard request without a matching room blocks later standard
   requests. This is intended FIFO behaviour.

3. Calendar meaning
   A selectable date is valid for the complete selected stay, not only one day.

4. Payment timing
   Allocation creates CONFIRMED/UNPAID. Payment is collected at check-in.

5. Loyalty timing
   Loyalty processes new completed paid Bookings when its menu opens.

6. Demo timers
   Housekeeping auto-inspection and Loyalty near-expiry data use short demo
   timing. Normal Loyalty EARN batches use a one-year policy.

7. Discount consistency (resolved)
   MembershipTier is now the single source for room discount rates:
   NONE 0%, SILVER 5%, GOLD 8%, PLATINUM 10%, DIAMOND 15%, ELITE 20%.

   LoyaltyRewardsControl.getRoomDiscountRate() delegates directly to
   MembershipTier.getRoomDiscountRate() instead of keeping its own table, so
   Booking.finalAmount and the Loyalty payment preview always agree.

8. Front-Desk check-in does not verify Housekeeping cleaning status
   FrontDeskControl.checkInBooking() checks only that the Booking is
   CONFIRMED and not arriving early - it never checks Room.getCleaningStatus().
   A guest can therefore be checked into (ACTIVE) a room whose Housekeeping
   status is still DIRTY, if the room became dirty after allocation, or if a
   future-dated booking's target date arrives before Housekeeping actually
   finishes cleaning it. Walk-In/VIP only check cleaning status at allocation
   time, and only for a same-day request. Not yet fixed - the fix belongs in
   FrontDeskControl.checkInBooking(), owned by the Front-Desk module.

9. Room/date overlap rule not yet consistent across all three allocation
   paths (open team task)
   WalkInRegistrationControl now delegates its schedule-overlap check to the
   shared util.RoomScheduleAvailability.isAvailable(...), which also adds a
   1-day Housekeeping turnaround buffer past each booking's check-out date
   (see the Walk-In "Housekeeping turnaround buffer" note above).
   VIPAllocationControl.isRoomAvailableForSchedule() and
   FrontDeskControl.isRoomTakenInRange() still keep their own local copies of
   the same rule WITHOUT the buffer. Until each owner switches their private
   method to call RoomScheduleAvailability.isAvailable(...) instead, Walk-In
   can disagree with VIP/Front-Desk about whether a room is bookable on its
   previous guest's check-out day.


======================================================================
21. REPORT PREPARATION
======================================================================

- Copy Control source code as text, not screenshots.
- Use font size 11 for source listings.
- Include author names in each member's Control and Entity classes.
- Mark ADT declarations and interface method calls in blue in the report.
- Explain why each module selected its ADT behaviour.
- Explain multi-entity dependencies in each report.
- Include before/after output for add, edit, cancel/remove, display, search,
  filter, allocation, check-in/out, rollback, redemption, and expiry.
- Include two or more summary reports where required.
- Complete the required AI usage disclosure honestly.

Example declaration:

ListQueueInterface<WalkInRegistration> registrationQueue =
        new DoublyLinkedListQueue<>(); // ADT collection declaration

Example calls:

registrationQueue.enqueue(registration); // ADT method called: enqueue()
registrationQueue.peek();                 // ADT method called: peek()
registrationQueue.dequeue();              // ADT method called: dequeue()


======================================================================
22. BUILD VERIFICATION
======================================================================

Last verified after the Loyalty integration updates:

- Maven clean compile: BUILD SUCCESS
- Main menu startup: successful
- L002 automatic processing and SILVER -> GOLD: successful
- Duplicate EARN prevention: successful
- Completed-stay processing lookup: successful
- Seeded SILVER/GOLD Loyalty tiers: successful

Before submission, every member should test the integrated project from a clean
start on the same main branch.


======================================================================
END OF README
======================================================================
