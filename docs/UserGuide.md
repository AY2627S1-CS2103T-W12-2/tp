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

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Opens the help window with a link to this guide. The result message is `Opened help window.`

Format: `help`


### Adding a contact: `add`

Adds a contact to LarperBoard.

Format: `add n/NAME [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A contact can have any number of tags, including zero.
</div>

* `LINKEDIN_HANDLE` is the last part of the contact's LinkedIn profile link (e.g. `john-doe` in `linkedin.com/in/john-doe`). It may contain only letters, digits and hyphens, and must be 3 to 50 characters long.
* `NAME` must contain only alphanumeric characters and spaces and cannot be blank. Leading and trailing spaces are removed.
* `PHONE` is optional and must contain 3 to 20 digits when supplied.
* `EMAIL` is optional and must be a valid email address when supplied.
* A contact is a duplicate when its name exactly matches an existing contact's name. Names are compared case-sensitively; a different phone number does not make the name unique. Duplicate contacts cannot be added.

Examples:
* `add n/Jane Tan`
* `add n/Betsy Crowe p/1234567 e/betsycrowe@example.com l/betsy-crowe t/friend t/criminal`

### Listing all contacts: `list`

Shows a list of all contacts in LarperBoard.

Format: `list`

### Editing a contact: `edit`

Edits an existing contact in LarperBoard.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`

* Edits the contact at the specified `INDEX`. The index refers to the number shown in the displayed contact list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the contact's existing tags are replaced; adding tags with `edit` is not cumulative.
* To remove all of a contact's tags, enter `t/` without a tag after it.
* To remove a contact's LinkedIn handle, enter `l/` without a handle after it.

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

### Locating contacts by name: `find`

Finds contacts whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]…​`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Contacts matching at least one keyword are returned (an `OR` search); for example, `find Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find Aisha` returns the `Aisha Rahman` contact added in the quick start.
* `find Aisha Ben` returns contacts named `Aisha Rahman` or `Ben Tan` when both have been added.

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

Shows the selected contact's full details in the detail pane without changing the contact.

Format: `view INDEX`

* `INDEX` refers to the contact's number in the currently displayed list and **must be a positive integer** 1, 2, 3, …​
* A successful command reports `Now viewing:` followed by the contact's details.

Examples:
* `view 1` shows the 1st displayed contact.
* `find Aisha` followed by `view 1` shows the 1st contact in the search results.

### Clearing all entries: `clear`

Clears all contacts from LarperBoard.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

LarperBoard automatically saves data after every successful command. You do not need to save manually.

### Editing the data file

LarperBoard data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, LarperBoard starts with an empty contact list at the next run. The invalid file remains on disk until you run a successful command. We recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause LarperBoard to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous LarperBoard home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`<br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com l/james-ho t/friend t/colleague`
**Clear** | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit** | `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [l/LINKEDIN_HANDLE] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Exit** | `exit`
**Find** | `find KEYWORD [MORE_KEYWORDS]…​`<br> e.g., `find James Jake`
**Help** | `help`
**List** | `list`
**Tag** | `tag INDEX t/TAG [t/TAG]…​`<br> e.g., `tag 1 t/frontend t/react`
**View** | `view INDEX`<br> e.g., `view 1`
