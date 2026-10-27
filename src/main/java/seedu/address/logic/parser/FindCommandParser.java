package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TypedSearchPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Pattern PREFIX_LIKE_TOKEN = Pattern.compile("(?:^|\\s)[A-Za-z]+/");
    // Reserve other commands' fields without rejecting literal text such as c/o, ai/ml or URLs.
    private static final Pattern UNSUPPORTED_FIELD = Pattern.compile("(?:^|\\s)(?:e|p|l|d|md|r|f)/");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (UNSUPPORTED_FIELD.matcher(trimmedArgs).find()) {
            throw invalidFormat();
        }

        ArgumentMultimap fields = ArgumentTokenizer.tokenize(args, CliSyntax.PREFIX_NAME, CliSyntax.PREFIX_TAG,
                CliSyntax.PREFIX_NOTE, CliSyntax.PREFIX_EVENT);
        List<String> nameTerms = fields.getAllValues(CliSyntax.PREFIX_NAME);
        List<String> tagTerms = fields.getAllValues(CliSyntax.PREFIX_TAG);
        List<String> noteTerms = fields.getAllValues(CliSyntax.PREFIX_NOTE);
        List<String> eventTerms = fields.getAllValues(CliSyntax.PREFIX_EVENT);

        if (!nameTerms.isEmpty() || !tagTerms.isEmpty() || !noteTerms.isEmpty() || !eventTerms.isEmpty()) {
            if (!fields.getPreamble().isBlank()) {
                throw new ParseException(FindCommand.MESSAGE_MIXED_SEARCH);
            }
            if (hasBlankTerm(nameTerms) || hasBlankTerm(tagTerms)
                    || hasBlankTerm(noteTerms) || hasBlankTerm(eventTerms)) {
                throw invalidFormat();
            }
            return new FindCommand(new TypedSearchPredicate(nameTerms, tagTerms, noteTerms, eventTerms));
        }
        if (PREFIX_LIKE_TOKEN.matcher(trimmedArgs).find()) {
            throw invalidFormat();
        }
        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

    private static boolean hasBlankTerm(List<String> terms) {
        return terms.stream().anyMatch(String::isBlank);
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

}
