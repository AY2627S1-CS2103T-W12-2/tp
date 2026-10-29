package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
        boolean secondWordIsClear = words.length >= 2 && words[1].equalsIgnoreCase(NoteCommand.KEYWORD_CLEAR);

        boolean isSetForm = hasNotePrefix && words.length == 1;
        boolean isClearForm = !hasNotePrefix && words.length == 2 && secondWordIsClear;
        if (!isSetForm && !isClearForm) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    describeMistake(words.length, hasNotePrefix, secondWordIsClear)));
        }

        // the index comes first in both forms, and is checked before the note
        Index index = ParserUtil.parseDisplayedIndex(words[0]);
        Note note = isSetForm ? ParserUtil.parseNote(argMultimap.getValue(PREFIX_NOTE).get()) : Note.EMPTY;
        return new NoteCommand(index, note);
    }

    /**
     * Returns the hint that fits the mistake in arguments that are neither {@code INDEX nt/NOTE} nor
     * {@code INDEX clear}. {@code wordCount} is the number of words before the {@code nt/} prefix.
     */
    private static String describeMistake(int wordCount, boolean hasNotePrefix, boolean secondWordIsClear) {
        if (wordCount == 0) {
            return NoteCommand.MESSAGE_MISSING_INDEX;
        }
        if (hasNotePrefix && wordCount == 2 && secondWordIsClear) {
            return NoteCommand.MESSAGE_NOT_BOTH;
        }
        if (wordCount == 1) {
            return NoteCommand.MESSAGE_MISSING_NOTE;
        }
        if (!hasNotePrefix && !secondWordIsClear) {
            return NoteCommand.MESSAGE_NOTE_WITHOUT_PREFIX;
        }
        return NoteCommand.MESSAGE_TOO_MANY_WORDS;
    }

}
