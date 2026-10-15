package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the Help window's published guide location.
 */
public class HelpWindowTest {

    @Test
    public void userGuideUrl_pointsToProjectGuide() {
        String expectedUrl = "https://ay2627s1-cs2103t-w12-2.github.io/tp/UserGuide.html";

        assertEquals(expectedUrl, HelpWindow.USERGUIDE_URL);
        assertEquals("Refer to the user guide: " + expectedUrl, HelpWindow.HELP_MESSAGE);
    }
}
