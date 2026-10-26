package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;

public class TagColorTest {

    private static final Set<String> PALETTE_CLASSES = Set.of(
            "tag-color-0", "tag-color-1", "tag-color-2",
            "tag-color-3", "tag-color-4", "tag-color-5");

    @Test
    public void styleClassFor_sameName_isStableAndIgnoresCase() {
        String first = TagColor.styleClassFor(new Tag("React"));

        assertEquals(first, TagColor.styleClassFor(new Tag("react")));
        assertEquals(first, TagColor.styleClassFor(new Tag("REACT")));
    }

    @Test
    public void styleClassFor_validNames_alwaysUsesPalette() {
        String[] names = {"react", "backend", "frontend", "java", "figma", "mentor",
            "ui-ux", "c++", "0", "z".repeat(20)};
        for (String name : names) {
            assertTrue(PALETTE_CLASSES.contains(TagColor.styleClassFor(new Tag(name))));
        }
    }

    @Test
    public void styleClassFor_differentNames_canUseDifferentColors() {
        assertNotEquals(TagColor.styleClassFor(new Tag("react")), TagColor.styleClassFor(new Tag("backend")));
    }
}
