package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalNotes.EMOJI;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Email;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Name;
import seedu.address.model.person.Note;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_LINKEDIN = "benson_meier";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_TIME = "yesterday";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_LINKEDIN = BENSON.getLinkedIn().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL,
                        VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL,
                VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL,
                        VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_returnsPersonWithEmptyPhone() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL,
                VALID_LINKEDIN, VALID_TAGS);
        assertEquals("", person.toModelType().getPhone().value);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL,
                        VALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_returnsPersonWithEmptyEmail() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null,
                VALID_LINKEDIN, VALID_TAGS);
        assertEquals("", person.toModelType().getEmail().value);
    }

    @Test
    public void toModelType_legacyAddressField_ignored() throws Exception {
        String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE
                + "\",\"email\":\"" + VALID_EMAIL + "\",\"address\":\"Blk 1, Old Street\",\"linkedIn\":\""
                + VALID_LINKEDIN + "\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
        assertEquals(new PersonBuilder(BENSON).withTags().build(), person.toModelType());
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_invalidLinkedIn_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                INVALID_LINKEDIN, VALID_TAGS);
        String expectedMessage = LinkedIn.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullLinkedIn_returnsPersonWithoutLinkedIn() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                null, VALID_TAGS);
        assertEquals(new LinkedIn(""), person.toModelType().getLinkedIn());
    }

    @Test
    public void toModelType_activityRoundTrip_preservesTimestamps() throws Exception {
        Activity activity = new Activity(Instant.parse("2026-10-17T04:12:33Z"),
                Instant.parse("2026-10-18T09:00:00Z"), Instant.parse("2026-10-19T10:30:15Z"));
        Person person = new PersonBuilder(BENSON).withActivity(activity).build();

        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    @Test
    public void toModelType_partialActivityRoundTrip_keepsAbsentFieldsAbsent() throws Exception {
        Activity activity = new Activity(Instant.parse("2026-10-17T04:12:33Z"), null, null);
        Person person = new PersonBuilder(BENSON).withActivity(activity).build();

        assertEquals(activity, new JsonAdaptedPerson(person).toModelType().getActivity());
    }

    @Test
    public void toJson_activity_usesIsoUtcStrings() throws Exception {
        Activity activity = new Activity(Instant.parse("2026-10-17T04:12:33Z"), null, null);
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(new PersonBuilder(BENSON)
                .withActivity(activity).build()));

        assertTrue(json.contains("\"createdAt\""));
        assertTrue(json.contains("2026-10-17T04:12:33Z"));
    }

    @Test
    public void toModelType_missingActivityFields_returnsEmptyActivity() throws Exception {
        // a v1.3 data file has none of the activity fields
        String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE
                + "\",\"email\":\"" + VALID_EMAIL + "\",\"linkedIn\":\"" + VALID_LINKEDIN + "\"}";
        JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);

        assertEquals(Activity.EMPTY, person.toModelType().getActivity());
    }

    @Test
    public void toModelType_explicitNullActivityFields_returnsEmptyActivity() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, null, null, null);
        assertEquals(Activity.EMPTY, person.toModelType().getActivity());
    }

    @Test
    public void toModelType_invalidCreatedAt_throwsFieldSpecificMessage() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, INVALID_TIME, null, null);
        assertThrows(IllegalValueException.class, timeError("createdAt"), person::toModelType);
    }

    @Test
    public void toModelType_invalidLastEditedAt_throwsFieldSpecificMessage() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, null, INVALID_TIME, null);
        assertThrows(IllegalValueException.class, timeError("lastEditedAt"), person::toModelType);
    }

    @Test
    public void toModelType_invalidLastViewedAt_throwsFieldSpecificMessage() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, null, null, INVALID_TIME);
        assertThrows(IllegalValueException.class, timeError("lastViewedAt"), person::toModelType);
    }

    @Test
    public void toModelType_timesThatAreNotWholeSecondUtc_throwsIllegalValueException() {
        String[] notWholeSecondUtc = {
            "2026-10-17T04:12:33.123Z", // fraction of a second
            "2026-10-17T04:12:33.0Z", // even a zero fraction
            "2026-10-17T12:12:33+08:00", // offset instead of Z
            "2026-10-17T04:12:33", // no Z
            "2026-10-17 04:12:33Z", // space instead of T
            "2026-10-17T04:12Z", // no seconds
            "2026-02-30T04:12:33Z", // no such day
            "2027-02-29T04:12:33Z", // 29 Feb in a year that is not a leap year
            "2026-13-01T04:12:33Z", // no such month
            "2026-10-17T25:00:00Z", // no such hour
            "2026-10-17T04:60:00Z", // no such minute
            "2026-10-17t04:12:33z", // lower-case separators
            "2026-10-17T24:00:00Z", // no such hour (end of day is accepted by Instant.parse, but not here)
            "2026-10-17T23:59:60Z", // leap second
            " 2026-10-17T04:12:33Z", // leading space
            "2026-10-17T04:12:33Z ", // trailing space
            "" // empty
        };

        for (String time : notWholeSecondUtc) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                    VALID_TAGS, time, time, time);
            assertThrows(IllegalValueException.class, timeError("createdAt"), person::toModelType);
        }
    }

    @Test
    public void toModelType_wholeSecondUtcTimes_accepted() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, "2026-10-17T00:00:00Z", "2028-02-29T23:59:59Z", "1999-12-31T23:59:59Z");

        Activity activity = person.toModelType().getActivity();

        assertEquals(Instant.parse("2026-10-17T00:00:00Z"), activity.getCreatedAt().get());
        assertEquals(Instant.parse("2028-02-29T23:59:59Z"), activity.getLastEditedAt().get());
        assertEquals(Instant.parse("1999-12-31T23:59:59Z"), activity.getLastViewedAt().get());
    }

    @Test
    public void toModelType_nonStringJsonTimes_throwsFieldSpecificError() throws Exception {
        String[] nonStrings = {"{}", "[]", "[\"2026-10-17T04:12:33Z\"]", "123", "true"};
        String[] fields = {"createdAt", "lastEditedAt", "lastViewedAt"};

        for (String field : fields) {
            for (String value : nonStrings) {
                String json = "{\"name\":\"" + VALID_NAME + "\",\"phone\":\"" + VALID_PHONE
                        + "\",\"email\":\"" + VALID_EMAIL + "\",\"linkedIn\":\"" + VALID_LINKEDIN
                        + "\",\"" + field + "\":" + value + "}";
                JsonAdaptedPerson person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
                assertThrows(IllegalValueException.class, timeError(field), person::toModelType);
            }
        }
    }

    @Test
    public void invalidFieldMessageFormat_matchesAgreedWording() {
        assertEquals("Invalid contact data: 'note' must be a string.",
                String.format(JsonAdaptedPerson.INVALID_FIELD_MESSAGE_FORMAT, "note", "must be a string"));
    }

    private static String timeError(String field) {
        return "Invalid contact data: '" + field + "' must be an ISO-8601 UTC timestamp.";
    }

    @Test
    public void toModelType_noteRoundTrip_preservesNote() throws Exception {
        Person person = new PersonBuilder(BENSON).withNote("Met at booth 4; wants a React teammate!").build();

        assertEquals(person, new JsonAdaptedPerson(person).toModelType());
    }

    /*
     * Equivalence partitions for the note field in the data file:
     *   - missing (null), which means no note                          - valid
     *   - 1 to MAX_LENGTH characters once surrounding spaces are cut  - valid
     *   - empty, only spaces, more than MAX_LENGTH, not a string       - invalid
     * Boundary values: MAX_LENGTH and MAX_LENGTH + 1 characters.
     */
    @Test
    public void toModelType_validNoteValues_areLoaded() throws Exception {
        String longest = "a".repeat(Note.MAX_LENGTH);

        assertEquals(Note.EMPTY, personWithNote(null).toModelType().getNote());
        assertEquals(new Note("hello"), personWithNote("  hello  ").toModelType().getNote());
        assertEquals(new Note(longest), personWithNote(longest).toModelType().getNote());
    }

    @Test
    public void toModelType_invalidNoteValues_throwFieldSpecificMessage() {
        Object[] invalidValues = {"", "   ", "a".repeat(Note.MAX_LENGTH + 1), 12345};

        for (Object value : invalidValues) {
            assertThrows(IllegalValueException.class, noteError(), personWithNote(value)::toModelType);
        }
    }

    @Test
    public void toModelType_noteOfEmoji_countsEachEmojiAsOneCharacter() throws Exception {
        String halfOfTheLimitPlusOne = EMOJI.repeat(Note.MAX_LENGTH / 2 + 1);
        String longest = EMOJI.repeat(Note.MAX_LENGTH);

        assertEquals(new Note(halfOfTheLimitPlusOne), personWithNote(halfOfTheLimitPlusOne).toModelType().getNote());
        assertEquals(new Note(longest), personWithNote(longest).toModelType().getNote());
        assertThrows(IllegalValueException.class, noteError(),
                personWithNote(EMOJI.repeat(Note.MAX_LENGTH + 1))::toModelType);
    }

    @Test
    public void toModelType_noteOfEmojiRoundTripThroughJson_isKept() throws Exception {
        Person person = new PersonBuilder(BENSON).withNote(EMOJI.repeat(Note.MAX_LENGTH)).build();

        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        Person loaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();

        assertEquals(person, loaded);
    }

    @Test
    public void toJson_noteAndNoNote_writesStringOrNull() throws Exception {
        Person bensonWithNote = new PersonBuilder(BENSON).withNote("hi").build();
        String withNote = JsonUtil.toJsonString(new JsonAdaptedPerson(bensonWithNote));
        String withoutNote = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));

        assertTrue(withNote.contains("\"note\" : \"hi\""));
        assertTrue(withoutNote.contains("\"note\" : null"));
    }

    @Test
    public void toModelType_followUpDateRoundTrip_preservesDate() throws Exception {
        Person person = new PersonBuilder(BENSON).withFollowUpDate(LocalDate.of(2028, 2, 29)).build();

        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        assertTrue(json.contains("\"followUpDate\" : \"2028-02-29\""));
        Person loaded = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(Optional.of(LocalDate.of(2028, 2, 29)), loaded.getFollowUpDate());
        assertEquals(person, loaded);
    }

    @Test
    public void toModelType_missingOrNullFollowUpDate_meansNoFollowUp() throws Exception {
        String missing = "{\"name\":\"Amy Bee\",\"phone\":\"85355255\",\"email\":\"amy@gmail.com\"}";
        String explicitNull = "{\"name\":\"Amy Bee\",\"phone\":\"85355255\",\"email\":\"amy@gmail.com\","
                + "\"followUpDate\":null}";

        assertEquals(Optional.empty(),
                JsonUtil.fromJsonString(missing, JsonAdaptedPerson.class).toModelType().getFollowUpDate());
        assertEquals(Optional.empty(),
                JsonUtil.fromJsonString(explicitNull, JsonAdaptedPerson.class).toModelType().getFollowUpDate());
        assertTrue(JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON)).contains("\"followUpDate\" : null"));
    }

    @Test
    public void toModelType_malformedFollowUpDate_throwsFieldSpecificMessage() throws Exception {
        String expected = "Invalid contact data: 'followUpDate' must be a real date in YYYY-MM-DD format.";
        String[] malformed = {"\"2027-02-29\"", "\"2026-02-30\"", "\"2026-13-01\"", "\"2026-10-32\"",
            "\"25-10-2026\"", "\"2026-1-5\"", "\"2026/10/25\"", "\"2026-10-25 \"", "\" 2026-10-25\"",
            "\"2026-10-25T00:00:00Z\"", "\"+2026-10-25\"", "\"0000-01-01\"", "\"\"", "\"tomorrow\"",
            "20261025", "true", "{}", "[]", "[\"2026-10-25\"]"};
        for (String value : malformed) {
            String json = "{\"name\":\"Amy Bee\",\"phone\":\"85355255\",\"email\":\"amy@gmail.com\","
                    + "\"followUpDate\":" + value + "}";
            JsonAdaptedPerson adapted = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
            assertThrows(IllegalValueException.class, expected, adapted::toModelType);
        }
    }

    /*
     * Equivalence partitions for the starred field in the data file:
     *   - missing or null, which means not starred  - valid
     *   - true or false                             - valid
     *   - anything else, such as a string or number - invalid
     */
    @Test
    public void toModelType_starredRoundTrip_preservesStar() throws Exception {
        Person starred = new PersonBuilder(BENSON).withStarred(true).build();

        assertTrue(new JsonAdaptedPerson(starred).toModelType().isStarred());
        assertFalse(new JsonAdaptedPerson(BENSON).toModelType().isStarred());
    }

    @Test
    public void toModelType_missingOrNullStarred_defaultsToNotStarred() throws Exception {
        assertFalse(personWithStarred("").toModelType().isStarred());
        assertFalse(personWithStarred(", \"starred\": null").toModelType().isStarred());
    }

    @Test
    public void toModelType_invalidStarred_throwsFieldSpecificMessage() throws Exception {
        String expected = "Invalid contact data: 'starred' must be true or false.";

        assertThrows(IllegalValueException.class, expected, personWithStarred(", \"starred\": \"yes\"")::toModelType);
        assertThrows(IllegalValueException.class, expected, personWithStarred(", \"starred\": 1")::toModelType);
    }

    private static JsonAdaptedPerson personWithStarred(String starredEntry) throws Exception {
        String json = "{\"name\": \"" + VALID_NAME + "\", \"phone\": \"" + VALID_PHONE + "\", \"email\": \""
                + VALID_EMAIL + "\", \"linkedIn\": \"" + VALID_LINKEDIN + "\"" + starredEntry + "}";
        return JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
    }

    private static JsonAdaptedPerson personWithNote(Object note) {
        return new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN, VALID_TAGS,
                null, null, null, note);
    }

    private static String noteError() {
        return "Invalid contact data: 'note' must be a string of at most " + Note.MAX_LENGTH + " characters.";
    }
}
