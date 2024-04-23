package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.example.Sudoku.print2Dgrid;

/** This class is responsible for removing numbers from a grid to make it playable (solvable by our models) */
public class PlayableGridGenerator{
    private Sudoku solver;

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

    /** Takes a full sudoku grid and removes cases one by one until a model finds two solutions, doens't stop until
     * at least 55 cases have been removed.
     * 55 seems to be the best in between speed of generation and difficulty of the grids generated
     * Not specifying a minimum will end up in the generation of too easy grids */

    public int[][] processGrid(int[][] grid){
        // This function will remove numbers from the grid until it finds a grid with two solutions
        // The removal is made following this logic: the number to remove is chosen randomly among the non-empty cells
        // If the solver finds more than one solution, the number is put back in the grid
        // The process stops when the minimum amount of numbers to remove is reached

        int n = 0; // The amount of numbers removed

        solver.targetGrid = grid;
        solver.setModelLevel(ModelLevel.MEDIUM);

        // We create a list of all non-empty cells
        List<int[]> nonEmptyCells = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (grid[i][j] != 0) {
                    nonEmptyCells.add(new int[]{i, j});
                }
            }
        }

        // we shuffle the list
        Collections.shuffle(nonEmptyCells);

        // iterate over the list
        for (int[] cell : nonEmptyCells) {
            int i = cell[0];
            int j = cell[1];

            // Remove the number from the grid
            int oldValue = grid[i][j];
            grid[i][j] = 0;

            solver.targetGrid = grid;
            solver.buildModel();

            // Extract how many solutions the solver found
            int solutions = solver.getModel().getSolver().findAllSolutions().size();

            if(solutions > 1) {
                // If the solver found more than one solution, re-put the number in the grid
                grid[i][j] = oldValue;
            } else {
                n++;
                if(n >= 55){
                    // We only stop when we have reached the minimum amount of numbers to remove
                    break;
                }
            }
        }

        return grid;
    }

}
