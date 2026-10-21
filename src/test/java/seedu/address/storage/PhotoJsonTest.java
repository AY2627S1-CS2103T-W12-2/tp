package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.JsonUtil;

public class PhotoJsonTest {
    @Test
    public void toModelType_oldJsonAndRoundTrip_preserveOptionalReference() throws Exception {
        var old = JsonUtil.fromJsonString("{\"name\":\"Alice\"}", JsonAdaptedPerson.class).toModelType();
        assertTrue(old.getPhotoPath().isEmpty());
        var person = ALICE.toBuilder().withPhotoPath("photos/portrait.png").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        assertEquals(person, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        assertEquals(person, person.toBuilder().build());
    }

    @Test
    public void toModelType_badReference_doesNotPreventOtherContactsLoading() throws Exception {
        var person = JsonUtil.fromJsonString("{\"name\":\"Alice\",\"photoPath\":{}}",
                JsonAdaptedPerson.class).toModelType();
        assertTrue(person.getPhotoPath().isEmpty());
    }
}
