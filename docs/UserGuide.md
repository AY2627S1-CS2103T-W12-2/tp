---
layout: page
title: User Guide
---

Keep track of people you meet at hackathons. Add a contact, record why you want to reconnect, and find them when you need them. Contact management works offline; reading this online guide needs internet.

[Quick start](#quick-start) · [Commands](#features) · [Find](#locating-contacts-find) · [Data and recovery](#saving-the-data) · [Command summary](#command-summary)

## Quick start

1. Install Java 25 or later. On macOS, follow the [Java installation guide](https://se-education.org/guides/tutorials/javaInstallationMac.html).
1. Download `larperboard.jar` from the [releases page](https://github.com/AY2627S1-CS2103T-W12-2/tp/releases).
1. Put it in a writable folder. Open a terminal in that folder and run:

   ```sh
   java -jar larperboard.jar
   ```

1. Type each command below in the app's command box, pressing Enter after each one:

   ```text
   add n/Nora Lim p/98765432 t/react
   find Nora
   view 1
   note 1 nt/Met at the AI booth
   ```

1. Run `list` to return to all contacts. Run `undo` to reverse your last contact change.

![LarperBoard contact list and detail panel](images/Ui-v1.4.1.png)

## Features

These instructions describe the current development build. The [v1.4.1 release notes](releases/v1.4.1.md) describe the published JAR. Some feedback wording differs in older releases.

### Read command syntax

* Replace uppercase placeholders: `add n/NAME` becomes `add n/Aisha Rahman`.
* Square brackets mean optional; `...` means you can repeat that field.
* Use lowercase command words and prefixes such as `n/`. Put the index first, then fields in any order.
* `INDEX` is a positive number from the list **currently shown**. Search, filtering, and sorting can change it.
* Separate fields with spaces or tabs. Pasted tabs are converted to spaces.
* `help`, `list`, `clear`, `due`, and `exit` ignore extra arguments. `undo` and `redo` reject them.

### Viewing help: `help`

Run `help`, press F1, or choose **Help**. The help window shows this guide's URL and a **Copy URL** button. Paste the URL into a browser to read it.

### Adding a contact: `add`

Format: `add n/NAME [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]...`

1. Enter `add n/Jane Tan` to save a contact with just a name.
1. Include any optional details, for example:

   ```text
   add n/Jane Tan p/91234567 e/jane@example.com l/jane-tan t/frontend t/react
   ```

A duplicate has the same name (ignoring case) **and** phone number. A different phone permits the same name.

| Field | Accepted value |
|---|---|
| Name | 1–50 ASCII letters, digits, and spaces; cannot be blank. Extra whitespace is collapsed. |
| Phone | 3–20 digits, or omitted. |
| Email | A valid email address, at most 254 characters, or omitted. |
| LinkedIn handle | The part after `linkedin.com/in/`; 3–50 letters, digits, or hyphens. |
| Tag | 1–20 letters, digits, hyphens, or `+`; stored in lowercase. |

### Listing all contacts: `list`

Run `list` to clear the filter and sort and show all contacts in their original order. Command recall is kept.

### Reading the list heading

The heading shows the active filter, order, and count, for example `Group: react - Original order - 6 contact(s)`.

* `find`, `group`, `filter`, and `due` replace the previous filter; they do not combine.
* Sorting keeps the filter. `filter starred off` clears the filter but keeps the sort.
* An empty result is valid. Run `list` to see everyone again.

### Sorting contacts by activity time: `sort`

Format: `sort added|edited|viewed`

1. Run `sort added` for newest contacts first, `sort edited` for most recently changed, or `sort viewed` for most recently viewed.
1. Use the new displayed indexes for subsequent commands.

Sort keywords ignore case (`sort Added` works); the command word remains lowercase. Unknown times appear last. Equal times keep the original contact order. Viewing a contact under `sort viewed` can move it to the top. Only these three sort options are available; use `list` to reset the order.

### Editing a contact: `edit`

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]...`

1. Find the contact and check its displayed index.
1. Supply at least one field, for example `edit 1 p/91234567 e/new@example.com`.
1. Run `view 1` to inspect it, using its current index if the list reordered.

Unspecified fields stay unchanged. `edit 1 t/react` **replaces** all tags; `edit 1 t/` removes all tags. `edit 1 l/` removes the LinkedIn handle. Name, phone, and email follow the rules above. Supplying existing values changes no timestamps or undo history.

### Adding tags to a contact: `tag`

Format: `tag INDEX t/TAG [t/TAG]...`

Run `tag 1 t/frontend t/React` to add tags while keeping existing ones. Repeated tags keep one lowercase copy. Use `edit` to replace or remove tags.

Tags have consistent colors. Cards show one row with `+N` for hidden tags; `view INDEX` shows every tag.

### Adding a note to a contact: `note`

Format: `note INDEX nt/NOTE` or `note INDEX clear`

1. Run `note 1 nt/Met at booth 4, interested in React` to save or replace the note.
1. Run `view 1` to read the full note; scroll the detail panel for long notes.
1. Run `note 1 clear` to remove it.

Notes can contain slashes and up to 1000 UTF-16 code units (some emoji count twice). Blank notes are rejected; use `clear` instead. Use `nt/` once. Repeating the same note or clearing an absent note does not add an undo step.

### Locating contacts: `find`

Choose one form:

| Goal | Example | Matching rule |
|---|---|---|
| Search full words in names | `find Alex Sam` | Either word, ignoring case. `Alex` does not match `Alexis`. |
| Search part of a name | `find n/alex` | Matches both Alex and Alexis. |
| Search tags or notes | `find t/react nt/booth` | Both fields must match. |
| Search event names | `find ev/singapore` | Matches part of a recorded meeting event. |

Typed format: `find [n/TERM] [t/TERM] [nt/TERM] [ev/TERM]...`

* Supply at least one nonblank term. Typed searches ignore case and match partial text.
* Repeat a field for alternatives: `find n/Alex n/Sam t/react` means (Alex **or** Sam) **and** react.
* Do not mix plain words with typed fields. Phone, email, LinkedIn, dates, and roles cannot be searched.
* Keep each prefix together: `t/react`, not `t /react`. Reserved prefixes such as `r/` are rejected even inside note/event search text; ordinary `ai/ml`, `c/o`, and URLs are allowed there.

Rejected searches keep the current results, viewed contact, saved data, and history. Zero matches is a normal result. `find` replaces the filter and keeps the current sort.

### Grouping contacts by tag: `group`

Format: `group t/TAG`

Run `group t/react` to show contacts with that exact tag, ignoring case. Only one tag is accepted. It replaces the filter and keeps the sort. Run `list` to return to everyone.

### Starring important contacts: `star` and `unstar`

Run `star 1` to mark the first displayed contact with ★, or `unstar 1` to remove it. Repeating either action is a no-op. Stars appear on the card and detail panel.

### Showing only starred contacts: `filter`

Run `filter starred` to show starred contacts. Run `filter starred off` to show everyone while keeping the sort. Keywords `starred` and `off` ignore case. Unstarring a contact removes it from the starred list.

### Deleting a contact: `delete`

1. Check the displayed list and index.
1. Run `delete 1` to remove its first contact.
1. Run `undo` if you deleted the wrong contact.

Only one positive index is accepted. Deleting the viewed contact clears the detail panel.

### Viewing a contact: `view`

Run `view INDEX`, for example `view 1`, to show full details and record the last-viewed time. Missing fields show `-`; unknown activity times show `—`. Times use Singapore time.

Searching or filtering keeps the viewed contact even when it is hidden. Editing the viewed contact refreshes its details; if the edit makes it leave the current results, selection is cleared. Deleting it or clearing all contacts also shows `No contact selected`.

**Known bug:** editing a different contact can currently clear a hidden viewed contact. The fix is tracked in [#227](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/227).

### Clearing all entries: `clear`

Run `clear` to delete all contacts. Run `undo` to restore them during the same session.

### Undoing a contact change: `undo`

Run `undo` to reverse the last successful contact change, including its filter, order, and selection. Up to 50 changes are kept in this session.

* Failed and no-op commands add no history. Viewing, searching, and sorting are not undoable contact changes.
* Later explicit view times on surviving contacts are preserved; a deleted contact returns with its pre-deletion view time.
* History is lost when you close the app.

### Redoing an undone contact change: `redo`

Run `redo` to reapply the most recently undone change. A new successful contact change clears redo history. Failed, read-only, and no-op commands keep it.

### Exiting the program: `exit`

Run `exit` to show the exit message for about one second, then close. The Exit menu and window close button close immediately. Contacts are already saved; undo/redo and command recall end with the session.

### Recalling previous commands: Up and Down arrows

1. Focus the command box and press Up for an earlier command.
1. Press Down to move forward. Moving past the newest command restores your unfinished draft.
1. Edit the recalled text if needed, then press Enter.

Recall keeps the last 50 commands, including failed commands, and skips consecutive duplicates. Other text fields keep their normal arrow behavior. This is command recall; Tab completion is unavailable.

### Choosing between `meeting`, `followup` and `due`

| You want to… | Use |
|---|---|
| Record where, when, or in what role you met someone | `meeting 1 ev/Hack Singapore md/2026-10-20 r/Mentor` |
| Plan when to contact them again | `followup 1 d/2026-11-01` |
| See who needs a follow-up today | `due` |

### Recording meeting context: `meeting`

Format: `meeting INDEX [ev/EVENT] [md/YYYY-MM-DD] [r/ROLE]`

1. Run `meeting 1 ev/Hack Singapore md/2026-10-20 r/Mentor` to record the meeting.
1. Run `meeting 1 r/Participant` to change just the role. Omitted fields stay unchanged.
1. Run `meeting 1 clear date` to remove one field, or `meeting 1 clear` to remove all meeting details.
1. Use `find ev/singapore` to find the contact later.

* Clear one field with `clear event`, `clear date`, or `clear role`.
* Event and role accept 1–80 UTF-16 code units; most letters count once, some emoji twice. Blank values are rejected.
* Dates must be real `YYYY-MM-DD` dates, years 0001–9999. `2028-02-29` works; `2027-02-29` does not.
* Meeting fields do not generate tags. The card shows a short event preview; `view INDEX` shows the full context.
* Real changes update last-edited time and add one undo step; unchanged values do neither.

![Meeting details](images/meeting-v1.4.1.png)

### Setting a follow-up date: `followup`

Format: `followup INDEX d/YYYY-MM-DD` or `followup INDEX clear`

1. Run `followup 1 d/2026-11-01` to set or replace a date.
1. Run `view 1` to see it in full.
1. Run `followup 1 clear` to remove it.

Use real dates in the same format as meeting dates. Repeating an existing date or clearing an absent date changes nothing. A real change updates last-edited time and can be undone.

Cards show `Follow up: DATE`. Dates before today in Singapore show `(Overdue)` in red and bold. Today is due, but not overdue. Refresh the list after midnight to update the card cue.

### Showing contacts due for follow-up: `due`

1. Run `due` to list dates on or before today's Singapore date, earliest first. Equal dates keep the original order; absent/future dates are excluded.
1. Use the indexes now shown to view or change a contact.
1. Run `due` again after midnight to refresh its cutoff, or `list` to reset everything.

`due` replaces the filter and sort. A later `find`, `group`, or `filter` replaces its filter but keeps due-date order. `sort due` is unavailable. The heading shows `Due on/before DATE - Sorted by due - N contact(s)`.

Clearing a due date or moving it after today removes that contact from the due list. If it was viewed, selection clears; a different viewed contact stays selected. Running `due` alone keeps the detail panel, including a hidden contact. Zero due contacts is normal.

### Contact photos: `photo`

Format: `photo INDEX f/RELATIVE_PATH` or `photo INDEX clear`

1. Put a PNG or JPEG inside the folder containing `larperboard.jar`, for example `images/me.png`.
1. Run `photo 1 f/images/me.png` to attach it. The app copies it into its managed `photos/` folder.
1. Run `view 1` to see the photo. Repeat with a different file to replace it.
1. Run `photo 1 clear` to remove the photo reference.

* Images must be readable, at most **5 MiB (5,242,880 bytes)** and **4096×4096 pixels**. The extension must match the decoded image format.
* Use a relative path inside the JAR folder. Absolute paths, URLs, `..`, and symlinks escaping that folder are rejected. The JAR folder must be writable.
* Spaces in filenames work. If a path contains ` f/`, quote it: `photo 1 f/"my f/portrait.png"`. Close both quotes and put nothing after them.
* Photos appear only in the detail panel, scaled inside a 160×120 frame. Narrow panels place the photo below the name. Missing/invalid images show `No photo` and do not block other contacts.
* An identical current image or clearing an absent photo is a no-op. Real changes support undo/redo.
* Replacing, clearing, deleting, or failing to save **does not delete imported files**. Keep them while contacts or session history may refer to them.

![Contact photo](images/photo-v1.4.1.png)

### Saving the data

Successful commands save automatically, except `help` and `exit`. A save failure restores the contacts, filter, sort, selection, and undo/redo stacks. Check file/folder access, then retry. Technical details go to the log.

| Data | Default location |
|---|---|
| Contacts | `data/addressbook.json` under the working folder (where you launch the app). The status bar shows the path. |
| Imported photos | `photos/` beside the JAR, even if launched from another working folder. Gradle development uses the working folder. |
| Window settings | `preferences.json` in the working folder. |

### Editing the data file

Prefer commands for routine edits. For direct JSON edits:

1. Close LarperBoard.
1. Back up `data/addressbook.json` and the managed `photos/` folder.
1. Edit the JSON using the [data field reference](DataFormat.md).
1. Reopen LarperBoard and confirm your contacts loaded **before running another command**.

**Invalid JSON or contact values start the app with an empty list. A later successful command, even `list`, can overwrite the invalid file.** If contacts are unexpectedly missing, close the app with `exit`, restore/repair your backup, and reopen it. `help` and `exit` do not overwrite contact data.

### Fixing a rejected command

| Problem | What to do |
|---|---|
| Unknown command | Use a command in the list shown, or open `help`. |
| Invalid format | Follow the short hint. Keep prefixes together and put the index first. |
| Invalid index | Run `list` or inspect the current results; use one positive displayed index. |
| Duplicate field | Supply single-value prefixes once. Tags and typed search terms may repeat. |
| Invalid text/date | Check the field limits and use a real `YYYY-MM-DD` date. |
| Photo rejected | Check its relative path, readable PNG/JPEG format, size, and dimensions. |
| Save failure | Check that the data file and its folder are writable; retry. The attempted change was rolled back. |

## FAQ

**How do I move to another computer?** Close both apps. Copy `data/addressbook.json` into the new working folder and `photos/` beside the new JAR. Keep relative paths. Back up destination files first. Copy `preferences.json` only for the same window settings; session histories do not transfer.

**Does it export or import contacts?** There is no export, CSV/QR import, or archive command. JSON saving and photo import are available. GitHub/X/Telegram fields, fuzzy search, statistics, clipboard commands, and Tab completion are unavailable.

## Known issues

* Meeting text counts some emoji twice; truncating an event preview can split one. See [#195](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/195).
* If the app opens off-screen after removing a monitor, close it, back up/delete `preferences.json`, then reopen.
* If `help` does not restore a minimized help window, restore that window manually.
* See the `view` section for the hidden-selection bug awaiting [#239](https://github.com/AY2627S1-CS2103T-W12-2/tp/pull/239).

## Command summary

| Task | Command |
|---|---|
| Help | `help` or F1 |
| Add | `add n/NAME [p/PHONE] [e/EMAIL] [l/HANDLE] [t/TAG]...` |
| Edit | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [l/HANDLE] [t/TAG]...` |
| Add tags | `tag INDEX t/TAG [t/TAG]...` |
| Note | `note INDEX nt/NOTE` or `note INDEX clear` |
| Star | `star INDEX` / `unstar INDEX` |
| Find | `find WORD [WORD]...` or `find [n/TERM] [t/TERM] [nt/TERM] [ev/TERM]...` |
| Group | `group t/TAG` |
| Starred filter | `filter starred [off]` |
| Sort | `sort added\|edited\|viewed` |
| All contacts | `list` |
| Full details | `view INDEX` |
| Delete | `delete INDEX` |
| Delete all | `clear` |
| Meeting | `meeting INDEX [ev/EVENT] [md/YYYY-MM-DD] [r/ROLE]` or `meeting INDEX clear [event\|date\|role]` |
| Follow-up | `followup INDEX d/YYYY-MM-DD` or `followup INDEX clear` |
| Due contacts | `due` |
| Photo | `photo INDEX f/PATH` or `photo INDEX clear` |
| History | `undo` / `redo` |
| Recall | Up / Down in the command box |
| Close | `exit` |

### Prefix cheat sheet

| Prefix | Meaning | Commands |
|---|---|---|
| `n/` | Name | add, edit, find |
| `p/` | Phone | add, edit |
| `e/` | Email | add, edit |
| `l/` | LinkedIn handle | add, edit |
| `t/` | Tag | add, edit, tag, group, find |
| `nt/` | Note | note, find |
| `ev/` | Meeting event | meeting, find |
| `md/` | Meeting date | meeting |
| `r/` | Meeting role | meeting |
| `d/` | Follow-up date | followup |
| `f/` | Photo path | photo |
