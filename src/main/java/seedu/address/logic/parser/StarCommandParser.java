package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.StarCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for a {@code star} or an {@code unstar} command, which both take a single index.
 */
public class StarCommandParser implements Parser<StarCommand> {

    private final boolean starred;

    /**
     * Creates a parser for {@code star} if {@code starred} is true, and for {@code unstar} otherwise.
     */
    public StarCommandParser(boolean starred) {
        this.starred = starred;
    }

    /**
     * Parses a single displayed-list index into a {@code StarCommand}.
     *
     * @throws ParseException if the arguments are not exactly one index, or the index is not a positive integer
     */
    @Override
    public StarCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.strip();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    starred ? StarCommand.MESSAGE_MISSING_INDEX : StarCommand.MESSAGE_UNSTAR_MISSING_INDEX));
        }
        if (trimmedArgs.split("\\s+").length > 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    starred ? StarCommand.MESSAGE_EXTRA_WORDS : StarCommand.MESSAGE_UNSTAR_EXTRA_WORDS));
        }

        return new StarCommand(ParserUtil.parseDisplayedIndex(trimmedArgs), starred);
    }
}
