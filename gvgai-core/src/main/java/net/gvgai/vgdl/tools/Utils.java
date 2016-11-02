package net.gvgai.vgdl.tools;

public class Utils {
    /**
     * Adds a small noise to the input value.
     * @param input value to be altered
     * @param epsilon relative amount the input will be altered
     * @param random random variable in range [0,1]
     * @return epsilon-random-altered input value
     */
    public static double noise( double input, double epsilon, double random ) {
        if (input != -epsilon) {
            return (input + epsilon) * (1.0 + epsilon * (random - 0.5));
        }
        else {
            //System.out.format("Utils.tiebreaker(): WARNING: value equal to epsilon: %f\n",input);
            return (input + epsilon) * (1.0 + epsilon * (random - 0.5));
        }
    }

    //Normalizes a value between its MIN and MAX.
    public static double normalise( double a_value, double a_min, double a_max ) {
        if (a_min < a_max) {
            return (a_value - a_min) / (a_max - a_min);
        }
        else {
            return a_value;
        }
    }

    private Utils() {

    }
}
