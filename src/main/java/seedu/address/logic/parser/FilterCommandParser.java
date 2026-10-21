package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.FilterCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for a {@code filter} command, which is {@code starred} or {@code starred off}.
 */
public class FilterCommandParser implements Parser<FilterCommand> {

    /**
     * Parses {@code starred} into a command that shows only starred contacts, and {@code starred off} into one
     * that shows all contacts again. The keywords are not case-sensitive.
     *
     * @throws ParseException if the arguments are neither of these
     */
    @Override
    public FilterCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String[] words = args.isBlank() ? new String[0] : args.strip().split("\\s+");

        boolean startsWithStarred = words.length >= 1 && words[0].equalsIgnoreCase(FilterCommand.KEYWORD_STARRED);
        if (startsWithStarred && words.length == 1) {
            return new FilterCommand(true);
        }
        if (startsWithStarred && words.length == 2 && words[1].equalsIgnoreCase(FilterCommand.KEYWORD_OFF)) {
            return new FilterCommand(false);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FilterCommand.MESSAGE_USAGE));
    }
}
