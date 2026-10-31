package se233.project2.view;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnimationTest {

    private final Animation<String> loop = new Animation<>(List.of("a", "b", "c"), 0.1, true);
    private final Animation<String> once = new Animation<>(List.of("a", "b", "c"), 0.1, false);

    @Test
    void showsEachFrameForItsDuration() {
        assertEquals("a", loop.frameAt(0));
        assertEquals("a", loop.frameAt(0.09));
        assertEquals("b", loop.frameAt(0.1));
        assertEquals("c", loop.frameAt(0.25));
    }

    @Test
    void loopingAnimationWrapsAround() {
        assertEquals("a", loop.frameAt(0.32));
        assertEquals("b", loop.frameAt(0.45));
    }

    @Test
    void oneShotAnimationHoldsItsLastFrame() {
        assertEquals("c", once.frameAt(0.3));
        assertEquals("c", once.frameAt(100));
    }

    @Test
    void negativeTimeShowsTheFirstFrame() {
        assertEquals("a", once.frameAt(-1));
    }

    @Test
    void durationIsFramesTimesFrameLength() {
        assertEquals(0.3, loop.getDuration(), 1e-9);
        assertEquals(3, loop.getFrameCount());
    }

    @Test
    void emptyAnimationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Animation<String>(List.of(), 0.1, true));
    }
}
