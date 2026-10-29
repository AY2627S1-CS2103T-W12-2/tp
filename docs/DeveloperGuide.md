---
layout: page
title: Developer Guide
---

Start here to run LarperBoard, find the code for a feature, and make a safe change. For command syntax, use the [User Guide](UserGuide.md).

[Run the app](#setting-up-getting-started) · [Architecture](#architecture) · [Change a command](#add-or-change-a-command) · [Feature internals](#implementation) · [Reference guides](#documentation-logging-testing-dev-ops)

## Setting up, getting started

1. Install JDK 25 and clone your fork of this repository.
1. Open a terminal in the repository root and run:

   ```sh
   ./gradlew run
   ```

1. In the app, run `add n/Test Contact`, `find Test`, then `view 1` to inspect the new contact.
1. Run the checks before changing code:

   ```sh
   ./gradlew clean check javadoc shadowJar
   ```

On Windows, use `gradlew.bat`. For IntelliJ configuration, see [Setting up](SettingUp.md). For release checks and CI, see [DevOps](DevOps.md).

## Design

### Architecture

A command follows this path:

```text
CommandBox → LogicManager → AddressBookParser → Command.execute(Model)
                         → Storage.saveAddressBook → history entry → UI update
```

1. `CommandBox` sends the entered text to `LogicManager.execute`.
1. `AddressBookParser` chooses a command; its parser validates the arguments.
1. `LogicManager` captures a model snapshot, then executes the command.
1. The command reads/changes `Model`. JavaFX properties update the UI.
1. Logic saves the contacts. On failure, it restores the snapshot. On success, real contact mutations enter history.

`help` and `exit` return UI action flags and skip contact saving. Other successful commands currently save even if they do not change contacts.

![Application architecture](images/ArchitectureDiagram.png)

| Component | Start here | Responsibility |
|---|---|---|
| Startup | `MainApp` | Create and connect the components; load data and preferences. |
| UI | `MainWindow`, `CommandBox`, `PersonDetailPanel` | Submit commands and display model state. |
| Logic | `LogicManager`, `AddressBookParser` | Parse, execute, save, roll back, and record history. |
| Model | `ModelManager`, `Person`, `AddressBook` | Store immutable contacts, visible results, selection, and session history. |
| Storage | `JsonAddressBookStorage`, `JsonAdaptedPerson`, `PhotoStorage` | Read/write JSON and validate local images. |
| Shared code | `commons` | Indexes, validation helpers, settings, and logging. |

Source files are under [`src/main/java/seedu/address`](https://github.com/AY2627S1-CS2103T-W12-2/tp/tree/master/src/main/java/seedu/address). UI layouts are under [`src/main/resources/view`](https://github.com/AY2627S1-CS2103T-W12-2/tp/tree/master/src/main/resources/view). Components use the `Logic`, `Model`, and `Storage` interfaces; their implementations are the corresponding managers.

### UI component

1. Edit the relevant `.fxml` layout and Java controller together.
1. Bind displayed values to model properties; do not keep a second contact store in the UI.
1. Keep file reads and photo decoding off the JavaFX thread.

`PersonCard` uses `TagRow` for one tag row with `+N` overflow. Tag colors are deterministic. `PersonDetailPanel` wraps all tags and scrolls full notes/meeting details. Both show a star marker; only the detail panel shows a photo.

`ContactPhoto` loads validated image bytes on one background worker. Replaced requests cannot display stale results. Photos fit a 160×120 frame; when the header is narrower than 400 pixels, the frame moves below the name. Missing images use the same-size placeholder.

### Logic component

To trace `edit 1 p/91234567`, open these files in order:

1. `AddressBookParser`: routes `edit` to `EditCommandParser`.
1. `EditCommandParser`: parses the index and optional fields into a descriptor.
1. `EditCommand.execute`: resolves the current displayed index, validates duplicates, and builds a complete contact copy.
1. `ModelManager.editPerson`: applies the edit and timestamp.
1. `LogicManager.execute`: saves or rolls back, then records history.

### Model component

`Person` is immutable. Always start with `person.toBuilder()` when changing an existing contact; it keeps fields the command does not edit.

```java
Person updated = target.toBuilder().withNote(note).build();
model.editPerson(target, updated);
```

Use `Model.editPerson`, which preserves existing activity and stamps a real change. `setPerson` is a lower-level replacement and does not apply those edit rules. A new `Person` field must be included in its builder, equality/detail comparison, JSON adapter, and tests.

### Storage component

1. `JsonAdaptedPerson` validates persisted fields and converts them to `Person`.
1. `JsonAddressBookStorage` writes a temporary file beside the destination.
1. It atomically replaces the JSON file, preserving existing POSIX permissions. Unsupported atomic moves fail; there is no partial-write fallback.

The default JSON path is relative to the **working folder**. Photos are relative to the **JAR folder**. See [Data format](DataFormat.md) for field rules and recovery. Saving over a symlink replaces the link rather than its target. Failed writes remove the temporary file; a process crash can leave `addressBook-*.tmp` files to remove later.

### Common classes

Reuse `Index`, `ParserUtil`, `Messages`, and existing value objects for validation. Use `LogsCenter` for diagnostics. Note/meeting text and typed searches containing those fields must be redacted; never put private input into error messages.

## Implementation

### Add or change a command

1. Reproduce the problem with a focused test in `src/test/java/seedu/address`.
1. Add or update `logic/commands/<Name>Command.java` and its parser in `logic/parser`.
1. For a new command, register it in `AddressBookParser` and the available-command list.
1. Resolve indexes through `model.getFilteredPersonList()` **when the command executes**. Capture the target before edits can reorder that list.
1. Build changes with `target.toBuilder()` and apply them through `model.editPerson`.
1. Return a short `CommandResult`. Leave saving, rollback, and history to `LogicManager`.
1. Add parser, command, and integration tests for changed behavior. Include failed/no-op paths and filtered indexes where relevant.
1. Update the User Guide example and command summary. Run `./gradlew check javadoc`.

Do not add separate save/history code to feature commands. If the command changes selection or filters, verify rollback and undo/redo too.

### Find input validation

`FindCommandParser` accepts plain whole-name words or typed partial searches (`n/`, `t/`, `nt/`, `ev/`). Repeated terms in one field use OR; different fields use AND.

1. Reject empty input and unsupported reserved fields (`e/`, `p/`, `l/`, `d/`, `md/`, `r/`, `f/`).
1. Reject spaces inside search prefixes, such as `t /react`.
1. Tokenize typed fields. Reject blank values and a plain preamble mixed with typed fields.
1. Create `TypedSearchPredicate`, or `NameContainsKeywordsPredicate` for plain words.

Literal `c/o`, `ai/ml`, and URLs remain valid typed text. Reserved fields remain syntax even inside note/event search text. Invalid parsing leaves the model, disk, and history untouched.

### Meeting context implementation

1. `MeetingCommandParser` validates the displayed index, duplicate fields, clear grammar, text bounds, and dates.
1. `MeetingCommand` merges only supplied fields into the existing `Meeting` value.
1. A real change calls `editPerson`; unchanged values return without mutation.
1. Shared logic saves and records history, or restores the snapshot on failure.

Event/role limits use `String.length()` (80 UTF-16 code units). Dates reuse `Meeting.parseDate`: real `YYYY-MM-DD`, years 0001–9999. Context fields are independent of tags. JSON omits empty meeting context; absent/null fields stay absent, while malformed present values reject loading.

![Meeting update sequence](images/MeetingUpdateSequenceDiagram.png)

### Contact photo implementation

1. `PhotoCommandParser` reads one relative path or clear form, including quoted paths containing ` f/`.
1. `PhotoCommand` validates the displayed index **before importing**.
1. `PhotoStorage` rejects absolute/traversal/symlink escapes, bounds reads to 5 MiB plus one byte, checks dimensions before decoding, and verifies extension/format agreement.
1. An accepted image is copied into a confined `photos/` directory with a generated name. An identical current image is a no-op.
1. The contact stores only the relative `photoPath`; shared logic handles saving and undo/redo.

Images are at most 4096×4096. Tests inject a temporary photo root. Imported assets are retained after replacement, clear, deletion, and failed saves so history can restore references. Unsafe saved paths are never passed directly to JavaFX. Missing/invalid references show a placeholder and do not reject otherwise valid contacts.

![Photo import sequence](images/PhotoImportSequenceDiagram.png)

### Contact activity

| Action | Timestamp behavior |
|---|---|
| Add | Set `createdAt` from `Model.now()`. |
| Real contact edit | Preserve creation/view times; set `lastEditedAt`. |
| View | Set `lastViewedAt`; do not record a contact mutation. |
| Failure or no-op | Keep timestamps and history unchanged. |

`Activity` stores optional whole-second UTC `Instant` values. `ModelManager` holds an injectable `Clock`; its default display zone is `Asia/Singapore`. Missing legacy times stay unknown. The UI shows `yyyy-MM-dd HH:mm` in Singapore time, or `—`.

### Follow-up dates and the due list

1. `FollowUpCommandParser` checks index, duplicate `d/`, and date/clear syntax. It reuses `Meeting.parseDate` but reports follow-up-specific guidance.
1. `FollowUpCommand` compares old/new values; equal values do not edit the model.
1. A real change uses `editPerson` and clears selection only if the viewed target leaves the active results.
1. `DueCommand` captures today's Singapore date, applies `FollowUpDuePredicate`, then sets `SortCriterion.DUE`.

Due means date ≤ today; overdue means date < today. `Person.isFollowUpDueOn` and `isFollowUpOverdueOn` define those boundaries. Due dates sort earliest first, with stable original-order ties. The cutoff is captured when `due` runs; it does not refresh at midnight automatically. Run it again to refresh.

### Activity-time sorting

`SortCriterion` defines `ADDED`, `EDITED`, `VIEWED`, and internal `DUE` comparators. Only the first three are accepted by `sort`. Newest known activity appears first; unknown times appear last. `ModelManager` breaks ties by current original-list position.

`SortCommandParser` normalizes activity keywords with `Locale.ROOT`; unknown keywords and extra arguments receive a short syntax hint.

The visible list is a `FilteredList` followed by a `SortedList`. Keep references to selected targets before a mutation/view can reorder it. `list` clears both filter and sort; a new filter keeps the sort.

### Active filter label

Use `Model.updateFilteredPersonList(predicate, description)` for filter changes. The heading combines filter, order, and live count; it does not expose private search terms. Snapshots include the description so undo/redo and rollback restore consistent feedback.

### Viewed contact selection

`Model.getViewedPerson()` is independent of list membership. `find`, `group`, `filter`, and `due` keep it, including for zero results. `view` chooses by current displayed index. Delete/clear remove selection when the selected contact is removed.

When the viewed contact is edited, `ModelManager.setPerson` updates the selected object. Commands that hide their viewed target clear it. The known `EditCommand` bug that also clears a different hidden contact is tracked in [#227](https://github.com/AY2627S1-CS2103T-W12-2/tp/issues/227); do not treat its pending fix as merged behavior.

### Starring contacts

`StarCommand` handles star/unstar through a boolean target state. Repeating the same state is a no-op. A real change uses the complete builder and `editPerson`. Unstarring the viewed contact in the starred filter clears selection when it leaves the list.

### Undo history

1. Before execution, `LogicManager` captures contacts, selection, predicate, sort, filter label, and both history stacks.
1. After a successful save, `recordSuccessfulMutation` compares contact data while ignoring view times.
1. A real mutation pushes the previous state onto the 50-entry undo stack and clears redo.
1. `undo` moves the current state to redo and restores the previous state; `redo` reverses that movement.
1. A save failure restores the pre-command snapshot and both stacks.

Later explicit views of surviving contacts are preserved. Deleted contacts return with their saved view time. Snapshots include every contact field but only photo references, not image bytes. History is session-only; restored contacts are saved to JSON.

![Undo and redo history](images/UndoRedoHistoryStateDiagram.svg)

### Command history

`CommandHistory` is separate from undo/redo. `CommandBox` records submissions before execution, including failures. It keeps 50 commands, removes consecutive duplicates, preserves the draft while browsing, and handles plain Up/Down only in the command box. List/clear/undo do not erase recall history.

## Documentation, logging, testing, dev-ops

| Task | Guide |
|---|---|
| Edit guide pages or diagrams | [Documentation](Documentation.md) |
| Change application logs | [Logging](Logging.md) |
| Run automated tests | [Testing](Testing.md) |
| Check UI, persistence, and boundary cases | [Manual testing](ManualTesting.md) |
| Build, CI, Pages, and release | [DevOps](DevOps.md) |
| Understand supported/deferred scope | [Requirements](Requirements.md) |
| Inspect saved fields and repair JSON | [Data format](DataFormat.md) |

<a id="appendix-requirements"></a>
<a id="user-stories"></a>
<a id="use-cases"></a>
<a id="non-functional-requirements"></a>
<a id="glossary"></a>

## Appendix: Requirements

See [Requirements](Requirements.md) for user stories, use cases, and quality requirements. Not implemented: export, CSV/QR import, fuzzy/LinkedIn search, name/starred sorting, statistics, Tab completion, extra social platforms, clipboard commands, or archiving. JSON saving and photo import do not imply contact export/import support.

<a id="appendix-instructions-for-manual-testing"></a>

## Appendix: Instructions for manual testing

Follow the [manual test checklist](ManualTesting.md) using a temporary installation. Record the commit/JAR hash, environment, steps, expected result, and actual result for each failure. Search existing issues before filing a new report.

## Acknowledgements

Based on [SE-EDU AddressBook-Level3](https://github.com/se-edu/addressbook-level3). Libraries include JavaFX, Jackson, JUnit, and Gradle; see `build.gradle` for versions. Diagram sources are in `docs/diagrams`.
