package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String[] invalidTagNames = {"", "tag with spaces", "tag_with_underscore", "tag#", "a".repeat(21)};

        for (String invalidTagName : invalidTagNames) {
            assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
        }
    }

    @Test
    public void constructor_mixedCaseTagName_storesLowercaseTagName() {
        Tag tag = new Tag("UI-UX+2");

        assertEquals("ui-ux+2", tag.tagName);
    }

    @Test
    public void constructor_caseInsensitiveTagNames_areEqual() {
        Tag uppercaseTag = new Tag("UI-UX");
        Tag lowercaseTag = new Tag("ui-ux");

        assertEquals(uppercaseTag, lowercaseTag);
        assertEquals(uppercaseTag.hashCode(), lowercaseTag.hashCode());
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        assertTrue(Tag.isValidTagName("a"));
        assertTrue(Tag.isValidTagName("a".repeat(20)));
        assertTrue(Tag.isValidTagName("UI-UX+2"));
        assertFalse(Tag.isValidTagName("a".repeat(21)));
        assertFalse(Tag.isValidTagName("tag with spaces"));
        assertFalse(Tag.isValidTagName("tag_with_underscore"));
    }

}
