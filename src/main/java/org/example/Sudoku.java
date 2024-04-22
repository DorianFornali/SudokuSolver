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


        // The sum of each row/col/box must be equal to 45

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

        // Hidden Pairs Constraint
        for (int num1 = 1; num1 <= n; num1++) {
            for (int num2 = num1 + 1; num2 <= n; num2++) {
                for (IntVar[][] cellSet : new IntVar[][][]{rows, cols, carres}) {
                    for (IntVar[] cells : cellSet) {
                        IntVar countNum1 = model.intVar("count_" + num1, 0, 2);
                        IntVar countNum2 = model.intVar("count_" + num2, 0, 2);
                        model.count(num1, cells, countNum1).post();
                        model.count(num2, cells, countNum2).post();
                        model.or(
                                model.and(
                                        model.arithm(countNum1, "=", 2),
                                        model.arithm(countNum2, "=", 2)
                                ),
                                model.and(
                                        model.arithm(countNum1, "!=", 2),
                                        model.arithm(countNum2, "!=", 2)
                                )
                        ).post();
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
            //print2Dgrid(gridToSolve);
            // We first try to solve with the easy model
            sudoku.setModelLevel(ModelLevel.EASY);
            sudoku.buildModel();
            sudoku.execute(args);

            float time = sudoku.getModel().getSolver().getMeasures().getTimeCount();
            long failCount = sudoku.getModel().getSolver().getMeasures().getFailCount();
            long backtracks = sudoku.getModel().getSolver().getMeasures().getBackTrackCount();

            if(failCount > 0 || backtracks > 0) {
                // We failed / backtracked with easy model so we try the medium
                //System.out.println("The grid is too hard to" +
                //        " solve with the easy model, trying with the medium model");
                // Now trying with medium model
                sudoku.setModelLevel(ModelLevel.MEDIUM);
                sudoku.buildModel();
                sudoku.execute(args);

                time = sudoku.getModel().getSolver().getMeasures().getTimeCount();
                failCount = sudoku.getModel().getSolver().getMeasures().getFailCount();
                backtracks = sudoku.getModel().getSolver().getMeasures().getBackTrackCount();

                if(failCount > 0 || backtracks > 0){
                    // We failed / backtracked with easy model so we try the hard
                    //System.out.println("The grid is too hard to" +
                    //        " solve with the medium model, trying with the hard model");
                    // Now trying with hard model
                    //System.out.println("Trying to solve a supposedly hard grid");
                    sudoku.setModelLevel(ModelLevel.HARD);
                    sudoku.buildModel();
                    sudoku.execute(args);

                    time = sudoku.getModel().getSolver().getMeasures().getTimeCount();
                    failCount = sudoku.getModel().getSolver().getMeasures().getFailCount();
                    backtracks = sudoku.getModel().getSolver().getMeasures().getBackTrackCount();
                    // before setting it as diabolic we check if it took more than 5 seconds to solve
                    if((failCount > 0 || backtracks > 0) && time > 0.05f){
                        //System.out.println("The grid is too hard for all models, setting it as diabolic");
                        gridsAssessedBucket.get(GridDifficulty.DIABOLIC).add(gridToSolve);
                    }
                    else{
                        //System.out.println("Solved a grid with the hard model");
                        gridsAssessedBucket.get(GridDifficulty.HARD).add(gridToSolve);
                    }
                } else {
                    //System.out.println("Solved a grid with the medium model");
                    gridsAssessedBucket.get(GridDifficulty.MEDIUM).add(gridToSolve);
                }

            } else {
                //System.out.println("Solved a grid with the easy model");
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