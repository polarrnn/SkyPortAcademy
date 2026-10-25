package com.airport.visualizer.hashing;

import com.airport.visualizer.core.StepRecorder;
import java.util.function.Function;

/**
 * Where a hash table reports its work: counters plus "take a picture now" steps.
 * {@link #QUIET} ignores pictures so the same code can be timed without any drawing.
 */
public interface HashStepSink {

    boolean isRecording();

    /** One key comparison. */
    void compare();

    /** One probe (looking at a slot) or one hop along a chain's next arrow. */
    void probe();

    void step(String title, String narration, HashSnapshot snapshot);

    /** Counts nothing, records nothing. */
    HashStepSink QUIET = new HashStepSink() {
        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public void compare() {
        }

        @Override
        public void probe() {
        }

        @Override
        public void step(String title, String narration, HashSnapshot snapshot) {
        }
    };

    /** Records into a {@link StepRecorder}; the mapper wraps the snapshot into the visualizer's own state type. */
    static HashStepSink recording(StepRecorder recorder, Function<HashSnapshot, Object> mapper) {
        return new HashStepSink() {
            @Override
            public boolean isRecording() {
                return true;
            }

            @Override
            public void compare() {
                recorder.compare();
            }

            @Override
            public void probe() {
                recorder.swap();
            }

            @Override
            public void step(String title, String narration, HashSnapshot snapshot) {
                recorder.record(title, narration, mapper.apply(snapshot));
            }
        };
    }
}
