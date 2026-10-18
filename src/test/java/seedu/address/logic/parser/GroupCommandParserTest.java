package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.GroupCommand;
import seedu.address.logic.Messages;
import seedu.address.model.tag.Tag;

public class GroupCommandParserTest {

    private final GroupCommandParser parser = new GroupCommandParser();

    @Test
    public void parse_validArgs_returnsGroupCommand() {
        assertParseSuccess(parser, " t/React", new GroupCommand(new Tag("react")));
    }

    @Test
    public void parse_missingTag_throwsParseException() {
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, GroupCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_emptyTag_throwsParseException() {
        assertParseFailure(parser, " t/", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, " t/react!", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateTags_throwsParseException() {
        assertParseFailure(parser, " t/react t/backend",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TAG));
    }

    @Test
    public void parse_preamble_throwsParseException() {
        assertParseFailure(parser, " react", String.format(MESSAGE_INVALID_COMMAND_FORMAT, GroupCommand.MESSAGE_USAGE));
    }
}
