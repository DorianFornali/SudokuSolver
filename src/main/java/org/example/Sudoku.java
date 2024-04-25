package org.example;
/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import java.util.*;

import static org.chocosolver.solver.search.strategy.Search.minDomLBSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * <a href="">wikipedia</a>:<br/>
 * "The objective is to fill a 9?9 grid with digits so that
 * each column, each row, and each of the nine 3?3 sub-grids that compose the grid
 * contains all of the digits from 1 to 9.
 * The puzzle setter provides a partially completed grid, which typically has a unique solution."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/04/11
 */
public class Sudoku extends AbstractProblem {
    private static final int n = 9;
    private static final int N = 1000;

    // Bucket containing the grids that have been assessed and their difficulty
    public static Map<GridDifficulty, ArrayList<int[][]>> gridsAssessedBucket;
    IntVar[][] rows, cols, carres;

    /** The current grid to solve */
    public int[][] targetGrid;
    ModelLevel modelLevel;

    // ----------------------- Solving data ------------------------------

    private List<Float> timeTaken = new ArrayList<>();
    private float maxTime = 0;

    public Sudoku(){
        super();
        gridsAssessedBucket = new HashMap<>();
        gridsAssessedBucket.put(GridDifficulty.EASY, new ArrayList<>());
        gridsAssessedBucket.put(GridDifficulty.MEDIUM, new ArrayList<>());
        gridsAssessedBucket.put(GridDifficulty.HARD, new ArrayList<>());
        gridsAssessedBucket.put(GridDifficulty.DIABOLIC, new ArrayList<>());
    }

    public void buildModel() {

        switch (modelLevel) {
            case EASY:
                buildEasyModel();
                break;
            case MEDIUM:
                buildMediumModel();
                break;
            case HARD:
                buildHardModel();
                break;
        }

    }

