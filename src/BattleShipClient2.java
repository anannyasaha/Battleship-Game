

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BattleShipClient2 extends Application {
    private TextArea ta = new TextArea();

    private final String path = "scores.csv";

    //boards
    private Board playerBoard= new Board();
    private Board opponentBoard = new Board();
    //to keep track of the grids which have been discovered
    private Board disclosedboardPlayer = new Board();
    private Board disclosedboardopponent = new Board();
    //position what have been hit
    private Board hitboardPlayer = new Board();
    private Board hitboardopponent = new Board();
    //position what have been missed
    private Board missboardPlayer = new Board();
    private Board missboardopponent = new Board();
    //to take the coordinates from the user and the opponent and initialising it to something invalid.
    int[] coordinate = {10, 10};
    //to make sure what input length is put by the user.


    public int numberofturns=2;
    public int playerscore;
    public String name;

    //game saving
    String filename = "current game.txt";
    //io streams
    private DataOutputStream toServer = null;
    private DataInputStream fromServer = null;

    //create board graphics
    private GridPane pane = new GridPane();
    private Canvas playerCanvas = new Canvas(500, 500);
    private Canvas opponentCanvas = new Canvas(500, 500);
    private GraphicsContext gcP = playerCanvas.getGraphicsContext2D();
    private GraphicsContext gcO = opponentCanvas.getGraphicsContext2D();

    //create ships
    private Ship destroyer = new Ship(4, "Destroyer");
    private Ship battleship = new Ship(3, "Battleship");
    private Ship patrol = new Ship(2, "Patrol");
    private Ship submarine = new Ship(3, "Submarine");
    private Ship carrier = new Ship(5, "Carrier");
    private final Ship[] ships = {destroyer, battleship, patrol, submarine, carrier};
    private boolean playerTurn = true;

    @Override
    public void start(Stage stage) throws Exception {
        Image imagewater = new Image("water.jpg");
        Image imagefire = new Image("fire.PNG");
        String fire = "fire.mp3";
        String water = "water.mp3";
        Media soundfire = new Media(new File(fire).toURI().toString());
        MediaPlayer mediafire = new MediaPlayer(soundfire);

        Media soundwater = new Media(new File(water).toURI().toString());
        MediaPlayer mediawater = new MediaPlayer(soundwater);

        //set stage
        Label labelformoves=new Label("Moves");
        labelformoves.setMinSize(20,20);
        Label moves=new Label();
        moves.setMinSize(70,70);
        GridPane paneformove=new GridPane();
        paneformove.add(labelformoves,0,0);
        paneformove.add(moves,0,1);
        ta.setWrapText(true);
        Button startGame = new Button("START THE GAME");
        Button exitGame = new Button("EXIT AND SAVE THE GAME ");
        Button resumegame = new Button("RESUME THE PREVIOUS GAME");
        Button sendScore = new Button("SEND SCORE");

        //coordinates text
        String strRowCor1 = "    0       ";
        for(int i = 1; i<10; i++) {
            int buf = 10 - i;
            if(i < 5 ) {
                strRowCor1 = strRowCor1  + "       " + i + "       ";
            } else if(i >= 5) {
                strRowCor1 = strRowCor1  + "       " + i + "     ";
            }
        }
        Text bottomRowCor1 = new Text(strRowCor1);
        bottomRowCor1.setStyle("-fx-font-weight: bold");

        String strRowCor2 = "    0       ";
        for(int i = 1; i<10; i++) {
            int buf = 10 - i;
            if(i < 5 ) {
                strRowCor2 = strRowCor2  + "       " + i + "       ";
            } else if(i >= 5) {
                strRowCor2 = strRowCor2  + "       " + i + "     ";
            }
        }
        Text bottomRowCor2 = new Text(strRowCor2);
        bottomRowCor2.setStyle("-fx-font-weight: bold");


        // tf.setPromptText("Enter Name");
        // tf.setMaxWidth(500);
        ta.setEditable(false);
        ta.setFont(new Font("Verdana", 15));
        //tf.setAlignment(Pos.CENTER_LEFT);
        pane.add(playerCanvas, 0, 0);
        pane.add(opponentCanvas, 1, 0);
        pane.add(paneformove,2,0);

        //pane.add(tf, 1, 3, 1, 1);

        pane.add(sendScore, 0, 3, 2, 1);

        pane.add(ta, 0, 4, 2, 1);
        pane.add(startGame, 0, 2, 2, 1);
        pane.add(resumegame, 1, 2, 2, 1);
        pane.add(exitGame, 1, 3, 2, 1);

        pane.add(bottomRowCor1,0,1,1,1);
        pane.add(bottomRowCor2,1,1,1,1);

        pane.setHgap(50);
        stage.setScene(new Scene(pane));
        stage.setTitle("BattleShip");
        stage.show();

        //set boards
        setBoard(opponentBoard);
        setBoard(playerBoard);


        //draw grid
        pane.setMinHeight(600);
        for (int i = 50; i <= 500; i += 50) {
            gcP.strokeLine(i, 0, i, 500);
            gcP.strokeLine(0, i, 500, i);

            //gcP.strokeText(String.valueOf(i / 50), i, 15);
            gcP.strokeText(String.valueOf(i / 50 - 1), 3, i);

            gcO.strokeLine(i, 0, i, 500);
            gcO.strokeLine(0, i, 500, i);

            //gcO.strokeText(String.valueOf(i / 50), i, 15);
            gcO.strokeText(String.valueOf(i / 50 - 1), 3, i);
        }
        ta.appendText("Welcome to the battleship game.To Start the game click Start the Game.Then write down the number of columns and rows to attack different position.The board on left is your board where the SHips have been placed for you.If you want to save the game click exit and save.If you want to resume a previous game select Resume \n");
        ta.appendText("You have to beat the opponent by least moves possible \n");
        //play game
        resumegame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    load();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        opponentCanvas.setOnMouseClicked(e-> {



                   coordinate[0]=Math.floorDiv((int) e.getX(),50);
                   coordinate[1]=Math.floorDiv((int) e.getY(),50);
           if(disclosedboardopponent.getBoard()[coordinate[0]][coordinate[1]]==true){
                if (hitboardopponent.getBoard()[coordinate[0]][coordinate[1]] == true) {
                    gcO.drawImage(imagefire, coordinate[0] * 50 + 2, coordinate[1] * 50 + 2, 45, 45);
                }
                if (missboardopponent.getBoard()[coordinate[0]][coordinate[1]] == true) {
                    gcO.drawImage(imagewater, coordinate[0] * 50 + 2, coordinate[1] * 50 + 2, 45, 45);
                }
            }
           else if (opponentBoard.hasLost()) {
                    ta.appendText("You have won! \n");
                    ta.appendText("Click 'send score'\n");
                    ta.appendText("You will see if you have beaten highest Score '\n");
                    playerscore = 1000 / numberofturns;
                    ta.appendText("Your score is " + playerscore + '\n');

                } else if (playerBoard.hasLost()) {
                    playerscore = 0;
                    ta.appendText("You have lost. \n");

                }

           else
               {
                if (opponentBoard.isHit(coordinate)) {

                    //gcO.setFill(Color.RED);
                    gcO.drawImage(imagefire, coordinate[0] * 50 + 2, coordinate[1] * 50 + 2, 45, 45);


                    hitboardopponent.getBoard()[coordinate[0]][coordinate[1]] = true;
                    //ta.appendText("You have Hit opponent.\n");
                    mediafire.stop();
                    mediafire.play();

                } else if (!opponentBoard.isHit(coordinate)) {
                    missboardopponent.getBoard()[coordinate[0]][coordinate[1]] = true;
                   // ta.appendText("You have missed. \n");
                    //gcO.setFill(Color.BLACK);
                    mediawater.stop();
                    mediawater.play();
                    gcO.drawImage(imagewater, coordinate[0] * 50 + 2, coordinate[1] * 50 + 2, 45, 45);
                    // System.out.println(coordinate[0]);
                    // System.out.println(coordinate[1]);

                }
                disclosedboardopponent.getBoard()[coordinate[0]][coordinate[1]] = true;
                //isValidMove()
                // draw columns numbers again over to keep the visible
                for (int i = 50; i <= 500; i += 50) {
                    //gcO.strokeText(String.valueOf(i / 50), i, 15);
                    gcO.strokeText(String.valueOf(i / 50 - 1), 3, i);
                }
                //ta.appendText(--numberofturns + " is your turn number \n");
                moves.setFont(new Font("Verdana",40));
                moves.setText(String.valueOf(--numberofturns));
                //System.out.println(coordinate[0]);
                // System.out.println(coordinate[1]);
                changeTurn();

                //computer turn
                   PauseTransition pause = new PauseTransition(Duration.seconds(2));
                   pause.play();
                   Random r = new Random();
                int[] coordinate2 = new int[2];
                coordinate2[0] = r.nextInt(10);
                coordinate2[1] = r.nextInt(10);
                while (!isValidMove(coordinate2, disclosedboardPlayer.getBoard())) {

                    coordinate2[0] = r.nextInt(10);
                    coordinate2[1] = r.nextInt(10);
                }
                if (playerBoard.isHit(coordinate2)) {

                    //gcP.setFill(Color.RED);
                    //playerscore = playerscore - 25;
                    // opponentscore = opponentscore + 25;
                    hitboardPlayer.getBoard()[coordinate2[0]][coordinate2[1]] = true;
                    gcP.drawImage(imagefire, coordinate2[0] * 50 + 2, coordinate2[1] * 50 + 2, 45, 45);
                    mediafire.stop();
                    mediafire.play();
                    //ta.appendText("You have been HIT! \n ");

                } else {
                   // ta.appendText("Opponent has missed. \n");
                   // gcP.setFill(Color.BLACK);
                    mediawater.stop();
                    mediawater.play();
                    gcP.drawImage(imagewater, coordinate2[0] * 50 + 2, coordinate2[1] * 50 + 2, 45, 45);
                    missboardPlayer.getBoard()[coordinate2[0]][coordinate2[1]] = true;

                }
                //gcP.fillOval(coordinate2[0] * 50 + 20, coordinate2[1] * 50 + 20, 10, 10);
                disclosedboardPlayer.getBoard()[coordinate2[0]][coordinate2[1]] = true;

                  changeTurn();
                // }
                System.out.println(coordinate[0]);
                System.out.println(coordinate[1]);

                //check who has lost
                if (opponentBoard.hasLost()) {
                    ta.appendText("You have won! \n");
                    ta.appendText("Click 'send score'\n");
                    ta.appendText("You will see if you have beaten highest Score '\n");
                    playerscore = 1000/numberofturns;
                    ta.appendText("Your score is "+playerscore+'\n');

                } else if (playerBoard.hasLost()) {
                    playerscore = 0;
                    ta.appendText("You have lost. \n");

                }


            }
        });

        exitGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    save();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
        sendScore.setOnAction(e->{
            try{
                List<String> records = new ArrayList<>();
                //System.out.println(playerscore);//REMOVE
                toServer.writeInt(playerscore);
                toServer.flush();
                //String highscores=fromServer.readUTF();

                ta.appendText(playerscore+"\n");
                ta.appendText(name+"\n");
                String highscore=fromServer.readUTF();
                ta.appendText(highscore+" is the Highest Score \n");
            }
            catch(IOException ex){
                System.err.println(ex);
            }});

        try {
            Socket socket = new Socket("localhost", 1490);

            //create an input stream to send data to the server
            fromServer = new DataInputStream(socket.getInputStream());

            //create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
        startGame.setOnAction(e->{
            try{
                name=fromServer.readUTF();
                ta.appendText("Your User name is " + name + "\n");
            }catch(Exception ex){
                ex.printStackTrace();
            }

        });
    }
    // user enters a coordinates to fire


    public boolean isValidMove(int[] coordinate, boolean[][] discloseboard) {
        if (coordinate[1] > 9 || coordinate[0] > 9) {
            //ta.appendText("Please Type a different coordinate.This coordinate is out of board");
            return false;
        } else if (discloseboard[coordinate[0]][coordinate[1]]) {
            //ta.appendText("Please Type a different coordinate.This grid is already discovered");
            return false;
        }

        return true;
    }

    public void placeShips(boolean[][] board, int[][] coordinate) {
        for (int[] coords : coordinate) {
            board[coords[0]][coords[1]] = true;
            Image imageship = new Image("ship.PNG");
            if (board == playerBoard.getBoard()) {
                //gcP.setFill(Color.AQUA);
                //gcP.fillRect(coords[0] * 50 + 2, coords[1] * 50 + 2, 45, 45);
                gcP.drawImage(imageship, coords[0] * 50 + 2, coords[1] * 50 + 2, 45, 45);
            }
        }
    }
    public void setBoard(Board board) {
        for (int i = 0; i < ships.length; i++) {
            Random r = new Random();

            int[][] c = new int[ships[i].getSpaces()][2];
            c[0][0] = r.nextInt(10); //number from 0-9
            c[0][1] = r.nextInt(10);

            //50% chance of ships being placed vertically or horizontally
            if (r.nextInt() % 2 ==0){
                //horizontal placement
                for (int j = 1; j < c.length; j++) {
                    c[j][0] = c[j-1][0] + 1;
                    c[j][1] = c[j-1][1];
                }
            } else {
                //vertical placement
                for (int j = 1; j < c.length; j++) {
                    c[j][0] = c[j-1][0];
                    c[j][1] = c[j-1][1] + 1;
                }
            }

            //check if position is valid
            if (board.isValid(c)) {
                placeShips(board.getBoard(), c);
                ships[i].setCoordinates(c);
            } else {
                i--;
            }
        }
    }

    public int[] convertToInt(String s) {
        int[] coordinates = new int[2];
        coordinates[0] = Character.getNumericValue(s.charAt(0));
        coordinates[1] = Character.getNumericValue(s.charAt(1));
        return coordinates;
    }

    public boolean getPlayerturn() {
        return playerTurn;
    }

    public void changeTurn() {
        playerTurn = !playerTurn;
        numberofturns++;
    }

    public List<String> getHighScores(String filePath) throws Exception {
        List<String> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            //read contents of file
            while ((line = reader.readLine()) != null) {
                String[] scoresonly=line.split(",");
                records.add(scoresonly[1]);
            }

            //sort array
            Collections.sort(records, new Comparator<String> () {
                @Override
                public int compare(String current, String other) {
                    return Integer.valueOf(current).compareTo(Integer.valueOf(other));
                }
            });
            return records;
        }
    }



    public void save() throws Exception {
        Writer writer = null;
        String state_of_board = "";
        try {
            //different line in the file different board.
            writer = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    state_of_board = i + "," + j + "," + playerBoard.getBoard()[i][j] + ",";
                    writer.write(state_of_board);
                }
            }
            writer.write("\n");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    state_of_board = String.valueOf(i) + "," + String.valueOf(j) + "," + String.valueOf(opponentBoard.getBoard()[i][j] + ",");
                    writer.write(state_of_board);
                }
            }
            writer.write("\n");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    state_of_board = String.valueOf(i) + "," + String.valueOf(j) + "," + String.valueOf(hitboardPlayer.getBoard()[i][j] + ",");
                    writer.write(state_of_board);
                }
            }
            writer.write("\n");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    state_of_board = String.valueOf(i) + "," + String.valueOf(j) + "," + String.valueOf(hitboardopponent.getBoard()[i][j] + ",");
                    writer.write(state_of_board);
                }
            }
            writer.write("\n");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    state_of_board = String.valueOf(i) + "," + String.valueOf(j) + "," + String.valueOf(missboardPlayer.getBoard()[i][j] + ",");
                    writer.write(state_of_board);
                }
            }
            writer.write("\n");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    state_of_board = String.valueOf(i) + "," + String.valueOf(j) + "," + String.valueOf(missboardopponent.getBoard()[i][j] + ",");
                    writer.write(state_of_board);
                }
            }
            writer.write("\n");
            System.out.println(numberofturns);
            writer.write(String.valueOf(numberofturns));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            writer.flush();
            writer.close();
        }
    }

    public void load() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filename));

        String[] entries = new String[300];
        String[] bool = new String[100];
        int index = 0;
        try {
            //this string has columnindex,rowindex and value in that index of just the first line
            String specific_line_text = Files.readAllLines(Paths.get(filename)).get(0);
            System.out.println(specific_line_text);

            entries = specific_line_text.split(",", -1);
            //loading the player board
            for (int i = 0; i < entries.length; i++) {
                try {
                    playerBoard.getBoard()[Integer.valueOf(entries[i])][Integer.valueOf(entries[++i])] = Boolean.valueOf(entries[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("jbj");
                }
            }
            Image imageship = new Image("ship.PNG");

            playerBoard.cleanBoard(gcP);
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    if (playerBoard.getBoard()[j][k]) {
                        gcP.drawImage(imageship, j * 50 + 2, k * 50 + 2, 45, 45);
                    }
                }
            }
            //loading the opponent board
            specific_line_text = Files.readAllLines(Paths.get(filename)).get(1);


            entries = specific_line_text.split(",", -1);
            opponentBoard.cleanBoard(gcO);
            for (int i = 0; i < entries.length; i++) {
                try {
                    opponentBoard.getBoard()[Integer.valueOf(entries[i])][Integer.valueOf(entries[++i])] = Boolean.valueOf(entries[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("jbj");
                }
            }
            //loading the hit board for player
            specific_line_text = Files.readAllLines(Paths.get(filename)).get(2);


            entries = specific_line_text.split(",", -1);

            for (int i = 0; i < entries.length; i++) {
                try {
                    hitboardPlayer.getBoard()[Integer.valueOf(entries[i])][Integer.valueOf(entries[++i])] = Boolean.valueOf(entries[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("jbj");
                }
            }
            Image imagefire = new Image("fire.PNG");


            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    if (hitboardPlayer.getBoard()[j][k]) {
                        gcP.drawImage(imagefire, j * 50 + 2, k * 50 + 2, 45, 45);
                    }
                }
            }
            specific_line_text = Files.readAllLines(Paths.get(filename)).get(3);


            entries = specific_line_text.split(",", -1);

            for (int i = 0; i < entries.length; i++) {
                try {
                    hitboardopponent.getBoard()[Integer.valueOf(entries[i])][Integer.valueOf(entries[++i])] = Boolean.valueOf(entries[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("jbj");
                }
            }


            //cleanBoard(playerBoard,gcP);
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    if (hitboardopponent.getBoard()[j][k]) {
                        gcO.drawImage(imagefire, j * 50 + 2, k * 50 + 2, 45, 45);
                    }
                }
            }
            specific_line_text = Files.readAllLines(Paths.get(filename)).get(4);


            entries = specific_line_text.split(",", -1);

            for (int i = 0; i < entries.length; i++) {
                try {
                    missboardPlayer.getBoard()[Integer.valueOf(entries[i])][Integer.valueOf(entries[++i])] = Boolean.valueOf(entries[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("jbj");
                }
            }
            Image imagewater = new Image("water.jpg");


            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    if (missboardPlayer.getBoard()[j][k]) {
                        gcP.drawImage(imagewater, j * 50 + 2, k * 50 + 2, 45, 45);
                    }
                }
            }
            specific_line_text = Files.readAllLines(Paths.get(filename)).get(5);


            entries = specific_line_text.split(",", -1);

            for (int i = 0; i < entries.length; i++) {
                try {
                    missboardopponent.getBoard()[Integer.valueOf(entries[i])][Integer.valueOf(entries[++i])] = Boolean.valueOf(entries[++i]);
                } catch (NumberFormatException e) {
                    System.out.println("jbj");
                }
            }



            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    if (missboardopponent.getBoard()[j][k]) {
                        gcO.drawImage(imagewater, j * 50 + 2, k * 50 + 2, 45, 45);
                    }
                }
            }
            specific_line_text = Files.readAllLines(Paths.get(filename)).get(6);
            numberofturns=Integer.parseInt(specific_line_text);
            numberofturns+=2;


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();

        } finally {
            br.close();
        }


    }


    public void cleanBoard(boolean[][] board, GraphicsContext gc) {
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 10; k++) {
                gc.setFill(Color.WHITE);
                gc.fillRect(i * 50 + 2, k * 50 + 2, 45, 45);
            }
        }
        for (int i = 50; i <= 500; i += 50) {
            gc.strokeText(String.valueOf(i / 50), i, 15);
            gc.strokeText(String.valueOf(i / 50 - 1), 3, i);
        }
    }
}

