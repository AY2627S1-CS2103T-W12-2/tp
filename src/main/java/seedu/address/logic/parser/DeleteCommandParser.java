package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for a {@code delete} command.
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {

    /** Message shown when a delete command contains more than one index. */
    public static final String MESSAGE_MULTIPLE_INDEXES = "Only one index parameter is accepted.";

    /**
     * Parses a single displayed-list index into a {@code DeleteCommand}.
     *
     * @param args arguments following the {@code delete} command word
     * @return the command targeting the specified index
     * @throws ParseException if the arguments contain multiple indexes or an invalid index
     */
    @Override
    public DeleteCommand parse(String args) throws ParseException {
        validateSingleIndex(args);
        return new DeleteCommand(parseDeleteIndex(args));
    }

    /** Ensures that the command contains at most one index parameter. */
    private static void validateSingleIndex(String args) throws ParseException {
        if (args.trim().split("\\s+").length > 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    MESSAGE_MULTIPLE_INDEXES + "\n" + DeleteCommand.MESSAGE_USAGE));
        }
    }

    /** Parses the index while preserving the usage message for a missing index. */
    private static Index parseDeleteIndex(String args) throws ParseException {
        if (args.trim().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }

        try {
            return ParserUtil.parseIndex(args);
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, pe);
        }
    }

}
