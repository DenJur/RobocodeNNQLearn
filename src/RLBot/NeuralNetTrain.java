package RLBot;

import java.io.IOException;
import java.util.Random;
import java.io.File;

public class NeuralNetTrain {
    public static final double ALPHA = 0.1;
    public static final double GAMMA = 0.9;
    public static double EPSILON = 0.0;
    private final String weightsFile = "nnWeight.txt";
    private final File wFile = new File(weightsFile);

    private NeuralNet nn;

    public NeuralNetTrain(boolean load) {
        nn = new NeuralNet(Action.NumberStats+Action.NumActions, 15, load, 0.01, 0.9);
        if(load) {
            nnLoad(weightsFile);
        }
    }

    public double nnTrain(double[] inputs, double imRwd) {
        /* Q-Learning (off-policy) learning */
        double prevQValue = nn.outputFor(inputs, false);
        double newQValue = prevQValue + ALPHA * (imRwd + GAMMA * getMaxQValue(inputs) - prevQValue);
        double diff = newQValue - prevQValue;
        nn.train(inputs, newQValue, false);
        return diff;
    }

    public int selectAction(double[] inputs) {
        Random rn = new Random();
        if (rn.nextDouble() <= EPSILON) {
            /* random move */
            return rn.nextInt(Action.NumActions);
        } else {
            /* greedy move */
            return getBestAction(inputs);
        }
    }

    public double getMaxQValue(double[] inputs) {

        double maxQ = Double.NEGATIVE_INFINITY;
        double[] temp;
        temp = inputs;
        for (int i = Action.NumberStats; i < Action.NumberStats+Action.NumActions; i++) {
            temp[i]=-1;
        }
        for (int i = Action.NumberStats; i < Action.NumberStats+Action.NumActions; i++) {
            temp[i] = 1;
            if(i>Action.NumberStats) temp[i-1]=-1;
            double tempQ = nn.outputFor(temp, false);
            if (tempQ > maxQ)
                maxQ = tempQ;
        }
        return maxQ;
    }

    public int getBestAction(double[] inputs) {

        double maxQ = Double.NEGATIVE_INFINITY;
        int bestAction = 0;
        double[] temp;
        temp = inputs;
        for (int i = Action.NumberStats; i < Action.NumberStats+Action.NumActions; i++) {
            temp[i]=-1;
        }
        for (int i = Action.NumberStats; i < Action.NumberStats+Action.NumActions; i++) {
            temp[i] = 1;
            if(i>Action.NumberStats) temp[i-1]=-1;
            double tempQ = nn.outputFor(temp, false);
            if (tempQ > maxQ) {
                maxQ = tempQ;
                bestAction = i;
            }
        }
        return bestAction-Action.NumberStats;
    }

    public void nnSave() {
        nn.save(wFile);
    }

    public void nnLoad(String s) {
        try {
            nn.load(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}