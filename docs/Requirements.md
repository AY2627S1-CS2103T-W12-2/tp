---
layout: page
title: Requirements
---

[Back to the Developer Guide](DeveloperGuide.md).

## Product scope

LarperBoard helps hackathon participants record people, meeting context, and follow-up plans with keyboard commands. Contacts and photos stay local. Automatic JSON saving is available; an export command is not.

## User stories

Priorities: Must-have, Nice-to-have, Unlikely-to-have. Status is separate from priority and says what LarperBoard does now:

* `Shipped`: available in LarperBoard now.
* `Partial`: part of the story is available; the Status cell says what is available.
* `Deferred`: not available in LarperBoard and not planned for v1.5.

| Priority | Status | As a …​ | I want to …​ | So that I can…​ |
| -------- | ------ | ------ | ---------- | ------------- |
| Must-have | Shipped | user | add a contact | add people quickly |
| Must-have | Shipped | user | delete unnecessary people | remove people who are no longer relevant to me |
| Nice-to-have | Shipped | user | edit people's information | correct a mistake without having to delete and re-add the contact |
| Nice-to-have | Shipped | user | undo my last destructive CLI action | avoid losing important contact information because of a fast typo |
| Nice-to-have | Shipped | hackathon participant | clear all contacts | start fresh for a brand new hackathon event |
| Nice-to-have | Shipped | user | group people together | organize everyone I met at a specific track or workshop |
| Unlikely-to-have | Partial (typed `find` matches part of a name, tag, note or event; plain `find` matches whole words; no fuzzy match) | forgetful participant | search contacts using partial text or fuzzy matching | find a person even if I only remember part of their name or handle |
| Nice-to-have | Partial (sort by added, edited or viewed time only) | user | sort people by keywords | easily find people using keywords |
| Unlikely-to-have | Partial (`filter starred`, `group`, and typed `find` with AND across fields; no saved or arbitrary queries) | power user | filter contacts | execute complex queries on my network |
| Must-have | Shipped | hackathon participant | attach project tags or stack keywords | remember what tech stack or project a participant is cracked at |
| Nice-to-have | Shipped | forgetful participant | add rapid freeform notes (e.g., "Met at booth 4") | remember the context when following up after the event |
| Nice-to-have | Shipped | hackathon participant | mark high-priority contacts with a "starred" flag | easily filter and focus on standout individuals |
| Nice-to-have | Partial (`meeting ev/ md/` records event and date manually; tags are separate; nothing is added automatically) | hackathon participant | attach the hackathon name/date as a tag | look back and remember which event I met each person at |
| Nice-to-have | Shipped | hackathon participant | tag a contact with their hackathon role (Frontend, Pitcher, Designer) | easily assemble balanced teams for future hackathons |
| Nice-to-have | Shipped | user | view a list of all saved contacts sorted by recency | quickly recall the last few people I spoke with |
| Nice-to-have | Shipped | user | see when a contact was added, last edited and last viewed | judge how up to date my information about them is |
| Nice-to-have | Shipped | hackathon participant | set a follow-up date on a contact | remember by when I wanted to get back to them |
| Nice-to-have | Shipped | hackathon participant | list the contacts whose follow-up date has arrived, earliest first | see who I should contact today without scanning my whole list |
| Must-have | Shipped | user | display a detailed view of a single contact card | read all notes, links, and tags in one clean view |
| Nice-to-have | Shipped | user | view images | remember the projects people have created |
| Nice-to-have | Deferred | CLI user | view contact summaries in a clean, formatted ASCII table | browse my address book without messy text wrapping |
| Nice-to-have | Shipped | visual learner | distinguish categories with colour codes | visually scan and distinguish categories at a glance |
| Nice-to-have | Deferred | user | run a stats/summary command (counts by tech stack or event) | get a high-level snapshot of my networking efforts |
| Nice-to-have | Partial (Up/Down command recall only) | power user | have fast keyboard navigation | navigate and manipulate data at maximum keyboard speed |
| Nice-to-have | Deferred | user | set up CLI auto-completion for commands and contact names | run commands faster with Tab completion |
| Nice-to-have | Partial (`help` shows the User Guide URL with a copy button; no interactive tutorial) | first-time user | run a help/tutorial command with interactive examples | learn key commands within 30 seconds of installing the app |
| Nice-to-have | Partial (LinkedIn only) | hackathon participant | save social profiles (GitHub, LinkedIn, X, Telegram) under dedicated flags | have direct links to reach out to a person on their preferred platform |
| Nice-to-have | Deferred | user | copy a contact's email/social handle to the clipboard from the CLI | quickly paste it into a browser or messaging app |
| Nice-to-have | Deferred | hackathon participant | import contact details via a short encoded CLI string or QR token | exchange contact information instantly without manual typing |
| Nice-to-have | Deferred | power user | export my contact list to JSON, CSV, or Markdown | import it into my personal CRM, Notion, or address book |
| Must-have | Shipped | offline user | have all data stored locally | use the app without an internet connection at crowded venues |
| Nice-to-have | Deferred | hackathon participant | archive or hide contacts I don't need anymore | avoid being distracted by people I no longer need to see in my active list |
| Nice-to-have | Shipped (same name and phone) | hackathon participant | be flagged when I try to add someone who is already in my list | avoid accidentally creating two separate entries for the same person met at different events |


## Use cases

The actor is the user; the system is LarperBoard. Each row gives the normal sequence. Errors preserve contact data; a save failure also restores the previous filter, sort, selection, and history.

