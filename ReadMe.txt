TARUMT RESORTS - INTEGRATED RESORT MANAGEMENT SYSTEM
BMCS2063 Data Structures and Algorithms Group Assignment

======================================================================
TEAM MEMBERS AND MODULES
======================================================================

Lim Jun Hao
Walk-In Booking Requests & Standard Allocation

Gan Koh Jun
Housekeeping & Task Log

Tan Keng Ting
Front-Desk Service

Gary Khor Wei Qi
Loyalty & Rewards Service

Brian Lee Kit Mun
VIP & Loyalty Tier Priority Allocation


======================================================================
PROJECT OVERVIEW
======================================================================

TARUMT Resorts is a Java console application that integrates standard room
booking requests, VIP priority allocation, front-desk operations,
housekeeping, and loyalty rewards.

The project uses:

- Entity-Control-Boundary (ECB) architecture
- DAO classes containing initialized demonstration data
- A team-developed generic collection ADT
- Shared entity references for integration between modules
- Console tables, calendars, searches, filters, updates, cancellations,
  allocation functions, and summary reports

The project does not use Java collection storage classes such as ArrayList,
LinkedList, HashMap, Stack, or java.util.Queue.


======================================================================
SYSTEM REQUIREMENTS AND EXECUTION
======================================================================

Java version:
JDK 26

Main class:
com.tarumt.resorts.TARUMTResorts

NetBeans execution:

1. Open the TARUMTResorts Maven project in Apache NetBeans.
2. Confirm that the project uses JDK 26.
3. Select Clean and Build Project.
4. Run TARUMTResorts.java.
5. Select a module from the main console menu.

PowerShell execution:

$env:JAVA_HOME = 'C:\Program Files\Apache NetBeans\jdk'
& 'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd' clean compile
& "$env:JAVA_HOME\bin\java.exe" -cp target\classes com.tarumt.resorts.TARUMTResorts


======================================================================
CUSTOM COLLECTION ADT
======================================================================

Interface:
ListQueueInterface<T>

Implementation:
DoublyLinkedListQueue<T>

The implementation uses doubly linked generic nodes with front and rear
references. It combines the collection behaviours needed by the integrated
modules through one interface and one implementation class.

Main operations:

- enqueue(T)
- dequeue()
- peek()
- getEntry(int)
- getIterator()
- contains(T)
- getNumberOfEntries()
- isEmpty()
- isFull()
- clear()
- priorityEnqueue(T, Comparator<T>)
- removeLast()
- peekLast()
- searchByKey(String, KeyExtractor<T>)
- filter(Predicate<T>)

Main operation complexity:

- enqueue/dequeue/peek: O(1)
- removeLast/peekLast: O(1)
- getEntry: O(min(position, n-position))
- contains/searchByKey/filter: O(n)
- priorityEnqueue: O(n)
- Complete iterator traversal: O(n)
- Collection storage: O(n)

Module-specific ADT use:

- Walk-In uses FIFO enqueue, peek, and dequeue.
- VIP uses comparator-based priorityEnqueue.
- Housekeeping uses rear access/removal for rollback.
- Front-Desk uses key-based searching and traversal.
- Loyalty uses filter, priorityEnqueue, searching, and FIFO redemption requests.
- WalkInRegistration uses an ADT attribute to store RegistrationChange history.


======================================================================
MODULE 1 - WALK-IN BOOKING REQUESTS & STANDARD ALLOCATION
======================================================================

The module manages standard reservation requests in chronological FIFO order.

Functions:

1. Submit a standard booking request.
2. Display a date-availability calendar for the complete stay.
3. Process the front request using strict FIFO.
4. Display the active waiting queue.
5. Search by Registration ID or Guest ID.
6. Update room type, check-in date, or stay duration.
7. Record update/status changes in the entity's ADT history.
8. Cancel a WAITING request using Registration ID + Guest ID.
9. Generate registration-analysis and room-demand reports.

FIFO rule:

The system peeks at the front request, searches for a suitable room, and only
dequeues after the Booking is saved successfully. If no room is available, the
front request remains and later standard requests do not bypass it.

Successful allocation creates a CONFIRMED and UNPAID Booking. Actual check-in
and payment are handled by Front-Desk.


