package com.jelly.MightyMiner.features.impl.helper;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Ease {
    public static final List<Function<Float, Float>> EASING_FUNCTIONS = Arrays.asList(
        Ease::easeOutBack,
        Ease::easeOutSine,
        Ease::easeInOutSine,
        Ease::easeOutQuad,
        Ease::easeOutCubic,
        Ease::easeOutCirc,
        Ease::easeOutMinJerk
    );

    public static float easeOutBack(float x) {
        return 1 + 2 * (x - 1) * (x - 1) * (x - 1) + (x - 1) * (x - 1);
    }

    public static float easeOutSine(float x) {
        return (float) Math.sin((x * Math.PI) / 2);
    }

    public static float easeInOutSine(float x) {
        return (float) (-(Math.cos(x * Math.PI) - 1) / 2);
    }

    public static float easeOutQuad(float x) {
        return 1 - (1 - x) * (1 - x);
    }

    public static float easeOutCubic(float x) {
        return 1 - (1 - x) * (1 - x) * (1 - x);
    }

    public static float easeOutCirc(float x) {
        return (float) Math.sqrt(1 - (x - 1) * (x - 1));
    }

    public static float easeOutMinJerk(float x) {
        return (float) (6 * Math.pow(x, 5) - 15 * Math.pow(x, 4) + 10 * Math.pow(x, 3));
    }

    public static Function<Float, Float> getRandomEaseFunction(){
        int rand = new Random().nextInt(EASING_FUNCTIONS.size()-1);
        return EASING_FUNCTIONS.get(rand);
    }
}
