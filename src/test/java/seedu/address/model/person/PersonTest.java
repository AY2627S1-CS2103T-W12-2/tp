package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
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
                + ", note=none, activity=" + ALICE.getActivity() + ", meeting=" + ALICE.getMeeting() + "}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void toStringMethod_withNote_showsOnlyTheLengthOfTheNote() {
        Person person = new PersonBuilder(ALICE).withNote("private text").build();

        assertTrue(person.toString().contains("note=[12 characters]"));
        assertFalse(person.toString().contains("private text"));
    }

    // ---- toBuilder ----

    @Test
    public void toBuilder_build_returnsEqualPerson() {
        Person withEverything = new PersonBuilder(ALICE).withActivity(new Activity(T1, T2, T3)).build();

        assertEquals(withEverything, withEverything.toBuilder().build());
    }

    @Test
    public void toBuilder_setters_changeOnlyTheirOwnField() {
        Person original = new PersonBuilder(ALICE).withActivity(new Activity(T1, T2, T3)).build();

        Person renamed = original.toBuilder().withName(BOB.getName()).build();
        assertEquals(BOB.getName(), renamed.getName());
        assertEquals(original.toBuilder().withName(original.getName()).build(), original);
        assertEquals(original.getPhone(), renamed.getPhone());
        assertEquals(original.getEmail(), renamed.getEmail());
        assertEquals(original.getLinkedIn(), renamed.getLinkedIn());
        assertEquals(original.getTags(), renamed.getTags());
        assertEquals(original.getActivity(), renamed.getActivity());

        Person rephoned = original.toBuilder().withPhone(BOB.getPhone()).build();
        assertEquals(BOB.getPhone(), rephoned.getPhone());
        assertEquals(original.getName(), rephoned.getName());
        assertEquals(original.getActivity(), rephoned.getActivity());

        Person reemailed = original.toBuilder().withEmail(BOB.getEmail()).build();
        assertEquals(BOB.getEmail(), reemailed.getEmail());
        assertEquals(original.getActivity(), reemailed.getActivity());

        Person relinked = original.toBuilder().withLinkedIn(BOB.getLinkedIn()).build();
        assertEquals(BOB.getLinkedIn(), relinked.getLinkedIn());
        assertEquals(original.getActivity(), relinked.getActivity());

        Person retagged = original.toBuilder().withTags(Set.of(new Tag("solo"))).build();
        assertEquals(Set.of(new Tag("solo")), retagged.getTags());
        assertEquals(original.getActivity(), retagged.getActivity());

        Person reactive = original.toBuilder().withActivity(Activity.EMPTY).build();
        assertEquals(Activity.EMPTY, reactive.getActivity());
        assertEquals(original.getName(), reactive.getName());
        assertEquals(original.getTags(), reactive.getTags());
    }

    @Test
    public void toBuilder_build_doesNotChangeOriginal() {
        Person original = new PersonBuilder(ALICE).build();
        Person copy = original.toBuilder().withName(BOB.getName()).withPhone(BOB.getPhone()).build();

        assertEquals(new PersonBuilder(ALICE).build(), original);
        assertFalse(copy.equals(original));
    }

    @Test
    public void toBuilder_tags_areDefensivelyCopied() {
        Set<Tag> tags = new HashSet<>(Set.of(new Tag("one")));
        Person.Builder builder = ALICE.toBuilder().withTags(tags);
        tags.add(new Tag("two"));

        assertEquals(Set.of(new Tag("one")), builder.build().getTags());
    }

    @Test
    public void toBuilder_buildTwice_givesIndependentEqualPersons() {
        Person.Builder builder = ALICE.toBuilder();

        assertEquals(builder.build(), builder.build());
    }

    @Test
    public void toBuilder_nullValues_throwNullPointerException() {
        Person.Builder builder = ALICE.toBuilder();

        assertThrows(NullPointerException.class, () -> builder.withName(null));
        assertThrows(NullPointerException.class, () -> builder.withPhone(null));
        assertThrows(NullPointerException.class, () -> builder.withEmail(null));
        assertThrows(NullPointerException.class, () -> builder.withLinkedIn(null));
        assertThrows(NullPointerException.class, () -> builder.withTags(null));
        assertThrows(NullPointerException.class, () -> builder.withActivity(null));
    }

    @Test
    public void toBuilder_keepsIdentityRule() {
        // identity is still name (ignoring case) plus phone, whatever else the builder changes
        Person changed = ALICE.toBuilder().withEmail(BOB.getEmail()).withTags(Set.of()).build();

        assertTrue(ALICE.isSamePerson(changed));
        assertFalse(ALICE.hasSameDetails(changed));
    }

    @Test
    public void constructor_withoutNote_hasEmptyNote() {
        assertEquals(Note.EMPTY, ALICE.getNote());
        assertEquals(Note.EMPTY, new Person(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getLinkedIn(), ALICE.getTags(), ALICE.getActivity()).getNote());
    }

    @Test
    public void note_isPartOfEqualsHashCodeAndHasSameDetails() {
        Person withNote = new PersonBuilder(ALICE).withNote("Met at booth 4").build();
        Person sameNote = new PersonBuilder(ALICE).withNote("Met at booth 4").build();
        Person otherNote = new PersonBuilder(ALICE).withNote("Met at booth 5").build();

        assertEquals(withNote, sameNote);
        assertEquals(withNote.hashCode(), sameNote.hashCode());
        assertNotEquals(withNote, otherNote);
        assertNotEquals(withNote, ALICE);

        // a different note is a genuine change of the details, but not of the identity
        assertTrue(withNote.hasSameDetails(sameNote));
        assertFalse(withNote.hasSameDetails(otherNote));
        assertFalse(withNote.hasSameDetails(ALICE));
        assertTrue(withNote.isSamePerson(otherNote));
    }

    @Test
    public void toBuilder_withNote_changesOnlyTheNote() {
        Person person = new PersonBuilder(ALICE).withLinkedIn("alice-pauline").withNote("old note")
                .withActivity(new Activity(Instant.parse("2026-10-17T04:12:33Z"), null, null)).build();

        Person updated = person.toBuilder().withNote(new Note("new note")).build();

        assertEquals(new Note("new note"), updated.getNote());
        assertEquals(person.getName(), updated.getName());
        assertEquals(person.getPhone(), updated.getPhone());
        assertEquals(person.getEmail(), updated.getEmail());
        assertEquals(person.getLinkedIn(), updated.getLinkedIn());
        assertEquals(person.getTags(), updated.getTags());
        assertEquals(person.getActivity(), updated.getActivity());
        // copies made for other reasons keep the note
        assertEquals(new Note("old note"), person.toBuilder().build().getNote());
        assertEquals(new Note("old note"), person.withActivity(Activity.EMPTY).getNote());
    }

    @Test
    public void toBuilder_withNullNote_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ALICE.toBuilder().withNote(null));
    }
}