    /** The easy model consists of alldiff with arithmetic methods for checking inequalities  */
    private void buildEasyModel(){
        model = new Model();

        rows = new IntVar[n][n];
        cols = new IntVar[n][n];
        carres = new IntVar[n][n];


        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (targetGrid[i][j] > 0) {
                    rows[i][j] = model.intVar(targetGrid[i][j]);
                } else {
                    rows[i][j] = model.intVar("c_" + i + "_" + j, 1, n, false);
                }
                cols[j][i] = rows[i][j];
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    carres[j + k * 3][i] = rows[k * 3][i + j * 3];
                    carres[j + k * 3][i + 3] = rows[1 + k * 3][i + j * 3];
                    carres[j + k * 3][i + 6] = rows[2 + k * 3][i + j * 3];
                }
            }
        }


        // Now we create the constraints
        // We will check the equality for each pair of values in the row, column and sub-box with arithmetic method
        // And post() the constraint

        for(int x = 0; x < n; x++){
            for(int y = 0; y < n; y++){
                for(int z = 0; z < n; z++){
                    if(z != y){
                        model.arithm(rows[x][y], "!=", rows[x][z]).post();
                        model.arithm(cols[x][y], "!=", cols[x][z]).post();
                        model.arithm(carres[x][y], "!=", carres[x][z]).post();
                    }
                }
            }
        }

    }


    /** Medium model, consists of the three allDiff constraints using choco solver method with AC consistency */
    private void buildMediumModel(){
        model = new Model();

        rows = new IntVar[n][n];
        cols = new IntVar[n][n];
        carres = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (targetGrid[i][j] > 0) {
                    rows[i][j] = model.intVar(targetGrid[i][j]);
                } else {
                    rows[i][j] = model.intVar("c_" + i + "_" + j, 1, n, false);
                }
                cols[j][i] = rows[i][j];
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    carres[j + k * 3][i] = rows[k * 3][i + j * 3];
                    carres[j + k * 3][i + 3] = rows[1 + k * 3][i + j * 3];
                    carres[j + k * 3][i + 6] = rows[2 + k * 3][i + j * 3];
                }
            }
        }

        for (int i = 0; i < n; i++) {
            model.allDifferent(rows[i], "AC").post();
            model.allDifferent(cols[i], "AC").post();
            model.allDifferent(carres[i], "AC").post();
        }
    }

    /** The hard model has the three allDiff constraints in addition to multiple other implicit constraints to improve
     * its performance */
    private void buildHardModel() {
        model = new Model();

        rows = new IntVar[n][n];
        cols = new IntVar[n][n];
        carres = new IntVar[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (targetGrid[i][j] > 0) {
                    rows[i][j] = model.intVar(targetGrid[i][j]);
                } else {
                    rows[i][j] = model.intVar("c_" + i + "_" + j, 1, n, false);
                }
                cols[j][i] = rows[i][j];
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    carres[j + k * 3][i] = rows[k * 3][i + j * 3];
                    carres[j + k * 3][i + 3] = rows[1 + k * 3][i + j * 3];
                    carres[j + k * 3][i + 6] = rows[2 + k * 3][i + j * 3];
                }
            }
        }


        // Basic medium model constraints but with BC consistency (Hardly any difference)
        for (int i = 0; i < n; i++) {
            model.allDifferent(rows[i], "BC").post();
            model.allDifferent(cols[i], "BC").post();
            model.allDifferent(carres[i], "BC").post();
        }

        // Each number must appear 9 times in the entire box
        // THIS CONSTRAINT ACTUALLY MAKES THE SOLVER LESS POWERFUL, WILL BE IGNORED

        /*
        {
            IntVar[] digitCounts = new IntVar[9];
            for (int i = 0; i < 9; i++) {
                digitCounts[i] = model.intVar("count_" + (i + 1), 9, 9); // each digit appears exactly 9 times
                }

            // Flatten the rows array to a single array
            IntVar[] allCells = Arrays.stream(rows).flatMap(Arrays::stream).toArray(IntVar[]::new);

            // Create and post the count constraints
            for (int i = 0; i < 9; i++) {
                model.count(i + 1, allCells, digitCounts[i]).post();
            }

        }
        */

        // The sum of each row/col/box must be equal to 45
        // THIS CONSTRAINT ACTUALLY MAKES THE SOLVER LESS POWERFUL, WILL BE IGNORED
        /*
        {
            for (IntVar[] row : rows) {
                model.sum(row, "=", 45).post();
            }
            for (IntVar[] col : cols) {
                model.sum(col, "=", 45).post();
            }
            for (IntVar[] carre : carres) {
                model.sum(carre, "=", 45).post();
            }
        }
        
        */
        
        /*
        // Specific constraint, will be explained in the report
        // THIS CONSTRAINT MAKES NO DIFFERENCE IN THE MODEL'S PERFORMANCE
        // SO WE COMMENT IT NOT TO WASTE SPEED
        {
            IntVar[] corners = new IntVar[16];
            // Top left corner
            corners[0] = rows[0][0];
            corners[1] = rows[0][1];
            corners[2] = rows[1][0];
            corners[3] = rows[1][1];

            // Top right corner
            corners[4] = rows[0][7];
            corners[5] = rows[0][8];
            corners[6] = rows[1][7];
            corners[7] = rows[1][8];

            // Bottom left corner
            corners[8] = rows[7][0];
            corners[9] = rows[7][1];
            corners[10] = rows[8][0];
            corners[11] = rows[8][1];

            // Bottom right corner
            corners[12] = rows[7][7];
            corners[13] = rows[7][8];
            corners[14] = rows[8][7];
            corners[15] = rows[8][8];

            IntVar[] middle = new IntVar[16];
            for (int i = 0; i < 5; i++) {
                middle[i] = rows[2][2 + i];
            }
            for (int i = 0; i < 5; i++) {
                middle[i + 5] = rows[6][2 + i];
            }

            middle[10] = rows[3][2];
            middle[11] = rows[4][2];
            middle[12] = rows[5][2];
            middle[13] = rows[3][6];
            middle[14] = rows[4][6];
            middle[15] = rows[5][6];

            // Count the frequency of each number in corners and middle
            for (int j = 1; j <= n; j++) {
                IntVar occurrencesInCorners = model.intVar("occurrencesInCorners_" + j, 0, n);
                IntVar occurrencesInMiddle = model.intVar("occurrencesInMiddle_" + j, 0, n);

                model.count(j, corners, occurrencesInCorners).post();
                model.count(j, middle, occurrencesInMiddle).post();

                model.arithm(occurrencesInCorners, "=", occurrencesInMiddle).post();
            }
        }
        */
        
        // Specific constraint, will be explained in the report
        {
            IntVar[] targetRowArray;
            IntVar[] valueRowArray;
            IntVar[] targetColumnArray;
            IntVar[] valueColumnArray;
            IntVar unit = this.model.intVar(1);

            for (int  line = 0;  line < 9;  line++) {
                for (int i = 0; i < 3; i++) {
                    targetRowArray = new IntVar[9];
                    valueRowArray = new IntVar[9];
                    targetColumnArray = new IntVar[9];
                    valueColumnArray = new IntVar[9];

                    for (int j = 0; j < 9; j++) {
                        if (j / 3 != i) {
                            targetRowArray[j] = rows[ line][j];
                            targetColumnArray[j] = cols[ line][j];
                        } else {
                            for (int k = 0; k < 3; k++) {
                                if (k !=  line % 3) {
                                    int arrayIndex = j % 3 + k * 3;
                                    int rowIndex =  line -  line % 3 + k;
                                    valueRowArray[arrayIndex] = rows[rowIndex][j];
                                    valueColumnArray[arrayIndex] = cols[rowIndex][j];
                                }
                            }
                        }
                    }

                    List<IntVar> targetRowList = new ArrayList<>(Arrays.asList(targetRowArray));
                    targetRowList.removeIf(Objects::isNull);
                    targetRowArray = targetRowList.toArray(new IntVar[0]);

                    List<IntVar> valueRowList = new ArrayList<>(Arrays.asList(valueRowArray));
                    valueRowList.removeIf(Objects::isNull);
                    valueRowArray = valueRowList.toArray(new IntVar[0]);

                    List<IntVar> targetColumnList = new ArrayList<>(Arrays.asList(targetColumnArray));
                    targetColumnList.removeIf(Objects::isNull);
                    targetColumnArray = targetColumnList.toArray(new IntVar[0]);

                    List<IntVar> valueColumnList = new ArrayList<>(Arrays.asList(valueColumnArray));
                    valueColumnList.removeIf(Objects::isNull);
                    valueColumnArray = valueColumnList.toArray(new IntVar[0]);

                    for (int j = 0; j < 6; j++) {
                        model.count(valueRowArray[j], targetRowArray, unit).post();
                        model.count(valueColumnArray[j], targetColumnArray, unit).post();
                    }
                }
            }
        }
        
        

    }


    @Override
    public void configureSearch() {
        model.getSolver().setSearch(minDomLBSearch(append(rows)));
    }

    @Override
    public void solve() {
        //model.getSolver().showStatistics();
        model.getSolver().solve();
        //printGrid(targetGrid);
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }

        //printGrid(targetGrid);
        //model.getSolver().printStatistics();

    }

    /** Prints the grid via the model's vision (constants will be printed as cst and deducted numbers as c_x_y) */
    public void printGrid(int[][] grid) {
        StringBuilder st = new StringBuilder("Sudoku -- %s\n");
        st.append("\t");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                st.append(rows[i][j]).append("\t\t\t");
            }
            st.append("\n\t");
        }

        System.out.println(st);
    }

    public void setModelLevel(ModelLevel level) {
        this.modelLevel = level;
    }

    /** Prints the grid */
    public static void print2Dgrid(int[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        SudokuGridGenerator fullGridGenerator = new SudokuGridGenerator();
        PlayableGridGenerator playableGridGenerator = new PlayableGridGenerator(sudoku);

        System.out.println("Generating " + N + " sudoku grids and trying to solve them . . .");

        for (int i = 0; i < N; i++) {

            // Every 10% of the grids we print a message
            if (i % (N / 10) == 0 && i != 0) {
                System.out.println("Solved " + i + " grids");
            }
            // We generate N full grids
            int[][] grid = PlayableGridGenerator.toTwoDimensionalArray(fullGridGenerator.generateGrid());
            int[][] gridToSolve = playableGridGenerator.processGrid(grid);
            sudoku.targetGrid = gridToSolve;
            // We first try to solve with the easy model
            sudoku.setModelLevel(ModelLevel.EASY);
            sudoku.buildModel();
            sudoku.execute(args);

            float time = sudoku.getModel().getSolver().getMeasures().getTimeCount();
            long failCount = sudoku.getModel().getSolver().getMeasures().getFailCount();
            long backtracks = sudoku.getModel().getSolver().getMeasures().getBackTrackCount();

            if(failCount > 0 || backtracks > 0) {
                // We failed / backtracked with easy model so we try the medium
                sudoku.setModelLevel(ModelLevel.MEDIUM);
                sudoku.buildModel();
                sudoku.execute(args);

                time = sudoku.getModel().getSolver().getMeasures().getTimeCount();
                failCount = sudoku.getModel().getSolver().getMeasures().getFailCount();
                backtracks = sudoku.getModel().getSolver().getMeasures().getBackTrackCount();

                if(failCount > 0 || backtracks > 0){
                    // We failed / backtracked with easy model so we try the hard

                    sudoku.setModelLevel(ModelLevel.HARD);
                    sudoku.buildModel();
                    sudoku.execute(args);

                    time = sudoku.getModel().getSolver().getMeasures().getTimeCount();
                    failCount = sudoku.getModel().getSolver().getMeasures().getFailCount();
                    backtracks = sudoku.getModel().getSolver().getMeasures().getBackTrackCount();
                    if(failCount > 0 || backtracks > 0){
                        gridsAssessedBucket.get(GridDifficulty.DIABOLIC).add(gridToSolve);
                    }
                    else{
                        gridsAssessedBucket.get(GridDifficulty.HARD).add(gridToSolve);
                    }
                } else {
                    gridsAssessedBucket.get(GridDifficulty.MEDIUM).add(gridToSolve);
                }

            } else {
                gridsAssessedBucket.get(GridDifficulty.EASY).add(gridToSolve);
            }
            sudoku.timeTaken.add(time);
        }

        System.out.println("DONE TRYING TO SOLVE " + N + " GRIDS");
        if(!sudoku.timeTaken.isEmpty()) {
            System.out.println("Average time taken: " + sudoku.timeTaken.stream().reduce(0f, Float::sum) / sudoku.timeTaken.size());
            System.out.println("Max time taken: " + sudoku.timeTaken.stream().max(Float::compareTo).get());
        }

        // We conclude by saying how much grids of each difficulty we have
        System.out.println("EASY GRIDS: " + gridsAssessedBucket.get(GridDifficulty.EASY).size());
        System.out.println("MEDIUM GRIDS: " + gridsAssessedBucket.get(GridDifficulty.MEDIUM).size());
        System.out.println("HARD GRIDS: " + gridsAssessedBucket.get(GridDifficulty.HARD).size());
        System.out.println("DIABOLIC GRIDS: " + gridsAssessedBucket.get(GridDifficulty.DIABOLIC).size());

        // Interactive part, we ask the user if he wants to see a grid of a certain difficulty
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\n");
        while(true){
            System.out.println("Do you want to see a grid of a certain difficulty? (y/n)");
            String response = scanner.next();
            if(response.equals("n")){
                break;
            }
            else if(response.equals("y")){
                System.out.println("Which difficulty? (easy, medium, hard, diabolic)");
                String difficulty = scanner.next();
                if(difficulty.equals("easy")){
                    System.out.println("Printing an easy grid:");
                    print2Dgrid(gridsAssessedBucket.get(GridDifficulty.EASY).get((int) (Math.random() * gridsAssessedBucket.get(GridDifficulty.EASY).size())));
                }
                else if(difficulty.equals("medium")){
                    System.out.println("Printing a medium grid:");
                    print2Dgrid(gridsAssessedBucket.get(GridDifficulty.MEDIUM).get((int) (Math.random() * gridsAssessedBucket.get(GridDifficulty.MEDIUM).size())));
                }
                else if(difficulty.equals("hard")){
                    System.out.println("Printing a hard grid:");
                    print2Dgrid(gridsAssessedBucket.get(GridDifficulty.HARD).get((int) (Math.random() * gridsAssessedBucket.get(GridDifficulty.HARD).size())));
                }
                else if(difficulty.equals("diabolic")){
                    System.out.println("Printing a diabolic grid:");
                    print2Dgrid(gridsAssessedBucket.get(GridDifficulty.DIABOLIC).get((int) (Math.random() * gridsAssessedBucket.get(GridDifficulty.DIABOLIC).size())));
                }
                else{
                    System.out.println("Invalid difficulty");
                }
            }
            else{
                System.out.println("Invalid response");
            }
        }

        }
}