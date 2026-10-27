package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEETING_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.util.Locale;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Meeting;

/** Parses partial meeting updates and explicit clear forms without changing omitted fields. */
public class MeetingCommandParser implements Parser<MeetingCommand> {
    @Override
    public MeetingCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, PREFIX_EVENT, PREFIX_MEETING_DATE, PREFIX_ROLE);
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
        fields.verifyNoDuplicatePrefixesFor(PREFIX_EVENT, PREFIX_MEETING_DATE, PREFIX_ROLE);
        boolean hasFields = fields.getValue(PREFIX_EVENT).isPresent()
                || fields.getValue(PREFIX_MEETING_DATE).isPresent() || fields.getValue(PREFIX_ROLE).isPresent();
        if (preamble.length > 1) {
            String field = preamble.length == 3 ? preamble[2].toLowerCase(Locale.ROOT) : "";
            if (hasFields || !ParserUtil.isClearKeyword(preamble[1]) || preamble.length > 3
                    || (preamble.length == 3 && !Set.of("event", "date", "role").contains(field))) {
                throw usage();
            }
            return new MeetingCommand(index, Meeting.EMPTY, field);
        }
        if (!hasFields) {
            throw new ParseException(MeetingCommand.MESSAGE_NO_UPDATE);
        }
        return parseUpdate(index, fields);
    }

    /** Converts validated optional fields into an update, retaining field-specific validation messages. */
    private MeetingCommand parseUpdate(Index index, ArgumentMultimap fields) throws ParseException {
        try {
            Meeting updates = new Meeting(fields.getValue(PREFIX_EVENT).orElse(null),
                    fields.getValue(PREFIX_MEETING_DATE).map(Meeting::parseDate).orElse(null),
                    fields.getValue(PREFIX_ROLE).orElse(null));
            return new MeetingCommand(index, updates, null);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    private ParseException usage() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MeetingCommand.MESSAGE_USAGE));
    }
}
