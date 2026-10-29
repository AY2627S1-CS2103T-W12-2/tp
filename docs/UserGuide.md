---
layout: page
title: User Guide
---

LarperBoard is a desktop app for keeping track of people you meet at hackathons. Add their contact details and LinkedIn handles, tag them by role or tech stack, and find them by name when it is time to reconnect. Its command-line-style input lets you work quickly from your laptop, while the graphical interface keeps the contact list and details visible.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `larperboard.jar` from the [LarperBoard releases page](https://github.com/AY2627S1-CS2103T-W12-2/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for LarperBoard.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar larperboard.jar`.<br>
   The LarperBoard window should appear in a few seconds with sample contacts.

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/Aisha Rahman p/98765432 e/aisha@example.com l/aisha-rahman t/frontend t/react` : Adds a hackathon contact with a LinkedIn handle and tech-stack tags.

   * `find Aisha` : Shows contacts whose names match `Aisha`.

   * `delete 1` : Deletes the 1st contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

This guide describes v1.4.1. `sort` supports `added`, `edited`, and `viewed`. Separate
`find`, `group`, and `filter` commands replace the previous filter; combined filters
and `sort name` / `sort starred` from [#128](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/128) are deferred.

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Put a required `INDEX` first; prefixed parameters can then be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

* Spaces or tabs can separate prefixed parameters, including in pasted commands.
  The command box converts pasted tabs to spaces before running the command.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored. The `undo` and `redo` commands reject arguments.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Opens the help window with a link to this guide. The result message is `Opened help window.`

Format: `help`

If you type a command that LarperBoard does not know, for example a typo such as `strar 1`, the result message is `Unknown command.` followed by the list of every command you can use, in alphabetical order, for example `Unknown command. Available commands: add, clear, delete, …`. Use `help` to open this guide.


### Adding a contact: `add`

Adds a contact to LarperBoard.

Format: `add n/NAME [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A contact can have any number of tags, including zero. Each tag is shown in one of several colours, chosen from its
name, so the same tag always looks the same in the contact list and detail panel.
</div>

* `LINKEDIN_HANDLE` is the last part of the contact's LinkedIn profile link (e.g. `john-doe` in `linkedin.com/in/john-doe`). It may contain only letters, digits and hyphens, and must be 3 to 50 characters long.
* `NAME` must contain 1 to 50 alphanumeric characters and spaces and cannot be blank. Leading and trailing spaces are removed, and repeated whitespace is collapsed.
* `PHONE` is optional and must contain 3 to 20 digits when supplied.
* `EMAIL` is optional and must be a valid email address of at most 254 characters when supplied.
* A contact is a duplicate when its name matches an existing contact's name ignoring case and its phone number is the same. A different phone number permits the same name. Duplicate contacts cannot be added.
* In the contact list, a card shows its tags on a single row. If they do not all fit, the row ends with `+N`, where `N` is the number of tags left out. Use `view INDEX` to see all of a contact's tags.

Examples:
* `add n/Jane Tan`
* `add n/Betsy Crowe p/1234567 e/betsycrowe@example.com l/betsy-crowe t/friend t/criminal`

### Listing all contacts: `list`

Clears the active contact filter and sort, then shows all contacts in their original order.
The result message is `Listed all contacts.`, including when the list is empty.

Format: `list`

* Use `list` to return to the full, original-order list after `find`, `group`, `filter starred`, or `due`.
* `list` does not clear command history.

### Reading the list heading

The heading above the contact list keeps showing the active filter, order, and result count,
even after another command such as `view` replaces the command-result message.
For example, `group t/react` shows `Group: react - Original order - 6 contact(s)` when six contacts match.
`filter starred` shows `Starred only`, while plain and typed `find` show `Find results`.
The search heading identifies an active search but does not repeat the search terms.
`due` shows `Due on/before YYYY-MM-DD`, using the date when the command ran.

Changing the filter replaces its label; filters do not combine in this release.
Sorting preserves the filter label, and the count updates when contacts change, including to zero.
`filter starred off` removes the filter label while keeping the sort.

### Sorting contacts by activity time: `sort`

Sorts the currently displayed contacts by a recorded activity time. The active order and result count are shown above
the contact list.

Format: `sort added|edited|viewed`

* Sort keywords are case-insensitive: `sort Added`, `sort ADDED`, and `sort added` behave the same.
  The command word remains lowercase: `sort`.
* `sort added` puts most recently added contacts first.
* `sort edited` puts most recently edited contacts first.
* `sort viewed` puts most recently viewed contacts first. Viewing a contact can therefore move it in this list.
* Contacts without the chosen recorded time appear last. Contacts with equal times keep their original list order.
* The displayed indexes follow the order you see, so commands such as `view`, `edit`, `delete`, and `tag` use the
  sorted contact index.
* Use `list` to return to the full, original-order list after `find` or `group`.

Examples:

* `sort added` shows the newest contacts first.
* `sort viewed` followed by `view 2` records that view and may move that contact to the top.

### Editing a contact: `edit`

Edits an existing contact in LarperBoard.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`

* Edits the contact at the specified `INDEX`. The index refers to the number shown in the displayed contact list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the contact's existing tags are replaced; adding tags with `edit` is not cumulative.
* To remove all of a contact's tags, enter `t/` without a tag after it.
* To remove a contact's LinkedIn handle, enter `l/` without a handle after it.
* If the supplied values already match the contact, the command reports `Person unchanged:` followed by the
  contact's details. The displayed list, activity times, and undo history stay unchanged.

Examples:
* `edit 1 p/91234567 e/johndoe@example.com` changes the 1st contact's phone number and email address to `91234567` and `johndoe@example.com`.
* `edit 2 n/Betsy Crower t/` changes the 2nd contact's name to `Betsy Crower` and clears all existing tags.

### Adding tags to a contact: `tag`

Adds one or more tags to an existing contact without removing its current tags.

Format: `tag INDEX t/TAG [t/TAG]…​`

* `INDEX` refers to the contact's number in the currently displayed list and **must be a positive integer** 1, 2, 3, …​
* At least one nonempty `t/TAG` is required. A tag must be 1 to 20 characters long and contain only `A`–`Z`, `a`–`z`, digits, hyphens (`-`), or plus signs (`+`).
* Tags are stored in lowercase. Repeating a tag with different capitalization, or adding a tag the contact already has, keeps only one copy. The command still reports success if every requested tag is already present.
* A successful command reports `Updated Person:` followed by the contact's details.
* Use `edit INDEX t/TAG` to replace all tags, or `edit INDEX t/` to remove them all.

Examples:
* `tag 1 t/frontend t/React` adds `frontend` and `react` to the 1st displayed contact.
* `find Aisha` followed by `tag 1 t/mentor` adds `mentor` to the 1st contact in the search results.
* `tag 1 t/Java t/java` adds only one `java` tag.

### Adding a note to a contact: `note`

Sets, replaces or removes a free-form note on a contact, for example where you met them or what to follow up on.

Format: `note INDEX nt/NOTE` or `note INDEX clear`

* `INDEX` refers to the contact's number in the currently displayed list and **must be a positive integer** 1, 2, 3, …​
* `NOTE` can contain spaces and punctuation and can be up to 1000 characters long. An emoji such as 😀 counts as one character. An emoji that is made of several parts, such as a flag or a family, counts as several. Spaces at the start and end are removed. A note that is empty or only spaces is not accepted: use `note INDEX clear` to remove a note.
* Setting a note on a contact that already has one replaces the old note.
* The text after `nt/` is taken as typed, so it can contain `/`. Use `nt/` only once in a command.
* The note is shown in full in the detail panel when you [`view`](#viewing-a-contact-view) the contact, and it is not shown in the contact list. If the note is long, scroll the detail panel to read all of it.
* If the new note is the same as the old one, or you clear a contact that has no note, nothing is changed and the contact's last edited time stays as it was.

Examples:
* `note 1 nt/Met at the AI track, wants a React teammate` sets the note of the 1st contact.
* `find Aisha` followed by `note 1 nt/Follow up after the demo` sets the note of the 1st contact in the search results.
* `note 2 clear` removes the note of the 2nd contact.

If the note cannot be saved, LarperBoard shows these errors:

Input or situation | Message
-------------------|--------
`note`, `note 1`, or text without `nt/` such as `note 1 some text` | `Invalid command format!` followed by the command usage
`note 1 nt/` (empty or only spaces) | `Note cannot be blank. Use 'note INDEX clear' to remove it.`
A note of more than 1000 characters | `Note must be at most 1000 characters.`
`note 1 nt/a nt/b` (`nt/` used twice) | `Multiple values specified for the following single-valued field(s): nt/`
`note 0 nt/text`, `note abc clear`, or an index larger than the number of contacts shown | `The person index provided is invalid.`
A valid `note` command when the displayed list is empty | `The person list is empty.`

### Locating contacts: `find`

Use plain name keywords or typed field prefixes. A valid search may return zero contacts.
Both forms report `N contact(s) listed!`, where `N` is the number of matches, including zero.

Plain format: `find KEYWORD [MORE_KEYWORDS]…​`

* Plain search is case-insensitive and considers only names.
* Plain keywords use full-word matching and OR semantics; for example, `find Hans Bo` finds `Hans Gruber` or `Bo Yang`, while `Han` does not match `Hans`.

Typed format: `find [n/TERM] [t/TERM] [nt/TERM] [ev/TERM]…​`

* `n/` searches names, `t/` searches tags, `nt/` searches note text, and `ev/` searches meeting event names.
* Typed terms match partial text and ignore case.
* Repeated terms in one field use OR. Different fields use AND. For example, `find n/Alex n/Sam t/hackathon` finds an Alex or Sam contact that also has a `hackathon` tag.
* Do not mix plain keywords and typed prefixes. The message is `Use either plain keywords or field prefixes in one find command.`
* Empty terms and malformed prefixes show the standard format-and-usage error.
* Other command fields (`e/`, `p/`, `l/`, `d/`, `md/`, `r/`, and `f/`) are not supported by `find`.
  At the start of the arguments or after whitespace, these prefixes give `Invalid command format!` with usage,
  even after a valid search field: for example, `find n/aisha e/p0@example.com` is invalid, not a zero-match search.
  Ordinary slash-containing text in typed terms, such as `find ev/ai/ml workshop` or `find nt/c/o NUS`, remains valid.
  This also means a note or event search cannot contain these reserved prefixes after whitespace:
  `find nt/likes r/java` is rejected even if `r/java` is intended as literal note text.
* Indexes always refer to the contacts currently displayed. Use `list` to return to the full list.

Examples:
* `find Aisha Ben` finds contacts named `Aisha Rahman` or `Ben Tan` when both have been added.
* `find t/react nt/teammate` finds contacts tagged `react` whose note contains `teammate`.
* `find ev/hack singapore` finds contacts whose event name contains `hack singapore`.

### Grouping contacts by tag: `group`

Shows contacts with the specified tag.

Format: `group t/TAG`

* Tag matching is case-insensitive.
* The result message shows the selected tag and the number of matching contacts.
* `group` replaces the current `find` or `group` result rather than combining with it.
* Use `list` to return to the full contact list.

Examples:
* `group t/react`
* `group t/UI-UX`

### Starring important contacts: `star` and `unstar`

Marks a contact as important with a star, or removes the star again.

Format: `star INDEX` or `unstar INDEX`

* `INDEX` refers to the index number shown in the displayed contact list and must be a positive integer.
* A starred contact shows a ★ in the top right corner of its card in the contact list, and in the top right corner of the contact details.
* Starring a contact that is already starred (or removing the star from one that is not) changes nothing, and LarperBoard tells you so.
* Stars are saved with your data, and you can reverse a change with `undo`.

Examples:
* `star 1` stars the first contact in the displayed list.
* `unstar 1` removes the star from it.

### Showing only starred contacts: `filter`

Shows only your starred contacts, or shows all contacts again.

Format: `filter starred` or `filter starred off`

* The keywords are case-insensitive.
* The result message shows how many contacts are listed. If you have not starred anyone, the list is empty.
* `filter` replaces the current `find`, `group` or `filter` result rather than combining with it.
* `list` also shows all contacts again.

Examples:
* `filter starred` shows only the starred contacts.
* `filter starred off` shows every contact again.

### Deleting a contact: `delete`

Deletes the specified contact from the displayed list.

Format: `delete INDEX`

* Deletes the contact at the specified `INDEX`.
* The index refers to the number shown in the displayed contact list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `list` followed by `delete 2` deletes the 2nd contact in LarperBoard.
* `find Aisha` followed by `delete 1` deletes the 1st contact in the search results.

If deletion cannot be completed, LarperBoard shows these errors:

Input or situation | Message
-------------------|--------
A valid `delete INDEX` when the displayed list is empty | `The person list is empty.`
`delete 1 2` (more than one index) | `Only one index parameter is accepted.` (with the invalid-format message and usage)
`delete` (missing index) | `Invalid command format!` followed by the command usage
`delete 0`, `delete -1`, `delete abc`, or `delete 2147483648` (invalid or oversized index) | `The person index provided is invalid.`
`delete 99` when the displayed list is nonempty but has fewer than 99 contacts | `The person index provided is invalid.`

### Viewing a contact: `view`

Shows the selected contact's full details in the detail panel, next to the contact list. It does not change the contact's details, but it does record the time you last viewed the contact. This time is saved in the data file. The detail panel lists when the contact was created, last edited and last viewed in Singapore time, with `—` for a time that is not known.

Format: `view INDEX`

* `INDEX` refers to the contact's number in the currently displayed list and **must be a positive integer** 1, 2, 3, …​
* The detail panel shows the contact's name, phone number, email, LinkedIn handle and all of their tags. A field that the contact does not have is shown as `-`.
* The detail panel shows `No contact selected` until you view a contact. A successful command also reports `Now viewing:` followed by the contact's details.
* The detail panel stays on the contact you viewed until you view someone else. It is updated if you edit or tag that contact, and goes back to `No contact selected` if you delete that contact or clear all entries.
* `find`, `group`, and `filter` change the contact list without clearing the detail panel, even if the viewed contact is hidden or there are no results. A later `view INDEX` selects from the currently displayed results, not from the retained detail panel.

Examples:
* `view 1` shows the 1st displayed contact.<br>
  ![result for 'view 1'](images/Ui-v1.4.1.png)
* `find Aisha` followed by `view 1` shows the 1st contact in the search results.

If the contact cannot be viewed, LarperBoard shows these errors:

Input or situation | Message
-------------------|--------
A valid `view INDEX` when the displayed list is empty | `The person list is empty.`
`view 1 2` (more than one index) | `Only one index parameter is accepted.` (with the invalid-format message and usage)
`view` (missing index) | `Invalid command format!` followed by the command usage
`view 0`, `view -1`, `view abc`, or `view 2147483648` (invalid or oversized index) | `The person index provided is invalid.`
`view 99` when the displayed list is nonempty but has fewer than 99 contacts | `The person index provided is invalid.`

### Clearing all entries: `clear`

Clears all contacts from LarperBoard.

Format: `clear`

### Undoing a contact change: `undo`

Restores the contact list, displayed results, and viewed contact from before the most recent successful contact change. The restored contacts are saved to the data file. If there is no change to undo, LarperBoard reports `Nothing to undo.`

Format: `undo`

Up to 50 contact changes are available during the current app session. Adding, editing, tagging,
deleting, clearing, setting or clearing notes, starring or unstarring, changing follow-up dates,
changing meeting context, and attaching, replacing, or clearing photos can enter this history.
Each `undo` reverses one successful change. Failed commands, commands that leave contacts unchanged,
and commands such as `view`, `find`, and `list` do not add a step. Undo restores saved
contact details and their last-edited times without marking them as edited again. A later explicit
view time is kept for a contact that survives the undo; restoring a deleted contact does not count
as a new view. The history is not saved: after closing and reopening LarperBoard, restored contact
data remains in the JSON file, but no earlier undo steps are available.

### Redoing an undone contact change: `redo`

Reapplies the most recently undone contact change in the current app session and saves the restored
contacts to the data file. Format: `redo`. If there is no undone change, LarperBoard reports
`Nothing to redo.` You can redo several changes in reverse undo order, up to the 50-step history
limit. `redo` restores contact details, activity times, the displayed list and sort, and the viewed
contact without marking the contacts as edited again. Later explicit views of surviving contacts
are kept.

A new successful contact change after `undo` clears the redo steps. Failed commands, failed saves,
changes that leave contacts unchanged, and view or list commands keep them. Photo redo restores the
saved photo reference; imported image files are retained. Redo history is not available after the
app is closed and reopened, although the redone contacts remain in the data file.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

* After `exit`, the window stays open for about one second so that you can read the message in the result box, then it closes. The command box does not accept input during that time.
* Choosing **Exit** from the **File** menu, or closing the window with its close button, closes LarperBoard at once.

### Recalling previous commands: Up and Down arrows

Brings back commands that you submitted earlier in the same session, so that you can run or fix them without typing them again.

* While the command box is selected, press the **Up** arrow to bring back the previous command, and press it again to go further back.
* Press the **Down** arrow to move forward again. After the newest command, the command box shows the text that you had typed before you started browsing.
* Every command you submit is remembered, including commands that fail, such as an unknown command or an invalid index. A command that is the same as the one just before it is remembered only once.
* LarperBoard remembers the latest 50 commands. The history is cleared when you close the app and is not saved in the data file.
* You can edit a recalled command before you submit it. Editing does not change the remembered command.
* The arrow keys only work in the command box, and only when no other key such as `Shift` or `Ctrl` is held down.
* Commands such as `list` and `clear` do not clear the history. They are remembered like any other command.

Examples:
* Submit `find Aisha` and then `view 1`. Press **Up** once to bring back `view 1`, and press **Up** again to bring back `find Aisha`.
* Type `tag 1 t/` without submitting it, press **Up** to bring back an earlier command, then press **Down** to get `tag 1 t/` back.

### Recording meeting context: `meeting`

![Meeting feature at 1280×720](images/meeting-v1.4.1.png)

[View the simulated 150% layout](images/meeting-v1.4.1-150percent.png).

Use `meeting INDEX [ev/EVENT] [md/YYYY-MM-DD] [r/ROLE]` to set or replace one or more fields.
Omitted fields keep their existing values. For example:

```text
meeting 1 ev/Hack Singapore md/2026-10-20 r/Mentor
meeting 1 r/Participant
meeting 1 clear date
meeting 1 clear
find ev/singapore
```

`meeting INDEX clear` removes all context. Add `event`, `date`, or `role` to clear only that field.
Indexes refer to the displayed list, including after a search. Event and role each accept 1–80
UTF-16 code units and cannot be blank. Most letters count once; supplementary characters such as
`😀` count twice, so at most 40 such emoji fit. This meeting-field limit differs from the note limit.
Dates must be real calendar dates in exactly YYYY-MM-DD format
(years 0001–9999); they are displayed as Singapore calendar dates without time-zone conversion.
Tags are independent and are never generated from meeting fields.

Invalid text reports `Event and role must each contain 1 to 80 characters.` Invalid dates report
`Date must be a real date in YYYY-MM-DD format.` A command with no fields reports
`Specify a meeting field or use 'meeting INDEX clear'.` Invalid displayed indexes report
`The person index provided is invalid.` Failures leave the contact unchanged. Same-value updates
and clearing an already absent field are informational no-ops, with no timestamp or undo entry.

The list shows an event cue of at most 32 UTF-16 code units; `view INDEX` shows full event, date, and role
in a vertically scrolling detail panel. Editing a viewed contact refreshes the panel. If changing
its event excludes it from the current event search, its selection is cleared.

`find ev/TERM [ev/TERM]...` matches partial event names without case sensitivity, using OR across
repeated terms. No match is a valid empty list. Do not mix event terms with plain name keywords.

Meeting context is saved in optional JSON `meeting`, for example
`"meeting": {"eventName": "Hack Singapore", "date": "2026-10-20", "role": "Mentor"}`.
Missing/null context and fields mean absent values. Clearing the final field omits the object.
Malformed present values reject loading with `Invalid contact data: 'meeting.<field>' <reason>.`;
a non-object context names `meeting`. Back up JSON before editing it manually.
Successful real changes update last-edited time and are saved before entering session undo history.

### Setting a follow-up date: `followup`

Sets, replaces or removes the date by which you want to follow up with a contact, for example after a hackathon.

Format: `followup INDEX d/YYYY-MM-DD` or `followup INDEX clear`

* `INDEX` refers to the contact's number in the currently displayed list and **must be a positive integer** 1, 2, 3, …​
* `clear` can be typed in any letter case.
* The date must be a real calendar date in exactly `YYYY-MM-DD` format. Leap days such as `2028-02-29` are accepted; `2027-02-29` is not.
* A successful command reports `Follow-up date for NAME set to DATE.` or `Follow-up date cleared for NAME.`
* Setting the date a contact already has, or clearing a date that is not set, only reports `Follow-up date unchanged.` It does not change the last-edited time or add an undo step.
* A real change counts as an edit of the contact and sets its last-edited time. You can reverse it with `undo`.
* The contact card shows `Follow up: DATE`. Once the date is before today (Singapore time), the card shows `Follow up: DATE (Overdue)` and the whole line turns red and bold. A date that is today is not overdue. The card is drawn when the list is shown, so it does not change at midnight until the list is next refreshed.
* `view INDEX` shows the full date in the detail panel.

Examples:
* `followup 1 d/2026-10-25` sets the follow-up date of the 1st displayed contact.
* `followup 1 clear` removes it.
* `due` followed by `followup 1 clear` removes the 1st contact of the due list from that list.

If the follow-up date cannot be set, LarperBoard shows these errors and leaves the contact unchanged:

Input or situation | Message
-------------------|--------
`followup 1 d/2026-02-30`, `followup 1 d/2027-02-29`, `followup 1 d/2026-1-5`, or `followup 1 d/` (date that is not real or not in `YYYY-MM-DD` format) | `Date must be a real date in YYYY-MM-DD format.`
`followup 0 d/2026-10-25`, `followup abc clear`, or `followup 2147483648 clear` (invalid or oversized index) | `The person index provided is invalid.`
`followup 99 d/2026-10-25` when the displayed list has fewer than 99 contacts, or any `followup` with an index when the displayed list is empty | `The person index provided is invalid.`
`followup`, `followup 1`, `followup 1 clear d/2026-10-25`, or `followup 1 2 d/2026-10-25` | `Invalid command format!` followed by the command usage
`followup 1 d/2026-10-25 d/2026-10-26` (`d/` used twice) | `Multiple values specified for the following single-valued field(s): d/`
The data file cannot be saved because it or its folder is not writable | `Could not save data to file FILE_PATH due to insufficient permissions to write to the file or the folder.`
The data file cannot be saved for another reason | `Could not save data due to the following error: REASON`

The date is saved in the optional JSON field `followUpDate`, for example `"followUpDate": "2026-10-25"`. A missing or `null` value means no follow-up. A malformed value makes the data file invalid, so LarperBoard starts with an empty contact list and writes `Invalid contact data: 'followUpDate' must be a real date in YYYY-MM-DD format.` to its log. The next successful command other than `help` or `exit` (even `list`) saves the empty list over the file and the contacts in it are lost, so back up the file before editing it by hand. See [Editing the data file](#editing-the-data-file).

### Showing contacts due for follow-up: `due`

Shows the contacts that you planned to follow up with by today, so that you can see who to contact next.

Format: `due`

* Shows every contact whose follow-up date is today or earlier, with the earliest date first. Contacts with the same date stay in their original list order. Contacts with no follow-up date, or a date after today, are left out.
* "Today" is the Singapore date when you run the command. Nothing runs in the background, so run `due` again to refresh the list.
* A successful command reports `Due for follow-up on or before TODAY.` and then `N contact(s) listed!` on the next line, where `TODAY` is the date in `YYYY-MM-DD` format.
* `due` replaces any search, group, filter or sort that was active. A later `sort added|edited|viewed` can reorder the contacts it shows. `sort due` is not a valid sort.
* `due` does not clear the detail panel, even if the contact you are viewing is not due.
* The due-date order stays active for later `find`, `group` and `filter` commands, and the line above the list says `Sorted by due`, until you run `sort ...` or `list`.
* No contact being due is a normal result: the list is empty and the message ends with `0 contact(s) listed!`.
* Index commands such as `view 1` or `followup 1 clear` use the numbers shown in the due list. Clearing a date from the due list removes that contact from it.
* `list` shows all contacts again in their original order.
* Any text typed after `due` is ignored.

Examples:
* `followup 2 d/2026-10-18` followed by `due` lists the second contact first if its date is the earliest.
* `due` when no contact has a date up to today reports `0 contact(s) listed!`.

### Contact photos: `photo`

![Photo feature at 1280×720](images/photo-v1.4.1.png)

[View the simulated 150% layout](images/photo-v1.4.1-150percent.png).

Use `photo INDEX f/RELATIVE_PATH` to attach or replace one PNG/JPEG, or `photo INDEX clear` to
remove its reference. INDEX always refers to the displayed list. Place the source image inside the
folder containing `larperboard.jar`, for example:

```text
LarperBoard/
  larperboard.jar
  portrait.png
  photos/          (created and managed by the app)
```

```text
photo 1 f/portrait.png
photo 1 f/images/new-portrait.jpg
photo 1 clear
```

Subfolders and spaces in paths are allowed. Absolute paths, URLs, `..` traversal, and symlinks that
escape the JAR folder are rejected. If a path contains ` f/`, surround the whole path with double quotes,
for example `photo 1 f/"my f/portrait.png"`, so that the embedded text is treated as part of the filename.
Spaces after `f/` are also accepted, for example `photo 1 f/ "my f/portrait.png"`.
Quoted paths use the same file and folder validation as unquoted paths. Sources must be readable PNG/JPEG
files, at most **5 MiB**
(5,242,880 bytes) and **4096×4096 pixels**. Both filename extension and decoded image format
must agree. The JAR folder must be writable. The application copies accepted images to `photos/`
with generated collision-safe names and saves a relative reference such as
`"photoPath": "photos/contact-123.png"` in the editable JSON. The root is the JAR folder even when
launched from another working directory. During development with Gradle it is the working folder.

Errors are `Photo file must be a PNG or JPEG inside the application folder.` for unsafe paths,
wrong types, or undecodable content; `Photo must be at most 5 MiB and 4096×4096 pixels.` for
size/dimension limits; and `Photo file could not be read.` for file-access failures.
An invalid displayed index reports `The person index provided is invalid.`

Input | Error and recovery
------|-------------------
An unquoted path containing ` f/`, or multiple `f/` arguments | `Multiple values specified for the following single-valued field(s): f/`, followed by `If the path contains " f/", put the whole path in double quotes: f/"my f/portrait.png"`
An unclosed quoted path | A duplicate-file or invalid-format error, followed by the same quoting hint. Close both quotes around the whole path.

The photo is shown only in the detail panel (`view INDEX`), to the right of the contact's name, and not in
the contact list. It is scaled to fit inside a fixed 160×120 frame, preserving its aspect ratio, so photos of
different shapes never change the layout. If the detail panel is too narrow for the photo and the name
side by side (for example when the window is at its minimum width), the photo is shown under the name
instead. Absent, missing, malformed, or unreadable references show a
"No photo" placeholder of the same size;
unavailable images also produce a log warning and do not prevent other contacts loading.
Unsafe saved paths are never opened. Old JSON without `photoPath` loads with no photo.

Clearing/replacing a photo or deleting/clearing contacts **does not delete imported images**.
Unused files may remain, including after a failed save, so undo/redo references remain usable.
An identical current image and clearing an absent photo are no-ops. Only real successful changes
update last-edited time and enter undo history; failed saves restore the contact and history.
Keep the `photos/` folder when transferring contacts to another installation. Do not delete images
that may still be referenced by a contact or session history.


### Saving the data

LarperBoard automatically saves data after successful commands. You do not need to save manually.
`help` and `exit` do not write contact data and remain available if the contact data file is read-only.

### Editing the data file

LarperBoard data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, LarperBoard starts with an empty contact list at the next run. The invalid file remains on disk until a successful command other than `help` or `exit` saves contact data. We recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause LarperBoard to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

Each contact in the data file can also have these optional time fields. A missing or `null` field means the time is not known.

Field | Meaning | Format
------|---------|-------
`createdAt` | When the contact was added | Whole-second UTC time written exactly like `"2026-10-17T04:12:33Z"`
`lastEditedAt` | When the contact's details last really changed | Same as `createdAt`
`lastViewedAt` | When the `view` command last showed the contact | Same as `createdAt`

Each contact can also have an optional `note` field: a string of at most 1000 characters. A missing or `null` field means the contact has no note. A note that is empty or only spaces, longer than 1000 characters, or not a string makes the data file invalid, so LarperBoard starts with an empty contact list and writes `Invalid contact data: 'note' must be a string of at most 1000 characters.` to its log.

Each contact can also have an optional `starred` field, which is `true` or `false`. A missing or `null` field means the contact is not starred. Any other value makes the data file invalid, so LarperBoard starts with an empty contact list and writes `Invalid contact data: 'starred' must be true or false.` to its log.

LarperBoard fills the time fields in for you. Contacts saved by an older version have no times and keep none until they are changed or viewed. A time with a fraction of a second, an offset such as `+08:00`, spaces, or any other format makes the data file invalid. LarperBoard then starts with an empty contact list and writes `Invalid contact data: 'createdAt' must be an ISO-8601 UTC timestamp.` (naming the field with the problem) to its log. The next successful command other than `help` or `exit` (even `list`) saves the empty list over the file and the contacts in it are lost, so back up the file before editing it. Times are shown in Singapore time (Asia/Singapore).

Each contact can also have an optional `followUpDate` field, a date written like `"2026-10-25"`. A missing or `null` field means the contact has no follow-up date. A value that is not a real date in `YYYY-MM-DD` format makes the data file invalid, with the same effect as above. See [Setting a follow-up date](#setting-a-follow-up-date-followup).

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Close LarperBoard on both computers. Copy `data/addressbook.json` from the old working folder
and the managed `photos/` folder from beside the old JAR into the corresponding folders on the new computer.
Keep the relative folder layout. Copy `preferences.json` only if you want the same window settings.
Back up the destination files before replacing them. Undo/redo and command recall do not transfer.

--------------------------------------------------------------------------------------------------------------------

## Known issues

Meeting text counts supplementary characters twice, and an event preview can split a surrogate pair
when truncated. The pending [Unicode fix](https://github.com/AY2627S1-CS2103T-W12-2/tp/pull/181)
is not included in v1.4.1. `find` also reports `person(s)` while other list commands say `contact(s)`;
this wording difference does not change the results.

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`<br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com l/james-ho t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Due** | `due`
**Edit** | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Exit** | `exit`
**Filter** | `filter starred` or `filter starred off`
**Find** | `find KEYWORD [MORE_KEYWORDS]…​` or `find [n/TERM] [t/TERM] [nt/TERM] [ev/TERM]…​`<br> e.g., `find n/James t/react`
**Follow-up** | `followup INDEX d/YYYY-MM-DD` or `followup INDEX clear`<br> e.g., `followup 1 d/2026-10-25`
**Group** | `group t/TAG`<br> e.g., `group t/react`
**Help** | `help`
**List** | `list`
**Meeting** | `meeting INDEX [ev/EVENT] [md/YYYY-MM-DD] [r/ROLE]` or `meeting INDEX clear [event/date/role]`
**Photo** | `photo INDEX f/RELATIVE_PATH` or `photo INDEX clear`
**Note** | `note INDEX nt/NOTE` or `note INDEX clear`<br> e.g., `note 1 nt/Met at the AI track`
**Redo** | `redo`
**Sort** | `sort added|edited|viewed`<br> e.g., `sort viewed`
**Star** | `star INDEX`<br> e.g., `star 1`
**Tag** | `tag INDEX t/TAG [t/TAG]…​`<br> e.g., `tag 1 t/frontend t/react`
**Undo** | `undo`
**Unstar** | `unstar INDEX`<br> e.g., `unstar 1`
**View** | `view INDEX`<br> e.g., `view 1`
**Recall previous command** | Press **Up** or **Down** in the command box. See [Recalling previous commands](#recalling-previous-commands-up-and-down-arrows)
