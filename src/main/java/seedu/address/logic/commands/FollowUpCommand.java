package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Sets, replaces or clears the follow-up date of a displayed contact. */
public class FollowUpCommand extends Command {
    public static final String COMMAND_WORD = "followup";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " INDEX d/YYYY-MM-DD\n"
            + COMMAND_WORD + " INDEX clear\n"
            + "Example: " + COMMAND_WORD + " 1 d/2026-10-25";
    public static final String MESSAGE_SET_SUCCESS = "Follow-up date for %1$s set to %2$s.";
    public static final String MESSAGE_CLEARED = "Follow-up date cleared for %1$s.";
    public static final String MESSAGE_UNCHANGED = "Follow-up date unchanged.";

    private final Index index;
    private final LocalDate date;

    /** Creates a command that sets the date of the person at {@code index}, or clears it if {@code date} is null. */
    public FollowUpCommand(Index index, LocalDate date) {
        this.index = requireNonNull(index);
        this.date = date;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person target = model.getFilteredPersonList().get(index.getZeroBased());
        if (Objects.equals(target.getFollowUpDate().orElse(null), date)) {
            return new CommandResult(MESSAGE_UNCHANGED);
        }
        boolean editsViewedPerson = target.equals(model.getViewedPerson().get());
        model.editPerson(target, target.toBuilder().withFollowUpDate(date).build());
        if (editsViewedPerson && !model.getFilteredPersonList().contains(model.getViewedPerson().get())) {
            model.setViewedPerson(null);
        }
        String name = target.getName().fullName;
        return new CommandResult(date == null
                ? String.format(MESSAGE_CLEARED, name)
                : String.format(MESSAGE_SET_SUCCESS, name, date));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof FollowUpCommand otherCommand
                && index.equals(otherCommand.index) && Objects.equals(date, otherCommand.date));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("date", date).toString();
    }
}
