package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.SortCriterion;

/**
 * Sorts the currently visible contacts by an activity timestamp.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Sorts the currently displayed contacts.\n"
            + "Parameters: added|edited|viewed\n"
            + "Example: " + COMMAND_WORD + " viewed";
    public static final String MESSAGE_SUCCESS = "Sorted by %1$s (newest first).\n%2$d contact(s) listed!";

    private final SortCriterion sortCriterion;

    public SortCommand(SortCriterion sortCriterion) {
        this.sortCriterion = requireNonNull(sortCriterion);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.sortFilteredPersonList(sortCriterion);
        return new CommandResult(String.format(MESSAGE_SUCCESS, sortCriterion.getCommandArgument(),
                model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof SortCommand otherSortCommand)) {
            return false;
        }

        return sortCriterion == otherSortCommand.sortCriterion;
    }

    @Override
    public int hashCode() {
        return sortCriterion.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("sortCriterion", sortCriterion)
                .toString();
    }
}
