package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;

import org.junit.jupiter.api.Test;

/**
 * Tests the scroll position calculation of {@code PersonDetailPanel}, which does not need the JavaFX toolkit.
 * In these tests the scroll range is 0 to 1, the content is 800 pixels tall and the viewport 400, so 400 pixels
 * can scroll, unless stated.
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
        assertEquals(0.6, scrolled(0.5, -40, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_scrollUp_movesBackByTheNumberOfPixels() {
        assertEquals(0.4, scrolled(0.5, 40, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_noScroll_changesNothing() {
        assertTrue(scrolled(0.5, 0, 800, 400).isEmpty());
    }

    @Test
    public void scrolledVvalue_exactlyReachingEnds_isAllowed() {
        assertEquals(1, scrolled(0.9, -40, 800, 400).getAsDouble(), DELTA);
        assertEquals(0, scrolled(0.1, 40, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_pastTheEnds_staysBetweenTopAndBottom() {
        assertEquals(1, scrolled(0.95, -400, 800, 400).getAsDouble(), DELTA);
        assertEquals(0, scrolled(0.05, 400, 800, 400).getAsDouble(), DELTA);
    }

    @Test
    public void scrolledVvalue_shortContent_movesFurtherPerPixel() {
        // only 100 pixels can scroll, so the same 40 pixels is a bigger step
        assertEquals(0.4, scrolled(0, -40, 500, 400).getAsDouble(), DELTA);
    }

    /*
     * Partitions for the content size: taller than the viewport (scrolls, tested above), exactly as tall
     * (boundary, nothing to scroll), and shorter (nothing to scroll).
     */
    @Test
    public void scrolledVvalue_contentNotTallerThanViewport_changesNothing() {
        assertTrue(scrolled(0, -40, 400, 400).isEmpty());
        assertTrue(scrolled(0, -40, 300, 400).isEmpty());
    }

    /*
     * The scroll range is not assumed to be 0 to 1: with a range of 0 to 200, the same 40 pixels out of 400
     * scrollable pixels is a tenth of the range, and the result stays inside the range.
     */
    @Test
    public void scrolledVvalue_otherScrollRange_scalesAndStaysInsideTheRange() {
        assertEquals(120, PersonDetailPanel.scrolledVvalue(100, -40, 800, 400, 0, 200).getAsDouble(), DELTA);
        assertEquals(200, PersonDetailPanel.scrolledVvalue(190, -400, 800, 400, 0, 200).getAsDouble(), DELTA);
        assertEquals(0, PersonDetailPanel.scrolledVvalue(10, 400, 800, 400, 0, 200).getAsDouble(), DELTA);
    }

    /** Calculates the position with the default scroll range of 0 to 1. */
    private static OptionalDouble scrolled(double vvalue, double deltaY, double contentHeight,
            double viewportHeight) {
        return PersonDetailPanel.scrolledVvalue(vvalue, deltaY, contentHeight, viewportHeight, 0, 1);
    }
}
