package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Meeting;
import seedu.address.model.person.Person;

/** Updates supplied meeting fields or explicitly clears selected fields of a displayed contact. */
public class MeetingCommand extends Command {
    public static final String COMMAND_WORD = "meeting";
    public static final String MESSAGE_USAGE = "meeting INDEX ev/EVENT md/YYYY-MM-DD r/ROLE\n"
            + "Supply at least one field, or use meeting INDEX clear [event|date|role].";
    public static final String MESSAGE_NO_UPDATE = "Specify a meeting field or use 'meeting INDEX clear'.";

    private final Index index;
    private final Meeting updates;
    private final String clearField;

    /** Creates an update; null fields in updates are left unchanged. Empty clearField clears all context. */
    public MeetingCommand(Index index, Meeting updates, String clearField) {
        this.index = requireNonNull(index);
        this.updates = requireNonNull(updates);
        this.clearField = clearField;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person target = model.getFilteredPersonList().get(index.getZeroBased());
        Meeting replacement = merge(target.getMeeting());
        if (replacement.equals(target.getMeeting())) {
            return new CommandResult("Meeting context unchanged.");
        }
        boolean editsViewedPerson = target.equals(model.getViewedPerson().get());
        model.editPerson(target, target.toBuilder().withMeeting(replacement).build());
        if (editsViewedPerson
                && !model.getFilteredPersonList().contains(model.getViewedPerson().get())) {
            model.setViewedPerson(null);
        }
        return new CommandResult("Meeting context updated for " + target.getName() + ".");
    }

    /** Combines optional updates with existing values, or removes the explicitly named field. */
    private Meeting merge(Meeting existing) {
        if ("".equals(clearField)) {
            return Meeting.EMPTY;
        }
        String event = "event".equals(clearField) ? null
                : updates.getEventName().orElse(existing.getEventName().orElse(null));
        LocalDate date = "date".equals(clearField) ? null
                : updates.getDate().orElse(existing.getDate().orElse(null));
        String role = "role".equals(clearField) ? null
                : updates.getRole().orElse(existing.getRole().orElse(null));
        return new Meeting(event, date, role);
    }
}
