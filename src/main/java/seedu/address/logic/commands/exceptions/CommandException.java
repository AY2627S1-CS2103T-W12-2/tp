package seedu.address.logic.commands.exceptions;

/**
 * Represents an error which occurs during execution of a {@link seedu.address.logic.commands.Command}.
 */
public class CommandException extends Exception {
    /**
     * Constructs a command failure with a user-facing explanation.
     *
     * @param message Explanation to display to the user.
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code CommandException} with the specified detail {@code message} and {@code cause}.
     *
     * @param message Explanation to display to the user.
     * @param cause Underlying failure that prevented the command from completing.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
