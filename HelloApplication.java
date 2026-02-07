package com.example.project1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class HelloApplication extends Application {
    List<String[]> lines = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        welcome(stage);
    }

    public Button designButton(String name, int width, int height, String color1, String color2) {
        Button button = new Button(name);
        button.setPrefSize(width, height);
        button.setStyle("-fx-background-color:" + color1 + "; -fx-background-radius: 15px; -fx-text-fill:" + color2);
        return button;
    }

    public void welcome(Stage stage) {
        BorderPane pane = new BorderPane();

        Button read = designButton("Click To Read File", 150, 50, "#014920", "#FDFDFD");

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(read);

        pane.setCenter(vbox);

        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        stage.setMaximized(true);
        Scene scene = new Scene(pane, 0, 0);
        stage.setScene(scene);
        stage.setTitle("Welcome");
        stage.show();

        read.setOnAction(e -> {
            readFile(stage);
            stage.close();
            buttonsFrame(stage);
        });
    }

    public void buttonsFrame(Stage stage) {
        BorderPane pane = new BorderPane();

        Button optimalSol = designButton("Optimal Solution", 150, 50, "#014920", "#FDFDFD");
        Button anotherSol = designButton("Alternative Solution", 150, 50, "#014920", "#FDFDFD");
        Button tableData = designButton("Dynamic Table", 150, 50, "#014920", "#FDFDFD");

        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(optimalSol, anotherSol, tableData);

        TextArea text = new TextArea();
        text.setPrefRowCount(100);
        text.setPrefColumnCount(100);
        text.setMaxWidth(600);
        text.setMaxHeight(400);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(text, hbox);

        pane.setCenter(vbox);

        pane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        stage.setMaximized(true);
        Scene scene = new Scene(pane, 0, 0);
        stage.setScene(scene);
        stage.setTitle("Chose");
        stage.show();

        optimalSol.setOnAction(e -> {
            text.clear();
            text.appendText(optimalSolution(lines));
        });

        anotherSol.setOnAction(e -> {
            text.clear();
            text.appendText(alternativeSolution(lines));
        });

        tableData.setOnAction(e -> {
            text.clear();
            for (int i = 0; i < DPTable(lines).length; i++) {
                for (int j = 0; j < DPTable(lines)[i].length - 1; j++) {
                    text.appendText(DPTable(lines)[i][j] + "\t");
                }
                text.appendText("\n");
            }
        });
    }

    // the functions

    private void readFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                Scanner in = new Scanner(file);
                while (in.hasNextLine()) {
                    String str = in.nextLine();
                    lines.add(str.split(", "));
                }
                in.close();
            } catch (FileNotFoundException e) {
                System.out.println("error");
                e.printStackTrace();
            }
        }
    }

    public static String optimalSolution(List<String[]> lines) {
        // extract the number of cities from the first element of the input list
        int numCities = Integer.parseInt(lines.get(0)[0]);

        // create maps to associate city names with indices and vice versa
        HashMap<String, Integer> indexMap = new HashMap<>();
        HashMap<Integer, String> cityMap = new HashMap<>();

        // populate the maps with city names and indices
        int index = 0;
        for (int i = 2, j = 0; i < lines.size(); i++) {
            indexMap.put(lines.get(i)[j], index);
            cityMap.put(index, lines.get(i)[j]);
            index++;
        }

        // add the end city to the maps
        indexMap.put(lines.get(1)[1], index);
        cityMap.put(index, lines.get(1)[1]);

        // prepare a list to store edge costs
        List<String[]> edgeCosts = new ArrayList<>();

        // iterate over the input lines to extract edge costs
        int count = 0;
        for (int i = 2; i < lines.size(); i++) {
            for (int j = 1; j < lines.get(i).length; j++) {
                // extract the cost from the input string and calculate the total cost
                int commaIndex = lines.get(i)[j].indexOf(",");
                String substr = lines.get(i)[j].substring(commaIndex + 1);
                substr = substr.replaceAll("]", "");
                String[] splitStr = substr.split(",");
                int cost = Integer.parseInt(splitStr[0]) + Integer.parseInt(splitStr[1]);
                // prepare a formatted string for edge costs and add it to the list
                String formattedStr = lines.get(i)[0] + "," + lines.get(i)[j].charAt(1) + "," + cost;
                formattedStr = formattedStr.replaceAll("]", "");
                edgeCosts.add(formattedStr.split(","));
                count++;
            }
        }

        // add suffixes to the last three edge costs
        edgeCosts.get(count - 1)[1] = edgeCosts.get(count - 1)[1] + "nd";
        edgeCosts.get(count - 2)[1] = edgeCosts.get(count - 2)[1] + "nd";
        edgeCosts.get(count - 3)[1] = edgeCosts.get(count - 3)[1] + "nd";

        // initialize arrays to store costs and dynamic programming table
        int[][] adjacencyMatrix = new int[numCities + 1][numCities + 1];
        int[][] dpTable = new int[numCities + 1][numCities + 1];
        int counter = 0;

        // initialize arrays with zeros
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                adjacencyMatrix[i][j] = 0;
                dpTable[i][j] = 0;
            }
        }

        // populate the adjacency matrix with edge costs
        for (int i = 0; i < edgeCosts.size(); i++) {
            int startCityIndex = indexMap.get(edgeCosts.get(i)[0]);
            int endCityIndex = indexMap.get(edgeCosts.get(i)[1]);
            adjacencyMatrix[startCityIndex + 1][endCityIndex + 1] = Integer.parseInt(edgeCosts.get(i)[2]);
        }

        // create maps to associate city names with indices and vice versa for the optimal solution
        HashMap<String, Integer> indexPathMap = new HashMap<>();
        HashMap<Integer, String> indexCityMap = new HashMap<>();
        int indexOptimal = 1;
        for (int i = 2, j = 0; i < lines.size(); i++) {
            indexPathMap.put(lines.get(i)[j], indexOptimal);
            indexCityMap.put(indexOptimal, lines.get(i)[j]);
            indexOptimal++;
        }
        indexPathMap.put(lines.get(1)[1], indexOptimal);
        indexCityMap.put(indexOptimal, lines.get(1)[1]);

        // initialize arrays and variables for calculating the optimal solution
        int[] costs = new int[numCities + 1];
        int[] path = new int[numCities + 1];
        ArrayList<String[]> temp = new ArrayList<>();
        costs[numCities] = 0;
        int[][] dpTable1 = new int[numCities + 1][numCities + 1];
        HashMap<Integer, String> alternativeSolutions = new HashMap<>();
        int counter1 = 0;
        int counter2 = 0;
        int solutionCount = 0;
        String solutions = "";

        // perform dynamic programming to find the optimal solution
        for (int v = numCities - 1, e = numCities - 2; v >= 1 && e >= 0; v--, e--) {
            for (int i = numCities - v; i >= 1; i--) {
                int min = 32767;
                for (int k = i + 1; k <= numCities - e; k++) {
                    int totalCost = (adjacencyMatrix[i][k] + costs[k]);
                    solutions += totalCost + " ";
                    // update the minimum cost and path if needed
                    if (adjacencyMatrix[i][k] != 0 && adjacencyMatrix[i][k] + costs[k] < min) {
                        min = adjacencyMatrix[i][k] + costs[k];
                        path[i] = k;
                    }
                    costs[i] = min;
                    counter1++;
                }
                alternativeSolutions.put(solutionCount, solutions);
                solutionCount++;
                solutions = "";
                counter2++;
            }
            String str = "";
            for (int i = 0; i < costs.length; i++) {
                str += costs[i] + " ";
            }
            temp.add(str.split(" "));
        }

        // convert the temporary array to the dynamic programming table
        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < temp.get(i).length; j++) {
                int replacement = Integer.parseInt(temp.get(i)[j]);
                if (replacement == 32767)
                    replacement = 0;
                dpTable1[j][i + 1] = replacement;
            }
        }

        // calculate the number of stages in the optimal solution
        int stages = counter1 / counter2;
        int[] optimalPath = new int[stages + 2];
        optimalPath[1] = 1;
        optimalPath[stages] = numCities;

        // populate the path array with indices representing the optimal path
        for (int i = 2; i <= stages + 1; i++) {
            optimalPath[i] = path[optimalPath[i - 1]];
        }

        // convert the indices in the path array to city names
        String optimalPathStr = "";
        for (int i = 1; i < optimalPath.length; i++) {
            optimalPathStr += indexCityMap.get(optimalPath[i]) + " ";
        }

        // calculate the total cost of the optimal solution
        int totalCostOptimal = 0;
        for (int i = 1; i < 2; i++) {
            totalCostOptimal += costs[i];
        }

        // prepare the optimal solution string
        String optimalSolution = optimalPathStr + totalCostOptimal;
        return optimalSolution;
    }

    public static String alternativeSolution(List<String[]> lines) {
        // parse the number of cities from the first element of the first array in the input list
        int numCities = Integer.parseInt(lines.get(0)[0]);

        // create a map to store the index of each city and its corresponding name
        HashMap<String, Integer> indexMap = new HashMap<>();
        HashMap<Integer, String> cityMap = new HashMap<>();

        // populate the index map and city map
        int index = 0;
        for (int i = 2; i < lines.size(); i++) {
            indexMap.put(lines.get(i)[0], index);
            cityMap.put(index, lines.get(i)[0]);
            index++;
        }
        indexMap.put(lines.get(1)[1], index);
        cityMap.put(index, lines.get(1)[1]);

        // store edge costs in a list
        List<String[]> edgeCost = new ArrayList<>();

        // loop through the input list to extract edge costs
        int count = 0;
        for (int i = 2; i < lines.size(); i++) {
            for (int j = 1; j < lines.get(i).length; j++) {
                // extract the edge cost from the input string and add it to the edge cost list
                int commaIndex = lines.get(i)[j].indexOf(",");
                String str = lines.get(i)[j].substring(commaIndex + 1);
                str = str.replaceAll("]", "");
                String[] s = str.split(",");
                int cost = Integer.parseInt(s[0]) + Integer.parseInt(s[1]);
                String st = lines.get(i)[0] + "," + lines.get(i)[j].charAt(1) + "," + cost;
                st = st.replaceAll("]", "");
                edgeCost.add(st.split(","));
                count++;
            }
        }

        // adjust last three edge costs to nd
        edgeCost.get(count - 1)[1] = edgeCost.get(count - 1)[1] + "nd";
        edgeCost.get(count - 2)[1] = edgeCost.get(count - 2)[1] + "nd";
        edgeCost.get(count - 3)[1] = edgeCost.get(count - 3)[1] + "nd";

        // initialize adjacency matrix and dynamic programming table
        int[][] adjacencyMatrix = new int[numCities + 1][numCities + 1];
        int[][] dpTable = new int[numCities + 1][numCities + 1];

        // populate adjacency matrix with edge costs
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                adjacencyMatrix[i][j] = 0;
                dpTable[i][j] = 0;
            }
        }

        for (int i = 0; i < edgeCost.size(); i++) {
            int start = indexMap.get(edgeCost.get(i)[0]);
            int end = indexMap.get(edgeCost.get(i)[1]);
            adjacencyMatrix[start + 1][end + 1] = Integer.parseInt(edgeCost.get(i)[2]);
        }

        // create maps to store the index and name of each city in the path
        HashMap<String, Integer> indexPath = new HashMap<>();
        HashMap<Integer, String> cityPath = new HashMap<>();
        int pathIndex = 1;
        for (int i = 2; i < lines.size(); i++) {
            indexPath.put(lines.get(i)[0], pathIndex);
            cityPath.put(pathIndex, lines.get(i)[0]);
            pathIndex++;
        }
        indexPath.put(lines.get(1)[1], pathIndex);
        cityPath.put(pathIndex, lines.get(1)[1]);

        // initialize arrays for costs and paths
        int[] cost = new int[numCities + 1];
        int[] d = new int[numCities + 1];
        ArrayList<String[]> temp = new ArrayList<>();
        cost[numCities] = 0;
        int[][] dpTable1 = new int[numCities + 1][numCities + 1];
        HashMap<Integer, String> alternativeSolution = new HashMap<>();
        int counter1 = 0;
        int counter2 = 0;
        int solCount = 0;
        String solString = "";

        // calculate alternative solutions
        for (int v = numCities - 2, e = numCities - 2; v >= 1 && e >= 0; v--, e--) {
            for (int i = numCities - v; i >= 1; i--) {
                int min = 32767;
                for (int k = i + 1; k <= numCities - e; k++) {
                    int ye = (adjacencyMatrix[i][k] + cost[k]);
                    solString += ye + " ";
                    if (adjacencyMatrix[i][k] != 0 && adjacencyMatrix[i][k] + cost[k] <= min) {
                        min = adjacencyMatrix[i][k] + cost[k];
                        d[i] = k;
                    }
                    cost[i] = min;
                    counter1++;
                }
                alternativeSolution.put(solCount, solString);
                solCount++;
                solString = "";
                counter2++;
            }
            String sr = "";
            for (int i = 0; i < cost.length; i++) {
                sr += cost[i] + " ";
            }
            temp.add(sr.split(" "));
        }

        // populate dynamic programming table
        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < temp.get(i).length; j++) {
                int rep = Integer.parseInt(temp.get(i)[j]);
                if (rep == 32767)
                    rep = 0;
                dpTable1[j][i + 1] = rep;
            }
        }

        // calculate stages and paths
        int stages = counter1 / counter2;
        int[] path = new int[stages + 2];
        path[1] = 1;
        path[stages] = numCities;
        for (int i = 2; i <= stages + 1; i++) {
            path[i] = d[path[i - 1]];
        }

        // generate optimal solution string
        String pathString = "";
        for (int i = 1; i < path.length; i++) {
            pathString += cityPath.get(path[i]) + " ";
        }
        int totalCost = 0;
        for (int i = 1; i < 2; i++) {
            totalCost += cost[i];
        }
        String optimalSolution = pathString + totalCost;
        return optimalSolution;
    }

    public static String[][] DPTable(List<String[]> lines) {
        // number of cities is number of nodes
        // parse the number of cities from the first element of the first array in the input list
        int numCities = Integer.parseInt(lines.get(0)[0]);

        // create a map to store the index of each city and its corresponding name
        HashMap<String, Integer> indexMap = new HashMap<>();
        HashMap<Integer, String> cityMap = new HashMap<>();

        // populate the index map and city map
        int index = 0;
        for (int i = 2, j = 0; i < lines.size(); i++) {
            indexMap.put(lines.get(i)[j], index);
            cityMap.put(index, lines.get(i)[j]);
            index++;
        }
        indexMap.put(lines.get(1)[1], index);
        cityMap.put(index, lines.get(1)[1]);

        // store edge costs in a list
        List<String[]> edgeCost = new ArrayList<>();

        // loop through the input list to extract edge costs
        int count = 0;
        for (int i = 2; i < lines.size(); i++) {
            for (int j = 1; j < lines.get(i).length; j++) {
                int commaIndex = lines.get(i)[j].indexOf(",");
                String str = lines.get(i)[j].substring(commaIndex + 1);
                str = str.replaceAll("]", "");
                String[] s = str.split(",");
                int cost = Integer.parseInt(s[0]) + Integer.parseInt(s[1]);
                String st = lines.get(i)[0] + "," + lines.get(i)[j].charAt(1) + "," + cost;
                st = st.replaceAll("]", "");
                edgeCost.add(st.split(","));
                count++;
            }
        }

        // adjust last three edge costs to nd
        edgeCost.get(count - 1)[1] = edgeCost.get(count - 1)[1] + "nd";
        edgeCost.get(count - 2)[1] = edgeCost.get(count - 2)[1] + "nd";
        edgeCost.get(count - 3)[1] = edgeCost.get(count - 3)[1] + "nd";

        // initialize adjacency matrix and dynamic programming table
        int[][] adjacencyMatrix = new int[numCities + 1][numCities + 1];
        int[][] dpTable = new int[numCities + 1][numCities + 1];
        int counter = 0;
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                adjacencyMatrix[i][j] = 0;
                dpTable[i][j] = 0;
            }
        }

        // populate adjacency matrix with edge costs
        for (int i = 0, j = 0; i < edgeCost.size(); i++) {
            int start = indexMap.get(edgeCost.get(i)[j]);
            int end = indexMap.get(edgeCost.get(i)[j + 1]);
            adjacencyMatrix[start + 1][end + 1] = Integer.parseInt(edgeCost.get(i)[j + 2]);
        }

        // create maps to store the index and name of each city in the path
        HashMap<String, Integer> indexPath = new HashMap<>();
        HashMap<Integer, String> cityPath = new HashMap<>();
        int pathIndex = 1;
        for (int i = 2, j = 0; i < lines.size(); i++) {
            indexPath.put(lines.get(i)[j], pathIndex);
            cityPath.put(pathIndex, lines.get(i)[j]);
            pathIndex++;
        }
        indexPath.put(lines.get(1)[1], pathIndex);
        cityPath.put(pathIndex, lines.get(1)[1]);

        // initialize arrays for costs and paths
        int[] cost = new int[numCities + 1];
        int[] d = new int[numCities + 1];
        ArrayList<String[]> temp = new ArrayList<>();
        cost[numCities] = 0;
        int[][] dpTable1 = new int[numCities + 1][numCities + 1];
        HashMap<Integer, String> alternativeSolution = new HashMap<>();
        int counter1 = 0;
        int counter2 = 0;
        int solCount = 0;
        String solString = "";

        // calculate alternative solutions
        for (int v = numCities - 1, e = numCities - 2; v >= 1 && e >= 0; v--, e--) {
            for (int i = numCities - v; i >= 1; i--) {
                int min = 32767;
                for (int k = i + 1; k <= numCities - e; k++) {
                    int ye = (adjacencyMatrix[i][k] + cost[k]);
                    solString += ye + " ";
                    if (adjacencyMatrix[i][k] != 0 && adjacencyMatrix[i][k] + cost[k] < min) {
                        min = adjacencyMatrix[i][k] + cost[k];
                        d[i] = k;
                    }
                    cost[i] = min;
                    counter1++;
                }
                alternativeSolution.put(solCount, solString);
                solCount++;
                solString = "";
                counter2++;
            }
            String sr = "";
            for (int i = 0; i < cost.length; i++) {
                sr += cost[i] + " ";
            }
            temp.add(sr.split(" "));
        }

        // populate dynamic programming table
        for (int i = 0; i < temp.size(); i++) {
            for (int j = 0; j < temp.get(i).length; j++) {
                int rep = Integer.parseInt(temp.get(i)[j]);
                if (rep == 32767)
                    rep = 0;
                dpTable1[j][i + 1] = rep;
            }
        }

        // convert dynamic programming table to string array
        String[][] strDp = new String[numCities + 2][numCities + 2];
        for (int l = 0; l < dpTable1.length; l++) {
            for (int m = 0; m < dpTable1[l].length; m++) {
                strDp[l][m] = dpTable1[l][m] + "";
            }
        }

        // populate city names in the first row and column of the string array
        int cities = 0;
        for (int k = 1; k < dpTable1.length; k++) {
            strDp[0][k] = cityMap.get(cities);
            cities++;
        }
        int cities1 = 0;
        for (int k = 1; k < dpTable1.length; k++) {
            strDp[k][0] = cityMap.get(cities1);
            cities1++;
        }

        // calculate stages and paths
        int stages = counter1 / counter2;
        int[] path = new int[stages + 2];
        path[1] = 1;
        path[stages] = numCities;

        // generate alternative solutions
        for (int i = 2; i <= stages + 1; i++) {
            path[i] = d[path[i - 1]];
        }

        // return the dynamic programming table
        return strDp;
    }
}
