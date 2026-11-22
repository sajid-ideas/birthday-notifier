package com.family.birthday;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.OptionalInt;

/**
 * One family member.
 *
 * @param name        display name, e.g. "Amy"
 * @param birthday    day and month of birth
 * @param birthYear   optional year of birth (0 if unknown) - used to show the age
 * @param phone       WhatsApp number with country code, e.g. +919876543210
 * @param apiKey      this person's CallMeBot API key; blank = not opted in, no messages sent
 */
public record Member(String name, MonthDay birthday, int birthYear, String phone, String apiKey) {

    public boolean canReceiveWhatsApp() {
        return apiKey != null && !apiKey.isBlank() && phone != null && !phone.isBlank();
    }

    /** True if this member's birthday falls on the given date. Feb 29 birthdays are celebrated on Feb 28 in non-leap years. */
    public boolean hasBirthdayOn(LocalDate date) {
        if (birthday.equals(MonthDay.of(2, 29)) && !date.isLeapYear()) {
            return date.getMonthValue() == 2 && date.getDayOfMonth() == 28;
        }
        return birthday.equals(MonthDay.from(date));
    }

    public OptionalInt ageOn(LocalDate date) {
        return birthYear > 0 ? OptionalInt.of(date.getYear() - birthYear) : OptionalInt.empty();
    }
}
