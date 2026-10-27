package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    /** Message shown when an index is not a positive integer. */
    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /** Message shown when an index is larger than {@link Integer#MAX_VALUE}. */
    public static final String MESSAGE_INDEX_TOO_LARGE = "The provided index is too large.";

    /**
     * Parses a one-based index after trimming surrounding whitespace.
     *
     * @param oneBasedIndex textual index to parse
     * @return the parsed index
     * @throws ParseException if the index is invalid or exceeds {@link Integer#MAX_VALUE}
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        validateIndexCharacters(trimmedIndex);
        return Index.fromOneBased(parsePositiveIntegerIndex(trimmedIndex));
    }

    /**
     * Parses an index that refers to the displayed person list, reporting any problem with it as
     * {@code MESSAGE_INVALID_PERSON_DISPLAYED_INDEX}.
     *
     * @param oneBasedIndex textual index to parse
     * @return the parsed index
     * @throws ParseException if the index is not a positive integer that fits in an {@code int}
     */
    public static Index parseDisplayedIndex(String oneBasedIndex) throws ParseException {
        try {
            return parseIndex(oneBasedIndex);
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, pe);
        }
    }

    /** Ensures that an index contains only decimal digits. */
    private static void validateIndexCharacters(String index) throws ParseException {
        if (!index.matches("[0-9]+")) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
    }

    /** Converts an index to a positive integer and reports integer overflow separately. */
    private static int parsePositiveIntegerIndex(String index) throws ParseException {
        try {
            int parsedIndex = Integer.parseInt(index);
            if (parsedIndex == 0) {
                throw new ParseException(MESSAGE_INVALID_INDEX);
            }
            return parsedIndex;
        } catch (NumberFormatException nfe) {
            throw new ParseException(MESSAGE_INDEX_TOO_LARGE, nfe);
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed and inner whitespace runs will be collapsed.     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String normalizedName = name.trim().replaceAll("\\s+", " ");
        if (!Name.isValidName(normalizedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(normalizedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String linkedIn} into a {@code LinkedIn}.
     * Leading and trailing whitespaces will be trimmed.
     * An empty string is accepted and represents the absence of a handle.
     *
     * @throws ParseException if the given {@code linkedIn} is invalid.
     */
    public static LinkedIn parseLinkedIn(String linkedIn) throws ParseException {
        requireNonNull(linkedIn);
        String trimmedLinkedIn = linkedIn.trim();
        if (!LinkedIn.isValidLinkedIn(trimmedLinkedIn)) {
            throw new ParseException(LinkedIn.MESSAGE_CONSTRAINTS);
        }
        return new LinkedIn(trimmedLinkedIn);
    }

    /**
     * Parses a {@code String note} into a {@code Note}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code note} is blank, or is longer than {@link Note#MAX_LENGTH}
     *         characters once trimmed.
     */
    public static Note parseNote(String note) throws ParseException {
        requireNonNull(note);
        String trimmedNote = note.strip();
        if (trimmedNote.isEmpty()) {
            throw new ParseException(Note.MESSAGE_BLANK);
        }
        if (Note.countCharacters(trimmedNote) > Note.MAX_LENGTH) {
            throw new ParseException(Note.MESSAGE_TOO_LONG);
        }
        return new Note(trimmedNote);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
