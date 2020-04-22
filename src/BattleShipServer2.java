import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class BattleShipServer2 extends Application  {
    File filename=new File("scores.csv");
    File filename2=new File("scores2.csv");

    FileWriter fileWriter;
    BufferedWriter bufferedWriter;
    TextArea ta = new TextArea();

    {
        try {
            fileWriter = new FileWriter(filename, true);
            bufferedWriter=new BufferedWriter(fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String info="";
    TextField textforname = new TextField("ENTER NAME");
    int score;
    String name;
    @Override
    public void start(Stage stage) throws Exception {
        Label Usernamelabel = new Label("User Name");

       // Button btOpenAccount=new Button();


        //TextArea ta = new TextArea();

       // ScrollPane sp=new ScrollPane(ta);
        GridPane gridPane = new GridPane();
        gridPane.add(Usernamelabel, 0, 0);
        gridPane.add(textforname, 1, 0);
       // gridPane.add(btOpenAccount,2,0);
        gridPane.setBackground(new Background(new BackgroundImage(new Image("battleship.jpg", 500, 700, false, true), BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        // gridPane.add(sp,0,1);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        //borderPane.setBottom(ta);

        Scene scene = new Scene(borderPane, 500, 700);
        stage.setScene(scene);
        stage.show();


        new Thread(() -> {
            try {
                // Create a server socket
                ServerSocket serverSocket = new ServerSocket(1490);
                ta.appendText("MultiThreadServer started at "
                        + new Date() + '\n');
                while (true) {
                    // Listen for a new connection request
                    Socket socket = serverSocket.accept();
                    InetAddress inetAddress = socket.getInetAddress();
                    ta.appendText("Client's host name is "
                            + inetAddress.getHostName() + "\n");

                    new Thread(new HandleAClient(socket)).start();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }



        }).start();
    }
    class HandleAClient implements Runnable {
        private Socket socket; // A connected socket

        /** Construct a thread */
        public HandleAClient(Socket socket) {
            this.socket = socket;
        }

        /** Run a thread */
        public void run() {
            try {
                // Create data input and output streams
                DataInputStream inputFromClient = new DataInputStream(
                        socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(
                        socket.getOutputStream());

                // Continuously serve the client
                while (true) {
                    // Receive score from the client
                    name=textforname.getText();
                    outputToClient.writeUTF(name);
                    String record;
                    score = inputFromClient.readInt();
                    ta.appendText(String.valueOf(score));
                    //System.out.println(name);
                    // save name and score
                    if(score!=0){
                    info=name+","+score+"/n";
                    //saveincsv(info);
                    writeScores(filename);}

                    record=getHighScores(filename);
                    outputToClient.writeUTF(record);
                    // Send name back to the client

                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
   /* public void saveincsv(String csv){
        try {
            bufferedWriter.newLine();
            bufferedWriter.write(csv);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public void writeScores(File filePath) throws Exception {
        FileWriter writer = new FileWriter(filePath, true);

        try {
            //write to score to file
            writer.append(name+","+score + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //close filewriter
            writer.flush();
            writer.close();
        }
    }
    public String getHighScores(File filePath) throws Exception {
        List<String> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            //read contents of file
            while ((line = reader.readLine()) != null) {
                String[] scoresonly = line.split(",");
                records.add(scoresonly[1]);
            }

            //sort array
            Collections.sort(records, new Comparator<String>() {
                @Override
                public int compare(String current, String other) {
                    return Integer.valueOf(other).compareTo(Integer.valueOf(current));
                }
            });
            return records.get(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}