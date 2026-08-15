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
                                "G001", "Ali", "012-3456789", "ali@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G002", "Lee Boon Yew", "013-4567890", "boon@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G003", "Brian Kam", "014-5678901", "brianKam@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G004", "Edward Tan Keng Ting", "016-6789012", "EdwardTan@mail.com",
                                MembershipTier.PLATINUM));

                guests.enqueue(new Guest(
                                "G005", "Isaac", "017-7890123", "isaac@mail.com",
                                MembershipTier.DIAMOND));

                guests.enqueue(new Guest(
                                "G006", "Jerry", "018-8901234", "jerry@mail.com",
                                MembershipTier.ELITE));

                guests.enqueue(new Guest(
                                "G007", "Gary", "019-9012345", "gary@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G008", "Koh Jun", "011-0123456", "kohjun@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G009", "Kai Xian", "012-1234567", "kai@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G010", "You Jing Hong", "013-2345678", "jinghong@mail.com",
                                MembershipTier.PLATINUM));

                guests.enqueue(new Guest(
                                "G011", "Tee Teck Lee", "014-3456789", "tecklee@mail.com",
                                MembershipTier.DIAMOND));

                guests.enqueue(new Guest(
                                "G012", "Chong Jian Min", "016-4567890", "jianmin@mail.com",
                                MembershipTier.ELITE));

                guests.enqueue(new Guest(
                                "G013", "Sarah Khoo", "017-5678901", "sarahKhoo@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G014", "Wo Xiao Bing", "018-6789012", "xiaoBing@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G015", "Brian Lee Kit Mun", "019-7890123", "brianlee@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G016", "Tee Yik Wah", "011-8901234", "yikwah@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G017", "Wilson Ang", "012-9012345", "wilsonAng@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G018", "Yeong Wei Kin", "013-0123456", "weikin@mail.com",
                                MembershipTier.GOLD));

                guests.enqueue(new Guest(
                                "G019", "Chia Kah Shun", "014-1234567", "kahshun@mail.com",
                                MembershipTier.SILVER));

                guests.enqueue(new Guest(
                                "G020", "Lee Shen Fung", "016-2345678", "shenfung@mail.com",
                                MembershipTier.GOLD));
                // Additional non-member guests used by booking and reporting data.
                guests.enqueue(new Guest(
                                "G021", "Tan See Tian", "010-1000021", "seetian@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G022", "Sim Heng Sheng", "010-1000022", "hengsheng@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G023", "Yao Soon Han", "010-1000023", "soonhan@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G024", "Cheryl Low", "010-1000024", "cheryl@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G025", "Liu Zheng Yu", "010-1000025", "zhengyu@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G026", "Isaac Mok", "010-1000026", "isaacMok@mail.com",
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
                                "G033", "Tan Yit Shen", "010-1000033", "yitshen@mail.com",
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
                                "G038", "Keat Seng", "010-1000038", "keatseng@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G039", "Lim Le Yee", "010-1000039", "limleyee@mail.com",
                                MembershipTier.NONE));

                guests.enqueue(new Guest(
                                "G040", "Thinesh Kumar", "010-1000040", "thinesh@mail.com",
                                MembershipTier.NONE));
                return guests;
        }
}