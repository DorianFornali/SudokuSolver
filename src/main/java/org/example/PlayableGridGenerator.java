package org.example;

/** This class is responsible for removing numbers of the grid to make them playable */
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

    /** Takes a full sudoku grid and removes the maximum amount of numbers possible without increasing the amount of
     * possible solution */
    public int[][] processGrid(int[][] grid) {
        // The goal here is to remove randomly numbers from the grid until we reach a point where the next number to remove
        // would give a second solution to the grid solve. We then stop and return the grid.

        solver.targetGrid = grid;
        solver.setModelLevel(ModelLevel.MEDIUM);
        solver.buildModel();
        // The medium solver will be used for this operation
        while(solver.getModel().getSolver().getSolutionCount() <= 1) {

            // We remove a number from the grid
            System.out.println("Removing a number");
            int targetX = (int) (Math.random() * SIZE);
            int targetY = (int) (Math.random() * SIZE);
            grid[targetX][targetY] = 0;

            solver.targetGrid = grid;
            solver.execute();

            System.out.println("Resulting grid:");
            Sudoku.print2Dgrid(grid);
            // We extract how many solutions the solver found
            System.out.println("SOLUTIONS FOUND:" + solver.getModel().getSolver().getSolutionCount());
        }

        return grid;
    }
}
