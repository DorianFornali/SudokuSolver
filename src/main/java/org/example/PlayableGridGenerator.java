package org.example;

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

    /** Takes a full sudoku grid and removes cases one by one until a model finds two solutions */
    public int[][] processGrid(int[][] grid) {
        // The goal here is to remove randomly numbers from the grid until we reach a point where the next number to remove
        // would give a second solution to the grid solve. We then stop and return the grid.
        int n = 0; // The amount of numbers removed

        solver.targetGrid = grid;
        // The medium solver will be used for this operation
        solver.setModelLevel(ModelLevel.MEDIUM);

        while(true) {

            // We remove a number from the grid
            int targetX = (int) (Math.random() * SIZE);
            int targetY = (int) (Math.random() * SIZE);
            int oldValue = grid[targetX][targetY];
            grid[targetX][targetY] = 0;
            if(oldValue != 0){
                // Obviously, we don't consider removing already empty cases as a removal
                n++;
            }
            //System.out.println("Removed number at " + targetY + " " + targetX);

            solver.targetGrid = grid;
            solver.buildModel();

            /*System.out.println("Resulting grid:");
            Sudoku.print2Dgrid(grid);
            System.out.println("n = " + n);*/
            // We extract how many solutions the solver found
            int solutions = solver.getModel().getSolver().findAllSolutions().size();

            // System.out.println("SOLUTIONS FOUND:" + solutions);
            if(solutions > 1) {
                // If the solver found more than one solution, we re-put the number in the grid and return the grid
                //System.out.println("Found more than one solution, reverting.");
                grid[targetX][targetY] = oldValue;
                n--;
                if(n >= 53){
                    // We only stop when we have reached the minimum amount of numbers to remove
                    break;
                }
            }


        }
        //System.out.println("Done generating playable grid. Removed " + n + " numbers.");
        return grid;
    }

}
