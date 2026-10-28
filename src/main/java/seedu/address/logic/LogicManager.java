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
import seedu.address.storage.PhotoStorage;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

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
        } catch (AccessDeniedException e) {
            logger.log(Level.WARNING, "Restoring model after failed save", e);
            model.restore(before);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Restoring model after failed save", ioe);
            model.restore(before);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
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
