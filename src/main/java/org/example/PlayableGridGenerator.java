package org.example;

/** This class is responsible for removing numbers of the grid to make them playable */
public class PlayableGridGenerator{

    private final Sudoku solver;

    public PlayableGridGenerator(Sudoku solver) {
        this.solver = solver;
    }

    public static final int SIZE = 9;
    public static final int EMPTY = 0;

    /** Transforms a 1D full sudoku grid to 2D*/
    public static int[][] toTwoDimensionalArray(int[] grid) {
        int[][] twoD = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                twoD[i][j] = grid[i * SIZE + j];
            }
        }
        return twoD;
    }

    /** Takes a full sudoku grid and removes the maximum amount of numbers possible without increasing the amount of
     * possible solution */
    public int[][] processGrid(int[][] grid) {
        // The goal here is to remove randomly numbers from the grid until we reach a point where the next number to remove
        // would give a second solution to the grid solve. We then stop and return the grid.

        // First we need a way to check how many solutions the grid has
        solver.targetGrid = grid;
        return grid;
    }
}
