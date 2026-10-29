---
layout: page
title: Manual testing
---

[Back to the Developer Guide](DeveloperGuide.md) · [Automated tests](Testing.md)

## Prepare an isolated installation

1. Build with `./gradlew clean check javadoc shadowJar`.
1. Copy `build/libs/larperboard.jar` into a new writable folder. Do not use personal contact data.
1. Record the commit, JAR SHA-256, OS, Java version, resolution, and display scaling.
1. Open a terminal in that folder and run `java -jar larperboard.jar`.
1. Run `clear`, then enter:

   ```text
   add n/Alice Tan p/111 t/react
   add n/Bob Lee p/222 t/java
   add n/Carl Ng p/333 t/react
   ```

Run commands separately, pressing Enter each time. Check both feedback and the displayed list. Use `list` between independent cases; indexes always refer to what is currently shown.

## Core command checks

| Check | Steps | Expected |
|---|---|---|
| Add | `add n/Dana Lim`; repeat it. | First succeeds; duplicate is rejected without changing data/history. |
| Edit | `edit 1 e/alice@example.com`; repeat it. | Email changes once; repeating leaves timestamps/history unchanged. |
| Tags | `tag 1 t/React t/mentor`; `edit 1 t/backend`; `edit 1 t/` | Add merges lowercase tags; edit replaces; empty edit removes all. |
| View | `view 1` | Full details and Singapore activity times; missing fields use placeholders. |
| Delete | `delete 1`; `undo`; `redo` | Correct displayed contact is removed, restored, then removed again. |
| Clear | `clear`; `undo` | Empty list, then all contacts restored. |
| Invalid index | Try `view 0`, `delete -1`, `edit 999 n/Test`, `view 2147483648`. | Rejected; contacts and history stay unchanged. |
| Multiple indexes | `delete 1 2`, `view 1 2` | Rejected with format/index guidance. |
| Invalid fields | Try a 51-character name, 2/21-digit phones, invalid email, and a tag with spaces. | Validation feedback; no partial contact saved. |
| Help | `help`, F1, Help menu, Copy URL. | Help window shows the correct online guide URL; copy works. |
| Exit | Type `exit`; repeat with menu/window close in separate runs. | Typed exit briefly shows its message; menu/window close closes immediately. |

## Search, filters, sort, and selection

