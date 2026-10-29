package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.PhotoStorage;
import seedu.address.storage.PhotoStorage.PhotoException;

/** Attaches/replaces one validated local image or clears a displayed contact's photo reference. */
public class PhotoCommand extends Command {
    public static final String COMMAND_WORD = "photo";
    public static final String MESSAGE_USAGE = "Use: photo INDEX f/PATH or photo INDEX clear.";
    public static final String MESSAGE_MISSING_INDEX = "Add the contact's INDEX: photo INDEX f/PATH.";
    public static final String MESSAGE_MISSING_PATH = "Add f/PATH, or use: photo INDEX clear.";
    public static final String MESSAGE_NOT_BOTH = "Use f/PATH or clear, not both.";
    public static final String MESSAGE_WITHOUT_PREFIX = "Put the photo path after f/: photo INDEX f/PATH.";
    public static final String MESSAGE_EXTRA_WORDS = "Use one INDEX before f/PATH or clear.";

    private final Index index;
    private final String source;
    private final PhotoStorage photos;

    /** Creates an attach/replace command, or a clear command when source is empty. */
    public PhotoCommand(Index index, String source, PhotoStorage photos) {
        this.index = requireNonNull(index);
        this.source = requireNonNull(source);
        this.photos = requireNonNull(photos);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (index.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person target = model.getFilteredPersonList().get(index.getZeroBased());
        String replacement;
        try {
            replacement = source.isEmpty() ? "" : photos.importPhoto(source, target.getPhotoPath());
        } catch (PhotoException e) {
            throw new CommandException(e.getMessage(), e);
        }
        if (replacement.equals(target.getPhotoPath())) {
            return new CommandResult("Contact photo unchanged.");
        }
        model.editPerson(target, target.toBuilder().withPhotoPath(replacement).build());
        return new CommandResult("Photo updated for " + target.getName() + ".");
    }
}