| Task | Steps | Alternative or failure |
|---|---|---|
| Add | 1. Enter contact fields. 2. Validate. 3. Save and confirm. | Missing/invalid name or duplicate: explain the error. |
| Edit | 1. Choose a displayed index. 2. Supply replacement fields. 3. Save. | Invalid index/values or duplicate: reject. Unchanged values add no history. |
| Tag | 1. Choose an index. 2. Supply tags. 3. Add them to existing tags. | Missing/invalid tags: reject. Existing tags keep one copy. |
| Find | 1. Enter plain words or typed terms. 2. Replace the filter. 3. Show matches. | Mixed forms, unsupported/malformed prefixes, or blank terms: reject. Zero matches is valid. |
| Group/filter | 1. Choose a tag or starred filter. 2. Show matches with current sort. | Invalid arguments: reject. Zero matches is valid. |
| Sort/list | 1. Choose an activity sort, or run list. 2. Update heading and indexes. | Unknown sort: reject. List clears filter and order. |
| View | 1. Choose a displayed index. 2. Record viewed time. 3. Show full details. | Empty/out-of-range/overflowing index: use the current index error. |
| Delete/clear | 1. Choose a displayed contact, or all contacts. 2. Remove and save. | Invalid delete index: reject. Clear on empty data succeeds without history. |
| Note | 1. Choose an index. 2. Set/replace text or clear. 3. Save. | Blank/long note: reject. No change adds no history. |
| Star | 1. Choose an index. 2. Set/remove star. 3. Save. | Repeating the same state changes nothing. |
| Meeting | 1. Choose an index. 2. Supply fields or clear form. 3. Merge supplied fields and save. | Invalid date/text, duplicate field, or mixed clear/update: reject. |
| Photo | 1. Choose an index. 2. Validate/import an image or clear. 3. Save its relative reference. | Unsafe/unreadable/large/invalid image: reject. Missing stored image shows a placeholder. |
| Follow-up/due | 1. Set/clear a date. 2. Run due. 3. Show dates on/before today in Singapore, earliest first. | Invalid date: reject. No due contacts is valid. |
| Undo/redo | 1. Check the history stack. 2. Restore the saved state. 3. Save restored contacts. | Empty stack: report nothing to undo/redo. Failed save restores both stacks. |
| Help | 1. Run help or F1. 2. Show online guide URL. 3. Copy/open it in a browser. | Reading the guide needs internet; help itself does not. |
| Command recall | 1. Focus command box. 2. Press Up/Down. 3. Edit and submit recalled text. | At newest, restore unfinished draft. Keep at most 50 commands. |
| Startup | 1. Open app. 2. Read saved JSON. 3. Show contacts. | Missing file: sample contacts. Invalid file: empty list and warning; later saves can overwrite it. |
| Exit | 1. Run exit. 2. Show message briefly. 3. Close and discard session histories. | Menu/window close exits immediately. No contact save is needed. |

## Non-functional requirements

1. **Environment**: Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2. **Environment**: Should work without an installer, and should be packaged as a single JAR file no larger than 100 MB.
3. **Performance**: Should respond to any command within 1 second while holding up to 1000 contacts.
4. **Performance**: Should be ready to accept commands within 3 seconds of launch while holding up to 1000 contacts.
5. **Capacity**: Should be able to hold up to 1000 contacts, which is more than 30 hackathons' worth at 30 new contacts per event, without noticeable sluggishness in performance for typical usage.
6. **Usability**: A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
7. **Usability**: All features should be usable with the keyboard alone, so that the user does not need to reach for the mouse in the middle of a conversation.
8. **Usability**: A new contact should be recordable with a single command, without the user having to respond to follow-up prompts.
9. **Usability**: When a command is rejected, the error message should state what was wrong and show the correct command format.
10. **Offline use**: Contact management must work offline. Reading the online User Guide needs internet.
11. **Privacy**: Should store contact data only on the user's computer, and should not send it to any remote server.
12. **Data**: Should save changes automatically after every command that modifies data, without requiring a separate save command.
13. **Data**: The _data file_ should be a human-editable text file, so that advanced users can view and edit their contacts without using LarperBoard.
14. **Reliability**: A missing or corrupted _data file_ should not cause LarperBoard to crash or fail to start.
15. **Display**: Should be usable at screen resolutions of 1280x720 and higher, at screen scales of 100% and 125%.
16. **Scope**: Should be designed for a single user, and is not expected to support multiple users sharing one _data file_.
17. **Scope**: Should not depend on a database management system or on a remote server.
18. **Maintainability**: The code should follow the project's Java coding standard, as enforced by Checkstyle in the build.
19. **Maintainability**: New features should fit into the existing `UI`, `Logic`, `Model` and `Storage` components without changing how the components depend on one another.


## Glossary

| Term | Meaning |
|---|---|
| Contact / `Person` | One saved person; `Person` is the model class. |
| Displayed index | One-based position in the currently visible, sorted/filtered list. |
| Activity | Created, last-edited, and last-viewed timestamps; unknown values display `—`. |
| Meeting context | Where, when, and in what role the user met a contact. |
| Follow-up | Date by which the user plans to reconnect. Due includes today; overdue excludes today. |
| Tag | Lowercase keyword used to group or find contacts. |
| Star | Priority marker used by `filter starred`. |
| Command recall | Session history of submitted text, separate from undoable contact changes. |
| Export / archive | Deferred capabilities; automatic JSON saving is not an export command. |
