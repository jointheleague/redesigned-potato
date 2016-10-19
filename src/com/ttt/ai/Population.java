package com.ttt.ai;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.ttt.model.Board;
import com.ttt.model.TicTacToe;
import com.ttt.model.Tile;

public class Population {

	private ArrayList<HashMap<Double, NeuralNetwork>> pool;
	private Board board;
	private double maxFitness = 0;
	private Random random = new Random();
	private NeuralNetwork base;
	private double mutateRate;
	private int generation = 0;

	public Population(NeuralNetwork base, int n, double mutateRate) {
		pool = new ArrayList<HashMap<Double, NeuralNetwork>>();
		board = new Board(TicTacToe.tiles);
		this.base = base;
		this.mutateRate = mutateRate;
		maxFitness = Math.pow((2 * ((board.BOARD_WIDTH * board.BOARD_HEIGHT) - board.WIN_COUNT)), 2);

		for (int i = 0; i < n; i++) {
			NeuralNetwork nn = new NeuralNetwork();

			Layer[] layers = base.getAllLayers();
			nn.setInputLayer(layers[0]);
			for (int a = 1; a < layers.length - 1; a++) {
				nn.addLogicLayer(layers[a]);
			}
			nn.setOutputLayer(layers[layers.length-1]);
			nn.connectSynapsesBetweenLayers();
			HashMap<Double, NeuralNetwork> map = new HashMap<Double, NeuralNetwork>();
			map.put(0.0, nn);
			pool.add(map);
		}
	}

	public double draw() {
		selection();
		System.out.println("post selection");
		ArrayList<HashMap<Double, NeuralNetwork>> newPool = new ArrayList<HashMap<Double, NeuralNetwork>>();

		for (int i = 0; i < pool.size(); i++) {
			//System.out.println(i);
			HashMap<Double, NeuralNetwork> hash = new HashMap<Double, NeuralNetwork>();
			hash.put(0.0, mutate(crossover(acceptReject(), acceptReject())));
			newPool.add(hash);
		}
System.out.println("Post Crossover");
		double highestFit = Double.MIN_VALUE;
		for (int i = 0; i < pool.size(); i++) {
			if (pool.get(i).keySet().toArray(new Double[1])[0] > highestFit) {
				highestFit = pool.get(i).keySet().toArray(new Double[1])[0];
			}
		}

		pool.removeAll(pool);
		pool.addAll(newPool);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(15);
		System.out.println("Generation: " + generation + ". Highest Fitness: " + nf.format(highestFit));
		generation++;
		return highestFit;
	}

	public void selection() {
		for (int i = 0; i < pool.size(); i++) {
			System.out.println(i);
			board = new Board(TicTacToe.tiles);
			HashMap<Double, NeuralNetwork> hash = pool.get(i);
			NeuralNetwork nn = hash.get(0.0);
			
			int turns = 0;
			int xturns = 0;
			int oturns = 0;
			while (!board.checkWin(Tile.X) && !board.checkWin(Tile.O)
					&& turns < board.BOARD_HEIGHT * board.BOARD_WIDTH) {
				double[] tiles = new double[board.BOARD_WIDTH * board.BOARD_HEIGHT];
				Tile[][] t = board.getTiles();
				int index = 0;
				for (int row = 0; row < t.length; row++) {
					for (int col = 0; col < t[0].length; col++) {
						int val = 0;
						if (t[row][col] == Tile.O) {
							val = -1;
						} else if (t[row][col] == Tile.X) {
							val = 1;
						}
						tiles[index] = val;
						index++;
					}
				}
				for (int x = 0; x < tiles.length; x++) {
					//nn.getInputLayer().getNeuron(x).setValue(tiles[x]);
				}

				nn.compute();

				Layer output = nn.getOutputLayer();
				int[] indexes = bubbleSort(output);

				for (int place : indexes) {
					int x = place % board.BOARD_WIDTH;
					int y = place / board.BOARD_HEIGHT;
					if (board.getTile(x, y) == Tile.EMPTY) {
						board.placeAt(x, y, Tile.X);
						xturns++;
						break;
					}
				}

				int place = pickRandom();
				int x = place % board.BOARD_WIDTH;
				int y = place / board.BOARD_HEIGHT;

				board.placeAt(x, y, Tile.O);
				oturns++;
				turns++;
			}
			hash = new HashMap<Double, NeuralNetwork>();
			if (board.checkWin(Tile.X)) {
				hash.put(Math.pow(2 * ((board.BOARD_WIDTH * board.BOARD_HEIGHT) - xturns), 2), nn);
			} else if (board.checkWin(Tile.O)) {
				hash.put((double) xturns, nn);
			} else if (turns >= board.BOARD_WIDTH * board.BOARD_HEIGHT) {
				hash.put(Math.pow(1 * ((board.BOARD_WIDTH * board.BOARD_HEIGHT) - xturns), 2), nn);
			}
		}
	}

	public int pickRandom() {
		int besafe = 0;
		while (besafe < 10000) {
			int place = random.nextInt(board.BOARD_HEIGHT * board.BOARD_WIDTH);
			int x = place % board.BOARD_WIDTH;
			int y = place / board.BOARD_HEIGHT;
			if (board.getTile(x, y) == Tile.EMPTY) {
				return place;
			}
			besafe++;
		}
		return 0;
	}

