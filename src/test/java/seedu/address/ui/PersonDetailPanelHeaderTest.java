package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.PersonDetailPanel.isNarrowHeader;

import org.junit.jupiter.api.Test;

/**
 * Tests when {@code PersonDetailPanel} shows the photo under the name, which does not need the JavaFX toolkit.
 */
public class PersonDetailPanelHeaderTest {

    /*
     * Partitions for the header width: narrower than the limit (the photo goes under the name), as wide as the
     * limit or wider (beside the name). Boundary values: just below and exactly at the limit of 400 pixels.
     */
    @Test
    public void isNarrowHeader_belowTheLimit_isNarrow() {
        assertTrue(isNarrowHeader(0));
        assertTrue(isNarrowHeader(305));
        assertTrue(isNarrowHeader(399.9));
    }

    @Test
    public void isNarrowHeader_atOrAboveTheLimit_isNotNarrow() {
        assertFalse(isNarrowHeader(400));
        assertFalse(isNarrowHeader(660));
    }
}
