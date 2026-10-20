package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Shows only the starred contacts, or shows all contacts again.
 */
public class FilterCommand extends Command {

    public static final String COMMAND_WORD = "filter";
    public static final String KEYWORD_STARRED = "starred";
    public static final String KEYWORD_OFF = "off";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows only the starred contacts, or shows all contacts again.\n"
            + "Parameters: " + KEYWORD_STARRED + " [" + KEYWORD_OFF + "]\n"
            + "Examples: " + COMMAND_WORD + " " + KEYWORD_STARRED + "\n"
            + "          " + COMMAND_WORD + " " + KEYWORD_STARRED + " " + KEYWORD_OFF;

    public static final String MESSAGE_STARRED_ON = "Showing only starred contacts.\n%1$d contact(s) listed!";
    public static final String MESSAGE_STARRED_OFF = "Showing all contacts.\n%1$d contact(s) listed!";

    private static final Logger logger = LogsCenter.getLogger(FilterCommand.class);

    private final boolean starredOnly;

    /**
     * Creates a {@code FilterCommand} that shows only starred contacts if {@code starredOnly} is true, and
     * all contacts otherwise.
     */
    public FilterCommand(boolean starredOnly) {
        this.starredOnly = starredOnly;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(starredOnly ? Person::isStarred : Model.PREDICATE_SHOW_ALL_PERSONS);

        // the detail panel must not keep showing a contact that the list no longer contains
        Person viewed = model.getViewedPerson().get();
        if (viewed != null && !model.getFilteredPersonList().contains(viewed)) {
            model.setViewedPerson(null);
            logger.fine("Cleared the viewed contact because the filter hides it");
        }

        int count = model.getFilteredPersonList().size();
        logger.info("Starred filter " + (starredOnly ? "on" : "off") + ": " + count + " contact(s) shown");
        return new CommandResult(String.format(starredOnly ? MESSAGE_STARRED_ON : MESSAGE_STARRED_OFF, count));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FilterCommand otherFilterCommand)) {
            return false;
        }

        return starredOnly == otherFilterCommand.starredOnly;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("starredOnly", starredOnly)
                .toString();
    }
}
