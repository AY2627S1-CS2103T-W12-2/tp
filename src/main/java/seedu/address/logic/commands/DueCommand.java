package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import seedu.address.model.Model;
import seedu.address.model.SortCriterion;
import seedu.address.model.person.FollowUpDuePredicate;

/**
 * Shows the contacts whose follow-up date is today or earlier, earliest date first.
 */
public class DueCommand extends Command {

    public static final String COMMAND_WORD = "due";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows contacts to follow up with, due on or "
            + "before today, earliest first.\n"
            + "Example: " + COMMAND_WORD;
    public static final String MESSAGE_SUCCESS = "Due for follow-up on or before %1$s.\n%2$d contact(s) listed!";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        // today is read when the command runs, in the clock's zone (Asia/Singapore), and never rescheduled
        LocalDate today = LocalDate.now(model.getClock());
        model.updateFilteredPersonList(new FollowUpDuePredicate(today), "Due on/before " + today);
        // set after the filter so that due wins over any sort that was active
        model.sortFilteredPersonList(SortCriterion.DUE);
        return new CommandResult(String.format(MESSAGE_SUCCESS, today, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DueCommand;
    }

    @Override
    public int hashCode() {
        return DueCommand.class.hashCode();
    }
}
