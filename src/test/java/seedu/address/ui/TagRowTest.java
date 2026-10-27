package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntToDoubleFunction;

import org.junit.jupiter.api.Test;

public class TagRowTest {

    private static final double GAP = 5;
    private static final IntToDoubleFunction PLUS = hidden -> 20;
    private static final IntToDoubleFunction PLUS_BY_DIGITS = hidden -> 8 + 6 * String.valueOf(hidden).length();

    @Test
    public void visibleTagCount_allTagsFit_returnsAll() {
        // 30 + 5 + 40 = 75 fits in 100; +N is not needed so its width is ignored
        assertEquals(2, TagRow.visibleTagCount(List.of(30.0, 40.0), GAP, PLUS, 100));
    }

    @Test
    public void visibleTagCount_allTagsExactlyFit_returnsAll() {
        assertEquals(2, TagRow.visibleTagCount(List.of(30.0, 40.0), GAP, PLUS, 75));
    }

    @Test
    public void visibleTagCount_emptyList_returnsZero() {
        assertEquals(0, TagRow.visibleTagCount(List.of(), GAP, PLUS, 100));
        assertEquals(0, TagRow.visibleTagCount(List.of(), GAP, PLUS, 0));
    }

    @Test
    public void visibleTagCount_someTagsFitWithOverflowLabel_returnsThoseThatFit() {
        // 30 + 5 + 30 + 5 + 20 (+N) = 90 fits in 90; adding the third tag (80) would not
        assertEquals(2, TagRow.visibleTagCount(List.of(30.0, 30.0, 30.0, 30.0), GAP, PLUS, 90));
    }

    @Test
    public void visibleTagCount_overflowLabelNeedsRoom_dropsOneMoreTag() {
        // three tags fit (30 + 5 + 30 + 5 + 30 = 100) but then +N (5 + 20) does not, so only two stay
        assertEquals(2, TagRow.visibleTagCount(List.of(30.0, 30.0, 30.0, 30.0), GAP, PLUS, 100));
    }

    @Test
    public void visibleTagCount_overflowLabelFitsToTheLastPixel_keepsTag() {
        // 30 + 5 + 30 + 5 + 30 + 5 + 20 = 125
        assertEquals(3, TagRow.visibleTagCount(List.of(30.0, 30.0, 30.0, 30.0), GAP, PLUS, 125));
        assertEquals(2, TagRow.visibleTagCount(List.of(30.0, 30.0, 30.0, 30.0), GAP, PLUS, 124.9));
    }

    @Test
    public void visibleTagCount_singleTagTooLong_returnsZero() {
        assertEquals(0, TagRow.visibleTagCount(List.of(200.0), GAP, PLUS, 100));
    }

    @Test
    public void visibleTagCount_firstTagLeavesNoRoomForOverflowLabel_returnsZero() {
        // 90 fits alone in 100, but with +N (90 + 5 + 20) it does not, and a second tag exists
        assertEquals(0, TagRow.visibleTagCount(List.of(90.0, 10.0), GAP, PLUS, 100));
    }

    @Test
    public void visibleTagCount_noRoomAtAll_returnsZero() {
        assertEquals(0, TagRow.visibleTagCount(List.of(30.0, 30.0), GAP, PLUS, 0));
    }

    @Test
    public void visibleTagCount_firstTagTooWideButLaterOnesNarrow_stopsAtFirstMisfit() {
        // tags keep their order: a wide first tag hides everything even though the second would fit
        assertEquals(0, TagRow.visibleTagCount(List.of(200.0, 10.0), GAP, PLUS, 100));
    }

    @Test
    public void visibleTagCount_fewerHiddenTagsHaveShorterOverflowLabel_keepsMoreTags() {
        List<Double> tenTags = Collections.nCopies(10, 20.0);
        // all ten: 200 + 9 * 5 = 245, too wide for 240.
        // nine tags: 180 + 8 * 5 = 220; "+1" is 14 wide, so 220 + 5 + 14 = 239 fits in 240.
        // Reserving "+10" (20 wide) would need 245 and drop the ninth tag.
        assertEquals(9, TagRow.visibleTagCount(tenTags, GAP, PLUS_BY_DIGITS, 240));
        assertEquals(8, TagRow.visibleTagCount(tenTags, GAP, PLUS_BY_DIGITS, 238.9));
    }

    @Test
    public void visibleTagCount_overflowWidth_isAskedForTheNumberOfHiddenTags() {
        List<Integer> asked = new ArrayList<>();
        TagRow.visibleTagCount(List.of(30.0, 30.0, 30.0, 30.0), GAP, hidden -> {
            asked.add(hidden);
            return 20;
        }, 90);
        // 4 tags do not fit in 90; 3 tags (hidden 1) do not fit with +N; 2 tags (hidden 2) do
        assertEquals(List.of(1, 2), asked);
    }
}
