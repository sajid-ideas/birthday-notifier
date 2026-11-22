package com.family.birthday;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Entry point. Run once a day at 00:00 (India time). If anyone's birthday is today,
 * every opted-in family member gets a WhatsApp reminder and the birthday person gets a wish.
 *
 * Configuration (environment variables):
 *   MEMBERS_CSV         CSV content (used on GitHub Actions, stored as a secret)
 *   MEMBERS_FILE        path to a CSV file (used locally), default: members.csv
 *   DRY_RUN             "true" = print messages instead of sending
 *   TIMEZONE            default Asia/Kolkata
 *   DATE_OVERRIDE       YYYY-MM-DD, pretend today is this date (for testing)
 *   SEND_DELAY_SECONDS  pause between messages, default 5 (be gentle with the free API)
 */
public class BirthdayNotifierApp {

    public static void main(String[] args) throws Exception {
        ZoneId zone = ZoneId.of(env("TIMEZONE", "Asia/Kolkata"));
        String override = env("DATE_OVERRIDE", "");
        LocalDate today = override.isBlank() ? LocalDate.now(zone) : LocalDate.parse(override);
        boolean dryRun = Boolean.parseBoolean(env("DRY_RUN", "false"));
        int delay = Integer.parseInt(env("SEND_DELAY_SECONDS", "5"));

        String csv = env("MEMBERS_CSV", "");
        List<Member> members = csv.isBlank()
                ? MemberLoader.fromFile(Path.of(env("MEMBERS_FILE", "members.csv")))
                : MemberLoader.fromCsv(csv);

        System.out.printf("Date: %s (%s) | members: %d | dry run: %s%n", today, zone, members.size(), dryRun);

        List<Member> celebrants = members.stream().filter(m -> m.hasBirthdayOn(today)).toList();
        if (celebrants.isEmpty()) {
            System.out.println("No birthdays today. Nothing to send.");
            return;
        }
        System.out.println("Birthday today: " + celebrants.stream().map(Member::name).toList());

        Notifier notifier = dryRun ? new ConsoleNotifier() : new CallMeBotWhatsAppNotifier();
        String reminder = MessageBuilder.reminder(celebrants, today);

        int sent = 0, failed = 0, skipped = 0;
        for (Member m : members) {
            if (!m.canReceiveWhatsApp()) {
                skipped++;
                continue;
            }
            String text = celebrants.contains(m) ? MessageBuilder.wish(m) : reminder;
            if (notifier.send(m, text)) {
                sent++;
                System.out.println("  ✔ sent to " + m.name());
            } else {
                failed++;
                System.out.println("  ✘ failed for " + m.name());
            }
            if (!dryRun) CallMeBotWhatsAppNotifier.sleep(delay);
        }

        System.out.printf("Done. sent=%d failed=%d skipped(not opted in)=%d%n", sent, failed, skipped);
        if (sent == 0 && failed > 0) {
            System.exit(1); // makes the GitHub Actions run show red so you notice
        }
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return v == null ? def : v.strip();
    }
}
