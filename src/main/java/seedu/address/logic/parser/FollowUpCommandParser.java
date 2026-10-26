package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP_DATE;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Meeting;

/** Parses {@code followup INDEX d/YYYY-MM-DD} and {@code followup INDEX clear}. */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {
    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, PREFIX_FOLLOW_UP_DATE);
        String[] preamble = fields.getPreamble().split("\\s+");
        if (preamble[0].isEmpty() || ParserUtil.isClearKeyword(preamble[0])) {
            throw usage();
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble[0]);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, e);
        }
        fields.verifyNoDuplicatePrefixesFor(PREFIX_FOLLOW_UP_DATE);
        boolean hasDate = fields.getValue(PREFIX_FOLLOW_UP_DATE).isPresent();

        if (preamble.length == 2 && preamble[1].equalsIgnoreCase("clear") && !hasDate) {
            return new FollowUpCommand(index, null);
        }
        if (preamble.length != 1 || !hasDate) {
            throw usage();
        }
        try {
            LocalDate date = Meeting.parseDate(fields.getValue(PREFIX_FOLLOW_UP_DATE).get());
            return new FollowUpCommand(index, date);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    private ParseException usage() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_USAGE));
    }
}
