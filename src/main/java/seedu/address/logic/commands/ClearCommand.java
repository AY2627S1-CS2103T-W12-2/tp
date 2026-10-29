package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "All contacts have been cleared!";
    public static final String MESSAGE_ALREADY_EMPTY = "No contacts to clear.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        boolean alreadyEmpty = model.getAddressBook().getPersonList().isEmpty();
        model.setAddressBook(new AddressBook());
        return new CommandResult(alreadyEmpty ? MESSAGE_ALREADY_EMPTY : MESSAGE_SUCCESS);
    }
}
