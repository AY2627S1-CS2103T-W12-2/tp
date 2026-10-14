package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {

    private final TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_validArgs_returnsTagCommand() {
        TagCommand expectedCommand = new TagCommand(INDEX_FIRST_PERSON,
                Set.of(new Tag("friends"), new Tag("work")));

        assertParseSuccess(parser, "1 t/friends t/work", expectedCommand);
    }

    @Test
    public void parse_duplicateTagsIgnoringCase_returnsSingleLowercaseTag() {
        TagCommand expectedCommand = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("java")));

        assertParseSuccess(parser, "1 t/Java t/java", expectedCommand);
    }

    @Test
    public void parse_tagAtMaximumLength_returnsTagCommand() {
        String tagName = "a".repeat(20);
        TagCommand expectedCommand = new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag(tagName)));

        assertParseSuccess(parser, "1 t/" + tagName, expectedCommand);
    }

    @Test
    public void parse_missingTags_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "1", expectedMessage);
        assertParseFailure(parser, "1 ", expectedMessage);
    }

    @Test
    public void parse_missingIndex_throwsInvalidCommandFormat() {
        assertParseFailure(parser, " t/java",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidTag_throwsTagConstraints() {
        String[] invalidTags = {"", "a".repeat(21), "two words", "bad_tag", "bad#tag"};

        for (String invalidTag : invalidTags) {
            assertParseFailure(parser, "1 t/" + invalidTag, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidIndex_throwsInvalidPersonIndex() {
        String[] invalidIndexes = {"0", "-1", "abc", "999999999999"};

        for (String invalidIndex : invalidIndexes) {
            assertParseFailure(parser, invalidIndex + " t/x", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
    }
}
