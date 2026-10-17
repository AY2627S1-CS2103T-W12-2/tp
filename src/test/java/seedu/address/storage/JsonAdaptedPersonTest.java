package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Activity;
import seedu.address.model.person.Email;
import seedu.address.model.person.LinkedIn;
import seedu.address.model.person.Name;
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
    private static final String NON_UTC_TIME = "2026-10-17T12:12:33+08:00";

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
    public void toModelType_invalidCreatedAt_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, INVALID_TIME, null, null);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_INVALID_TIME_FORMAT,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidLastEditedAt_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, null, INVALID_TIME, null);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_INVALID_TIME_FORMAT,
                person::toModelType);
    }

    @Test
    public void toModelType_invalidLastViewedAt_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, null, null, INVALID_TIME);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_INVALID_TIME_FORMAT,
                person::toModelType);
    }

    @Test
    public void toModelType_timeWithOffset_normalisedToSameInstant() throws Exception {
        // hand-edited files may use an offset; it is read as the same instant and saved back as UTC
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, NON_UTC_TIME, null, null);
        assertEquals(Instant.parse("2026-10-17T04:12:33Z"),
                person.toModelType().getActivity().getCreatedAt().get());
    }

    @Test
    public void toModelType_emptyTimeString_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_LINKEDIN,
                VALID_TAGS, "", null, null);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_INVALID_TIME_FORMAT,
                person::toModelType);
    }

}
