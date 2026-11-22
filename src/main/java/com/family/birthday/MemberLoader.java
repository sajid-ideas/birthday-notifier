package com.family.birthday;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads members from CSV text:
 *
 * <pre>
 * name,birthday,phone,apikey
 * Amy,22-11-1995,+919876543210,123456
 * Rahul,05-03,+919812345678,
 * </pre>
 *
 * birthday is DD-MM-YYYY or DD-MM (year optional). Blank apikey = person has not
 * activated CallMeBot yet, so they are skipped as a recipient (their birthday is still announced).
 * Lines starting with # and the header line are ignored.
 */
public final class MemberLoader {

    private MemberLoader() {}

    public static List<Member> fromFile(Path path) throws IOException {
        return fromCsv(Files.readString(path));
    }

    public static List<Member> fromCsv(String csv) {
        List<Member> members = new ArrayList<>();
        String[] lines = csv.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty() || line.startsWith("#") || line.toLowerCase().startsWith("name,")) {
                continue;
            }
            String[] cols = line.split(",", -1);
            if (cols.length < 2) {
                throw new IllegalArgumentException("Line " + (i + 1) + ": expected name,birthday,phone,apikey but got: " + line);
            }
            String name = cols[0].strip();
            String birthdayText = cols[1].strip();
            String phone = cols.length > 2 ? cols[2].strip() : "";
            String apiKey = cols.length > 3 ? cols[3].strip() : "";

            String[] parts = birthdayText.split("[-/.]");
            if (parts.length < 2 || parts.length > 3) {
                throw new IllegalArgumentException("Line " + (i + 1) + ": birthday must be DD-MM or DD-MM-YYYY, got: " + birthdayText);
            }
            try {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
                members.add(new Member(name, MonthDay.of(month, day), year, phone, apiKey));
            } catch (NumberFormatException | DateTimeException e) {
                throw new IllegalArgumentException("Line " + (i + 1) + ": invalid birthday '" + birthdayText + "' for " + name, e);
            }
        }
        return members;
    }
}