	public int[] bubbleSort(Layer output) {

		int n = output.getNeurons().length;
		int temp = 0;
		Neuron nTemp;
		Neuron[] out = output.getNeurons();
		int[] indexes = new int[out.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}

		for (int i = 0; i < n; i++) {
			for (int j = 1; j < (n - i); j++) {

				if (out[j - 1].getValue() > out[j].getValue()) {
					temp = j - 1;
					nTemp = out[j - 1];
					out[j - 1] = out[j];
					indexes[j - 1] = indexes[j];
					out[j] = nTemp;
					indexes[j] = temp;
				}

			}
		}
		return indexes;
	}

	public int highestIndex(Layer output) {
		int highestIndex = 0;
		double highest = Double.MIN_VALUE;
		for (int x = 0; x < output.getNeurons().length; x++) {
			if (output.getNeuron(x).getValue() > highest) {
				highest = output.getNeuron(x).getValue();
				highestIndex = x;
			}
		}
		return highestIndex;
	}

	public NeuralNetwork crossover(NeuralNetwork p1, NeuralNetwork p2) {
		NeuralNetwork child = new NeuralNetwork();

		Layer[] layers = p1.getAllLayers();
		child.setInputLayer(layers[0]);
		for (int a = 1; a < layers.length - 1; a++) {
			child.addLogicLayer(layers[a]);
		}
		Random rand = new Random();

		for (int i = 0; i < p1.getAllLayers().length - 1; i++) {
			Layer l = p1.getAllLayers()[i];
			System.out.println("for 1");
			for (int n = 0; n < l.getNeurons().length; n++) {
				System.out.println("for 2");
				Neuron neuron = l.getNeuron(n);
				System.out.println(neuron.getOutgoingSynapses().length);
				int endings = neuron.getOutgoingSynapses().length;
				for (int s = 0; s < endings; s++) {
					//System.out.println("for 3: " + s);
					if(s > endings){
						System.out.println("Somethings happening.");
					}
					Synapse s1 = neuron.getOutgoingSynapses()[s];
					Synapse s2 = p2.getAllLayers()[i].getNeuron(n).getOutgoingSynapses()[s];

					if (rand.nextBoolean() == true) {
						Synapse syn = new Synapse(child.getAllLayers()[i].getNeuron(n), child.getAllLayers()[i + 1]
								.getNeuron(child.getAllLayers()[i + 1].indexOf(s1.getTargetNeuron())), s1.getWeight());
						child.getAllLayers()[i].getNeuron(n).addOutgoingSynapse(syn);
						child.getAllLayers()[i + 1]
								.getNeuron(child.getAllLayers()[i + 1].indexOf(syn.getTargetNeuron()))
								.addIncomingSynapse(syn);
					} else {
						Synapse syn = new Synapse(child.getAllLayers()[i].getNeuron(n), child.getAllLayers()[i + 1]
								.getNeuron(child.getAllLayers()[i + 1].indexOf(s2.getTargetNeuron())), s2.getWeight());
						child.getAllLayers()[i].getNeuron(n).addOutgoingSynapse(syn);
						child.getAllLayers()[i + 1]
								.getNeuron(child.getAllLayers()[i + 1].indexOf(syn.getTargetNeuron()))
								.addIncomingSynapse(syn);
					}
				}
			}
		}

		return child;
	}

	public NeuralNetwork mutate(NeuralNetwork nn) {
		NeuralNetwork mutation = new NeuralNetwork();

		Layer[] layers = nn.getAllLayers();
		mutation.setInputLayer(layers[0]);
		for (int a = 1; a < layers.length - 1; a++) {
			mutation.addLogicLayer(layers[a]);
		}
		Random rand = new Random();

		for (int i = 0; i < nn.getAllLayers().length - 1; i++) {
			Layer l = nn.getAllLayers()[i];
			for (int n = 0; n < l.getNeurons().length; n++) {
				Neuron neuron = l.getNeuron(n);
				for (int s = 0; s < neuron.getOutgoingSynapses().length; s++) {
					Synapse syn = neuron.getOutgoingSynapses()[s];

					if (rand.nextDouble() < mutateRate) {
						syn = new Synapse(neuron, syn.getTargetNeuron(), -0.5 + (0.5 - -0.5) * rand.nextDouble());
						mutation.getAllLayers()[i].getNeuron(n).addOutgoingSynapse(syn);
						mutation.getAllLayers()[i + 1]
								.getNeuron(mutation.getAllLayers()[i + 1].indexOf(syn.getTargetNeuron()))
								.addIncomingSynapse(syn);
					}
				}
			}
		}

		return mutation;
	}

	public NeuralNetwork acceptReject() {
		int besafe = 0;
		while (besafe < 10000) {
			int index = random.nextInt(pool.size());
			HashMap<Double, NeuralNetwork> partner = pool.get(index);
			int r = random.nextInt((int) maxFitness);
			double key = partner.keySet().toArray(new Double[partner.keySet().size()])[0];

			if (r < key) {
				return partner.get(key);
			}
			besafe++;
		}
		return pool.get(0).get(pool.get(0).keySet().toArray(new Double[1])[0]);

	}

}
