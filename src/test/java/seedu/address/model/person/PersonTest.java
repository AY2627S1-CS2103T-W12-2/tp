package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LINKEDIN_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    private static final Instant T1 = Instant.parse("2026-10-17T04:12:33Z");
    private static final Instant T2 = Instant.parse("2026-10-18T09:00:00Z");
    private static final Instant T3 = Instant.parse("2026-10-19T10:30:15Z");

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, different phone -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same name and phone, all other attributes different -> returns true
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns true
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertTrue(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different LinkedIn handle -> returns false
        editedAlice = new PersonBuilder(ALICE).withLinkedIn(VALID_LINKEDIN_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different follow-up date -> returns false
        editedAlice = new PersonBuilder(ALICE).withFollowUpDate("2026-10-25").build();
        assertFalse(ALICE.equals(editedAlice));

        // different activity -> returns false
        editedAlice = new PersonBuilder(ALICE).withActivity(new Activity(T1, null, null)).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void constructor_withoutActivity_hasEmptyActivity() {
        assertEquals(Activity.EMPTY, new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getLinkedIn(), ALICE.getTags()).getActivity());
    }

    @Test
    public void withActivity_copiesEverythingElse() {
        Activity activity = new Activity(T1, T2, null);
        Person copy = ALICE.withActivity(activity);

        assertEquals(activity, copy.getActivity());
        assertEquals(ALICE.getName(), copy.getName());
        assertEquals(ALICE.getPhone(), copy.getPhone());
        assertEquals(ALICE.getEmail(), copy.getEmail());
        assertEquals(ALICE.getLinkedIn(), copy.getLinkedIn());
        assertEquals(ALICE.getTags(), copy.getTags());
        // original untouched
        assertEquals(Activity.EMPTY, ALICE.getActivity());
    }

    @Test
    public void withActivity_keepsFollowUpDate() {
        Person withDate = new PersonBuilder(ALICE).withFollowUpDate("2026-10-25").build();

        assertEquals(new FollowUpDate("2026-10-25"), withDate.withActivity(new Activity(T1, null, null))
                .getFollowUpDate());
    }

    @Test
    public void getFollowUpDate_defaultsToNone() {
        assertEquals(FollowUpDate.NONE, ALICE.getFollowUpDate());
    }

    @Test
    public void constructor_nullFollowUpDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Person(ALICE.getName(), ALICE.getPhone(),
                ALICE.getEmail(), ALICE.getLinkedIn(), ALICE.getTags(), null, Activity.EMPTY));
    }

    @Test
    public void withActivity_nullActivity_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.withActivity(null));
    }

    @Test
    public void hasSameDetails() {
        // same object -> true
        assertTrue(ALICE.hasSameDetails(ALICE));

        // null -> false
        assertFalse(ALICE.hasSameDetails(null));

        // only activity differs -> true
        assertTrue(ALICE.hasSameDetails(new PersonBuilder(ALICE).withActivity(new Activity(T1, T2, T3)).build()));

        // follow-up date differs -> false
        assertFalse(ALICE.hasSameDetails(new PersonBuilder(ALICE).withFollowUpDate("2026-10-25").build()));

        // a detail differs -> false
        assertFalse(ALICE.hasSameDetails(new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build()));
        assertFalse(ALICE.hasSameDetails(new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build()));

        // detail and activity both differ -> false
        assertFalse(ALICE.hasSameDetails(new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB)
                .withActivity(new Activity(T1, null, null)).build()));
    }

    @Test
    public void hashCode_sameValues_sameHashCode() {
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail()
                + ", linkedIn=" + ALICE.getLinkedIn() + ", tags=" + ALICE.getTags()
                + ", followUpDate=" + ALICE.getFollowUpDate() + ", activity=" + ALICE.getActivity() + "}";
        assertEquals(expected, ALICE.toString());
    }
}
