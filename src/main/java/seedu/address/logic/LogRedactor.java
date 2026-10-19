package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.NoteCommand;

/**
 * Hides personal text from what is written to the application log.
 * The text of a note is personal, so the arguments of a {@code note} command are replaced by
 * {@value #HIDDEN} wherever a command is logged. Every other command is logged as typed.
 */
public final class LogRedactor {

    public static final String HIDDEN = "[hidden]";

    private LogRedactor() {}

    /**
     * Returns {@code commandText} as it may be logged: a {@code note} command keeps only its command word,
     * and any other command is returned unchanged.
     */
    public static String redactCommand(String commandText) {
        requireNonNull(commandText);
        String[] wordAndArguments = commandText.strip().split("\\s+", 2);
        boolean hasNoteArguments = wordAndArguments.length == 2
                && wordAndArguments[0].equals(NoteCommand.COMMAND_WORD);
        return hasNoteArguments ? NoteCommand.COMMAND_WORD + " " + HIDDEN : commandText;
    }

    /**
     * Returns the {@code arguments} of the command with {@code commandWord} as they may be logged:
     * hidden for a {@code note} command, and unchanged for any other command.
     */
    public static String redactArguments(String commandWord, String arguments) {
        requireNonNull(commandWord);
        requireNonNull(arguments);
        return commandWord.equals(NoteCommand.COMMAND_WORD) ? HIDDEN : arguments;
    }
}
