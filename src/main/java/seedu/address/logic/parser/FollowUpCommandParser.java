package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUpDate;

/**
 * Parses input arguments and creates a new {@link FollowUpCommand} object.
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {

    /**
     * Parses the given arguments in the context of the {@code FollowUpCommand}.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_DATE);

        if (argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_DATE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DATE);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, pe);
        }

        FollowUpDate followUpDate = ParserUtil.parseFollowUpDate(argMultimap.getValue(PREFIX_DATE).get());
        return new FollowUpCommand(index, followUpDate);
    }
}
