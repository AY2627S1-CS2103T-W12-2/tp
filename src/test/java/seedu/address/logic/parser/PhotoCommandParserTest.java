package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.PhotoCommandParser.PREFIX_FILE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalTimes.FIXED_CLOCK;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.PhotoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.PhotoStorage;
import seedu.address.testutil.PersonBuilder;

public class PhotoCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            PhotoCommand.MESSAGE_USAGE);
    private static final String QUOTE_HINT = "If the path contains \" f/\", put the whole path in double quotes: "
            + "f/\"my f/portrait.png\"";

    @TempDir
    private Path folder;

    @Test
    public void parse_quotedPathWithPrefixText_importsTheExactFile() throws Exception {
        assertQuotedPathImported(" 1 f/\"my f/portrait.png\"");
    }

    @Test
    public void parse_spaceBeforeQuotedPath_importsTheExactFile() throws Exception {
        assertQuotedPathImported(" 1 f/ \"my f/portrait.png\"");
    }

    private void assertQuotedPathImported(String args) throws Exception {
        Path image = folder.resolve("my f/portrait.png");
        Files.createDirectories(image.getParent());
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", image.toFile());
        PhotoStorage photos = new PhotoStorage(folder);
        ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        model.addPerson(new PersonBuilder().build());

        new PhotoCommandParser(photos).parse(args).execute(model);

        String reference = model.getFilteredPersonList().getFirst().getPhotoPath();
        assertArrayEquals(Files.readAllBytes(image), photos.readPhoto(reference).orElseThrow());
    }

    @Test
    public void parse_quotedUnsafePath_keepsStorageValidation() throws Exception {
        ModelManager model = new ModelManager(new AddressBook(), new UserPrefs(), FIXED_CLOCK);
        model.addPerson(new PersonBuilder().build());
        PhotoCommand command = new PhotoCommandParser(new PhotoStorage(folder)).parse(" 1 f/\"../portrait.png\"");

        assertThrows(CommandException.class,
                PhotoStorage.MESSAGE_INVALID, () -> command.execute(model));
    }

    @Test
    public void parse_quotedBlankPathOrMissingIndex_rejectsInvalidFormat() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));
        assertParseFailure(parser, " 1 f/\" \"", INVALID_FORMAT);
        assertParseFailure(parser, " clear f/\"portrait.png\"", INVALID_FORMAT);
        assertParseFailure(parser, " 0 f/\"portrait.png\"", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

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

        assertParseFailure(parser, " 1 f/a.png f/b.png",
                getErrorMessageForDuplicatePrefixes(PREFIX_FILE) + "\n" + QUOTE_HINT);
    }

    @Test
    public void parse_unquotedPrefixInsidePath_explainsHowToQuoteIt() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));
        assertParseFailure(parser, " 1 f/my f/portrait.png",
                getErrorMessageForDuplicatePrefixes(PREFIX_FILE) + "\n" + QUOTE_HINT);
    }

    @Test
    public void parse_unclosedQuote_explainsHowToQuoteIt() {
        PhotoCommandParser parser = new PhotoCommandParser(new PhotoStorage(folder));
        assertParseFailure(parser, " 1 f/\"my f/portrait.png",
                getErrorMessageForDuplicatePrefixes(PREFIX_FILE) + "\n" + QUOTE_HINT);
        assertParseFailure(parser, " 1 f/\"portrait.png", INVALID_FORMAT + "\n" + QUOTE_HINT);
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
