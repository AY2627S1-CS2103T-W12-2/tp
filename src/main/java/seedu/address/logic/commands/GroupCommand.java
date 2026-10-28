package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.TagMatchesPredicate;
import seedu.address.model.tag.Tag;

/**
 * Lists contacts with the specified tag.
 */
public class GroupCommand extends Command {

    public static final String COMMAND_WORD = "group";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows contacts with the specified tag.\n"
            + "Parameters: t/TAG\n"
            + "Example: " + COMMAND_WORD + " t/react";
    public static final String MESSAGE_SUCCESS = "Group: %1$s\n%2$d contact(s) listed!";

    private final Tag tag;
    private final TagMatchesPredicate predicate;

    /**
     * Creates a {@code GroupCommand} that lists contacts with {@code tag}.
     */
    public GroupCommand(Tag tag) {
        this.tag = requireNonNull(tag);
        predicate = new TagMatchesPredicate(tag);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate, "Group: " + tag.tagName);
        return new CommandResult(String.format(MESSAGE_SUCCESS, tag, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof GroupCommand otherGroupCommand)) {
            return false;
        }

        return tag.equals(otherGroupCommand.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("tag", tag)
                .toString();
    }
}
