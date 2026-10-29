package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LogRedactorTest {

    @Test
    public void redactCommand_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> LogRedactor.redactCommand(null));
    }

    @Test
    public void redactCommand_noteCommandWithArguments_hidesTheArguments() {
        assertEquals("note [hidden]", LogRedactor.redactCommand("note 1 nt/private text"));
        // any form of note command, including a wrong one that still contains text, and extra spaces
        assertEquals("note [hidden]", LogRedactor.redactCommand("note 1 clear"));
        assertEquals("note [hidden]", LogRedactor.redactCommand("note 1 some private text"));
        assertEquals("note [hidden]", LogRedactor.redactCommand("   note    1 nt/private text   "));
    }

    @Test
    public void redactCommand_meetingCommand_hidesValidAndMalformedArguments() {
        for (String command : new String[]{"meeting 1 ev/Private event r/Private role", "meeting 1 clear",
            "meeting invalid private text", "  meeting\t1 ev/Private event  "}) {
            assertEquals("meeting [hidden]", LogRedactor.redactCommand(command));
        }
        assertEquals("meeting", LogRedactor.redactCommand("meeting"));
        assertEquals("meetings 1 ev/text", LogRedactor.redactCommand("meetings 1 ev/text"));
        assertEquals("[hidden]", LogRedactor.redactArguments("meeting", " 1 ev/Private event r/Private role"));
    }

    @Test
    public void redactCommand_personalTypedSearch_hidesAllArgumentsRegardlessOfFieldOrder() {
        for (String arguments : new String[]{"ev/Private event", "n/Alice ev/Private event", "ev/Private event t/Hack",
            "ev/First ev/Second", "Alice ev/Private event", "ev/", "n/Alice\nev/Private event", "nt/Private note"}) {
            assertEquals("find [hidden]", LogRedactor.redactCommand("find " + arguments));
            assertEquals("[hidden]", LogRedactor.redactArguments("find", " " + arguments));
        }
        assertEquals("find Alice", LogRedactor.redactCommand("find Alice"));
        assertEquals("find n/Alice t/Hack", LogRedactor.redactCommand("find n/Alice t/Hack"));
        assertEquals("find Steve/event", LogRedactor.redactCommand("find Steve/event"));
        assertEquals(" n/Alice t/Hack", LogRedactor.redactArguments("find", " n/Alice t/Hack"));
    }

    @Test
    public void redactCommand_commandWordInAnyLetterCase_stillHidesPersonalText() {
        assertEquals("NOTE [hidden]", LogRedactor.redactCommand("NOTE 1 nt/private text"));
        assertEquals("Meeting [hidden]", LogRedactor.redactCommand("Meeting 1 ev/Private event"));
        assertEquals("FIND [hidden]", LogRedactor.redactCommand("FIND nt/Private note"));
        assertEquals("[hidden]", LogRedactor.redactArguments("NOTE", " 1 nt/private text"));
        assertEquals("FIND Alice", LogRedactor.redactCommand("FIND Alice"));
    }

    @Test
    public void redactCommand_otherCommands_areUnchanged() {
        assertEquals("note", LogRedactor.redactCommand("note"));
        assertEquals("delete 1", LogRedactor.redactCommand("delete 1"));
        assertEquals("add n/Amy Bee", LogRedactor.redactCommand("add n/Amy Bee"));
        // only the command word note is special, not other words that start with it
        assertEquals("notes 1 nt/text", LogRedactor.redactCommand("notes 1 nt/text"));
        assertEquals("", LogRedactor.redactCommand(""));
    }

    @Test
    public void redactArguments() {
        assertThrows(NullPointerException.class, () -> LogRedactor.redactArguments(null, ""));
        assertThrows(NullPointerException.class, () -> LogRedactor.redactArguments("note", null));

        assertEquals("[hidden]", LogRedactor.redactArguments("note", " 1 nt/private text"));
        assertEquals(" 1", LogRedactor.redactArguments("delete", " 1"));
    }
}
