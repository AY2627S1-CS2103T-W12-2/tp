package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Finds and lists persons matching a plain name search or typed field search.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = "Use " + COMMAND_WORD + " KEYWORD [MORE_KEYWORDS] or "
            + COMMAND_WORD + " [n/TERM] [t/TERM] [nt/TERM] [ev/TERM].\n"
            + "Supply at least one non-empty term; typed fields may repeat.";
    public static final String MESSAGE_MIXED_SEARCH =
            "Use either plain keywords or field prefixes in one find command.\n"
                    + "Example: find Alex or find n/Alex t/react.";
    public static final String MESSAGE_PLAIN_NO_MATCH_HINT =
            "Without a prefix, find searches names only. "
            + "Search tags with find t/TAG, notes with find nt/TERM and events with find ev/TERM.";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate, "Find results");
        int count = model.getFilteredPersonList().size();
        String message = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, count);
        if (count == 0 && predicate instanceof NameContainsKeywordsPredicate) {
            message += "\n" + MESSAGE_PLAIN_NO_MATCH_HINT;
        }
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
