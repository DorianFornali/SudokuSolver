
<h1>Sudoku constraint programming</h1>

<h2>Universitary Project Master 1 - University Cote d'Azur</h2>

<br>

The goal was to create an algorithm capable of generating multiple valid sudoku grids and use choco-solver to create three constraint programming models that would help us assess each grid and rate them with a difficulty. For instance, if the best model can't solve the grid without backtracking, then it means the grid has a diabolic difficulty.

  

<h2>Prerequisites</h2>

- Maven installed with version 3.9 or higher

- Java 17 installed

  

<h2>How to run</h2>
The pom.xml contains a build plugin that executes the main function, to run:
  
```shell
mvn clean compile exec:java
```
