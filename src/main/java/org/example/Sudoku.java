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
    IntVar[][] rows, cols, carres;

    /** The current grid to solve */
    int[][] targetGrid;


    public void buildModel() {
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

    @Override
    public void configureSearch() {
        model.getSolver().setSearch(minDomLBSearch(append(rows)));

    }

    @Override
    public void solve() {
        model.getSolver().showStatistics();
        model.getSolver().solve();
        /*try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }*/

        printGrid(targetGrid);

        model.getSolver().printStatistics();
    }

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

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        SudokuGridGenerator fullGridGenerator = new SudokuGridGenerator();
        int[][] grid = PlayableGridGenerator.toTwoDimensionalArray(fullGridGenerator.generateGrid());

        PlayableGridGenerator playableGridGenerator = new PlayableGridGenerator(sudoku);
        int[][] gridToSolve = playableGridGenerator.processGrid(grid);

        // The grid is now ready to be solved
        sudoku.targetGrid = gridToSolve;
        sudoku.execute(args);
    }

}