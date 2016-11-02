package com.ttt.ai.hal;

import java.util.Random;

public class TTTSim {
	int[][] board;
	boolean playing = true;
	int moves;

	public TTTSim(int size) {
		moves = 0;
		board = new int[size][size];
		size = size - 1;
		for (int i = 0; i <= size; i++) {
			for (int j = 0; j <= size; j++) {
				try {
					board[i][j] = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	public int[][] getBoard() {
		return board;
	}

	public void setBoard(int[][] b) {
		board = b;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public void printBoard() {
		System.out.println("");
		System.out.println("Moves:" + moves);
		for (int i = 0; i < board.length; i++) {
			System.out.println("");
			for (int j = 0; j < board.length; j++) {
				if (board[i][j] == -1)
					System.out.print(2);
				else
					System.out.print(board[i][j]);

			}
		}
		System.out.println("");
	}

	public boolean place(int type, int x, int y) {
		if (board[x][y] != 0)
			return false;
		board[x][y] = type;
		moves++;
		return true;
	}

	public boolean TestForWin() {
		boolean win = false;
		if (isWinner(-1) || isWinner(1)) {
			this.playing = false;
			System.out.println("Winner Found");
			win = true;
		}
		if (isTie()) {
			this.playing = false;
			win = true;
			System.out.println("Tie");
		}
		return win;
	}

	public boolean isTie() {
		boolean tie = true;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j] == 0)
					tie = false;
			}
		}
		if (tie) {
			System.out.println("Tie!");
		}
		return tie;
	}

	public boolean isWinner(int player) {
		for (int i = 0; i < board.length; i++) {
			boolean verticalWin = true, horizontalWin = true;
			for (int j = 0; j < board.length; j++) {
				if (board[i][j] != player)
					horizontalWin = false;
				if (board[j][i] != player)
					verticalWin = false;
				if (!(horizontalWin || verticalWin))
					break;
			}
			if (horizontalWin || verticalWin) {
				playing = false;
				return true;
			}
		}
		boolean diagonalWinOne = true, diagonalWinTwo = true;
		for (int n = 0; n < board.length; n++) {
			diagonalWinOne = true;
			diagonalWinTwo = true;
			int row = board.length - 1 - n;
			if (board[n][n] != player)
				diagonalWinOne = false;
			if (board[row][n] != player)
				diagonalWinTwo = false;
			if (!(diagonalWinOne || diagonalWinTwo))
				break;
		}
		if (diagonalWinOne || diagonalWinTwo) {
			playing = false;
			return true;
		} else
			return false;
	}

	public boolean playing() {
		return playing;
	}

	public double score(int player) {
	//TODO scoreing.
	}

	public void randomMove(int player) {
		boolean go = true;
		Random r = new Random();
		while (go) {
			int x = r.nextInt(board.length);
			int y = r.nextInt(board.length);
			if (board[x][y] == 0) {
				board[x][y] = player;
				go = false;
			}

		}
		moves++;
	}
}