package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;

/**
 * Sets, replaces or clears the note of a person identified using its displayed index.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String KEYWORD_CLEAR = "clear";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets, replaces or clears the note of the person identified by the index number used in the "
            + "displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_NOTE + "NOTE\n"
            + "            or INDEX " + KEYWORD_CLEAR + " to remove the note\n"
            + "Examples: " + COMMAND_WORD + " 1 " + PREFIX_NOTE + "Met at the AI track, wants a React teammate\n"
            + "          " + COMMAND_WORD + " 1 " + KEYWORD_CLEAR;

    public static final String MESSAGE_SET_SUCCESS = "Saved the note for %1$s.";
    public static final String MESSAGE_CLEAR_SUCCESS = "Cleared the note for %1$s.";
    public static final String MESSAGE_UNCHANGED = "The note for %1$s is already that text. Nothing was changed.";
    public static final String MESSAGE_NO_NOTE_TO_CLEAR = "%1$s has no note to clear. Nothing was changed.";

    private static final Logger logger = LogsCenter.getLogger(NoteCommand.class);

    private final Index targetIndex;
    private final Note note;

    /**
     * Creates a {@code NoteCommand} that gives the person at {@code targetIndex} the given {@code note}.
     * An empty {@code note} clears the note.
     */
    public NoteCommand(Index targetIndex, Note note) {
        requireNonNull(targetIndex);
        requireNonNull(note);
        this.targetIndex = targetIndex;
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (lastShownList.isEmpty()) {
            logger.fine("Note command rejected: the displayed list is empty");
            throw new CommandException(Messages.MESSAGE_EMPTY_PERSON_LIST);
        }
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.fine("Note command rejected: index " + targetIndex.getOneBased()
                    + " is outside the displayed list of " + lastShownList.size());
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(targetIndex.getZeroBased());
        String name = personToUpdate.getName().fullName;

        if (personToUpdate.getNote().equals(note)) {
            logger.fine("Note command changed nothing for the person at index " + targetIndex.getOneBased());
            String message = note.isEmpty() ? MESSAGE_NO_NOTE_TO_CLEAR : MESSAGE_UNCHANGED;
            return new CommandResult(String.format(message, name));
        }

        model.editPerson(personToUpdate, personToUpdate.toBuilder().withNote(note).build());
        // the text of a note is personal, so only its length is logged
        logger.info("Note " + (note.isEmpty() ? "cleared" : "saved (" + note.value.length() + " characters)")
                + " for the person at index " + targetIndex.getOneBased());
        String message = note.isEmpty() ? MESSAGE_CLEAR_SUCCESS : MESSAGE_SET_SUCCESS;
        return new CommandResult(String.format(message, name));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NoteCommand otherNoteCommand)) {
            return false;
        }

        return targetIndex.equals(otherNoteCommand.targetIndex) && note.equals(otherNoteCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("note", note.toLogString())
                .toString();
    }
}
