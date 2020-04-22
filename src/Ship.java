public class Ship {
    private int spaces;
    private String name;
    private boolean sunk;
    private int[][] coordinates;
    private int hits;

    Ship(int spaces, String name){
        this.spaces = spaces;
        this.name = name;
        hits = 0;
        this.sunk = false;
        coordinates = new int[spaces][2];
    }

    //getters and setters
    public int getSpaces() {return spaces;}
    public String getName() {return name;}
    public int[][] getCoordinates() {return coordinates;}
    public void setCoordinates(int[][] coords) {coordinates = coords;}

    //check if ship has been hit
    public boolean isHit(int[] coordinates, boolean[][] board) {
        return board[coordinates[0]][coordinates[1]];
    }

    //check if ship has been sunk
    public boolean isSunk() {
        return hits == spaces;
    }
}