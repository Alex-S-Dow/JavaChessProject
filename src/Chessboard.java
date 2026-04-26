//Actual Chessboard class where the game logic is done

//May have to change some of the imports for the PieceType's and Board graph based on where they are in the project/IDE.
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import static java.lang.Math.abs;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

//Create some constants
enum PieceType {king, queen, rook, knight, bishop, pawn, none}
enum PlayerColor {white, black, none}

public class Chessboard {
    //jPanel that will be pulled by the main class and added to the jFrame
    private final JPanel startGUI = new JPanel(new BorderLayout(3, 3));
    //jPanel that represents the actual board with an 8x8 gridLayout, added to the startGui in initialization
    private JPanel chessboard = new JPanel(new GridLayout(0, 8));

    //Arrays to represent the board
    private JButton[][] chessboardsquares = new JButton[8][8];
    private Piece[][] chessboardstatus = new Piece[8][8];
    private int[][] squarevalue;

    //Arrays to hold the images of each piece
    private ImageIcon[] w_pieceImages = new ImageIcon[7];
    private ImageIcon[] b_pieceImages = new ImageIcon[7];

    //Holds the total value of players pieces
    private int w_pieceValue = 0;
    private int b_pieceValue = 0;

    //boolean to switch between playing with someone or against the computer
    boolean TwoPlayers = true;
    //boolean used when promoting a pawn
    boolean promoting = false;
    //JLabel Array to hold labels for the toolbar
    private JLabel[] messages = {new JLabel("Click Reset to Start"), new JLabel("Click Computer to play against the Computer"), new JLabel("Difficulty")};
    //Variable used for the toolbar difficulty button
    private int difficulty = 0;
    //Variable that the game will use to determine the computers difficulty. Will not change during the game
    private int gameDifficulty = 0;
    private boolean computerGame = false;
    //Variables held to help the computer algorithms
    BoardGraph gamemoves = new BoardGraph();
    //Storage for data to graph some metrics, used for my Algorithms project, can be discarded if not needed.
    ArrayList<Long> moveTimes = new ArrayList<>();
    ArrayList<Integer> graphSize = new ArrayList<>();

