package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.PhotoCommandParser.PREFIX_FILE;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.PhotoCommand;
import seedu.address.storage.PhotoStorage;

public class PhotoCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            PhotoCommand.MESSAGE_USAGE);

    @TempDir
    private Path folder;

    /*
     * The word clear comes after the index. A command that starts with it has no index, or has the words in the
     * wrong order, so it reports the usage and not an invalid index.
     */
    @Test
    public void parse_clearInPlaceOfTheIndex_throwsUsageAndNotInvalidIndex() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));

        assertParseFailure(parser, " clear", INVALID_FORMAT);
        assertParseFailure(parser, " CLEAR", INVALID_FORMAT);
        assertParseFailure(parser, " clear 1", INVALID_FORMAT);
    }

    @Test
    public void parse_clearInPlaceOfTheIndexWithDuplicateFile_throwsUsageAndNotDuplicatePrefix() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));

        assertParseFailure(parser, " clear f/a.png f/b.png", INVALID_FORMAT);
        assertParseFailure(parser, " CLEAR 1 f/a.png f/b.png", INVALID_FORMAT);
    }

    @Test
    public void parse_indexWithDuplicateFile_stillThrowsDuplicatePrefix() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));

        assertParseFailure(parser, " 1 f/a.png f/b.png", getErrorMessageForDuplicatePrefixes(PREFIX_FILE));
    }

    @Test
    public void parse_wrongIndexBeforeClear_stillThrowsInvalidIndex() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));

        assertParseFailure(parser, " 0 clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, " abc clear", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertParseFailure(parser, " abc f/portrait.png", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void parse_indexThenClear_stillParses() throws Exception {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));

        // PhotoCommand has no equals, so check the kind of command that comes back
        assertTrue(parser.parse(" 1 clear") instanceof PhotoCommand);
        // clear is accepted in any letter case in the second position too, as it is in the first
        assertTrue(parser.parse(" 1 CLEAR") instanceof PhotoCommand);
        assertTrue(parser.parse(" 1 f/portrait.png") instanceof PhotoCommand);
    }
}
