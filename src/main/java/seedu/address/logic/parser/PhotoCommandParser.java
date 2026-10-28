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
    private static final Pattern QUOTED_FILE = Pattern.compile("\\s*(\\S+)\\s+f/\"([^\"]*)\"\\s*");
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
                throw usage();
            }
            Index index = ParserUtil.parseDisplayedIndex(quotedFile.group(1));
            String source = quotedFile.group(2);
            if (source.isBlank()) {
                throw usage();
            }
            return new PhotoCommand(index, source, photos);
        }
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, PREFIX_FILE);
        String[] preamble = fields.getPreamble().split("\\s+");
        if (preamble[0].isEmpty() || ParserUtil.isClearKeyword(preamble[0])) {
            throw usage();
        }
        fields.verifyNoDuplicatePrefixesFor(PREFIX_FILE);
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble[0]);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, e);
        }
        if (preamble.length == 2 && ParserUtil.isClearKeyword(preamble[1]) && fields.getValue(PREFIX_FILE).isEmpty()) {
            return new PhotoCommand(index, "", photos);
        }
        if (preamble.length != 1 || fields.getValue(PREFIX_FILE).orElse("").isBlank()) {
            throw usage();
        }
        return new PhotoCommand(index, fields.getValue(PREFIX_FILE).orElseThrow(), photos);
    }

    private ParseException usage() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PhotoCommand.MESSAGE_USAGE));
    }
}
