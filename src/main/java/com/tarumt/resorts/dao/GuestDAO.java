package com.tarumt.resorts.dao;

import com.tarumt.resorts.entity.Guest;
import com.tarumt.resorts.entity.MembershipTier;
import com.tarumt.resorts.adt.DoublyLinkedListQueue;
import com.tarumt.resorts.adt.ListQueueInterface;

/**
 * GuestDAO.java
 * Returns a collection pre-filled with hard-coded sample Guest entities.
 * Per tutor clarification: no file/database I/O, just sample data.
 *
 * @author Junhao
 */
public class GuestDAO {

        public ListQueueInterface<Guest> getAllGuests() {
                ListQueueInterface<Guest> guests = new DoublyLinkedListQueue<>();
                guests.enqueue(new Guest(
                                "G001", "Amy Tan", "012-3456789", "amy@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G002", "Ben Lee", "013-4567890", "ben@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G003", "Cara Wong", "014-5678901", "cara@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G004", "David Ho", "016-6789012", "david@mail.com",
                                MembershipTier.PLATINUM));

                guests.enqueue(new Guest(
                                "G005", "Ella Ng", "017-7890123", "ella@mail.com",
                                MembershipTier.DIAMOND));

                guests.enqueue(new Guest(
                                "G006", "Farah Ismail", "018-8901234", "farah@mail.com",
                                MembershipTier.ELITE));

                guests.enqueue(new Guest(
                                "G007", "George Tan", "019-9012345", "george@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G008", "Hana Yusof", "011-0123456", "hana@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G009", "Ivan Chong", "012-1234567", "ivan@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G010", "Jasmine Lim", "013-2345678", "jasmine@mail.com",
                                MembershipTier.PLATINUM));

                guests.enqueue(new Guest(
                                "G011", "Kumar Raj", "014-3456789", "kumar@mail.com",
                                MembershipTier.DIAMOND));

                guests.enqueue(new Guest(
                                "G012", "Lily Chen", "016-4567890", "lily@mail.com",
                                MembershipTier.ELITE));

                guests.enqueue(new Guest(
                                "G013", "Mohan Das", "017-5678901", "mohan@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G014", "Nurul Aina", "018-6789012", "nurul@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G015", "Oscar Teoh", "019-7890123", "oscar@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G016", "Priya Sharma", "011-8901234", "priya@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G017", "Qistina Rahman", "012-9012345", "qistina@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G018", "Ryan Goh", "013-0123456", "ryan@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G019", "Siti Aisyah", "014-1234567", "siti@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G020", "Tan Wei Ming", "016-2345678", "tanwm@mail.com",
                                MembershipTier.GOLD));
                // Additional non-member guests used by booking and reporting data.
                guests.enqueue(new Guest(
                                "G021", "Aiman Zaki", "010-1000021", "aiman@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G022", "Alicia Ong", "010-1000022", "alicia@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G023", "Brandon Chua", "010-1000023", "brandon@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G024", "Cheryl Low", "010-1000024", "cheryl@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G025", "Daniel Wong", "010-1000025", "daniel@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G026", "Emma Lau", "010-1000026", "emma@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G027", "Faisal Ahmad", "010-1000027", "faisal@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G028", "Grace Yap", "010-1000028", "grace@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G029", "Harith Omar", "010-1000029", "harith@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G030", "Irene Chew", "010-1000030", "irene@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G031", "Jason Ng", "010-1000031", "jason@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G032", "Kavitha Nair", "010-1000032", "kavitha@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G033", "Lucas Teh", "010-1000033", "lucas@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G034", "Mei Xin", "010-1000034", "meixin@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G035", "Nadia Aziz", "010-1000035", "nadia@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G036", "Owen Lim", "010-1000036", "owen@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G037", "Pui Yee", "010-1000037", "puiyee@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G038", "Raymond Chan", "010-1000038", "raymond@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G039", "Sarah Lee", "010-1000039", "sarah@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G040", "Thinesh Kumar", "010-1000040", "thinesh@mail.com",
                                MembershipTier.NONE));
                return guests;
        }
}