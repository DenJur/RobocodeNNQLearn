package RLBot;

public class NeuralNetState {
    public static final int NumTargetBearing = 4;

    /* state pre-processing */

    /* quantize energy level */
    public static double[] getEnergy(double energy) {
        double[] result = new double[5];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;
        int index = (int) (energy / 20.0);
        if (index > 4) index = 4;
        result[index] = 1;
        return result;
    }

    public static double[] getHeat(double heat) {
        double[] result = new double[2];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;
        if (heat != 0)
            result[0] = 1;
        else
            result[1] = 1;
        return result;
    }

    public static double[] getX(double x) {
        double[] result = new double[8];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;
        int index = (int) (x / 100);
        if (index > 7) index = 7;
        result[index] = 1;
        return result;
    }

    public static double[] getY(double y) {
        double[] result = new double[6];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;
        int index = (int) (y / 100);
        if (index > 5) index = 5;
        result[index] = 1;
        return result;
    }

    /* get robot headings in degrees; normalize to 45 deg precision*/
    public static double[] getHeading(double heading) {
        double[] result = new double[4];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;
        int index = (int) (heading / 90.0);
        if (index > 3) index = 3;
        result[index] = 1;
        return result;
    }

    /* linear quantization of distance */
    public static double[] getTargetDistance(double value) {
        double[] result = new double[10];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;

        int index = (int) (value / 100);
        if (index > 9) index = 9;
        result[index] = 1;
        return result;
    }

    public static double[] getTargetBearing(double bearing) {
        double twoPI = Math.PI * 2;
        int index;
        if (bearing < 0.0)
            bearing = twoPI + bearing;

        double rad = twoPI / NumTargetBearing; // quantatized into 4
        double newBearing = bearing + rad / 2;  // advance by 1/8 rads

        if (newBearing > twoPI)
            newBearing -= twoPI;
        index = (int) (newBearing / rad);

        double[] result = new double[4];
        for (int i = 0; i < result.length; i++)
            result[i] = -1;

        if (index > 3) index = 3;
        result[index] = 1;
        return result;
    }

}
