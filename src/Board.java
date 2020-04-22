

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Board {
    private boolean[][] board;
    Board() {board = new boolean[10][10];}

    public boolean[][] getBoard(){return board;}

    public boolean isValid(int[] coordinate){

        if(coordinate[0] > 9 || coordinate[1] > 9){
            return false;
        }
        //if the space is occupied
        if (board[coordinate[0]][coordinate[1]]){
            return false;
        }

        return true;
    }
    public boolean isValid (int[][] coordinates){
        for (int[] coord : coordinates) {
            if (!isValid(coord))
                return false;
        }
        return true;
    }
    public boolean isHit( int[] coordinate){
        if (board[coordinate[0]][coordinate[1]]) {
            board[coordinate[0]][coordinate[1]] = false;
            return true;
        }
        return false;
    }
    public boolean hasLost(){
        for (boolean[] coords : board) {
            for (int j = 0; j < board.length; j++) {
                if (coords[j])
                    return false;
            }
        }

        return true;
    }

    public void cleanBoard(GraphicsContext gc) {
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
