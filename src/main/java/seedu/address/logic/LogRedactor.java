package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.commands.NoteCommand;

/**
 * Hides personal text from what is written to the application log.
 * Note and meeting text is personal, so their command arguments are replaced by
 * {@value #HIDDEN} wherever a command is logged. Typed searches containing those fields are also hidden.
 */
public final class LogRedactor {

    public static final String HIDDEN = "[hidden]";
    private static final Pattern PERSONAL_SEARCH_PREFIX = Pattern.compile("(?:^|\\s)(?:ev|nt)\\s*/");

    private LogRedactor() {}

    /**
     * Returns {@code commandText} as it may be logged: a personal-text command keeps only its command word,
     * and any other command is returned unchanged.
     */
    public static String redactCommand(String commandText) {
        requireNonNull(commandText);
        String[] wordAndArguments = commandText.strip().split("\\s+", 2);
        boolean hasPrivateArguments = wordAndArguments.length == 2
                && containsPersonalText(wordAndArguments[0], wordAndArguments[1]);
        return hasPrivateArguments ? wordAndArguments[0] + " " + HIDDEN : commandText;
    }

    /**
     * Returns the {@code arguments} of the command with {@code commandWord} as they may be logged:
     * hidden for note/meeting commands and personal typed searches, otherwise unchanged.
     */
    public static String redactArguments(String commandWord, String arguments) {
        requireNonNull(commandWord);
        requireNonNull(arguments);
        return containsPersonalText(commandWord, arguments) ? HIDDEN : arguments;
    }

    private static boolean containsPersonalText(String commandWord, String arguments) {
        // normalised like AddressBookParser, so every spelling the parser accepts is redacted
        String word = commandWord.toLowerCase(Locale.ROOT);
        return word.equals(NoteCommand.COMMAND_WORD)
                || word.equals(MeetingCommand.COMMAND_WORD)
                || (word.equals(FindCommand.COMMAND_WORD) && PERSONAL_SEARCH_PREFIX.matcher(arguments).find());
    }
}
