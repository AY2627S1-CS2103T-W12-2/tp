package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PhotoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.storage.PhotoStorage;

/** Parses a single photo source or the explicit clear form. Importing happens only after index validation. */
public class PhotoCommandParser implements Parser<PhotoCommand> {
    public static final Prefix PREFIX_FILE = new Prefix("f/");
    public static final String MESSAGE_QUOTED_PATH =
            "Use one f/ path. Quote paths containing \" f/\": f/\"my f/portrait.png\"";
    public static final String MESSAGE_UNCLOSED_QUOTE =
            "Close both double quotes around the path: f/\"portrait.png\"";
    public static final String MESSAGE_AFTER_QUOTE =
            "Put nothing after the closing quote: f/\"portrait.png\".";
    private static final Pattern QUOTED_FILE = Pattern.compile("\\s*(\\S+)\\s+f/\\s*\"([^\"]*)\"\\s*");
    private final PhotoStorage photos;

    public PhotoCommandParser(PhotoStorage photos) {
        this.photos = requireNonNull(photos);
    }

    @Override
    public PhotoCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher quotedFile = QUOTED_FILE.matcher(args);
        if (quotedFile.matches()) {
            if (ParserUtil.isClearKeyword(quotedFile.group(1))) {
                throw usage(PhotoCommand.MESSAGE_MISSING_INDEX);
            }
            Index index = ParserUtil.parseDisplayedIndex(quotedFile.group(1));
            String source = quotedFile.group(2);
            if (source.isBlank()) {
                throw usage(PhotoCommand.MESSAGE_MISSING_PATH);
            }
            return new PhotoCommand(index, source, photos);
        }
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, PREFIX_FILE);
        String[] preamble = fields.getPreamble().split("\\s+");
        if (preamble[0].isEmpty() || ParserUtil.isClearKeyword(preamble[0])) {
            throw usage(PhotoCommand.MESSAGE_MISSING_INDEX);
        }
        try {
            fields.verifyNoDuplicatePrefixesFor(PREFIX_FILE);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_QUOTED_PATH, e);
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble[0]);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, e);
        }
        String source = fields.getValue(PREFIX_FILE).orElse("");
        if (preamble.length > 1 && ParserUtil.isClearKeyword(preamble[1])) {
            if (fields.getValue(PREFIX_FILE).isPresent()) {
                throw usage(PhotoCommand.MESSAGE_NOT_BOTH);
            }
            if (preamble.length != 2) {
                throw usage(PhotoCommand.MESSAGE_EXTRA_WORDS);
            }
            return new PhotoCommand(index, "", photos);
        }
        if (source.startsWith("\"")) {
            throw new ParseException(source.indexOf('"', 1) >= 0 ? MESSAGE_AFTER_QUOTE : MESSAGE_UNCLOSED_QUOTE);
        }
        if (preamble.length != 1) {
            throw usage(fields.getValue(PREFIX_FILE).isPresent()
                    ? PhotoCommand.MESSAGE_EXTRA_WORDS : PhotoCommand.MESSAGE_WITHOUT_PREFIX);
        }
        if (source.isBlank()) {
            throw usage(PhotoCommand.MESSAGE_MISSING_PATH);
        }
        return new PhotoCommand(index, fields.getValue(PREFIX_FILE).orElseThrow(), photos);
    }

    private ParseException usage(String hint) {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, hint));
    }
}
