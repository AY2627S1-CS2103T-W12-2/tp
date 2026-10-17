package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUpDate;
import seedu.address.model.person.Person;

/**
 * Sets, replaces or clears the follow-up date of a person identified using its displayed index.
 */
public class FollowUpCommand extends Command {

    public static final String COMMAND_WORD = "followup";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sets, replaces or clears the follow-up date of the "
            + "person identified by the index number used in the displayed person list. "
            + "Use an empty " + PREFIX_DATE + " to clear the date.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_DATE + "DATE (as YYYY-MM-DD)\n"
            + "Examples: " + COMMAND_WORD + " 1 " + PREFIX_DATE + "2026-10-25\n"
            + "          " + COMMAND_WORD + " 1 " + PREFIX_DATE;

    public static final String MESSAGE_SET_SUCCESS = "Set follow-up date: %1$s";
    public static final String MESSAGE_CLEAR_SUCCESS = "Cleared follow-up date: %1$s";
    public static final String MESSAGE_NOTHING_TO_CLEAR = "This person has no follow-up date to clear: %1$s";

    private final Index index;
    private final FollowUpDate followUpDate;

    /**
     * Creates a {@code FollowUpCommand} to set the person at {@code index} to {@code followUpDate}.
     * {@code FollowUpDate.NONE} clears the date.
     */
    public FollowUpCommand(Index index, FollowUpDate followUpDate) {
        requireNonNull(index);
        requireNonNull(followUpDate);

        this.index = index;
        this.followUpDate = followUpDate;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(index.getZeroBased());

        if (followUpDate.isEmpty() && personToUpdate.getFollowUpDate().isEmpty()) {
            return new CommandResult(String.format(MESSAGE_NOTHING_TO_CLEAR, Messages.format(personToUpdate)));
        }

        Person updatedPerson = new Person(personToUpdate.getName(), personToUpdate.getPhone(),
                personToUpdate.getEmail(), personToUpdate.getLinkedIn(), personToUpdate.getTags(), followUpDate,
                personToUpdate.getActivity());
        // editPerson does nothing when the date is the same as before
        model.editPerson(personToUpdate, updatedPerson);

        String message = followUpDate.isEmpty() ? MESSAGE_CLEAR_SUCCESS : MESSAGE_SET_SUCCESS;
        return new CommandResult(String.format(message, Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FollowUpCommand otherFollowUpCommand)) {
            return false;
        }

        return index.equals(otherFollowUpCommand.index) && followUpDate.equals(otherFollowUpCommand.followUpDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("followUpDate", followUpDate)
                .toString();
    }
}
