package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Locale;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.SortCriterion;

/**
 * Parses input arguments and creates a new {@code SortCommand} object.
 */
public class SortCommandParser implements Parser<SortCommand> {

    @Override
    public SortCommand parse(String args) throws ParseException {
        String criterion = args.trim().toLowerCase(Locale.ROOT);
        return switch (criterion) {
            case "added" -> new SortCommand(SortCriterion.ADDED);
            case "edited" -> new SortCommand(SortCriterion.EDITED);
            case "viewed" -> new SortCommand(SortCriterion.VIEWED);
            default -> throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE));
        };
    }
}
