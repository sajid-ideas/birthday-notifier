# Family Birthday Notifier 🎂

Every night at **12:00 AM (India time)** this checks whether anyone in the family has a birthday that day. If someone does:

- **everyone in the family** who has opted in gets a WhatsApp message: *"Today is the birthday of Amy (turns 31)! Don't forget to send your wishes"*
- **the birthday person** gets a "Happy Birthday" wish instead

It's written in Java 17 with no runtime libraries. WhatsApp messages go through the free **CallMeBot** API, and the job runs free on **GitHub Actions**, so you don't need a server.

---

## 1. Each family member activates CallMeBot (one time, about 1 minute)

CallMeBot's free API can only message people who have opted in themselves. Each person who wants the messages does this from **their own phone**:

1. Open the official guide at https://www.callmebot.com/blog/free-api-whatsapp-messages/ and save the **CallMeBot phone number** shown there as a contact. (The number changes sometimes, so always copy it from that page.)
2. Send that contact this WhatsApp message: `I allow callmebot to send me messages`
3. The bot replies with an **API key** (a number like `123456`). They send that key to you.

If someone doesn't get a key within a couple of minutes, CallMeBot says to try again after 24 hours.

People who skip this still have their birthdays announced to everyone else. They just won't receive messages themselves.

> CallMeBot's free service is for personal use. A family group is fine, but don't use it for a business.

## 2. Fill in the family list

The format (see `members.csv` for an example):

```
name,birthday,phone,apikey
Amy,22-11-1995,+919876543210,123456
Rahul,05-03,+919812345678,654321
Nani,14-08-1950,+919800000000,
```

| Column   | Format | Notes |
|----------|--------|-------|
| name     | text   | No commas |
| birthday | `DD-MM-YYYY` or `DD-MM` | The year is optional. If given, the message shows their age |
| phone    | `+91XXXXXXXXXX` | The WhatsApp number, with country code |
| apikey   | the person's CallMeBot key | Leave blank if they haven't activated |

Someone born on 29 February gets their message on 28 February in non-leap years.

⚠️ The real list holds phone numbers and keys, so **don't commit it to GitHub**. Store it as a secret (step 3).

## 3. Deploy for free on GitHub Actions

1. Create a **private** repository on GitHub and push this project to it.
   (A private repo includes 2,000 free minutes a month. This job uses about 1–5 minutes a day.)
2. In the repo, go to **Settings → Secrets and variables → Actions → New repository secret**.
   - Name: `MEMBERS_CSV`
   - Value: paste the **entire contents** of your real CSV, including the header line.
3. Go to the **Actions** tab and enable workflows if GitHub asks.
4. **Test it:** choose **Daily birthday notifier → Run workflow**, enter a date that matches someone's birthday (e.g. `2026-11-22`), and leave **Dry run** ticked. The log shows every message it *would* send.
5. When the dry run looks right, run it again with **Dry run** unticked to send real messages. Try it with a list that only has your own number first.

That's all. It runs by itself every night at 18:30 UTC (00:00 IST).

**Things to know about GitHub's scheduler**
- Scheduled runs can start a few minutes late, or more when GitHub is busy. The date is always worked out in India time, so a late run still picks the right birthday.
- If you need it exactly on time, create a free job on https://cron-job.org that calls GitHub's `workflow_dispatch` API at 00:00 IST.
- To update the family list, edit the `MEMBERS_CSV` secret. No code change is needed.

## 4. Run it on your own computer (optional)

You need Java 17+ and Maven.

```bash
mvn package
# test without sending anything:
DRY_RUN=true DATE_OVERRIDE=2026-11-22 java -jar target/birthday-notifier.jar
# for real, using your own CSV:
MEMBERS_FILE=members.real.csv java -jar target/birthday-notifier.jar
```

On Windows PowerShell, set variables with `$env:DRY_RUN="true"` first.
To run it automatically on your PC, use Task Scheduler (Windows) or `cron` (Linux/Mac) at 00:00. The computer has to be on at that time.

## Settings (environment variables)

| Variable | Default | Meaning |
|----------|---------|---------|
| `MEMBERS_CSV` | – | CSV content (takes priority over the file) |
| `MEMBERS_FILE` | `members.csv` | Path to the CSV file |
| `DRY_RUN` | `false` | `true` prints messages instead of sending them |
| `DATE_OVERRIDE` | today | Pretend today is `YYYY-MM-DD` |
| `TIMEZONE` | `Asia/Kolkata` | Time zone used to decide what "today" is |
| `SEND_DELAY_SECONDS` | `5` | Pause between messages, to go easy on the free API |

## Changing the message text

Edit `src/main/java/com/family/birthday/MessageBuilder.java`. WhatsApp formatting works (`*bold*`, `_italic_`), and so do emojis.

## Project layout

```
src/main/java/com/family/birthday/
  BirthdayNotifierApp.java       main: finds today's birthdays and sends the messages
  Member.java                    one family member + birthday/age logic
  MemberLoader.java              reads the CSV
  MessageBuilder.java            message wording
  Notifier.java                  interface (lets you add Telegram/email later)
  CallMeBotWhatsAppNotifier.java sends WhatsApp messages through CallMeBot
  ConsoleNotifier.java           dry-run printer
.github/workflows/birthday.yml   free nightly schedule on GitHub Actions
```
