package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import seedu.address.logic.commands.SortCommand;
import seedu.address.model.SortCriterion;

public class SortCommandParserTest {

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_validArgs_returnsSortCommand() {
        assertParseSuccess(parser, " added ", new SortCommand(SortCriterion.ADDED));
        assertParseSuccess(parser, "edited", new SortCommand(SortCriterion.EDITED));
        assertParseSuccess(parser, " viewed ", new SortCommand(SortCriterion.VIEWED));
    }

    @Test
    public void parse_caseVariants_returnsSortCommand() {
        for (SortCriterion criterion : List.of(SortCriterion.ADDED, SortCriterion.EDITED, SortCriterion.VIEWED)) {
            String keyword = criterion.getCommandArgument();
            String capitalized = keyword.substring(0, 1).toUpperCase(Locale.ROOT) + keyword.substring(1);
            SortCommand expectedCommand = new SortCommand(criterion);

            assertParseSuccess(parser, capitalized, expectedCommand);
            assertParseSuccess(parser, " \t" + keyword.toUpperCase(Locale.ROOT) + "\n", expectedCommand);
        }
        assertParseSuccess(parser, "aDdEd", new SortCommand(SortCriterion.ADDED));
        assertParseSuccess(parser, "eDiTeD", new SortCommand(SortCriterion.EDITED));
        assertParseSuccess(parser, "vIeWeD", new SortCommand(SortCriterion.VIEWED));
    }

    @Test
    @ResourceLock(Resources.LOCALE)
    public void parse_uppercaseWithTurkishLocale_returnsSortCommand() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertParseSuccess(parser, "EDITED", new SortCommand(SortCriterion.EDITED));
            assertParseSuccess(parser, "VIEWED", new SortCommand(SortCriterion.VIEWED));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "name", expectedMessage);
        assertParseFailure(parser, "added extra", expectedMessage);
        assertParseFailure(parser, "ADDED extra", expectedMessage);
        assertParseFailure(parser, "Added EDITED", expectedMessage);
        assertParseFailure(parser, "ad ded", expectedMessage);
        assertParseFailure(parser, "ADDEDD", expectedMessage);
        assertParseFailure(parser, "DUE", expectedMessage);
    }
}
