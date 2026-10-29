[![codecov](https://codecov.io/gh/AY2627S1-CS2103T-W12-2/tp/graph/badge.svg?token=69EYSQR3GS)](https://codecov.io/gh/AY2627S1-CS2103T-W12-2/tp)
[![CI Status](https://github.com/AY2627S1-CS2103T-W12-2/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103T-W12-2/tp/actions)

![Ui](docs/images/Ui-v1.4.1.png)

# LarperBoard

**LarperBoard is a desktop app for keeping track of people you meet at hackathons.** Add contact details, LinkedIn handles, and role or tech-stack tags through short commands, then find a person by name when you want to reconnect.

LarperBoard has a graphical contact list and detail panel, with command-line-style input for adding, finding, viewing, editing, and deleting contacts. Data is saved locally.

## Quick start

Install Java 25 or later, download `larperboard.jar` from the [v1.4.1 release](https://github.com/AY2627S1-CS2103T-W12-2/tp/releases/tag/v1.4.1), and run it from the folder where you want LarperBoard to save its data:

```text
java -jar larperboard.jar
```

Try `list`, then `add n/Aisha Rahman p/98765432 e/aisha@example.com l/aisha-rahman t/frontend`. See the [User Guide](docs/UserGuide.md#quick-start) for the full setup and command instructions.

## Run LarperBoard v1.4.1

With Java 25 installed, build the JAR from the project root using `./gradlew shadowJar`, then run `java -jar build/libs/larperboard.jar`.
The Help menu opens the [online User Guide](https://ay2627s1-cs2103t-w12-2.github.io/tp/UserGuide.html).

## Delete a contact

Use `delete INDEX` with one positive index from the currently displayed contact list. For example, `delete 2` removes the second displayed contact.

The command accepts only one index: `delete 1 2` reports `Only one index parameter is accepted.` An index larger than a Java integer reports `The provided index is too large.` See the [delete error cases](docs/UserGuide.md#deleting-a-person-delete) for empty or invalid lists and indexes.

See the [v1.4.1 release notes](docs/releases/v1.4.1.md) for changes, upgrade instructions, and known limitations.

## Documentation

* Using LarperBoard: see the [**User Guide**](docs/UserGuide.md).
* Developing LarperBoard: see the [**Developer Guide**](docs/DeveloperGuide.md).
* Meet the team: [**About Us**](docs/AboutUs.md).

## Acknowledgements

* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
