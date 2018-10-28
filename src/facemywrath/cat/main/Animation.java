package facemywrath.cat.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class Animation<T> {

    private List<Frame> frames = new ArrayList<>();
    private Main main;
    private Boolean looping = false;
    private Long loopDelay = 1L;

    public Animation(Main main) {
        this.main = main;
    }

    public Animation addFrame(Consumer<T> frameFunction, Long delay) {
        frames.add(new Frame<T>(frameFunction, delay, 1));
        return this;
    }

    public Animation addFrame(Consumer<T> frameFunction, Long delay, int repeat) {
        frames.add(new Frame<T>(frameFunction, delay, repeat));
        return this;
    }

    public void animate(T object) {
        run(object, 0, 1);
    }

    public Animation setLooping(Boolean looping) {
        this.looping = looping;
        return this;
    }

    public Animation setLooping(Boolean looping, Long loopDelay) {
        this.looping = looping;
        this.loopDelay = loopDelay;
        return this;
    }

    public Animation setLoopDelay(Long loopDelay) {
        this.loopDelay = loopDelay;
        return this;
    }

    @SuppressWarnings("unchecked")
    private void run(T object, int i, int repeat) {
        if (i >= frames.size()) {
            if (looping)
                main.getServer().getScheduler().runTaskLater(main, () -> animate(object), loopDelay);
            return;
        }
        Frame frame = frames.get(i);
        main.getServer().getScheduler().runTaskLater(main, () -> {
            frame.run(object);
            run(object, (repeat >= frame.getRepetitions() ? i + 1 : i),
                    repeat < frame.getRepetitions() ? repeat + 1 : 1);
        }, frame.getDelay());
    }
}

class Frame<T> {
    private Consumer<T> frameFunction;
    private Long delay;
    private int repeat;

    public Frame(Consumer<T> fun, Long del, int repeat) {
        this.delay = del;
        this.frameFunction = fun;
        this.repeat = repeat;
    }

    public Long getDelay() {
        return this.delay;
    }

    public void run(T object) {
        frameFunction.accept(object);
    }

    public int getRepetitions() {
        return this.repeat;
    }
}

