package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;

public class MeetingStorageTest {
    @Test
    public void toModelType_oldJson_defaultsToNoMeeting() throws Exception {
        assertTrue(read("null").getMeeting().isEmpty());
        assertTrue(JsonUtil.fromJsonString("{\"name\":\"Alice\"}", JsonAdaptedPerson.class)
                .toModelType().getMeeting().isEmpty());
    }

    @Test
    public void toModelType_invalidFields_nameSpecificField() {
        for (String value : new String[]{"{\"eventName\":17}", "{\"eventName\":\"\"}",
            "{\"eventName\":\"" + "x".repeat(81) + "\"}", "{\"date\":\"2026-02-30\"}",
            "{\"role\":false}", "[]", "42"}) {
            IllegalValueException error = assertThrows(IllegalValueException.class, () -> read(value));
            assertTrue(error.getMessage().startsWith("Invalid contact data: 'meeting"));
        }
    }

    @Test
    public void toModelType_roundTrip_preservesContext() throws Exception {
        var person = read("{\"eventName\":\"Hack\",\"date\":\"2026-10-20\",\"role\":\"Mentor\"}");
        var json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        assertEquals(person, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        assertEquals(ALICE.getName(), person.getName());
    }

    private seedu.address.model.person.Person read(String meeting) throws Exception {
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE));
        json = json.substring(0, json.lastIndexOf('}')) + ",\"meeting\":" + meeting + "}";
        return JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
    }
}
