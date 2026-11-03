# LarperBoard v1.5 — assigned beta-test report

Date: 1 November 2026 (Asia/Singapore). Connected GitHub account: `jvally`.
Scope: [#268](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/268), with results for the shared [#267 tracker](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/267). This report archives results collected on 1 November 2026 and prepared for publication on 3 November 2026. The published JAR was not rerun during report preparation. Only the assigned LarperBoard coverage is included.

## Outcome

**328 assertions passed across five runs. No new defect was found in the assigned coverage.** This count includes command acceptance/rejection and state assertions; it is not a count of independent test scenarios. One existing behavior, #191, was reproduced and recorded under its existing `not_planned` disposition. Product source was not changed.

| Run | Passed assertions | Evidence |
| --- | ---: | --- |
| Fresh writable folder | 143 | [fresh/fresh-results.log](fresh/fresh-results.log) |
| Separate-process fresh-data reopen | 8 | [fresh/reopen-results.log](fresh/reopen-results.log) |
| Backed-up synthetic legacy-format JSON | 142 | [legacy/legacy-results.log](legacy/legacy-results.log) |
| Separate-process legacy-data reopen | 8 | [legacy/reopen-results.log](legacy/reopen-results.log) |
| Selection, timestamp, privacy, and close regressions | 27 | [fresh/supplement-results.log](fresh/supplement-results.log) |

## Exact build and environment

- Downloaded the published [v1.5 release asset](https://github.com/AY2627S1-CS2103T-W12-2/tp/releases/tag/v1.5), not a local build. Release ID `428070674`, JAR asset ID `689047139`; release tag commit `308487e0c1b3aba043ae387d2500800e168b2bb9` as recorded by the release.
- JAR SHA-256: `7ca2b5aef912c885ee4179259c09bdaff422e9b5bf6c350287b9d83c486be351`, matching GitHub's release asset digest.
- macOS 27.0.1, build 26A434, Apple M4. Zulu OpenJDK `25.0.3`, build `25.0.3+9-LTS`, Zulu `25.34+17-CA`, with JavaFX runtime modules `25.0.3` in this installed FX JDK.
- JavaFX reported a 1512×982 logical primary display, output scaling `2.0×2.0` (Retina), 127 DPI. Test stage requested 1280×720; captured content is 1280×692 after window chrome. Native 150% scaling was not tested.
- All data was synthetic and isolated in `fresh/` and `legacy/`. No personal contact file was used. Legacy data is a deliberately constructed pre-note/pre-star schema fixture, not a user's historical file. The original backup SHA-256 was `b28389c280851fdda9f093b2bcb1b200501978ad9d71d37bce5a5c307fa9d879`. The archived [legacy fixture](legacy/fixture.json) has the same JSON content with a final newline added; its SHA-256 is `d4d4e7102a20d6cf1d18c74bb1c3dfff1d0b58c9274f258c8b4af3d60679c4f8`.
- The [tagged v1.5 guide](https://github.com/AY2627S1-CS2103T-W12-2/tp/blob/v1.5/docs/UserGuide.md) was read during testing. It matched the `master` guide byte-for-byte when fetched on the test date; this is not a comparison with later guide revisions. Help's Copy URL matched the documented hosted guide URL exactly. No discrepancy was observed in the assigned command behavior.

## Checklist results

| Assigned checks | Result and evidence |
| --- | --- |
| Note set/replace/clear | PASS. Leading/trailing spaces stripped; punctuation, slash and emoji retained. Blank note rejected without changing saved note. |
| Note 1000/1001 limits | PASS for both ASCII and single-code-point emoji: 1000 accepted, 1001 rejected, previous value retained. |
| Long-note scrolling | PASS. Rendered detail content exceeded viewport and reached bottom. [fresh/long-note.png](fresh/long-note.png) was visually inspected: wrapped emoji note, scrollbar and bottom activity fields readable. |
| Repeat note no-ops | PASS. Duplicate set and clear did not displace the preceding real change in undo history. Supplemental delayed check confirmed unchanged activity timestamps for repeat note/star. |
| Star/unstar and filters | PASS. Repeats accepted as no-ops; `filter starred` showed only the starred contact; unstar produced an empty result and cleared the removed viewed contact. Undo restored it and redo removed it again. `filter starred off` restored all contacts. |
| Card/detail indicators | PASS. Star indicators visible when starred and hidden when unstarred. [fresh/journey.png](fresh/journey.png) visually confirmed both gold indicators and a single filtered contact. |
| Recall and draft | PASS. Up/Down browsed submitted commands, restored unfinished draft, included failed commands, deduplicated consecutive identical submissions, respected oldest/newest bounds, and retained exactly the latest 50 entries after 51 unique submissions. `list` preserved earlier entries. Editing recalled text did not alter its stored entry. |
| Arrows outside command box | PASS for events delivered to result text area; Up/Down left command draft unchanged. This is programmatic key-event coverage. |
| Help command/menu/F1 and Copy URL | PASS through actual UI handlers. Clipboard contents were restored after checking Copy URL. |
| Unknown command | PASS. Rejected with `Unknown command. Available commands: ...` and a visible error state. |
| Exit with Help open | PASS. Exit feedback remained visible before the delayed close, then both windows closed. |
| Window close and hidden-window regression | PASS. A window close-request event alone closed main and Help; directly hiding main also hid Help (#242). Native title-bar clicking was not performed. |
| Private-text logging | PASS. A synthetic sentinel was absent from persistent app logs after invalid note index/syntax, uppercase command/prefix, oversized note and malformed note-search prefix cases. Valid uppercase `NOTE` and valid note search were checked with a second sentinel, also absent. Harness transcripts intentionally contain synthetic command text; privacy assertions inspect `addressbook.log.0`, not those transcripts. |
| Persistence journey | PASS. Set note → star → view → starred filter → Help → exit → separate process reopen. Exact note and star survived, disk state matched model, recall reset and undo was empty. Repeated for backed-up legacy-format data. |

## Reproduction and command evidence

Archived transcripts omit trailing whitespace to meet repository checks; assertion messages and counts are unchanged.

The external Java harness compiled only test drivers against the downloaded JAR. It initialized the release's `MainApp` and real JavaFX windows, delivered command-box action/key events, observed rendered controls and model/storage state, and captured snapshots. This is automated UI testing with visual inspection of saved screenshots; it is not a manual keyboard/mouse beta session.

Representative sequence (full command responses and assertions are in the logs):

```text
clear
add n/Beta Tester p/91234567 e/beta@example.com t/test
add n/Other Tester p/92345678
note 1 nt/  Met at booth / A; punctuation: !? 😀
note 1 nt/Replacement / slash 😀
note 1 nt/Replacement / slash 😀
undo
redo
note 1 clear
note 1 clear
undo
redo
note 1 nt/   [rejected]
note 1 nt/[1000 ASCII letters, then 1001; 1000 emoji, then 1001]
view 1
star 1
star 1
undo
redo
filter starred
unstar 1
undo
redo
filter starred off
note 1 nt/Reconnect after demo 😀 /backend
star 1
view 1
filter starred
help
exit
```

The archived drivers are [Beta268.java](Beta268.java) and [Supplement268.java](Supplement268.java). Use a JavaFX-enabled JDK 25+ with display access. Download the published v1.5 JAR into this report's folder and verify the SHA-256 above. Run each driver in a disposable folder to avoid reusing personal contact data. Paths below are relative to this report's folder:

```sh
javac -cp larperboard.jar Beta268.java
javac -cp larperboard.jar:. Supplement268.java
# Use new disposable folders, separate from the archived evidence:
mkdir run-fresh
cd run-fresh
java -ea -cp ../larperboard.jar:.. Beta268
java -ea -cp ../larperboard.jar:.. Beta268 reload
# Run supplemental checks after the fresh journey:
java -ea -cp ../larperboard.jar:.. Supplement268
cd ..
mkdir -p run-legacy/data
cp legacy/fixture.json run-legacy/data/addressbook.json
cd run-legacy
java -ea -cp ../larperboard.jar:.. Beta268 old
java -ea -cp ../larperboard.jar:.. Beta268 reload
cd ..
```

## Existing issues and disposition

- [#173](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/173): PASS regression. A thousand emoji are accepted; 1001 are rejected. Issue already closed as completed.
- [#245](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/245): PASS regression for the tested valid/invalid private-text cases. Issue already closed as completed. This is not a claim that every possible private command argument is redacted.
- [#242](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/242): PASS regression for close-request and direct hidden-stage paths. Issue already closed as completed.
- [#191](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/191): REPRODUCED EXISTING LIMITATION, not a new report. `note 1 nt/SearchNeedle; find nt/SearchNeedle; view 1; note 1 clear` leaves zero displayed contacts but keeps Beta Tester viewed. Replacing the note with `Replaced` has the same outcome. Screenshot: [fresh/regression191-clear.png](fresh/regression191-clear.png). This issue is already closed with `state_reason: not_planned`; the shipped guide's view section explicitly lists selection-clearing mutations and omits `note`. Retained under that existing disposition; no claim of a new owner triage discussion or owner sign-off.

No new issue was filed. Existing issue searches and the four referenced regressions were reviewed before concluding this report.

## Limits and completion state

- Native physical key input, native title-bar clicking, Windows/Linux, secondary monitors and native 150% scaling: NOT TESTED. Automated JavaFX event delivery and macOS Retina snapshots cover this environment only.
- Actual historical customer files: NOT TESTED. Legacy compatibility was checked with the backed-up synthetic old-format file described above.
- A first sandboxed launch could not enumerate a display. The successful runs used display access. That launch failure was environmental and was not counted as a product defect.
- No local product build or broad repository test suite was substituted for the published artifact.
- All #268 checklist areas have results or explicit scope limitations. This report does not close #268 or the shared #267 tracker, and does not sign off the other four independent testers' assignments.
