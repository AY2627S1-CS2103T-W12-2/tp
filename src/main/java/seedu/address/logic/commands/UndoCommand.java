package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Restores the state before the most recently saved contact mutation.
 */
public class UndoCommand extends Command {
    public static final String COMMAND_WORD = "undo";
    public static final String MESSAGE_USAGE = "undo: Restores the previous contact state.\nParameters: none";
    public static final String MESSAGE_SUCCESS = "Undid the last contact change.";
    public static final String MESSAGE_NOTHING_TO_UNDO = "Nothing to undo.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.canUndo()) {
            throw new CommandException(MESSAGE_NOTHING_TO_UNDO);
        }
        model.undo();
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean recordsHistory() {
        return false;
    }

}
