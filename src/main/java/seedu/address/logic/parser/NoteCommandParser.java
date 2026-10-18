package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Note;

/**
 * Parses arguments for a {@code note} command.
 */
public class NoteCommandParser implements Parser<NoteCommand> {

    /**
     * Parses {@code INDEX nt/NOTE} into a command that sets the note, or {@code INDEX clear} into a command
     * that removes it.
     *
     * @param args arguments following the {@code note} command word
     * @throws ParseException if the arguments are not in either form, the index is invalid, or the note is
     *         blank or too long
     */
    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_NOTE);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NOTE);

        String[] words = argMultimap.getPreamble().isBlank()
                ? new String[0] : argMultimap.getPreamble().strip().split("\\s+");
        boolean hasNotePrefix = argMultimap.getValue(PREFIX_NOTE).isPresent();

        if (hasNotePrefix && words.length == 1) {
            return new NoteCommand(parseIndex(words[0]), ParserUtil.parseNote(argMultimap.getValue(PREFIX_NOTE).get()));
        }
        if (!hasNotePrefix && words.length == 2 && words[1].equalsIgnoreCase(NoteCommand.KEYWORD_CLEAR)) {
            return new NoteCommand(parseIndex(words[0]), Note.EMPTY);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_USAGE));
    }

    private static Index parseIndex(String index) throws ParseException {
        try {
            return ParserUtil.parseIndex(index);
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, pe);
        }
    }

}
