package com.ttt.ai.hal;

import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.MLMethod;
import org.encog.ml.MLResettable;
import org.encog.ml.MethodFactory;
import org.encog.ml.genetic.MLMethodGeneticAlgorithm;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.FeedForwardPattern;

public class AiTester {
	static int tb[][] = { { 0, 0, 1, 0, 0 }, { 0, 0, 1, 0, 0 }, { 0, 0, 1, 0, 0 }, { 0, 0, 1, 0, 0 },
			{ 0, 0, 1, 0, 0 }, };

	static int size = 3;

	public static BasicNetwork createNetwork() {
		FeedForwardPattern pattern = new FeedForwardPattern();
		pattern.setInputNeurons(size * size);
		pattern.addHiddenLayer(size * size);
		pattern.addHiddenLayer(size * size);
		pattern.addHiddenLayer(size * size);
		pattern.addHiddenLayer(size * size);
		pattern.setOutputNeurons(size * size);
		pattern.setActivationFunction(new ActivationTANH());
		BasicNetwork network = (BasicNetwork) pattern.generate();
		network.reset();
		return network;
	}

	public static void main(String[] args) {
		MLTrain train;
		train = new MLMethodGeneticAlgorithm(new MethodFactory() {
			@Override
			public MLMethod factor() {
				final BasicNetwork result = createNetwork();
				((MLResettable) result).reset();
				return result;
			}
		}, new HalScore(), 1);

		int epoch = 1;

		for (int i = 0; i < 10000; i++) {
			System.out.println("Training #" + epoch);
			train.iteration();
			System.out.println("Epoch #" + epoch + " Score:" + train.getError());
			epoch++;
		}
		train.finishTraining();

	}
}