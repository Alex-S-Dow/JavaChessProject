//Class that represents a graph of the moves taken during the game, used for some of the computer algorithms to decide between moves

import java.util.*;

public class BoardGraph {

    //Keeps a set of contained board states, helps not to add repeated board states
    HashSet<BoardState> vertices;
    HashMap<BoardState, HashSet<BoardState>> edges;

    public BoardGraph() {
        vertices = new HashSet<>();
        edges = new HashMap<>();
    }

    public void addVertex(BoardState b) {
        vertices.add(b);
        edges.put(b, new HashSet<>());
    }

    public void addEdge(BoardState source, BoardState dest) {
        //Adds the source/dest if they have not been added
        if(!edges.containsKey(source)) addVertex(source);
        if(!edges.containsKey(dest)) addVertex(dest);

        edges.get(source).add(dest);
    }

    public HashMap<BoardState, HashSet<BoardState>> getEdges() {
        return edges;
    }

    public int size() {
        return vertices.size();
    }

    //Clears the graph
    public void clear() {
        vertices.clear();
        edges.clear();
    }

    public static class BoardState {
        public Chessboard.Piece[][] board;
        public int w_val;
        public int b_val;
        public boolean check;
        public boolean stalemate;
        public boolean checkmate;
        PlayerColor turn;

        BoardState(Chessboard.Piece[][] b, int wv, int bv, boolean c, boolean cm, PlayerColor t) {
            board = b;
            w_val = wv;
            b_val = bv;
            check = c;
            checkmate = cm;
            stalemate = (!c && cm);
            turn = t;
        }
        //Returns the weight of the BoardState for the choosen playercolor
        public int weight(PlayerColor pc) {
            //Returns the max integer value if the choosen playercolor is not the turn player and the other player is in checkmate
            if(checkmate && !stalemate && pc != turn) return Integer.MAX_VALUE;
            if(pc == PlayerColor.white) {
                return w_val - b_val;
            }
            else {
                return b_val - w_val;
            }
        }

        @Override
        public String toString() {
            String s = "";
            for(int i = 0; i < 8; i++) {
                for(int j = 0; j < 8; j++) {
                    s = s + "[" + board[j][i] + "]";
                }
                s = s + "\n";
            }
            return s;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null) return false;
            if(this.getClass() != obj.getClass()) return false;

            BoardState other = (BoardState) obj;
            if(!Arrays.deepEquals(this.board, other.board)) return false;
            if(w_val != other.w_val) return false;
            if(b_val != other.b_val) return false;
            if(check != other.check) return false;
            if(checkmate != other.checkmate) return false;
            if(turn != other.turn) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + Arrays.deepHashCode(this.board);
            hash = 97 * hash + this.w_val;
            hash = 97 * hash + this.b_val;
            hash = 97 * hash + (this.check ? 1 : 0);
            hash = 97 * hash + (this.checkmate ? 1 : 0);
            hash = 97 * hash + Objects.hashCode(this.turn);
            return hash;
        }
    }
}
