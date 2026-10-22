package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Restores the most recently undone saved contact mutation.
 */
public class RedoCommand extends Command {
    public static final String COMMAND_WORD = "redo";
    public static final String MESSAGE_USAGE = "redo: Reapplies the most recently undone contact change.\n"
            + "Parameters: none";
    public static final String MESSAGE_SUCCESS = "Redid the last undone contact change.";
    public static final String MESSAGE_NOTHING_TO_REDO = "Nothing to redo.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.canRedo()) {
            throw new CommandException(MESSAGE_NOTHING_TO_REDO);
        }
        model.redo();
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean recordsHistory() {
        return false;
    }
}
