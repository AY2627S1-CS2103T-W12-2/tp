package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEETING_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Meeting;

/** Parses partial meeting updates and explicit clear forms without changing omitted fields. */
public class MeetingCommandParser implements Parser<MeetingCommand> {
    public static final String MESSAGE_DATE = "Enter a real meeting date as YYYY-MM-DD, e.g. 2026-10-29.";

    @Override
    public MeetingCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, PREFIX_EVENT, PREFIX_MEETING_DATE, PREFIX_ROLE);
        String[] preamble = fields.getPreamble().split("\\s+");
        if (preamble[0].isEmpty() || ParserUtil.isClearKeyword(preamble[0])) {
            throw usage(MeetingCommand.MESSAGE_MISSING_INDEX);
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
            if (hasFields) {
                throw usage(ParserUtil.isClearKeyword(preamble[1])
                        ? MeetingCommand.MESSAGE_NOT_BOTH : MeetingCommand.MESSAGE_EXTRA_WORDS);
            }
            if (!ParserUtil.isClearKeyword(preamble[1])) {
                throw usage(MeetingCommand.MESSAGE_WITHOUT_PREFIX);
            }
            if (preamble.length > 3
                    || (preamble.length == 3 && !Set.of("event", "date", "role").contains(field))) {
                throw usage(MeetingCommand.MESSAGE_CLEAR_USAGE);
            }
            return new MeetingCommand(index, Meeting.EMPTY, field);
        }
        if (!hasFields) {
            throw usage(MeetingCommand.MESSAGE_NO_UPDATE);
        }
        return parseUpdate(index, fields);
    }

    /** Converts validated optional fields into an update, retaining field-specific validation messages. */
    private MeetingCommand parseUpdate(Index index, ArgumentMultimap fields) throws ParseException {
        LocalDate date;
        try {
            date = fields.getValue(PREFIX_MEETING_DATE).map(Meeting::parseDate).orElse(null);
        } catch (IllegalArgumentException e) {
            throw new ParseException(MESSAGE_DATE, e);
        }
        try {
            Meeting updates = new Meeting(fields.getValue(PREFIX_EVENT).orElse(null), date,
                    fields.getValue(PREFIX_ROLE).orElse(null));
            return new MeetingCommand(index, updates, null);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    private ParseException usage(String hint) {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, hint));
    }
}
