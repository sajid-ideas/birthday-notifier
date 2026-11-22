package com.family.birthday;

/** Something that can deliver a text message to a member. */
public interface Notifier {
    /** @return true if the message was accepted for delivery */
    boolean send(Member recipient, String message);
}
