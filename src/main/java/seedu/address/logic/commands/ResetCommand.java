package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Clears the active contact filters and sort, and shows the original list order.
 */
public class ResetCommand extends Command {

    public static final String COMMAND_WORD = "reset";
    public static final String MESSAGE_SUCCESS = "Reset to the full contact list in original order.";

    @Override
    public CommandResult execute(Model model) {
        ListCommand.resetVisibleList(model);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
