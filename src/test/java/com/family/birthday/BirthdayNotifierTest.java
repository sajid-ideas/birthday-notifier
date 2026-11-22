package com.family.birthday;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BirthdayNotifierTest {

    private static final String CSV = """
            name,birthday,phone,apikey
            # comment line
            Amy,22-11-1995,+919876543210,111111
            Rahul,05-03,+919812345678,
            Leap,29-02-2000,+919800000000,222222
            """;

    @Test
    void parsesCsv() {
        List<Member> members = MemberLoader.fromCsv(CSV);
        assertEquals(3, members.size());
        assertEquals("Amy", members.get(0).name());
        assertTrue(members.get(0).canReceiveWhatsApp());
        assertFalse(members.get(1).canReceiveWhatsApp());
        assertEquals(0, members.get(1).birthYear());
    }

    @Test
    void matchesBirthdayAndAge() {
        Member amy = MemberLoader.fromCsv(CSV).get(0);
        LocalDate day = LocalDate.of(2026, 11, 22);
        assertTrue(amy.hasBirthdayOn(day));
        assertFalse(amy.hasBirthdayOn(day.plusDays(1)));
        assertEquals(31, amy.ageOn(day).getAsInt());
    }

    @Test
    void leapDayBirthdayFallsOnFeb28InNormalYears() {
        Member leap = MemberLoader.fromCsv(CSV).get(2);
        assertTrue(leap.hasBirthdayOn(LocalDate.of(2027, 2, 28)));
        assertTrue(leap.hasBirthdayOn(LocalDate.of(2028, 2, 29)));
        assertFalse(leap.hasBirthdayOn(LocalDate.of(2028, 2, 28)));
    }

    @Test
    void reminderListsEveryone() {
        List<Member> m = MemberLoader.fromCsv(CSV);
        String text = MessageBuilder.reminder(List.of(m.get(0), m.get(1)), LocalDate.of(2026, 11, 22));
        assertTrue(text.contains("*Amy* (turns 31) and *Rahul*"));
    }

    @Test
    void rejectsBadDate() {
        assertThrows(IllegalArgumentException.class, () -> MemberLoader.fromCsv("Bob,31-02,+91,1"));
    }
}
