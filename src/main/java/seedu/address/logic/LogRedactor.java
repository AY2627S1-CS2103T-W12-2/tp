package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.commands.NoteCommand;

/**
 * Hides personal text from what is written to the application log.
 * Note and meeting text is personal, so their command arguments are replaced by
 * {@value #HIDDEN} wherever a command is logged. Every other command is logged as typed.
 */
public final class LogRedactor {

    public static final String HIDDEN = "[hidden]";

    private LogRedactor() {}

    /**
     * Returns {@code commandText} as it may be logged: a personal-text command keeps only its command word,
     * and any other command is returned unchanged.
     */
    public static String redactCommand(String commandText) {
        requireNonNull(commandText);
        String[] wordAndArguments = commandText.strip().split("\\s+", 2);
        boolean hasPrivateArguments = wordAndArguments.length == 2
                && containsPersonalText(wordAndArguments[0]);
        return hasPrivateArguments ? wordAndArguments[0] + " " + HIDDEN : commandText;
    }

    /**
     * Returns the {@code arguments} of the command with {@code commandWord} as they may be logged:
     * hidden for a note or meeting command, and unchanged for any other command.
     */
    public static String redactArguments(String commandWord, String arguments) {
        requireNonNull(commandWord);
        requireNonNull(arguments);
        return containsPersonalText(commandWord) ? HIDDEN : arguments;
    }
    private static boolean containsPersonalText(String commandWord) {
        return commandWord.equals(NoteCommand.COMMAND_WORD) || commandWord.equals(MeetingCommand.COMMAND_WORD);
    }
}
