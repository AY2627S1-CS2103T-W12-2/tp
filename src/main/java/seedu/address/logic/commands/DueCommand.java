package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;

import seedu.address.model.Model;
import seedu.address.model.person.FollowUpDuePredicate;
import seedu.address.model.person.Person;

/**
 * Lists all persons whose follow-up date is today or earlier, ordered by follow-up date.
 * Today is the local date at the moment the command runs.
 */
public class DueCommand extends Command {

    public static final String COMMAND_WORD = "due";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists the persons whose follow-up date is today "
            + "or earlier, earliest date first.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "%1$d person(s) due for follow-up.";
    public static final String MESSAGE_NONE_DUE = "No one is due for follow-up.";

    /** Earliest date first. The sort is stable, so persons with the same date keep their address book order. */
    private static final Comparator<Person> BY_FOLLOW_UP_DATE =
            Comparator.comparing(person -> person.getFollowUpDate().getDate().orElse(LocalDate.MAX));

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate today = LocalDate.now(model.getClock());

        // filtering resets the sort order, so sort afterwards
        model.updateFilteredPersonList(new FollowUpDuePredicate(today));
        model.updateSortedPersonList(BY_FOLLOW_UP_DATE);

        int dueCount = model.getFilteredPersonList().size();
        return new CommandResult(dueCount == 0 ? MESSAGE_NONE_DUE : String.format(MESSAGE_SUCCESS, dueCount));
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
