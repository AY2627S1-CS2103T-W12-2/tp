package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.logic.commands.GroupCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a {@link GroupCommand} object.
 */
public class GroupCommandParser implements Parser<GroupCommand> {

    /**
     * Parses the given arguments in the context of the {@code GroupCommand}.
     *
     * @throws ParseException if the user input does not conform to the expected format.
     */
    @Override
    public GroupCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty() || argMultimap.getValue(PREFIX_TAG).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, GroupCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);
        Tag tag = ParserUtil.parseTag(argMultimap.getValue(PREFIX_TAG).get());
        return new GroupCommand(tag);
    }
}