    Chessboard() { //Constructor for the Chessboard
        InitPieceImages();
        InitBoardStatus();
        InitGUI();
        //Initializes the value of each square, used for the computer algorithms
        int[][] sqvalue = {
                {28, 32, 34, 34, 34, 34, 32, 28},
                {32, 39, 43, 43, 43, 43, 39, 32},
                {34, 43, 49, 49, 49, 49, 43, 34},
                {34, 43, 49, 51, 51, 49, 43, 34},
                {34, 43, 49, 51, 51, 49, 43, 34},
                {34, 43, 49, 49, 49, 49, 43, 34},
                {32, 39, 43, 43, 43, 43, 39, 32},
                {28, 32, 34, 34, 34, 34, 32, 28}
        };
        squarevalue = sqvalue;
    }
    //Sets up the board with null pieces. Basically empty squares
    public final void InitBoardStatus() {
        for(int i = 0; i < chessboardstatus.length; i++) {
            for(int j = 0; j < chessboardstatus[i].length; j++) {
                chessboardstatus[i][j] = new Piece();
            }
        }
    }
    //Initializes and scales the images for each chess piece
    public final void InitPieceImages() {

        //White Pieces
        w_pieceImages[0] = loadImage("king_w.png");
        w_pieceImages[1] = loadImage("queen_w.png");
        w_pieceImages[2] = loadImage("bishop_w.png");
        w_pieceImages[3] = loadImage("knight_w.png");
        w_pieceImages[4] = loadImage("rook_w.png");
        w_pieceImages[5] = loadImage("pawn_w.png");
        w_pieceImages[6] = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));

        //Black Pieces
        b_pieceImages[0] = loadImage("king_b.png");
        b_pieceImages[1] = loadImage("queen_b.png");
        b_pieceImages[2] = loadImage("bishop_b.png");
        b_pieceImages[3] = loadImage("knight_b.png");
        b_pieceImages[4] = loadImage("rook_b.png");
        b_pieceImages[5] = loadImage("pawn_b.png");
        b_pieceImages[6] = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
    }
    //Loads image from the resource folder src/main/resource/ChessPieceImages
    private ImageIcon loadImage(String filename) {
        //May need to change the exact path name if your images are stored in a different location, may need some trial and error
        URL imgURL = getClass().getClassLoader().getResource(filename);
        // Returns an exception if the img could not be loaded
        if (imgURL == null) {
            System.out.println("PNG's must be stored in:");
            // System print to find the classpath root, where the ide searches for resources
            System.out.println(getClass().getClassLoader().getResource(""));
            throw new RuntimeException("Image not found: " + filename);
        }
        return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH));
    }
    //Sets up the GUI to start the game and what to add to the JFrame in the main class
    public final void InitGUI() {
        //Set up the start GUI
        startGUI.setBorder(new EmptyBorder(5, 5, 5, 5));
        //Adds a toolbar
        JToolBar tools = new JToolBar();
        tools.setFloatable(false); //Makes the toolbar immovable
        startGUI.add(tools, BorderLayout.PAGE_START); //Adds the toolbar to the GUI panel

        //Start button for the game
        JButton startButton = new JButton("Reset");
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //Clears some variables used for my project, can be cut if not needed
                gamemoves.clear();
                moveTimes.clear();
                graphSize.clear();
                //Sets the board with each piece in the starting position
                setBoard();
            }
        });
        //Adds the startButton to the toolbar
        tools.add(startButton);
        tools.addSeparator();
        tools.add(messages[0]); //Adds the appropiate jLabel for the button on the toolbar
        tools.addSeparator(new Dimension(20, 0)); //Adds some space between labels and buttons on the toolbar

        //Button that changes whether you are playing against the computer or another person
        JButton computerButton = new JButton("Computer");
        computerButton.addActionListener(new ActionListener() {
            //Changes the text color if you are playing against the compter or not
            public void actionPerformed(ActionEvent e) {
                TwoPlayers = !TwoPlayers;
                if(!TwoPlayers) {
                    computerButton.setForeground(Color.red);
                }
                else {
                    computerButton.setForeground(Color.black);
                }
            }
        });
        //Adds the compuerButton to the toolbar
        tools.add(computerButton);
        tools.addSeparator();
        tools.add(messages[1]); //Adds the appropiate jLabel for the button on the toolbar
        tools.addSeparator(new Dimension(5, 0)); //Adds some space between labels and buttons on the toolbar

        //Button that changes the algorithm the computer uses
        JButton difficultyButton = new JButton("Easy");
        difficultyButton.setForeground(Color.green);
        difficultyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(difficulty == 0) {
                    difficulty++;
                    difficultyButton.setText("Med");
                    difficultyButton.setForeground(Color.yellow);
                }
                else if(difficulty == 1) {
                    difficulty++;
                    difficultyButton.setText("Hard");
                    difficultyButton.setForeground(Color.red);
                }
                else {
                    difficulty = 0;
                    difficultyButton.setText("Easy");
                    difficultyButton.setForeground(Color.green);
                }

            }
        });
        //Adds the difficultyButton to the toolbar
        tools.add(difficultyButton);
        tools.add(messages[2]); //Adds the appropiate jLabel for the button on the toolbar
        tools.addSeparator(new Dimension(2, 0)); //Adds some space between labels and buttons on the toolbar

        //Adds the chessboard to the GUI
        startGUI.add(chessboard);
        chessboard.setBorder(new LineBorder(Color.BLACK));
        //Default empty square image
        ImageIcon defaultIcon = new ImageIcon(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
        //Will make the buttons right next to each other
        Insets buttonMargin = new Insets(0,0,0,0);

        //Iterate through chessboardsquares to add the buttons for our game logic
        for(int i = 0; i < chessboardsquares.length; i++) {
            for(int j = 0; j < chessboardsquares[i].length; j++) {
                JButton b = new JButton();
                //Adds the button that will peform game actions
                b.addActionListener(new ButtonListener(i, j));
                b.setMargin(buttonMargin);
                b.setIcon(defaultIcon);
                //Changes the color of each button/square
                if((j % 2 == 1 && i % 2 == 1)|| (j % 2 == 0 && i % 2 == 0)) b.setBackground(Color.WHITE);
                else b.setBackground(Color.blue);
                b.setOpaque(true);
                b.setBorderPainted(false);
                chessboardsquares[j][i] = b;
            }
        }
        //Iterates through chessbaordsquares to add them to the chessboard jPanel
        for(int i = 0; i < chessboardsquares.length; i++) {
            for(int j = 0; j < chessboardsquares[i].length; j++) {
                chessboard.add(chessboardsquares[j][i]);
            }
        }
    }
    //Returns the image of a given chess piece
    public ImageIcon getImageIcon(Piece piece) {
        if(piece.color.equals(PlayerColor.white)) {
            switch (piece.type) {
                case king: return w_pieceImages[0];
                case queen: return w_pieceImages[1];
                case bishop: return w_pieceImages[2];
                case knight: return w_pieceImages[3];
                case rook: return w_pieceImages[4];
                case pawn: return w_pieceImages[5];
                case none: return w_pieceImages[6];
            }
        }
        else if(piece.color.equals(PlayerColor.black)) {
            switch (piece.type) {
                case king: return b_pieceImages[0];
                case queen: return b_pieceImages[1];
                case bishop: return b_pieceImages[2];
                case knight: return b_pieceImages[3];
                case rook: return b_pieceImages[4];
                case pawn: return b_pieceImages[5];
                case none: return b_pieceImages[6];
            }
        }
        else {
            return w_pieceImages[6];
        }
        return null;
    }
    //inner class to represent a chess piece
    public static class Piece {
        PieceType type;
        PlayerColor color;
        boolean hasMoved;

        //Basically creates a null piece, used for empty spaces
        Piece() {
            this.type = PieceType.none;
            this.color = PlayerColor.none;
            hasMoved = false;
        }
        //Creates an actually relevant piece with a color and type
        Piece(PlayerColor color, PieceType type, boolean moved) {
            this.type = type;
            this.color = color;
            hasMoved = moved;
        }

        @Override
        public String toString() {
            if(type == PieceType.pawn) {
                if(color == PlayerColor.white) return "wp";
                else return "bp";
            }
            if(type == PieceType.bishop) {
                if(color == PlayerColor.white) return "wb";
                else return "bb";
            }
            if(type == PieceType.knight) {
                if(color == PlayerColor.white) return "wk";
                else return "bk";
            }
            if(type == PieceType.rook) {
                if(color == PlayerColor.white) return "wr";
                else return "br";
            }
            if(type == PieceType.queen) {
                if(color == PlayerColor.white) return "wQ";
                else return "bQ";
            }
            if(type == PieceType.king) {
                if(color == PlayerColor.white) return "wK";
                else return "bK";
            }
            return "  ";
        }
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            if(obj == null) return false;
            if(this.getClass() != obj.getClass()) return false;

            Piece other = (Piece) obj;
            if(type != other.type) return false;
            return color == other.color;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + Objects.hashCode(this.type);
            hash = 59 * hash + Objects.hashCode(this.color);
            return hash;
        }
    }
    //Sets the icon for a square on the chessboard
    public void setIcon(int x, int y, Piece piece) {
        chessboardsquares[y][x].setIcon(getImageIcon(piece));
        chessboardstatus[y][x] = piece;
    }
    //Gets the type of piece at that position on the board
    public Piece getIcon(int x, int y) {
        return chessboardstatus[y][x];
    }
    // Highlights a specific square on the chess board.
    public void markPosition(int x, int y) {
        chessboardsquares[y][x].setBackground(Color.pink);
    }
    // Removes the highlight from a specific square on the chess board.
    public void unmarkPosition(int x, int y) {
        if((y % 2 == 1 && x % 2 == 1)|| (y % 2 == 0 && x % 2 == 0)) chessboardsquares[y][x].setBackground(Color.WHITE);
        else chessboardsquares[y][x].setBackground(Color.blue);
    }
    // Sets the status message on the GUI.
    public void setStatus(String input) {
        messages[0].setText(input);
    }
    //Gets the value of both players pieces on the current board
    public void getValues() {
        int wVal = 0;
        int bVal = 0;
        for(int i = 0; i < chessboardstatus.length; i++) {
            for(int j = 0; j < chessboardstatus[i].length; j++) {
                Piece p = chessboardstatus[i][j];
                if(p.color == PlayerColor.white) {
                    if(p.type == PieceType.pawn) wVal += 1;
                    else if(p.type == PieceType.bishop) wVal += 3;
                    else if(p.type == PieceType.knight) wVal += 3;
                    else if(p.type == PieceType.rook) wVal += 5;
                    else if(p.type == PieceType.queen) wVal += 9;
                    wVal += squarevalue[i][j];
                }
                else if(p.color == PlayerColor.black) {
                    if(p.type == PieceType.pawn) bVal += 1;
                    else if(p.type == PieceType.bishop) bVal += 3;
                    else if(p.type == PieceType.knight) bVal += 3;
                    else if(p.type == PieceType.rook) bVal += 5;
                    else if(p.type == PieceType.queen) bVal += 9;
                    bVal += squarevalue[i][j];
                }
            }
        }
        w_pieceValue = wVal;
        b_pieceValue = bVal;
    }
    //Sets up the starting board position
    public void setBoard() {
        //Resets the board
        for(int i = 0; i < chessboardstatus.length; i++) {
            for(int j = 0; j < chessboardstatus[i].length; j++) {
                chessboardstatus[i][j] = new Piece();
                setIcon(i, j, new Piece());
                unmarkPosition(i, j);
            }
        }
        //Sets all back row of each color
        //Black Pieces
        setIcon(0, 0, new Piece(PlayerColor.black, PieceType.rook, false));
        setIcon(0, 1, new Piece(PlayerColor.black, PieceType.knight, false));
        setIcon(0, 2, new Piece(PlayerColor.black, PieceType.bishop, false));
        setIcon(0, 3, new Piece(PlayerColor.black, PieceType.queen, false));
        setIcon(0, 4, new Piece(PlayerColor.black, PieceType.king, false));
        setIcon(0, 5, new Piece(PlayerColor.black, PieceType.bishop, false));
        setIcon(0, 6, new Piece(PlayerColor.black, PieceType.knight, false));
        setIcon(0, 7, new Piece(PlayerColor.black, PieceType.rook, false));
        //White Pieces
        setIcon(7, 0, new Piece(PlayerColor.white, PieceType.rook, false));
        setIcon(7, 1, new Piece(PlayerColor.white, PieceType.knight, false));
        setIcon(7, 2, new Piece(PlayerColor.white, PieceType.bishop, false));
        setIcon(7, 3, new Piece(PlayerColor.white, PieceType.queen, false));
        setIcon(7, 4, new Piece(PlayerColor.white, PieceType.king, false));
        setIcon(7, 5, new Piece(PlayerColor.white, PieceType.bishop, false));
        setIcon(7, 6, new Piece(PlayerColor.white, PieceType.knight, false));
        setIcon(7, 7, new Piece(PlayerColor.white, PieceType.rook, false));
        //Sets up both rows of pawns
        for(int i = 0; i < chessboardstatus.length; i++) {
            setIcon(1, i, new Piece(PlayerColor.black, PieceType.pawn, false));
            setIcon(6, i, new Piece(PlayerColor.white, PieceType.pawn, false));
        }
        //Sets some variables that are not supposed to change during a game
        gameDifficulty = difficulty;
        computerGame = !TwoPlayers;
        //Call Function to Start tracking the game
        onInitiateBoard();
    }
    public final JComponent getGUI() { //Returns the JPanel to add to the JFrame that will start the game
        return startGUI;
    }
    //Creates some variables for our button listener that performs the game actions
    private boolean firstClk, check, end;
    PlayerColor turn;
    Piece firstPc;
    Point firstPt;
    ArrayList<Point> Moveable; //Array that will hold movable points on the board
    BoardGraph.BoardState currentBoard;
    //Initializes the button listener variables
    void onInitiateBoard() {
        turn = PlayerColor.white;
        firstClk = true;
        end = false;
        Moveable = new ArrayList<>();
        setStatus(turn + "'s turn");
        //Updates the value of the boards pieces
        getValues();
        //Creates and adds the inital board to our graph
        currentBoard = new BoardGraph.BoardState(chessboardstatus, w_pieceValue, b_pieceValue, false, false, turn);
        gamemoves.addVertex(currentBoard);
    }
    //Button logic that plays the game
    class ButtonListener implements ActionListener {
        Point current; //Point on the board the button is at
        //Constructor for ButtonListener
        ButtonListener(int x, int y) {
            current = new Point(x, y);
        }
        //Logic behind game actions, IMCOMPLETE
        public void actionPerformed(ActionEvent e) {
            if(end) return; //Returns if game is over
            if(promoting) return; //Makes it so actions cannot be taken while promoting
            Piece Pc = getIcon(current.x, current.y);
            boolean checkPc = false;
            //Checks if where you are trying to move is an ally square
            if(!firstClk) {
                //Check if piece is ally
                checkPc = isAlly(current.x, current.y, firstPc.color);
            }

            if(firstClk || checkPc) {
                //Returns if piece clicked is not the turn players piece
                if(!Pc.color.equals(turn)) {
                    return;
                }
                //Resets the moveable array to not overlap
                if(checkPc) {
                    for(Point p : Moveable) {
                        unmarkPosition(p.x, p.y);
                    }
                    Moveable.clear();
                }
                //Adds the moveable points to the moveable arraylist
                switch (Pc.type) {
                    case pawn:
                        markPawn(current.x, current.y, Pc.color, Moveable);
                        break;
                    case bishop:
                        markBishop(current.x, current.y, Pc.color, Moveable);
                        break;
                    case knight:
                        markKnight(current.x, current.y, Pc.color, Moveable);
                        break;
                    case rook:
                        markRook(current.x, current.y, Pc.color, Moveable);
                        break;
                    case queen:
                        markQueen(current.x, current.y, Pc.color, Moveable);
                        break;
                    case king:
                        markKing(current.x, current.y, Pc.color, Moveable);
                        break;
                    case none:
                        break;
                }
                //Marks the squares on the board that are in the moveable array
                for(Point p : Moveable) {
                    markPosition(p.x, p.y);
                }
                //Update the variables for the next click
                firstPc = Pc;
                firstPt = current;
                firstClk = false;
            }
            else {
                //This will be where the second button clicked will start
                Point secondPt = current;
                boolean isValid = false;

                for(Point p : Moveable) {
                    //Checks if the button clicked is in the moveable array and moves the piece
                    if(p.equals(secondPt)) {
                        isValid = true;
                        //Promotion code for pawns
                        if((firstPc.type == PieceType.pawn) && (((turn == PlayerColor.white) && (current.x == 0)) || (turn == PlayerColor.black) && (current.x == 7))) {
                            promoting = true;
                            promotePawn(current.x, current.y);
                            break;
                        }
                        //Checks for castling
                        else if(firstPc.type == PieceType.king && current.x == firstPt.x && abs(firstPt.y - current.y) > 1) {
                            if(turn == PlayerColor.white) {
                                //Checks which direction we castled
                                if(firstPt.y - current.y > 1) {
                                    firstPc.hasMoved = true;
                                    setIcon(current.x, current.y, firstPc);
                                    setIcon(current.x, current.y + 1, getIcon(7, 0));
                                    setIcon(firstPt.x, firstPt.y, new Piece());
                                    setIcon(7, 0, new Piece());
                                }
                                else {
                                    firstPc.hasMoved = true;
                                    setIcon(current.x, current.y, firstPc);
                                    setIcon(current.x, current.y - 1, getIcon(7, 7));
                                    setIcon(firstPt.x, firstPt.y, new Piece());
                                    setIcon(7, 7, new Piece());
                                }
                            }
                            //Castling for the black king
                            else {
                                if(firstPt.y - current.y > 1) {
                                    firstPc.hasMoved = true;
                                    setIcon(current.x, current.y, firstPc);
                                    setIcon(current.x, current.y + 1, getIcon(0, 0));
                                    setIcon(firstPt.x, firstPt.y, new Piece());
                                    setIcon(0, 0, new Piece());
                                }
                                else {
                                    firstPc.hasMoved = true;
                                    setIcon(current.x, current.y, firstPc);
                                    setIcon(current.x, current.y - 1, getIcon(0, 7));
                                    setIcon(firstPt.x, firstPt.y, new Piece());
                                    setIcon(0, 7, new Piece());
                                }
                            }
                        }
                        //Generic second click operations
                        else {
                            firstPc.hasMoved = true;
                            setIcon(current.x, current.y, firstPc);
                            setIcon(firstPt.x, firstPt.y, new Piece());
                        }

                        //unmarks the positions in the moveable array
                        for(Point p0 : Moveable) {
                            unmarkPosition(p0.x, p0.y);
                        }

                        //Resets the game variables for the next first click
                        firstClk = true;
                        Moveable.clear();
                        getValues();

                        //Swaps turn player color
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }

                        //If a king is missing game ends. Accounting for possible bug
                        if(findKing(turn) == null) {
                            end = true;
                            setStatus("Missing King Game Over");
                            break;
                        }

                        //Checks for checks and Checkmate
                        String s1 = "";
                        String s2 = "";
                        if(isCheck(turn, findKing(turn))) {
                            s1 = "/ CHECK";
                            check = true;
                            if(isCheckMate(turn)) {
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                        }
                        else if(isCheckMate(turn)) {
                            s1 = "/ STALE";
                            s2 = "MATE / GAME OVER";
                            end = true;
                        }
                        else {
                            check = false;
                        }
                        setStatus(turn + "'s turn " + s1 + s2);
                        //Gets the new boardstate and adds it to our gamemoves graph
                        BoardGraph.BoardState newBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, check, false, turn);
                        gamemoves.addEdge(currentBoard, newBoard);

                        //Updates the current boardstate
                        currentBoard = copyBoardState(newBoard);
                        //System.out.print(currentBoard);
                        //Handles the computers moves after the player has moved
                        if(computerGame) {
                            if(gameDifficulty == 0) {
                                easyAlgorithm();
                                //Updates to the players turn
                                if(turn == PlayerColor.white) {
                                    turn = PlayerColor.black;
                                }
                                else {
                                    turn = PlayerColor.white;
                                }
                                PlayerColor cpu = null;
                                if(turn == PlayerColor.white) cpu = PlayerColor.black;
                                else cpu = PlayerColor.white;
                                //Updates the message if the computer was not checkmated
                                if(!isCheckMate(cpu)) {
                                    //Checks to update the status message
                                    s1 = "";
                                    s2 = "";
                                    if(isCheck(turn, findKing(turn))) {
                                        s1 = "/ CHECK";
                                        check = true;
                                        if(isCheckMate(turn)) {
                                            s2 = "MATE / GAME OVER";
                                            end = true;
                                        }
                                    }
                                    else if(isCheckMate(turn)) {
                                        s1 = "/ STALE";
                                        s2 = "MATE / GAME OVER";
                                        end = true;
                                    }
                                    else {
                                        check = false;
                                    }
                                    setStatus(turn + "'s turn " + s1 + s2);
                                }
                            }
                            if(gameDifficulty == 1) {
                                medAlgorithm();
                                //Updates to the players turn
                                if(turn == PlayerColor.white) {
                                    turn = PlayerColor.black;
                                }
                                else {
                                    turn = PlayerColor.white;
                                }
                                PlayerColor cpu = null;
                                if(turn == PlayerColor.white) cpu = PlayerColor.black;
                                else cpu = PlayerColor.white;
                                //Updates the message if the computer was not checkmated
                                if(!isCheckMate(cpu)) {
                                    //Checks to update the status message
                                    s1 = "";
                                    s2 = "";
                                    if(isCheck(turn, findKing(turn))) {
                                        s1 = "/ CHECK";
                                        check = true;
                                        if(isCheckMate(turn)) {
                                            s2 = "MATE / GAME OVER";
                                            end = true;
                                        }
                                    }
                                    else if(isCheckMate(turn)) {
                                        s1 = "/ STALE";
                                        s2 = "MATE / GAME OVER";
                                        end = true;
                                    }
                                    else {
                                        check = false;
                                    }
                                    setStatus(turn + "'s turn " + s1 + s2);
                                }
                            }
                            if(gameDifficulty == 2) {
                                hardAlgorithm();
                                //Updates to the players turn
                                if(turn == PlayerColor.white) {
                                    turn = PlayerColor.black;
                                }
                                else {
                                    turn = PlayerColor.white;
                                }
                                PlayerColor cpu = null;
                                if(turn == PlayerColor.white) cpu = PlayerColor.black;
                                else cpu = PlayerColor.white;
                                //Updates the message if the computer was not checkmated
                                if(!isCheckMate(cpu)) {
                                    //Checks to update the status message
                                    s1 = "";
                                    s2 = "";
                                    if(isCheck(turn, findKing(turn))) {
                                        s1 = "/ CHECK";
                                        check = true;
                                        if(isCheckMate(turn)) {
                                            s2 = "MATE / GAME OVER";
                                            end = true;
                                        }
                                    }
                                    else if(isCheckMate(turn)) {
                                        s1 = "/ STALE";
                                        s2 = "MATE / GAME OVER";
                                        end = true;
                                    }
                                    else {
                                        check = false;
                                    }
                                    setStatus(turn + "'s turn " + s1 + s2);
                                }
                            }
                        }
                        break;
                    }
                }
                //If the second point clicked is not moveable reset
                if(!isValid) {
                    for(Point p : Moveable) {
                        unmarkPosition(p.x, p.y);
                    }
                    firstClk = true;
                    Moveable.clear();
                }
            }
        }
    }
    //Marks the posible moves for a pawn
    void markPawn(int x, int y, PlayerColor pc, ArrayList<Point> moves) {
        int move;
        if(pc == PlayerColor.white) move = -1;
        else move = 1;
        //Checks if the pawn is unmoveable, need to implement promote function
        if((x == 7 && pc == PlayerColor.black) || (x == 0 && pc == PlayerColor.white)) return;
        Piece advance = getIcon(x + move, y);
        //Checks for empty space in front of the pawn
        if(advance.type == PieceType.none) {
            Piece originalPiece = getIcon(x + move, y);
            setIcon(x + move, y, getIcon(x, y));
            setIcon(x, y, new Piece());

            if(!isCheck(pc, findKing(pc))) {
                moves.add(new Point(x + move, y));
            }
            //Moves the pieces back to check the next pieces moves
            setIcon(x, y, getIcon(x + move, y));
            setIcon(x + move, y, originalPiece);
        }
        //Checks to see if the pawn has moved yet, and checks for the two space move
        if((pc == PlayerColor.black && x == 1) || (pc == PlayerColor.white && x == 6)) {
            Piece advanceTwo = getIcon(x+2*move, y);
            if(advance.type == PieceType.none && advanceTwo.type == PieceType.none) {
                Piece originalPiece = getIcon(x+2*move, y);
                setIcon(x+2*move, y, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(x+2*move, y));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(x+2*move, y));
                setIcon(x+2*move, y, originalPiece);
            }
        }
        //Switch case for the pawns taking a piece, checking the diagonal
        switch (y) {
            case 0:
                if(isEnemy(x + move, y + 1, pc)) {
                    Piece originalPiece = getIcon(x + move, y + 1);
                    setIcon(x + move, y + 1, getIcon(x, y));
                    setIcon(x, y, new Piece());

                    if(!isCheck(pc, findKing(pc))) {
                        moves.add(new Point(x + move, y + 1));
                    }
                    //Moves the pieces back to check the next pieces moves
                    setIcon(x, y, getIcon(x + move, y + 1));
                    setIcon(x + move, y + 1, originalPiece);
                }
                break;
            case 7:
                if(isEnemy(x + move, y - 1, pc)) {
                    Piece originalPiece = getIcon(x + move, y - 1);
                    setIcon(x + move, y - 1, getIcon(x, y));
                    setIcon(x, y, new Piece());

                    if(!isCheck(pc, findKing(pc))) {
                        moves.add(new Point(x + move, y - 1));
                    }
                    //Moves the pieces back to check the next pieces moves
                    setIcon(x, y, getIcon(x + move, y - 1));
                    setIcon(x + move, y - 1, originalPiece);
                }
                break;
            default:
                if(isEnemy(x + move, y + 1, pc)) {
                    Piece originalPiece = getIcon(x + move, y + 1);
                    setIcon(x + move, y + 1, getIcon(x, y));
                    setIcon(x, y, new Piece());

                    if(!isCheck(pc, findKing(pc))) {
                        moves.add(new Point(x + move, y + 1));
                    }
                    //Moves the pieces back to check the next pieces moves
                    setIcon(x, y, getIcon(x + move, y + 1));
                    setIcon(x + move, y + 1, originalPiece);
                }
                if(isEnemy(x + move, y - 1, pc)) {
                    Piece originalPiece = getIcon(x + move, y - 1);
                    setIcon(x + move, y - 1, getIcon(x, y));
                    setIcon(x, y, new Piece());

                    if(!isCheck(pc, findKing(pc))) {
                        moves.add(new Point(x + move, y - 1));
                    }
                    //Moves the pieces back to check the next pieces moves
                    setIcon(x, y, getIcon(x + move, y - 1));
                    setIcon(x + move, y - 1, originalPiece);
                }
                break;
        }
        if(moves.isEmpty()) {
            return;
        }
    }
    //Marks the posible moves for a bishop
    void markBishop(int x, int y, PlayerColor pc, ArrayList<Point> moves) {
        //Up Right Diagonal
        for(int i = x - 1, j = y + 1; i >= 0 && j < 8; --i, ++j) {
            if(getIcon(i, j).type == PieceType.none) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
            }
            if(isAlly(i, j, pc)) {
                break;
            }
            if(isEnemy(i, j, pc)) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
                break;
            }
        }
        //Down Right Diagonal
        for(int i = x + 1, j = y + 1; i < 8 && j < 8; ++i, ++j) {
            if(getIcon(i, j).type == PieceType.none) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
            }
            if(isAlly(i, j, pc)) {
                break;
            }
            if(isEnemy(i, j, pc)) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
                break;
            }
        }
        //Up Left Diagonal
        for(int i = x - 1, j = y - 1; i >= 0 && j >= 0; --i, --j) {
            if(getIcon(i, j).type == PieceType.none) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
            }
            if(isAlly(i, j, pc)) {
                break;
            }
            if(isEnemy(i, j, pc)) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
                break;
            }
        }
        //Down Left Diagonal
        for(int i = x + 1, j = y - 1; i < 8 && j >= 0; ++i, --j) {
            if(getIcon(i, j).type == PieceType.none) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
            }
            if(isAlly(i, j, pc)) {
                break;
            }
            if(isEnemy(i, j, pc)) {
                Piece originalPiece = getIcon(i, j);
                setIcon(i, j, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, j));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, j));
                setIcon(i, j, originalPiece);
                break;
            }
        }
        if(moves.isEmpty()) {
            return;
        }
    }
    //Marks the posible moves for a knight
    void markKnight(int x, int y, PlayerColor pc, ArrayList<Point> moves) {
        //TwoSquares Up/Down, OneSquare Left/Right
        for(int i = x - 2; i <= x + 2; i+=4) {
            for(int j = y - 1; j <= y + 2; j+=2) {
                if((i >= 0) && (i < 8) && (j >= 0) && (j < 8)) {
                    if(!isAlly(i, j, pc)) {
                        Piece originalPiece = getIcon(i, j);
                        setIcon(i, j, getIcon(x, y));
                        setIcon(x, y, new Piece());

                        if(!isCheck(pc, findKing(pc))) {
                            moves.add(new Point(i, j));
                        }
                        //Moves the pieces back to check the next pieces moves
                        setIcon(x, y, getIcon(i, j));
                        setIcon(i, j, originalPiece);
                    }
                }
            }
        }
        //OneSquare Up/Down, TwoSquares Left/Right
        for(int i = x - 1; i <= x + 1; i+=2) {
            for(int j = y - 2; j <= y + 2; j+=4) {
                if((i >= 0) && (i < 8) && (j >= 0) && (j < 8)) {
                    if(!isAlly(i, j, pc)) {
                        Piece originalPiece = getIcon(i, j);
                        setIcon(i, j, getIcon(x, y));
                        setIcon(x, y, new Piece());

                        if(!isCheck(pc, findKing(pc))) {
                            moves.add(new Point(i, j));
                        }
                        //Moves the pieces back to check the next pieces moves
                        setIcon(x, y, getIcon(i, j));
                        setIcon(i, j, originalPiece);
                    }
                }
            }
        }
    }
    //Marks the posible moves for a rook
    void markRook(int x, int y, PlayerColor pc, ArrayList<Point> moves) {
        //Right
        for(int i = y + 1; i < 8; ++i) {
            if(getIcon(x, i).type == PieceType.none) {
                Piece originalPiece = getIcon(x, i);
                setIcon(x, i, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(x, i));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(x, i));
                setIcon(x, i, originalPiece);
            }
            if(isAlly(x, i, pc)) break;
            if(isEnemy(x, i, pc)) {
                Piece originalPiece = getIcon(x, i);
                setIcon(x, i, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(x, i));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(x, i));
                setIcon(x, i, originalPiece);
                break;
            }
        }
        //Left
        for(int i = y - 1; i >= 0; --i) {
            if(getIcon(x, i).type == PieceType.none) {
                Piece originalPiece = getIcon(x, i);
                setIcon(x, i, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(x, i));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(x, i));
                setIcon(x, i, originalPiece);
            }
            if(isAlly(x, i, pc)) break;
            if(isEnemy(x, i, pc)) {
                Piece originalPiece = getIcon(x, i);
                setIcon(x, i, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(x, i));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(x, i));
                setIcon(x, i, originalPiece);
                break;
            }
        }
        //Down
        for(int i = x + 1; i < 8; ++i) {
            if(getIcon(i, y).type == PieceType.none) {
                Piece originalPiece = getIcon(i, y);
                setIcon(i, y, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, y));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, y));
                setIcon(i, y, originalPiece);
            }
            if(isAlly(i, y, pc)) break;
            if(isEnemy(i, y, pc)) {
                Piece originalPiece = getIcon(i, y);
                setIcon(i, y, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, y));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, y));
                setIcon(i, y, originalPiece);
                break;
            }
        }
        //Up
        for(int i = x - 1; i >= 0; --i) {
            if(getIcon(i, y).type == PieceType.none) {
                Piece originalPiece = getIcon(i, y);
                setIcon(i, y, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, y));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, y));
                setIcon(i, y, originalPiece);
            }
            if(isAlly(i, y, pc)) break;
            if(isEnemy(i, y, pc)) {
                Piece originalPiece = getIcon(i, y);
                setIcon(i, y, getIcon(x, y));
                setIcon(x, y, new Piece());

                if(!isCheck(pc, findKing(pc))) {
                    moves.add(new Point(i, y));
                }
                //Moves the pieces back to check the next pieces moves
                setIcon(x, y, getIcon(i, y));
                setIcon(i, y, originalPiece);
                break;
            }
        }
    }
    //Marks the posible moves for a queen
    void markQueen(int x, int y, PlayerColor pc, ArrayList<Point> moves) {
        markBishop(x, y, pc, moves);
        markRook(x, y, pc, moves);
    }
    //Marks the posible moves for a king
    void markKing(int x, int y, PlayerColor pc, ArrayList<Point> moves) {
        //Checks each square around the king for legal moves
        for(int i = x - 1; i <= x + 1; ++i) {
            for(int j = y - 1; j <= y + 1; ++j) {
                if((i >= 0) && (i < 8) && (j >= 0) && (j < 8)) {
                    if(!isAlly(i, j, pc)) {
                        Piece originalPiece = getIcon(i, j);
                        setIcon(i, j, getIcon(x, y));
                        setIcon(x, y, new Piece());

                        if(!isCheck(pc, findKing(pc))) {
                            moves.add(new Point(i, j));
                        }
                        //Moves the pieces back to check the next pieces moves
                        setIcon(x, y, getIcon(i, j));
                        setIcon(i, j, originalPiece);
                    }
                }
            }
        }
        //King has not moved; check for castling
        if(!getIcon(x, y).hasMoved && !isCheck(pc, findKing(pc))) {
            if(pc == PlayerColor.white) {
                //Checks if one of the rooks has not moved
                if(!getIcon(7, 7).hasMoved && getIcon(7, 7).type == PieceType.rook) {
                    //Checks if the path to the rook is empty
                    if(getIcon(7, 6).type == PieceType.none && getIcon(7, 5).type == PieceType.none) {
                        //Checks if those squares would be threatened
                        if(!threatened(pc, new Point(7, 6)) && !threatened(pc, new Point(7, 5))) {
                            setIcon(7, 6, new Piece(pc, PieceType.king, true));
                            setIcon(7, 5, new Piece(pc, PieceType.rook, true));
                            setIcon(x, y, new Piece());

                            if(!isCheck(pc, findKing(pc))) {
                                moves.add(new Point(7, 6));
                            }
                            //Moves the pieces back to check the next pieces moves
                            setIcon(7, 6, new Piece()); setIcon(7, 5, new Piece()); //Clears where we moved the king and rook
                            setIcon(x, y, new Piece(pc, PieceType.king, false));
                            setIcon(7, 7, new Piece(pc, PieceType.rook, false));
                        }
                    }
                }
                //Checks if the other rook has moved
                if(!getIcon(7, 0).hasMoved && getIcon(7, 0).type == PieceType.rook) {
                    //Checks if the path to the rook is empty
                    if(getIcon(7, 1).type == PieceType.none && getIcon(7, 2).type == PieceType.none && getIcon(7, 3).type == PieceType.none) {
                        //Checks if those squares would be threatened
                        if(!threatened(pc, new Point(7, 1)) && !threatened(pc, new Point(7, 2)) && !threatened(pc, new Point(7, 3))) {
                            setIcon(7, 2, new Piece(pc, PieceType.king, true));
                            setIcon(7, 3, new Piece(pc, PieceType.rook, true));
                            setIcon(x, y, new Piece());

                            if(!isCheck(pc, findKing(pc))) {
                                moves.add(new Point(7, 2));
                            }
                            //Moves the pieces back to check the next pieces moves
                            setIcon(7, 2, new Piece()); setIcon(7, 3, new Piece()); //Clears where we moved the king and rook
                            setIcon(x, y, new Piece(pc, PieceType.king, false));
                            setIcon(7, 0, new Piece(pc, PieceType.rook, false));
                        }
                    }
                }
            }
            else {
                //Checks if one of the rooks has not moved
                if(!getIcon(0, 7).hasMoved && getIcon(0, 7).type == PieceType.rook) {
                    //Checks if the path to the rook is empty
                    if(getIcon(0, 6).type == PieceType.none && getIcon(0, 5).type == PieceType.none) {
                        //Checks if those squares would be threatened
                        if(!threatened(pc, new Point(0, 6)) && !threatened(pc, new Point(0, 5))) {
                            setIcon(0, 6, new Piece(pc, PieceType.king, true));
                            setIcon(0, 5, new Piece(pc, PieceType.rook, true));
                            setIcon(x, y, new Piece());

                            if(!isCheck(pc, findKing(pc))) {
                                moves.add(new Point(0, 6));
                            }
                            //Moves the pieces back to check the next pieces moves
                            setIcon(0, 6, new Piece()); setIcon(0, 5, new Piece()); //Clears where we moved the king and rook
                            setIcon(x, y, new Piece(pc, PieceType.king, false));
                            setIcon(0, 7, new Piece(pc, PieceType.rook, false));
                        }
                    }
                }
                //Checks if the other rook has moved
                if(!getIcon(0, 0).hasMoved && getIcon(0, 0).type == PieceType.rook) {
                    //Checks if the path to the rook is empty
                    if(getIcon(0, 1).type == PieceType.none && getIcon(0, 2).type == PieceType.none && getIcon(0, 3).type == PieceType.none) {
                        //Checks if those squares would be threatened
                        if(!threatened(pc, new Point(0, 1)) && !threatened(pc, new Point(0, 2)) && !threatened(pc, new Point(0, 3))) {
                            setIcon(0, 2, new Piece(pc, PieceType.king, true));
                            setIcon(0, 3, new Piece(pc, PieceType.rook, true));
                            setIcon(x, y, new Piece());

                            if(!isCheck(pc, findKing(pc))) {
                                moves.add(new Point(0, 2));
                            }
                            //Moves the pieces back to check the next pieces moves
                            setIcon(0, 2, new Piece()); setIcon(0, 3, new Piece()); //Clears where we moved the king and rook
                            setIcon(x, y, new Piece(pc, PieceType.king, false));
                            setIcon(0, 0, new Piece(pc, PieceType.rook, false));
                        }
                    }
                }
            }
        }
    }
    //Determines if a given square has an ally piece
    boolean isAlly(int x, int y, PlayerColor pc) {
        return getIcon(x, y).color == pc;
    }
    //Determines if a given square has an enemy piece
    boolean isEnemy(int x, int y, PlayerColor pc) {
        return getIcon(x, y).color != pc && getIcon(x, y).color != PlayerColor.none;
    }
    //Function that returns the point the king of a color is at
    Point findKing(PlayerColor pc) {
        for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j) {
                if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.king) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }
    Point findQueen(PlayerColor pc) {
        for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j) {
                if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.queen) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }
    //Checks if a king of a color is in Check
    boolean isCheck(PlayerColor pc, Point king) {
        Point kingPosition = king;
        //Checks for knight checks
        for(int i = kingPosition.x - 2; i <= kingPosition.x + 2; i+=4) {
            for(int j = kingPosition.y - 1; j <= kingPosition.y + 1; j+=2) {
                if((i < 8) && (i >= 0) && (j < 8) && (j >= 0)) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.knight) {
                        return true;
                    }
                }
            }
        }
        for(int i = kingPosition.x - 1; i <= kingPosition.x + 1; i+=2) {
            for(int j = kingPosition.y - 2; j <= kingPosition.y + 2; j+=4) {
                if((i < 8) && (i >= 0) && (j < 8) && (j >= 0)) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.knight) {
                        return true;
                    }
                }
            }
        }
        //Checks for rook/queen checks
        for(int x = kingPosition.x - 1; x >= 0; --x) { //Left
            //Breaks once we hit a none empty square
            if(getIcon(x, kingPosition.y).type != PieceType.none) {
                if((isEnemy(x, kingPosition.y, pc)) && ((getIcon(x, kingPosition.y).type == PieceType.rook) || (getIcon(x, kingPosition.y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }

        }
        for(int x = kingPosition.x + 1; x < 8; ++x) { //Right
            if(getIcon(x, kingPosition.y).type != PieceType.none) {
                if((isEnemy(x, kingPosition.y, pc)) && ((getIcon(x, kingPosition.y).type == PieceType.rook) || (getIcon(x, kingPosition.y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int y = kingPosition.y - 1; y >= 0; y--) { //Up
            if(getIcon(kingPosition.x, y).type != PieceType.none) {
                if((isEnemy(kingPosition.x, y, pc)) && ((getIcon(kingPosition.x, y).type == PieceType.rook) || (getIcon(kingPosition.x, y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int y = kingPosition.y + 1; y < 8; ++y) { //Down
            if(getIcon(kingPosition.x, y).type != PieceType.none) {
                if((isEnemy(kingPosition.x, y, pc)) && ((getIcon(kingPosition.x, y).type == PieceType.rook) || (getIcon(kingPosition.x, y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        //Checks for bishop/queen
        for(int i = kingPosition.x-1, j = kingPosition.y-1; i>=0 && j>=0; --i, --j) { //Up-Left
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.white && i == kingPosition.x-1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }

        }
        for(int i = kingPosition.x-1, j = kingPosition.y+1; i>=0 && j<8; --i, ++j) { //Up-Right
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.white && i == kingPosition.x-1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        for(int i = kingPosition.x+1, j = kingPosition.y-1; i<8 && j>=0; ++i, --j) { //Down-Left
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.black && i == kingPosition.x+1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        for(int i = kingPosition.x+1, j = kingPosition.y+1; i<8 && j<8; ++i, ++j) { //Down-Right
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.black && i == kingPosition.x+1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        //Checks if it is next to the king/useful for checking if we can make a move
        for(int i = kingPosition.x - 1; i <= kingPosition.x + 1; ++i) {
            for(int j = kingPosition.y - 1; j <= kingPosition.y + 1; ++j) {
                if((i >= 0) && (i < 8) && (j >= 0) && (j < 8)) {

                    //Ignore the king's own square
                    if (i == kingPosition.x && j == kingPosition.y) continue;

                    if((isEnemy(i, j, pc)) && (getIcon(i, j).type == PieceType.king)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //Returns if a king of a color is checkmated
    boolean isCheckMate(PlayerColor pc) {
        boolean isMate = true;
        //Marks every possible move and checks if there is a valid move to get a false for isCheck();
        for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j) {
                if(isAlly(i, j, pc)) {
                    switch(getIcon(i, j).type) {
                        case pawn:
                            markPawn(i, j, pc, Moveable);
                            break;
                        case bishop:
                            markBishop(i, j, pc, Moveable);
                            break;
                        case knight:
                            markKnight(i, j, pc, Moveable);
                            break;
                        case rook:
                            markRook(i, j, pc, Moveable);
                            break;
                        case queen:
                            markQueen(i, j, pc, Moveable);
                            break;
                        case king:
                            markKing(i, j, pc, Moveable);
                            break;
                    }
                    //Checks every move for each piece
                    for(Point p : Moveable) {
                        Piece originalPiece = getIcon(p.x, p.y);
                        setIcon(p.x, p.y, getIcon(i, j));
                        setIcon(i, j, new Piece());

                        if(!isCheck(pc, findKing(pc))) {
                            isMate = false;
                        }
                        //Moves the pieces back to check the next pieces moves
                        setIcon(i, j, getIcon(p.x, p.y));
                        setIcon(p.x, p.y, originalPiece);
                        getValues();
                    }
                    //Resets Moveable for the next piece
                    Moveable = new ArrayList<>();
                }
            }
        }
        return isMate;
    }
    //Function used to promote a pawn
    void promotePawn(int x, int y) {
        //Have a jframe pop up with buttons to ask what piece to promote the pawn to.
        JFrame promoteFrame = new JFrame("Promotion");
        JPanel promoPanel = new JPanel();
        //Create a button for each piece that a pawn can be promoted to
        JButton qButton = new JButton();
        qButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIcon(x, y, new Piece(turn, PieceType.queen, true));
                setIcon(firstPt.x, firstPt.y, new Piece());
                promoting = false;
                //unmarks the positions in the moveable array
                for(Point p0 : Moveable) {
                    unmarkPosition(p0.x, p0.y);
                }
                //Resets the game variables for the next first click
                firstClk = true;
                Moveable.clear();
                getValues();
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                //If a king is missing game ends. Accounting for possible bug
                if(findKing(turn) == null) {
                    end = true;
                    setStatus("Missing King Game Over");
                }
                //Checks for checks and Checkmate
                String s1 = "";
                String s2 = "";
                if(isCheck(turn, findKing(turn))) {
                    s1 = "/ CHECK";
                    check = true;
                    if(isCheckMate(turn)) {
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                }
                else if(isCheckMate(turn)) {
                    s1 = "/ STALE";
                    s2 = "MATE / GAME OVER";
                    end = true;
                }
                else {
                    check = false;
                }
                setStatus(turn + "'s turn " + s1 + s2);
                //Gets the new boardstate and adds it to our gamemoves graph
                BoardGraph.BoardState newBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, check, false, turn);
                gamemoves.addEdge(currentBoard, newBoard);

                //Updates the current boardstate
                currentBoard = newBoard;
                //System.out.print(currentBoard);
                //Handles the computers moves after the player has moved
                if(computerGame) {
                    if(gameDifficulty == 0) {
                        easyAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 1) {
                        medAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 2) {
                        hardAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                }
                promoteFrame.dispose();
            }
        });
        qButton.setIcon(getImageIcon(new Piece(turn, PieceType.queen, true)));
        promoPanel.add(qButton);

        JButton rButton = new JButton();
        rButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIcon(x, y, new Piece(turn, PieceType.rook, true));
                setIcon(firstPt.x, firstPt.y, new Piece());
                promoting = false;
                //unmarks the positions in the moveable array
                for(Point p0 : Moveable) {
                    unmarkPosition(p0.x, p0.y);
                }
                //Resets the game variables for the next first click
                firstClk = true;
                Moveable.clear();
                getValues();
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                //If a king is missing game ends. Accounting for possible bug
                if(findKing(turn) == null) {
                    end = true;
                    setStatus("Missing King Game Over");
                }
                //Checks for checks and Checkmate
                String s1 = "";
                String s2 = "";
                if(isCheck(turn, findKing(turn))) {
                    s1 = "/ CHECK";
                    check = true;
                    if(isCheckMate(turn)) {
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                }
                else if(isCheckMate(turn)) {
                    s1 = "/ STALE";
                    s2 = "MATE / GAME OVER";
                    end = true;
                }
                else {
                    check = false;
                }
                setStatus(turn + "'s turn " + s1 + s2);
                //Gets the new boardstate and adds it to our gamemoves graph
                BoardGraph.BoardState newBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, check, false, turn);
                gamemoves.addEdge(currentBoard, newBoard);

                //Updates the current boardstate
                currentBoard = newBoard;
                //System.out.print(currentBoard);
                //Handles the computers moves after the player has moved
                if(computerGame) {
                    if(gameDifficulty == 0) {
                        easyAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 1) {
                        medAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 2) {
                        hardAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                }
                promoteFrame.dispose();
            }
        });
        rButton.setIcon(getImageIcon(new Piece(turn, PieceType.rook, true)));
        promoPanel.add(rButton);

        JButton bishButton = new JButton();
        bishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIcon(x, y, new Piece(turn, PieceType.bishop, true));
                setIcon(firstPt.x, firstPt.y, new Piece());
                promoting = false;
                //unmarks the positions in the moveable array
                for(Point p0 : Moveable) {
                    unmarkPosition(p0.x, p0.y);
                }
                //Resets the game variables for the next first click
                firstClk = true;
                Moveable.clear();
                getValues();
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                //If a king is missing game ends. Accounting for possible bug
                if(findKing(turn) == null) {
                    end = true;
                    setStatus("Missing King Game Over");
                }
                //Checks for checks and Checkmate
                String s1 = "";
                String s2 = "";
                if(isCheck(turn, findKing(turn))) {
                    s1 = "/ CHECK";
                    check = true;
                    if(isCheckMate(turn)) {
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                }
                else if(isCheckMate(turn)) {
                    s1 = "/ STALE";
                    s2 = "MATE / GAME OVER";
                    end = true;
                }
                else {
                    check = false;
                }
                setStatus(turn + "'s turn " + s1 + s2);
                //Gets the new boardstate and adds it to our gamemoves graph
                BoardGraph.BoardState newBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, check, false, turn);
                gamemoves.addEdge(currentBoard, newBoard);

                //Updates the current boardstate
                currentBoard = newBoard;
                //System.out.print(currentBoard);
                //Handles the computers moves after the player has moved
                if(computerGame) {
                    if(gameDifficulty == 0) {
                        easyAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 1) {
                        medAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 2) {
                        hardAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                }
                promoteFrame.dispose();
            }
        });
        bishButton.setIcon(getImageIcon(new Piece(turn, PieceType.bishop, true)));
        promoPanel.add(bishButton);

        JButton kButton = new JButton();
        kButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIcon(x, y, new Piece(turn, PieceType.knight, true));
                setIcon(firstPt.x, firstPt.y, new Piece());
                promoting = false;
                //unmarks the positions in the moveable array
                for(Point p0 : Moveable) {
                    unmarkPosition(p0.x, p0.y);
                }
                //Resets the game variables for the next first click
                firstClk = true;
                Moveable.clear();
                getValues();
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                //If a king is missing game ends. Accounting for possible bug
                if(findKing(turn) == null) {
                    end = true;
                    setStatus("Missing King Game Over");
                }
                //Checks for checks and Checkmate
                String s1 = "";
                String s2 = "";
                if(isCheck(turn, findKing(turn))) {
                    s1 = "/ CHECK";
                    check = true;
                    if(isCheckMate(turn)) {
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                }
                else if(isCheckMate(turn)) {
                    s1 = "/ STALE";
                    s2 = "MATE / GAME OVER";
                    end = true;
                }
                else {
                    check = false;
                }
                setStatus(turn + "'s turn " + s1 + s2);
                //Gets the new boardstate and adds it to our gamemoves graph
                BoardGraph.BoardState newBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, check, false, turn);
                gamemoves.addEdge(currentBoard, newBoard);

                //Updates the current boardstate
                currentBoard = newBoard;
                //currentBoard);
                //Handles the computers moves after the player has moved
                if(computerGame) {
                    if(gameDifficulty == 0) {
                        easyAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 1) {
                        medAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                    if(gameDifficulty == 2) {
                        hardAlgorithm();
                        //Updates to the players turn
                        if(turn == PlayerColor.white) {
                            turn = PlayerColor.black;
                        }
                        else {
                            turn = PlayerColor.white;
                        }
                        PlayerColor cpu = null;
                        if(turn == PlayerColor.white) cpu = PlayerColor.black;
                        else cpu = PlayerColor.white;
                        //Updates the message if the computer was not checkmated
                        if(!isCheckMate(cpu)) {
                            //Checks to update the status message
                            s1 = "";
                            s2 = "";
                            if(isCheck(turn, findKing(turn))) {
                                s1 = "/ CHECK";
                                check = true;
                                if(isCheckMate(turn)) {
                                    s2 = "MATE / GAME OVER";
                                    end = true;
                                }
                            }
                            else if(isCheckMate(turn)) {
                                s1 = "/ STALE";
                                s2 = "MATE / GAME OVER";
                                end = true;
                            }
                            else {
                                check = false;
                            }
                            setStatus(turn + "'s turn " + s1 + s2);
                        }
                    }
                }
                promoteFrame.dispose();
            }
        });
        kButton.setIcon(getImageIcon(new Piece(turn, PieceType.knight, true)));
        promoPanel.add(kButton);
        //Set up the jFrame
        promoteFrame.add(promoPanel);
        promoteFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                defaultPromo(x, y, promoteFrame);
            }
        });
        promoteFrame.setLocationByPlatform(true);
        promoteFrame.setResizable(false);
        promoteFrame.pack();
        promoteFrame.setMinimumSize(promoteFrame.getSize());
        promoteFrame.setVisible(true);
    }
    //Default promo if the user closes the promotion window
    void defaultPromo(int x, int y, JFrame frame) {
        setIcon(x, y, new Piece(turn, PieceType.queen, true));
        setIcon(firstPt.x, firstPt.y, new Piece());
        promoting = false;
        //unmarks the positions in the moveable array
        for(Point p0 : Moveable) {
            unmarkPosition(p0.x, p0.y);
        }
        //Resets the game variables for the next first click
        firstClk = true;
        Moveable.clear();
        getValues();
        if(turn == PlayerColor.white) {
            turn = PlayerColor.black;
        }
        else {
            turn = PlayerColor.white;
        }
        //If a king is missing game ends. Accounting for possible bug
        if(findKing(turn) == null) {
            end = true;
            setStatus("Missing King Game Over");
        }
        //Checks for checks and Checkmate
        String s1 = "";
        String s2 = "";
        if(isCheck(turn, findKing(turn))) {
            s1 = "/ CHECK";
            check = true;
            if(isCheckMate(turn)) {
                s2 = "MATE / GAME OVER";
                end = true;
            }
        }
        else if(isCheckMate(turn)) {
            s1 = "/ STALE";
            s2 = "MATE / GAME OVER";
            end = true;
        }
        else {
            check = false;
        }
        setStatus(turn + "'s turn " + s1 + s2);
        //Gets the new boardstate and adds it to our gamemoves graph
        BoardGraph.BoardState newBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, check, false, turn);
        gamemoves.addEdge(currentBoard, newBoard);

        //Updates the current boardstate
        currentBoard = newBoard;
        //System.out.print(currentBoard);
        //Handles the computers moves after the player has moved
        if(computerGame) {
            if(gameDifficulty == 0) {
                easyAlgorithm();
                //Updates to the players turn
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                PlayerColor cpu = null;
                if(turn == PlayerColor.white) cpu = PlayerColor.black;
                else cpu = PlayerColor.white;
                //Updates the message if the computer was not checkmated
                if(!isCheckMate(cpu)) {
                    //Checks to update the status message
                    s1 = "";
                    s2 = "";
                    if(isCheck(turn, findKing(turn))) {
                        s1 = "/ CHECK";
                        check = true;
                        if(isCheckMate(turn)) {
                            s2 = "MATE / GAME OVER";
                            end = true;
                        }
                    }
                    else if(isCheckMate(turn)) {
                        s1 = "/ STALE";
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                    else {
                        check = false;
                    }
                    setStatus(turn + "'s turn " + s1 + s2);
                }
            }
            if(gameDifficulty == 1) {
                medAlgorithm();
                //Updates to the players turn
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                PlayerColor cpu = null;
                if(turn == PlayerColor.white) cpu = PlayerColor.black;
                else cpu = PlayerColor.white;
                //Updates the message if the computer was not checkmated
                if(!isCheckMate(cpu)) {
                    //Checks to update the status message
                    s1 = "";
                    s2 = "";
                    if(isCheck(turn, findKing(turn))) {
                        s1 = "/ CHECK";
                        check = true;
                        if(isCheckMate(turn)) {
                            s2 = "MATE / GAME OVER";
                            end = true;
                        }
                    }
                    else if(isCheckMate(turn)) {
                        s1 = "/ STALE";
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                    else {
                        check = false;
                    }
                    setStatus(turn + "'s turn " + s1 + s2);
                }
            }
            if(gameDifficulty == 2) {
                hardAlgorithm();
                //Updates to the players turn
                if(turn == PlayerColor.white) {
                    turn = PlayerColor.black;
                }
                else {
                    turn = PlayerColor.white;
                }
                PlayerColor cpu = null;
                if(turn == PlayerColor.white) cpu = PlayerColor.black;
                else cpu = PlayerColor.white;
                //Updates the message if the computer was not checkmated
                if(!isCheckMate(cpu)) {
                    //Checks to update the status message
                    s1 = "";
                    s2 = "";
                    if(isCheck(turn, findKing(turn))) {
                        s1 = "/ CHECK";
                        check = true;
                        if(isCheckMate(turn)) {
                            s2 = "MATE / GAME OVER";
                            end = true;
                        }
                    }
                    else if(isCheckMate(turn)) {
                        s1 = "/ STALE";
                        s2 = "MATE / GAME OVER";
                        end = true;
                    }
                    else {
                        check = false;
                    }
                    setStatus(turn + "'s turn " + s1 + s2);
                }
            }
        }
        frame.dispose();
    }
    //Function to check if a piece is threatened
    boolean threatened(PlayerColor pc, Point piece) {
        Point location = piece;
        //Checks for knight checks
        for(int i = location.x - 2; i <= location.x + 2; i+=4) {
            for(int j = location.y - 1; j <= location.y + 1; j+=2) {
                if((i < 8) && (i >= 0) && (j < 8) && (j >= 0)) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.knight) {
                        return true;
                    }
                }
            }
        }
        for(int i = location.x - 1; i <= location.x + 1; i+=2) {
            for(int j = location.y - 2; j <= location.y + 2; j+=4) {
                if((i < 8) && (i >= 0) && (j < 8) && (j >= 0)) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.knight) {
                        return true;
                    }
                }
            }
        }
        //Checks for rook/queen checks
        for(int x = location.x - 1; x >= 0; --x) { //Left
            //Breaks once we hit a none empty square
            if(getIcon(x, location.y).type != PieceType.none) {
                if((isEnemy(x, location.y, pc)) && ((getIcon(x, location.y).type == PieceType.rook) || (getIcon(x, location.y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int x = location.x + 1; x < 8; ++x) { //Right
            if(getIcon(x, location.y).type != PieceType.none) {
                if((isEnemy(x, location.y, pc)) && ((getIcon(x, location.y).type == PieceType.rook) || (getIcon(x, location.y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int y = location.y - 1; y >= 0; y--) { //Up
            if(getIcon(location.x, y).type != PieceType.none) {
                if((isEnemy(location.x, y, pc)) && ((getIcon(location.x, y).type == PieceType.rook) || (getIcon(location.x, y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int y = location.y + 1; y < 8; ++y) { //Down
            if(getIcon(location.x, y).type != PieceType.none) {
                if((isEnemy(location.x, y, pc)) && ((getIcon(location.x, y).type == PieceType.rook) || (getIcon(location.x, y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        //Checks for bishop/queen
        for(int i = location.x-1, j = location.y-1; i>=0 && j>=0; --i, --j) { //Up-Left
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.white && i == location.x-1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }

        }
        for(int i = location.x-1, j = location.y+1; i>=0 && j<8; --i, ++j) { //Up-Right
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.white && i == location.x-1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        for(int i = location.x+1, j = location.y-1; i<8 && j>=0; ++i, --j) { //Down-Left
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.black && i == location.x+1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        for(int i = location.x+1, j = location.y+1; i<8 && j<8; ++i, ++j) { //Down-Right
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.black && i == location.x+1) {
                    if(isEnemy(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isEnemy(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        //Checks if it is next to the king/useful for checking if we can make a move
        for(int i = location.x - 1; i <= location.x + 1; ++i) {
            for(int j = location.y - 1; j <= location.y + 1; ++j) {
                if((i >= 0) && (i < 8) && (j >= 0) && (j < 8)) {

                    //Ignore the king's own square
                    if (i == location.x && j == location.y) continue;

                    if((isEnemy(i, j, pc)) && (getIcon(i, j).type == PieceType.king)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //Function to check if a piece is protected by an ally
    boolean pieceProtected(PlayerColor pc, Point piece) {
        Point location = piece;
        //Checks for knight checks
        for(int i = location.x - 2; i <= location.x + 2; i+=4) {
            for(int j = location.y - 1; j <= location.y + 1; j+=2) {
                if((i < 8) && (i >= 0) && (j < 8) && (j >= 0)) {
                    if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.knight) {
                        return true;
                    }
                }
            }
        }
        for(int i = location.x - 1; i <= location.x + 1; i+=2) {
            for(int j = location.y - 2; j <= location.y + 2; j+=4) {
                if((i < 8) && (i >= 0) && (j < 8) && (j >= 0)) {
                    if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.knight) {
                        return true;
                    }
                }
            }
        }
        //Checks for rook/queen checks
        for(int x = location.x - 1; x >= 0; --x) { //Left
            //Breaks once we hit a none empty square
            if(getIcon(x, location.y).type != PieceType.none) {
                if((isAlly(x, location.y, pc)) && ((getIcon(x, location.y).type == PieceType.rook) || (getIcon(x, location.y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int x = location.x + 1; x < 8; ++x) { //Right
            if(getIcon(x, location.y).type != PieceType.none) {
                if((isAlly(x, location.y, pc)) && ((getIcon(x, location.y).type == PieceType.rook) || (getIcon(x, location.y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int y = location.y - 1; y >= 0; y--) { //Up
            if(getIcon(location.x, y).type != PieceType.none) {
                if((isAlly(location.x, y, pc)) && ((getIcon(location.x, y).type == PieceType.rook) || (getIcon(location.x, y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        for(int y = location.y + 1; y < 8; ++y) { //Down
            if(getIcon(location.x, y).type != PieceType.none) {
                if((isAlly(location.x, y, pc)) && ((getIcon(location.x, y).type == PieceType.rook) || (getIcon(location.x, y).type == PieceType.queen))) {
                    return true;
                }
                break;
            }
        }
        //Checks for bishop/queen
        for(int i = location.x-1, j = location.y-1; i>=0 && j>=0; --i, --j) { //Up-Left
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.black && i == location.x-1) {
                    if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isAlly(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }

        }
        for(int i = location.x-1, j = location.y+1; i>=0 && j<8; --i, ++j) { //Up-Right
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.black && i == location.x-1) {
                    if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isAlly(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        for(int i = location.x+1, j = location.y-1; i<8 && j>=0; ++i, --j) { //Down-Left
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.white && i == location.x+1) {
                    if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isAlly(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        for(int i = location.x+1, j = location.y+1; i<8 && j<8; ++i, ++j) { //Down-Right
            if(getIcon(i, j).type != PieceType.none) {
                if(pc == PlayerColor.white && i == location.x+1) {
                    if(isAlly(i, j, pc) && getIcon(i, j).type == PieceType.pawn)
                        return true;
                }
                if((isAlly(i, j, pc)) && ((getIcon(i, j).type == PieceType.bishop) || getIcon(i, j).type == PieceType.queen)) {
                    return true;
                }
                break;
            }
        }
        //Checks if it is next to the king/useful for checking if we can make a move
        for(int i = location.x - 1; i <= location.x + 1; ++i) {
            for(int j = location.y - 1; j <= location.y + 1; ++j) {
                if((i >= 0) && (i < 8) && (j >= 0) && (j < 8)) {

                    //Ignore the piece's own square
                    if (i == location.x && j == location.y) continue;

                    if((isAlly(i, j, pc)) && (getIcon(i, j).type == PieceType.king)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //Algorithm for the easy difficulty; randomly picks between the next available moves
    void easyAlgorithm() {
        //Gets moves if we do not have moves for this boardstate
        if(gamemoves.getEdges().get(currentBoard).isEmpty()) {
            getMoves(currentBoard);
        }
        long startTime = System.nanoTime();
        //Gets a list of every move from our current boardstate
        HashSet<BoardGraph.BoardState> moves = gamemoves.getEdges().get(currentBoard);
        ArrayList<BoardGraph.BoardState> list = new ArrayList<>();
        list.addAll(moves);
        if(!moves.isEmpty()) {
            //Randomly selects one of our moves to update the board to.
            Random rand = new Random();
            BoardGraph.BoardState update = (list.get(rand.nextInt(list.size())));
            updateBoard(update);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        moveTimes.add(duration);
        graphSize.add(gamemoves.size());
        //If moves is 0 the game is over; checkmate
    }
    //Algorithm for the med difficulty; selects the next best move using a weight function
    void medAlgorithm() {
        //Gets moves if we do not have moves for this boardstate
        if(gamemoves.getEdges().get(currentBoard).isEmpty()) {
            getMoves(currentBoard);
        }
        long startTime = System.nanoTime();
        //Gets a list of every move from our current boardstate
        HashSet<BoardGraph.BoardState> moves = gamemoves.getEdges().get(currentBoard);
        ArrayList<BoardGraph.BoardState> list = new ArrayList<>();
        ArrayList<BoardGraph.BoardState> decidedMoves = new ArrayList<>();
        list.addAll(moves);
        if(!moves.isEmpty()) {
            int weight = Integer.MIN_VALUE;
            //Goes through the list of possible moves and puts the highest weighted moves into a queue
            for(BoardGraph.BoardState b : list) {
                if(b.weight(turn) > weight) {
                    weight = b.weight(turn);
                    decidedMoves.clear();
                    decidedMoves.add(b);
                }
                else if(b.weight(turn) == weight) {
                    decidedMoves.add(b);
                }
                else if(b.checkmate) { //Instantly selects a checkmate move
                    updateBoard(b);
                    end = true;
                    break;
                }
            }
            if(!end && !moves.isEmpty()) {
                //Random wiil select from equally weighted moves
                Random rand = new Random();
                BoardGraph.BoardState update = decidedMoves.get(rand.nextInt(decidedMoves.size()));
                updateBoard(update);
            }
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        moveTimes.add(duration);
        graphSize.add(gamemoves.size());
        //If moves is 0 the game is over; checkmate
    }
    //Algorithm for the hard difficulty; this algorithm uses a some future sight and path finding to decide their next move
    void hardAlgorithm() {
        //Add future moves to the graph
        exploreFuture(currentBoard, 0, 2, new HashSet<>());
        long startTime = System.nanoTime();
        //Get next move
        BoardGraph.BoardState update = findNextMove(currentBoard, turn);
        //If not checkmated update the board to our new move
        if(!end) {
            //Built in error catch just in case
            if(update == null) {
                System.out.println("NULL UPDATE");
                medAlgorithm();
            }
            else {
                updateBoard(update);
            }
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        moveTimes.add(duration);
        graphSize.add(gamemoves.size());
    }
    //Function to update the board with computer given moves
    void updateBoard(BoardGraph.BoardState b) {
        Piece[][] board = b.board;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                setIcon(i, j, board[j][i]);
            }
        }
    }
    //Explores every possible move from the given boardstate and adds them to the graph
    void getMoves(BoardGraph.BoardState b) {
        PlayerColor pc = b.turn;
        updateBoard(b);
        int children = 0;
        ArrayList<Point> moves = new ArrayList<>();
        for(int i = 0; i < 8; ++i) {
            for(int j = 0; j < 8; ++j) {
                if(isAlly(i, j, pc)) {
                    switch(getIcon(i, j).type) {
                        case pawn:
                            markPawn(i, j, pc, moves);
                            break;
                        case bishop:
                            markBishop(i, j, pc, moves);
                            break;
                        case knight:
                            markKnight(i, j, pc, moves);
                            break;
                        case rook:
                            markRook(i, j, pc, moves);
                            break;
                        case queen:
                            markQueen(i, j, pc, moves);
                            break;
                        case king:
                            markKing(i, j, pc, moves);
                            break;
                    }
                    //Checks every move for each piece
                    for(Point p : moves) {
                        Piece originalPiece = getIcon(p.x, p.y);
                        //Checks if the move would promote a pawn and handles that case
                        if(pc == PlayerColor.white && p.x == 0 && getIcon(i, j).type == PieceType.pawn) {
                            setIcon(p.x, p.y, new Piece(pc, PieceType.queen, true));
                        }
                        else if(pc == PlayerColor.black && p.x == 7 && getIcon(i, j).type == PieceType.pawn) {
                            setIcon(p.x, p.y, new Piece(pc, PieceType.queen, true));
                        }
                        //Castling check
                        else if(getIcon(i, j).type == PieceType.king && i == p.x && abs(p.y - j) > 1) {
                            if(p.y - j > 1) {
                                //Moves the rook and king into castled positions
                                setIcon(p.x, p.y, getIcon(i, j));
                                setIcon(p.x, p.y - 1, getIcon(p.x, 7));
                                setIcon(i, j, new Piece());
                                setIcon(p.x, 7, new Piece());
                            }
                            else {
                                //Moves the rook and king into castled positions
                                setIcon(p.x, p.y, getIcon(i, j));
                                setIcon(p.x, p.y + 1, getIcon(p.x, 0));
                                setIcon(i, j, new Piece());
                                setIcon(p.x, 0, new Piece());
                            }
                        }
                        else setIcon(p.x, p.y, getIcon(i, j));
                        setIcon(i, j, new Piece());

                        if(!isCheck(pc, findKing(pc))) {
                            PlayerColor other = null;
                            if(pc == PlayerColor.white) other = PlayerColor.black;
                            else other = PlayerColor.white;
                            getValues();
                            //Checks if the move places the piece in danger and modifies board values
                            if(threatened(pc, p)) {
                                if(pc == PlayerColor.white) {
                                    if(originalPiece.type == PieceType.pawn) w_pieceValue -= 1;
                                    else if(originalPiece.type == PieceType.bishop) w_pieceValue -= 3;
                                    else if(originalPiece.type == PieceType.knight) w_pieceValue -= 3;
                                    else if(originalPiece.type == PieceType.rook) w_pieceValue -= 5;
                                    else if(originalPiece.type == PieceType.queen) w_pieceValue -= 9;
                                    w_pieceValue -= squarevalue[p.x][p.y];
                                }
                                else {
                                    if(originalPiece.type == PieceType.pawn) b_pieceValue -= 1;
                                    else if(originalPiece.type == PieceType.bishop) b_pieceValue -= 3;
                                    else if(originalPiece.type == PieceType.knight) b_pieceValue -= 3;
                                    else if(originalPiece.type == PieceType.rook) b_pieceValue -= 5;
                                    else if(originalPiece.type == PieceType.queen) b_pieceValue -= 9;
                                    b_pieceValue -= squarevalue[p.x][p.y];
                                }
                            }
                            //Checks if we can check the king while being unthreatened
                            if(isCheck(other, findKing(other)) && !threatened(pc, p)) {
                                if(pc == PlayerColor.white) w_pieceValue += 20;
                                else b_pieceValue += 20;
                            }
                            //Checks if the queen is threatened and subtracts some value from the board
                            if(findQueen(pc) != null) { //First check that we have a queen
                                if(threatened(pc, findQueen(pc))) {
                                    if(pc == PlayerColor.white) w_pieceValue -= 5;
                                    else b_pieceValue -= 5;
                                }
                            }
                            //Checks if we checkmated the other player and sets the winners value to the max
                            if(isCheckMate(other)) {
                                if(pc == PlayerColor.white) w_pieceValue = Integer.MAX_VALUE;
                                else b_pieceValue = Integer.MAX_VALUE;
                            }
                            BoardGraph.BoardState mBoard = new BoardGraph.BoardState(copyBoard(chessboardstatus), w_pieceValue, b_pieceValue, isCheck(other, findKing(other)), isCheckMate(other), other);
                            gamemoves.addEdge(b, mBoard);
                            children++;
                        }
                        updateBoard(b);
                        getValues();
                    }
                    //Resets Moveable for the next piece
                    moves = new ArrayList<>();
                }
            }
        }
        //Two prints to see how the number of moves available change when placed in check
        if(b.check) System.out.print("CHECK:");
        System.out.println("Number of Moves: " + children);
        updateBoard(currentBoard);
    }
    //Function to search into the future to expand the graph, used for the hard algorithm
    void exploreFuture(BoardGraph.BoardState b, int time, int maxDepth, HashSet<BoardGraph.BoardState> visited) {
        //Returns if we have explored deep enough
        if(time > maxDepth) return;
        //Skips visited boardstates to avoid loops
        if(visited.contains(b)) return;
        visited.add(b);
        //Creates moves for the boardState if it has not been done before
        if(gamemoves.getEdges().get(b).isEmpty() || gamemoves.getEdges().get(b) == null) {
            //System.out.println("Getting Moves; Time: " + time);
            getMoves(b);
        }
        HashSet<BoardGraph.BoardState> neighbors = new HashSet<>(gamemoves.getEdges().get(b));
        //Go through the neighbors of the boardstate to search their futures up until time reaches its goal
        for(BoardGraph.BoardState neighbor : neighbors) {
            exploreFuture(copyBoardState(neighbor), time + 1, maxDepth, visited);
        }
    }
    //Function that searches for the path to the highest value boardstate/checkmate
    BoardGraph.BoardState findNextMove(BoardGraph.BoardState start, PlayerColor pc) {
        //HashMaps to hold the best found weights and paths between boardstates
        HashMap<BoardGraph.BoardState, Integer> bestWeight = new HashMap<>();
        HashMap<BoardGraph.BoardState, BoardGraph.BoardState> cameFrom = new HashMap<>();
        HashSet<BoardGraph.BoardState> visited = new HashSet<>(); //Keeps track of visted boardstates; helps avoid cycles
        //Priority queue used to search for the highest value path
        PriorityQueue<BoardGraph.BoardState> pq = new PriorityQueue<>((a, b) -> Integer.compare(bestWeight.get(b), bestWeight.get(a)));
        BoardGraph.BoardState bestEnd = start;          //Holds the boardstate we want to move towards
        int maxWeight = Integer.MIN_VALUE;  //Holds the greatest found value
        //Starts by adding the starting boardstate to the priority queue
        bestWeight.put(start, start.weight(pc));
        cameFrom.put(start, null);
        pq.add(start);
        int time = 0;
        while(!pq.isEmpty() && time < 100000) {
            BoardGraph.BoardState current = pq.poll();

            if(visited.contains(current)) continue; //Skips already visited boardstate
            //Marks this boardstate as visited
            visited.add(current);
            //Skips a boardstate that has no neighbors to avoid errors
            if(gamemoves.getEdges().get(current) == null) continue;
            for(BoardGraph.BoardState neighbor : gamemoves.getEdges().get(current)) {
                if(visited.contains(neighbor)) continue; //Skip already visited neighbors
                if(neighbor == null) continue; //Skip null neighbors
                time++;
                //Gets the total weight of this path
                int newWeight = bestWeight.get(current) + neighbor.weight(pc);
                //Adds a weight if the neighbor has not been seen before or updates it if we found a better path
                if(!bestWeight.containsKey(neighbor) || newWeight > bestWeight.get(neighbor)) {
                    bestWeight.put(neighbor, newWeight);
                    //Updates to the newly found best path
                    cameFrom.put(neighbor, current);
                    //Adds this boardstate to the queue
                    pq.add(neighbor);
                }
                //Tracks the best found boardstate we want to move towards
                if(newWeight > maxWeight) {
                    maxWeight = newWeight;
                    bestEnd = neighbor;
                }
                //Randomly picks between equally weighted boards
                //Adds some variance to not play the same thing everytime
                else if(newWeight == maxWeight) {
                    Random rand = new Random();
                    if(rand.nextBoolean()) {
                        maxWeight = newWeight;
                        bestEnd = neighbor;
                    }
                }
            }
        }

        //Use a stack to hold the path, and we will pop 
        //the top of the stack to return the next move
        Stack<BoardGraph.BoardState> path = new Stack<>();
        //Climb back up cameFrom set to find out path
        //System.out.println("Found Path:");
        while(bestEnd != null && !bestEnd.equals(start)) {
            //Shows the path the algorithm found to be the best
            //System.out.println(bestEnd);
            //Adds this boardstate to the path
            path.push(bestEnd);
            //Updates bestEnd to the boardState it came from
            bestEnd = cameFrom.get(bestEnd);
        }
        if(path.isEmpty()) {
            return null;
        }
        return path.pop();
    }
    //Makes a deep copy of the give 2-D piece array; makes the hashset/map work in our graph
    Piece[][] copyBoard(Piece[][] board) {
        Piece[][] copy = new Piece[8][8];
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                Piece p = board[i][j];
                copy[i][j] = new Piece(p.color, p.type, p.hasMoved);
            }
        }
        return copy;
    }
    //Returns a deep copy of the given boardstate
    BoardGraph.BoardState copyBoardState(BoardGraph.BoardState b) {
        BoardGraph.BoardState copy = new BoardGraph.BoardState(copyBoard(b.board), b.w_val, b.b_val, b.check, b.checkmate, b.turn);
        return copy;
    }
}