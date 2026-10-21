package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.PersonDetailPanel.scrolledVvalue;

import org.junit.jupiter.api.Test;

/**
 * Tests the scroll position calculation of {@code PersonDetailPanel}, which does not need the JavaFX toolkit.
 * In these tests the content is 800 pixels tall and the viewport 400, so 400 pixels can scroll, unless stated.
 */
public class PersonDetailPanelScrollTest {

    private static final double DELTA = 1e-9;

    /*
     * Partitions for the scroll amount: scrolling down (negative), scrolling up (positive), no scroll (zero),
     * and an amount that goes past the end. Boundary values: exactly reaching the top and the bottom.
     */
    @Test
    public void scrolledVvalue_scrollDown_movesByTheNumberOfPixels() {
        // 40 pixels out of 400 scrollable pixels is a tenth of the way
        assertEquals(0.6, scrolledVvalue(0.5, -40, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_scrollUp_movesBackByTheNumberOfPixels() {
        assertEquals(0.4, scrolledVvalue(0.5, 40, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_noScroll_changesNothing() {
        assertTrue(scrolledVvalue(0.5, 0, 800, 400).isEmpty());
    }

    @Test
    public void scrolledVvalue_exactlyReachingEnds_isAllowed() {
        assertEquals(1, scrolledVvalue(0.9, -40, 800, 400).getAsDouble(), DELTA);
        assertEquals(0, scrolledVvalue(0.1, 40, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_pastTheEnds_staysBetweenTopAndBottom() {
        assertEquals(1, scrolledVvalue(0.95, -400, 800, 400).getAsDouble(), DELTA);
        assertEquals(0, scrolledVvalue(0.05, 400, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_shortContent_movesFurtherPerPixel() {
        // only 100 pixels can scroll, so the same 40 pixels is a bigger step
        assertEquals(0.4, scrolledVvalue(0, -40, 500, 400).getAsDouble(), DELTA);
    }

    /*
     * Partitions for the content size: taller than the viewport (scrolls, tested above), exactly as tall
     * (boundary, nothing to scroll), and shorter (nothing to scroll).
     */
    @Test
    public void scrolledVvalue_contentNotTallerThanViewport_changesNothing() {
        assertTrue(scrolledVvalue(0, -40, 400, 400).isEmpty());
        assertTrue(scrolledVvalue(0, -40, 300, 400).isEmpty());
    }
}
