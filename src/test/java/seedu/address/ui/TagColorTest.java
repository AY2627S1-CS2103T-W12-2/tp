package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;

public class TagColorTest {

    private static final Set<String> PALETTE_CLASSES = IntStream.range(0, TagColor.PALETTE_SIZE)
            .mapToObj(index -> "tag-color-" + index)
            .collect(Collectors.toSet());

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
        Set<String> classes = Set.of("react", "backend", "frontend", "java", "figma", "mentor")
                .stream().map(name -> TagColor.styleClassFor(new Tag(name))).collect(Collectors.toSet());

        assertTrue(classes.size() > 1);
    }

    @Test
    public void css_everyPaletteClassDefinesBackground() throws IOException {
        String css;
        try (InputStream resource = getClass().getResourceAsStream("/view/DarkTheme.css")) {
            assertNotNull(resource);
            css = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }

        for (int index = 0; index < TagColor.PALETTE_SIZE; index++) {
            String selector = "#tags .tag-color-" + index;
            Pattern rule = Pattern.compile(Pattern.quote(selector) + "\\s*\\{[^}]*-fx-background-color\\s*:");
            assertTrue(rule.matcher(css).find(), "Missing background rule for " + selector);
        }
    }
}