======================================================================
MODULE 2 - VIP & LOYALTY TIER PRIORITY ALLOCATION
======================================================================

The module prioritizes eligible Loyalty members:

ELITE > DIAMOND > PLATINUM

Requests with the same tier retain their FIFO arrival order. The module uses
priorityEnqueue with a Comparator, validates full-stay room availability, and
creates a CONFIRMED/UNPAID Booking after successful allocation.

Functions include registration, priority queue display, allocation, update,
cancellation, searching, priority reports, and allocation-history reports.


======================================================================
MODULE 3 - FRONT-DESK SERVICE
======================================================================

The module searches shared Bookings and handles guest arrival and departure.

Functions:

- Confirmation-number and keyword search
- Current room availability
- Monthly availability calendar
- Check-in
- Check-out
- Confirmed-booking cancellation
- Booking/occupancy report
- Billing summary report

Check-in changes a valid CONFIRMED/UNPAID Booking to ACTIVE/PAID and marks the
Room occupied. Early arrival before the scheduled date is rejected.

Check-out changes ACTIVE to CHECKED_OUT, records the actual time, makes the Room
vacant, and creates a DIRTY Housekeeping record.


======================================================================
MODULE 4 - HOUSEKEEPING & TASK LOG
======================================================================

The normal cleaning sequence is:

DIRTY -> CLEANING -> INSPECTED -> READY

The module validates each transition, records room status history, displays
current status, performs rollback using peekLast/removeLast, and generates
status, stage-duration, and summary reports.

For demonstration, CLEANING automatically progresses to INSPECTED after one
minute. Front-Desk check-out begins a new DIRTY cycle.


======================================================================
MODULE 5 - LOYALTY & REWARDS SERVICE
======================================================================

The module manages accounts, points, tiers, redemptions, expiry, notifications,
and reports.

When the Loyalty menu opens, it scans the shared Booking collection. Every
unprocessed PAID + CHECKED_OUT Booking creates one independent EARN batch. A
Loyalty account is created automatically if the Guest does not have one.

Point rule:

RM1 of original gross Booking amount = 1 point

Each earning batch stores its own transaction time, remaining points, and
expiry time. The normal expiry policy is one year from the earning time.
Redemption consumes the earliest-expiring usable batch first.

Tier thresholds:

- NONE: 0-1,999
- SILVER: 2,000-4,999
- GOLD: 5,000-9,999
- PLATINUM: 10,000-14,999
- DIAMOND: 15,000-19,999
- ELITE: 20,000+

Prepared demonstration:

L002 begins at 4,800 SILVER. Processing completed Booking 20260010 awards
200 points, changes the total to 5,000, and upgrades the member to GOLD.
Re-entering the module does not award the Booking twice.


======================================================================
INTEGRATED SHARED-OBJECT FLOW
======================================================================

Walk-In/VIP
    -> creates shared CONFIRMED Booking

Front-Desk
    -> checks in Booking
    -> records PAID and ACTIVE
    -> checks out Booking
    -> records CHECKED_OUT

Housekeeping
    -> receives DIRTY room after check-out
    -> completes cleaning sequence

Loyalty
    -> scans shared completed paid Booking
    -> creates EARN batch
    -> updates points and shared Guest tier

VIP
    -> reads the updated shared Guest tier for future priority requests


======================================================================
ENTITY RELATIONSHIPS
======================================================================

Guest -> WalkInRegistration -> Booking -> Room
Guest -> VIPAllocationRequest -> Booking -> Room
Guest -> LoyaltyAccount -> LoyaltyTransaction -> Booking
Room -> RoomStatusLog

The reports combine two or more related entity classes rather than displaying
isolated static data.


======================================================================
INITIALIZED DEMONSTRATION DATA
======================================================================

- 40 Guest records
- 40 Room records
- 60 Booking records
- 20 Walk-In registration-history records
- 7 VIP allocation-history records
- 20 Loyalty accounts with multiple transaction batches
- Housekeeping status-log records

The data includes WAITING, ASSIGNED, CONFIRMED, ACTIVE, CHECKED_OUT, CANCELLED,
PAID, UNPAID, room availability, cleaning stages, and multiple membership tiers
so the module functions and reports can be demonstrated immediately.

