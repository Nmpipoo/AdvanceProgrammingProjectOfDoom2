package se233.project2.view;

import java.util.List;

// A sequence of frames shown one after another, each for frameSeconds.
// A looping animation repeats; a one-shot one stays on its last frame.
// Frames are usually sprite sheet rectangles, but can be anything (e.g. squash scales).
public class Animation<T> {

    private final List<T> frames;
    private final double frameSeconds;
    private final boolean loop;

    public Animation(List<T> frames, double frameSeconds, boolean loop) {
        if (frames.isEmpty()) {
            throw new IllegalArgumentException("An animation needs at least one frame");
        }
        this.frames = List.copyOf(frames);
        this.frameSeconds = frameSeconds;
        this.loop = loop;
    }

    public T frameAt(double time) {
        int index = (int) Math.floor(Math.max(0, time) / frameSeconds);
        if (loop) {
            index %= frames.size();
        } else {
            index = Math.min(index, frames.size() - 1);
        }
        return frames.get(index);
    }

    public double getDuration() {
        return frames.size() * frameSeconds;
    }

    public int getFrameCount() {
        return frames.size();
    }
}
