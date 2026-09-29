package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String REMARK_VALID = "Likes to swim.";
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

    private RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_indexSpecified_success() {
        // have remark
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + REMARK_VALID;
        assertParseSuccess(parser, userInput, new RemarkCommand(INDEX_FIRST_PERSON, new Remark(REMARK_VALID)));

        // no remark
        userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK;
        assertParseSuccess(parser, userInput, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")));
    }

    @Test
    public void parse_missingCompulsoryField_failure() {
        // no parameters
        assertParseFailure(parser, RemarkCommand.COMMAND_WORD, MESSAGE_INVALID_FORMAT);

        // no index
        assertParseFailure(parser, PREFIX_REMARK + REMARK_VALID, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        // negative index
        assertParseFailure(parser, "-1 " + PREFIX_REMARK + REMARK_VALID, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0 " + PREFIX_REMARK + REMARK_VALID, MESSAGE_INVALID_FORMAT);

        // non-empty preamble
        assertParseFailure(parser, INDEX_FIRST_PERSON.getOneBased() + " " + PREAMBLE_NON_EMPTY + " "
                + PREFIX_REMARK + REMARK_VALID, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedRemarkPrefix_failure() {
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + REMARK_VALID + " "
                + PREFIX_REMARK + REMARK_VALID;
        assertParseFailure(parser, userInput, getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }
}
