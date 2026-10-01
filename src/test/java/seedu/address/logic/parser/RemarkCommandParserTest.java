package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validInput_success() throws Exception {
        RemarkCommand expectedCommand = new RemarkCommand(Index.fromOneBased(2),
                new Remark("Likes baseball"));

        assertEquals(expectedCommand, parser.parse("2 r/Likes baseball"));
    }

    @Test
    public void parse_emptyRemark_success() throws Exception {
        RemarkCommand expectedCommand = new RemarkCommand(Index.fromOneBased(1), new Remark(""));

        assertEquals(expectedCommand, parser.parse("1 r/"));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("not-an-index r/remark"));
    }
}
