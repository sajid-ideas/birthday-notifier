package com.family.birthday;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Builds the WhatsApp texts. Edit the wording here. */
public final class MessageBuilder {

    private MessageBuilder() {}

    /** Message for everyone who is NOT celebrating today. */
    public static String reminder(List<Member> celebrants, LocalDate date) {
        String who = celebrants.stream()
                .map(m -> m.ageOn(date).isPresent()
                        ? "*" + m.name() + "* (turns " + m.ageOn(date).getAsInt() + ")"
                        : "*" + m.name() + "*")
                .collect(joinNatural());
        return "🎂 Family Birthday Alert 🎂\n\n"
                + "Today is the birthday of " + who + "!\n"
                + "Don't forget to call or send your wishes. 🎉";
    }

    /** Message for the birthday person themselves. */
    public static String wish(Member celebrant) {
        return "🎉 Happy Birthday, " + celebrant.name() + "! 🎂\n\n"
                + "Wishing you a wonderful year ahead, full of happiness and good health.\n"
                + "With love, from the whole family ❤️";
    }

    private static java.util.stream.Collector<String, ?, String> joinNatural() {
        return Collectors.collectingAndThen(Collectors.toList(), list -> {
            if (list.size() <= 1) return String.join("", list);
            return String.join(", ", list.subList(0, list.size() - 1)) + " and " + list.get(list.size() - 1);
        });
    }
}
