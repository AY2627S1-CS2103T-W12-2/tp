package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Stars or unstars a person identified using its displayed index, to mark important contacts.
 */
public class StarCommand extends Command {

    public static final String COMMAND_WORD = "star";
    public static final String UNSTAR_COMMAND_WORD = "unstar";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Stars the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_UNSTAR_USAGE = UNSTAR_COMMAND_WORD
            + ": Removes the star from the person identified by the index number used in the displayed "
            + "person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + UNSTAR_COMMAND_WORD + " 1";

    public static final String MESSAGE_STAR_SUCCESS = "Starred %1$s.";
    public static final String MESSAGE_UNSTAR_SUCCESS = "Removed the star from %1$s.";
    public static final String MESSAGE_ALREADY_STARRED = "%1$s is already starred. Nothing was changed.";
    public static final String MESSAGE_NOT_STARRED = "%1$s is not starred. Nothing was changed.";

    private static final Logger logger = LogsCenter.getLogger(StarCommand.class);

    private final Index targetIndex;
    private final boolean starred;

    /**
     * Creates a {@code StarCommand} that sets whether the person at {@code targetIndex} is {@code starred}.
     */
    public StarCommand(Index targetIndex, boolean starred) {
        this.targetIndex = requireNonNull(targetIndex);
        this.starred = starred;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (lastShownList.isEmpty()) {
            throw new CommandException(Messages.MESSAGE_EMPTY_PERSON_LIST);
        }
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person person = lastShownList.get(targetIndex.getZeroBased());
        String name = person.getName().fullName;

        if (person.isStarred() == starred) {
            logger.fine("Star command changed nothing for the person at index " + targetIndex.getOneBased());
            return new CommandResult(String.format(starred ? MESSAGE_ALREADY_STARRED : MESSAGE_NOT_STARRED, name));
        }

        model.editPerson(person, person.toBuilder().withStarred(starred).build());
        logger.info((starred ? "Starred" : "Unstarred") + " the person at index " + targetIndex.getOneBased());
        return new CommandResult(String.format(starred ? MESSAGE_STAR_SUCCESS : MESSAGE_UNSTAR_SUCCESS, name));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof StarCommand otherStarCommand)) {
            return false;
        }

        return targetIndex.equals(otherStarCommand.targetIndex) && starred == otherStarCommand.starred;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("starred", starred)
                .toString();
    }
}
