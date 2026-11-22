package com.family.birthday;

/** Dry-run notifier: prints what would be sent instead of sending it. */
public class ConsoleNotifier implements Notifier {
    @Override
    public boolean send(Member recipient, String message) {
        System.out.printf("  [DRY RUN] to %s (%s):%n    %s%n", recipient.name(), mask(recipient.phone()),
                message.replace("\n", "\n    "));
        return true;
    }

    static String mask(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return "******" + phone.substring(phone.length() - 4);
    }
}
