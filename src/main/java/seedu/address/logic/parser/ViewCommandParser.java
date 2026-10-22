package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MULTIPLE_INDEXES;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for a {@code view} command.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Parses a single displayed-list index into a {@code ViewCommand}.
     *
     * @param args arguments following the {@code view} command word
     * @return the command targeting the specified index
     * @throws ParseException if the arguments contain multiple indexes or an invalid index
     */
    @Override
    public ViewCommand parse(String args) throws ParseException {
        validateSingleIndex(args);
        return new ViewCommand(parseViewIndex(args));
    }

    /** Ensures that the command contains at most one index parameter. */
    private static void validateSingleIndex(String args) throws ParseException {
        if (args.trim().split("\\s+").length > 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MESSAGE_MULTIPLE_INDEXES + "\n" + ViewCommand.MESSAGE_USAGE));
        }
    }

    /** Parses the index while preserving the usage message for a missing index. */
    private static Index parseViewIndex(String args) throws ParseException {
        if (args.trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE));
        }

        return ParserUtil.parseDisplayedIndex(args);
    }

}