1. Recreate Alice, Bob, and Carl if needed. Add `note 1 nt/Met at booth 4` and `meeting 1 ev/Hack Singapore`.
1. Check plain `find Alice` and typed `find n/ali`: both match Alice. `find Alic` does not match her full name word.
1. Run `find n/Alice n/Bob t/react`: only Alice matches (OR within name, AND across fields).
1. Run `find ev/singa` and `find nt/booth`: Alice matches both.
1. Try `find n/Alice e/x`, `find n/Alice t /react`, and `find n /Alice`: reject unsupported/malformed fields without changing results, selection, disk, or history.
1. Run `find Nobody`: zero results is valid. `group t/react`, `filter starred`, and `due` replace earlier filters; they do not combine.
1. Run `list`, `star 2`, `filter starred`, then `filter starred off`: Bob is the only starred result, then everyone returns.
1. Try `sort added`, `sort edited`, and `sort viewed`. Check newest known time first, unknown times last, stable ties, and matching heading/count.
1. Under `sort viewed`, run `view 2`. The viewed contact can move to the top; its details must stay selected.
1. Edit/delete/tag using the displayed index after sorting. Confirm the intended contact changes.
1. View Carl, then `find Bob`. Carl should stay in the panel. Check editing Bob against the known [#227](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/227) state before calling it a new defect.
1. View a visible contact, then edit its name so it leaves the active name search. Selection clears.
1. Run `list`: all contacts return in original order and the heading resets.

## Notes, stars, and command recall

1. Set, replace, and clear a note. Repeat the same value/clear; no-op timestamps/history stay unchanged.
1. Check blank notes and 1000/1001 UTF-16-code-unit boundaries. Supplementary emoji count twice.
1. View a long note and scroll to its end. Confirm no note text appears on cards.
1. Star/unstar the same contact twice. The first real change updates time/history; repeating does not.
1. Unstar the viewed contact under `filter starred`: it leaves the list and selection clears.
1. Focus the command box. Enter valid, invalid, and consecutive duplicate commands; use Up/Down to recall them.
1. Type an unfinished draft, browse backward, then forward past the newest entry: the draft returns.
1. Edit recalled text; older stored text must stay unchanged. After 51 distinct commands, the oldest is evicted.
1. Check that arrows in other text controls and modified arrows keep normal behavior. `list`/`clear` do not erase recall.

## Meeting and follow-up dates

1. Run `meeting 1 ev/Hack Singapore md/2026-10-20 r/Mentor`, then `meeting 1 r/Participant`: event/date stay unchanged.
1. Clear each field with `meeting 1 clear event`, `clear date`, and `clear role`; repeat with whole-context `meeting 1 clear`.
1. Check blank and 80/81-code-unit event/role bounds, duplicate prefixes, mixed clear/update, and invalid indexes. Failures preserve data/history.
1. Check `2028-02-29` accepted; `2027-02-29`, `2026-02-30`, `2026-1-5`, year zero, and empty dates rejected for both meeting and follow-up.
1. Use `find ev/singa`; changing the viewed event so it no longer matches clears selection.
1. Run `followup 1 d/2020-03-01`, `followup 2 d/2020-01-01`, and `followup 3 d/2099-12-31`, then `due`.
1. Bob appears before Alice; Carl is absent. Set one date to today's Singapore date: it is due but not overdue.
1. View a due contact, then clear its date or move it after today: it leaves the list and selection clears. A different hidden viewed contact stays selected.
1. Repeat an existing date/absent clear: no timestamp or undo step. Undo/redo a real date change.
1. With a fixed injected clock in automated tests, cross Singapore midnight and rerun `due`; the new cutoff is used. Cards refresh their overdue cue when redrawn.

## Photos and layout

1. Put two valid PNG/JPEG files beside the JAR. Attach one with `photo 1 f/one.png`, replace with the other, clear, then undo/redo each step.
1. Confirm photo references in JSON and both retained files in `photos/`. Close/reopen and confirm the photo loads.
1. Test paths with spaces and a folder named `my f`: use `photo 1 f/"my f/portrait.png"`. Reject unclosed quotes/extra text after quotes.
1. Reject absolute paths, URLs, `..`, symlinks escaping the JAR folder, and an escaping `photos/` symlink.
1. Reject missing/unreadable/corrupt files, extension/format mismatch, over-5-MiB files, and width/height over 4096. No history entry or partial contact change.
1. Block writes to `photos/`; verify copy guidance and unchanged contacts/history. Reattaching an identical current image is a no-op.
1. Remove or damage a referenced image: the app shows `No photo` and loads other contacts. Unsafe stored paths are never opened.
1. At 1280×720 and 100%, 125%, and 150% scaling, check long names/notes, all tags, scrolling, and visible command feedback. Photos appear only in details, preserve aspect ratio inside 160×120, and move below the name on narrow panels.

## History, saving, and recovery

1. Mix add/edit/tag/note/star/meeting/photo/follow-up/delete/clear changes, then undo/redo. Confirm all fields, edited times, filter, sort, selection, and saved JSON restore together.
1. View a surviving contact after an edit; undo the edit. Its later view time remains. Delete a viewed contact; undo restores its pre-deletion view time.
1. Make 60 real changes and undo 50 times: the first ten remain. Redo 50; all return. A new real change clears redo; failed/no-op/read-only commands do not.
1. Make the contact file or folder unwritable, then try a real edit and undo/redo. Verify feedback, unchanged JSON, contacts, timestamps, filter, sort, selection, and both history stacks. `help` and `exit` still work.
1. Restore access, retry, then close/reopen. Contacts persist; undo/redo and command recall are empty.
1. Launch the JAR from a different working folder: JSON belongs to that folder; photos remain beside the JAR.
1. Close the app and back up JSON. Test old JSON without optional fields: missing activity stays unknown; missing photo/meeting/note/follow-up/star fields use their defaults.
1. Test malformed timestamp/date/note/star/meeting values and null contact/tag entries. The app starts with an empty list and logs validation feedback instead of crashing. **Do not run list or another saving command before restoring the backup.**
1. See [Data format](DataFormat.md) for exact optional fields and recovery rules. Missing/unsafe photo references are tolerated separately from invalid contact fields.

## Report a failure

1. Search open and closed issues for the same behavior.
1. Reduce the case to the fewest reproducible commands with disposable data.
1. Record expected/actual results, commit/JAR hash, OS/Java, and screenshot or log evidence.
1. File only a distinct confirmed defect. Distinguish observed UI results from behavior inferred from model bindings.
