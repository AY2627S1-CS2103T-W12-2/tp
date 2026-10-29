package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.ConcurrentDataChangeException;
import seedu.address.storage.DataFileBusyException;
import seedu.address.storage.PhotoStorage;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_SAVE_ERROR =
            "Could not save contacts. Nothing changed. Check file/folder access.";

    public static final String MESSAGE_SAVE_PERMISSION_ERROR =
            "Could not save contacts. Nothing changed. Allow file/folder writes.";

    public static final String MESSAGE_SAVE_CONFLICT =
            "Contacts changed in another copy. Nothing changed. Reopen the app before trying again.";

    public static final String MESSAGE_SAVE_BUSY =
            "Another copy is saving contacts. Nothing changed. Try again.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, PhotoStorage.forApplication());
    }

    /** Constructs logic with photo storage rooted at an explicit application folder. */
    public LogicManager(Model model, Storage storage, PhotoStorage photos) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser(photos);
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        // a note is personal, so the text of a note command is kept out of the log
        logger.info("----------------[USER COMMAND][" + LogRedactor.redactCommand(commandText) + "]");

        Command command = addressBookParser.parseCommand(commandText);
        Model.Snapshot before = model.createSnapshot();
        CommandResult commandResult;
        try {
            commandResult = command.execute(model);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Restoring model after unexpected command failure", e);
            model.restore(before);
            throw e;
        }

        // Help and exit do not change contact data and must remain available after a save failure.
        // Only HelpCommand and ExitCommand currently set these action flags.
        if (commandResult.isShowHelp() || commandResult.isExit()) {
            return commandResult;
        }

        try {
            storage.saveAddressBook(model.getAddressBook());
        } catch (ConcurrentDataChangeException e) {
            logger.log(Level.WARNING, "Restoring model after data conflict", e);
            model.restore(before);
            throw new CommandException(MESSAGE_SAVE_CONFLICT, e);
        } catch (DataFileBusyException e) {
            logger.log(Level.WARNING, "Restoring model after concurrent save", e);
            model.restore(before);
            throw new CommandException(MESSAGE_SAVE_BUSY, e);
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Restoring model after failed save", e);
            model.restore(before);
            throw new CommandException(MESSAGE_SAVE_PERMISSION_ERROR, e);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Restoring model after failed save", ioe);
            model.restore(before);
            throw new CommandException(MESSAGE_SAVE_ERROR, ioe);
        }

        if (command.recordsHistory()) {
            model.recordSuccessfulMutation(before);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ReadOnlyStringProperty getPersonListStatus() {
        return model.getPersonListStatus();
    }

    @Override
    public ReadOnlyObjectProperty<Person> getViewedPerson() {
        return model.getViewedPerson();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
