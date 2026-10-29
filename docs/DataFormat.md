---
layout: page
title: Contact data format
---

[Back to the User Guide](UserGuide.md#editing-the-data-file) · [Developer Guide](DeveloperGuide.md#storage-component)

## Before editing

1. Close the app.
1. Back up `data/addressbook.json` in the working folder and `photos/` beside the JAR.
1. Keep the JSON structure: one object with a `persons` array.
1. Reopen and confirm contacts loaded before entering commands.

**Invalid data starts an empty list. A later successful command other than `help` or `exit`, including `list`, can overwrite the file.** Close with `exit` and restore/repair the backup if loading fails.

## Fields on each contact

| Field | Value | Missing/null behavior |
|---|---|---|
| `name` | Required valid name; 1–50 ASCII letters/digits/spaces, nonblank. | Reject loading. |
| `phone` | Empty string or 3–20 digits. | No phone. |
| `email` | Empty string or valid email, at most 254 characters. | No email. |
| `linkedIn` | Empty string or 3–50 letters/digits/hyphens. | No handle. |
| `tags` | Array of objects such as `{"tagName":"react"}`; each tag 1–20 letters/digits/hyphens/plus signs. Null entries are invalid. | No tags. |
| `note` | Nonblank string, at most 1000 UTF-16 code units. Save no note as null rather than empty text. | No note. |
| `starred` | JSON `true` or `false`, not a quoted string. | Not starred. |
| `createdAt`, `lastEditedAt`, `lastViewedAt` | Whole-second UTC text, e.g. `2026-10-17T04:12:33Z`. Fractions, offsets, impossible times, and nonstrings are rejected. | Unknown time; displayed as `—`. |
| `followUpDate` | Real date, `YYYY-MM-DD`, years 0001–9999. | No follow-up. |
| `meeting` | Object with optional `eventName`, `date`, and `role` (below). | No meeting context. |
| `photoPath` | Relative image path inside the JAR folder, usually `photos/contact-….png`. | No photo. Invalid/unavailable references show a placeholder instead of rejecting contacts. |

Meeting event/role must be nonblank strings of 1–80 UTF-16 code units; dates use the same real-date format as follow-up. Missing/null meeting fields stay absent. Clearing all fields omits the meeting object on save. Unknown older fields are ignored.

Example contact:

```json
{
  "persons": [{
    "name": "Jane Tan",
    "phone": "91234567",
    "email": "jane@example.com",
    "linkedIn": "jane-tan",
    "tags": [{"tagName": "react"}],
    "note": "Met at booth 4",
    "starred": true,
    "meeting": {"eventName": "Hack Singapore", "date": "2026-10-20", "role": "Mentor"},
    "followUpDate": "2026-11-01"
  }]
}
```

## Recovery and portability

Malformed contact fields reject the file as a whole; the log identifies the field. Missing files start sample data; invalid files start an empty list. Photos are different: missing/unsafe references only show a placeholder and warning.

Copy the JSON and managed photo folder together when moving installations. Imported images are retained after replacement, clear, deletion, and failed saves because contacts or undo history may still reference them. Undo/redo and command recall are never saved to JSON.
