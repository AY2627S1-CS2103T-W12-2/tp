package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Adds tags to an existing person in the address book.
 */
public class TagCommand extends Command {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds tags to the person identified by the index "
            + "number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer) t/TAG [t/TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 t/friends t/colleagues";

    public static final String MESSAGE_SUCCESS = "Updated Person: %1$s";

    private final Index index;
    private final Set<Tag> tagsToAdd;

    /**
     * Creates a {@code TagCommand} to add {@code tagsToAdd} to the person at {@code index}.
     */
    public TagCommand(Index index, Set<Tag> tagsToAdd) {
        requireNonNull(index);
        requireNonNull(tagsToAdd);

        this.index = index;
        this.tagsToAdd = new HashSet<>(tagsToAdd);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUpdate = lastShownList.get(index.getZeroBased());
        Set<Tag> updatedTags = new HashSet<>(personToUpdate.getTags());
        updatedTags.addAll(tagsToAdd);

        if (updatedTags.equals(personToUpdate.getTags())) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(personToUpdate)));
        }

        Person updatedPerson = personToUpdate.toBuilder().withTags(updatedTags).build();
        model.editPerson(personToUpdate, updatedPerson);

        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(updatedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof TagCommand otherTagCommand)) {
            return false;
        }

        return index.equals(otherTagCommand.index) && tagsToAdd.equals(otherTagCommand.tagsToAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, tagsToAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("tagsToAdd", tagsToAdd)
                .toString();
    }
}
