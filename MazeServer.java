/**
 * Project 05
 * @author Colin Ashburn, cashburn, 807
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MazeServer implements Runnable {
    ServerSocket serverSocket;
    private boolean exit;
    private int port;
    Socket currentSocket;
    Socket firstSocket;
    ArrayList<Socket> sockets = new ArrayList<Socket>();
    ArrayList<String[]> requests = new ArrayList<String[]>();
    char[][] maze;
    int startRow, startCol;
    int endRow, endCol;
    int rows, cols;
    int curRow, curCol;
    static int level;

    public MazeServer(int port, char[][] maze) throws SocketException, IOException {
        serverSocket = new ServerSocket(port);
        this.port = port;

        this.maze = maze;
        rows = maze.length;
        cols = maze[0].length;
        startRow = 1;
        startCol = 0;
        endRow = rows - 2;
        endCol = cols - 1;
        curRow = startRow;
        curCol = startCol;
    }
    
    public MazeServer(int port) throws SocketException, IOException {
        serverSocket = new ServerSocket(port);
        this.port = port;
    }
    public MazeServer(char[][] maze) throws SocketException, IOException {
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();

        this.maze = maze;
        rows = maze.length;
        cols = maze[0].length;
        startRow = 1;
        startCol = 0;
        endRow = rows - 2;
        endCol = cols - 1;
        curRow = startRow;
        curCol = startCol;

    }
    
    public int getLocalPort() {
        return port;
    }
    
    public void run() {
        try {
            while (!exit) {
                currentSocket = serverSocket.accept();
                sockets.add(currentSocket);
                input();
            }
            serverSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void input() {
        try {
            PrintWriter pw = new PrintWriter(currentSocket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(currentSocket.getInputStream()));
            String line;
            line = in.readLine();
            String input = line;

            if (input.equals("isSolved")) {
                boolean isSolved = this.isSolved();
                pw.println(isSolved);
                pw.flush();
                if (isSolved)
                    line = ":SHUTDOWN";
            }
            
            else if (input.equals("checkUp")) {
                pw.println(this.checkUp());
                pw.flush();
            }
            else if (input.equals("checkDown")) {
                pw.println(this.checkDown());
                pw.flush();
            }
            else if (input.equals("checkLeft")) {
                pw.println(this.checkLeft());
                pw.flush();
            }
            else if (input.equals("checkRight")) {
                pw.println(this.checkRight());
                pw.flush();
            }

            else if (input.equals("moveUp")) {
                pw.println(this.moveUp());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }
            else if (input.equals("moveDown")) {
                pw.println(this.moveDown());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }
            else if (input.equals("moveLeft")) {
                pw.println(this.moveLeft());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }
            else if (input.equals("moveRight")) {
                pw.println(this.moveRight());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }

            else if (input.equals("checkBackUp")) {
                pw.println(this.checkBackUp());
                pw.flush();
            }
            else if (input.equals("checkBackDown")) {
                pw.println(this.checkBackDown());
                pw.flush();
            }
            else if (input.equals("checkBackLeft")) {
                pw.println(this.checkBackLeft());
                pw.flush();
            }
            else if (input.equals("checkBackRight")) {
                pw.println(this.checkBackRight());
                pw.flush();
            }


            else if (input.equals("moveBackUp")) {
                pw.println(this.moveBackUp());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }
            else if (input.equals("moveBackDown")) {
                pw.println(this.moveBackDown());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }
            else if (input.equals("moveBackLeft")) {
                pw.println(this.moveBackLeft());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }
            else if (input.equals("moveBackRight")) {
                pw.println(this.moveBackRight());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }


            else if (input.equals("undo")) {
                pw.println(this.undo());
                this.print(this.curRow, this.curCol);
                pw.flush();
            }


	    else if (line.equals("Test")) {
                System.out.println("MAZE_TEST");
                pw.println("The test succeeded.");
                pw.flush();
            }
                else if (line.equals(":RESET")) {
                    while (requests.size() > 0 || sockets.size() > 0) {
                        
                        
                            pw.println("RESPONSE: success");
                            pw.flush();
                        
                        if (!(sockets.get(0) == currentSocket)) {
                            
                            try {
                                PrintWriter pw2 = new PrintWriter(sockets.get(0).getOutputStream());
                                pw2.println("ERROR: connection reset");
                                pw2.flush();
                                pw2.close();
                                
                            }
                            catch (SocketException e) {
                            }
                        }
                            pw.close();
                            sockets.get(0).close();
                            if (requests.size() > 0)
                                requests.remove(0);
                            sockets.remove(0);
                    }
                }
                if (line.equals(":SHUTDOWN")) {
                    while (requests.size() > 0 || sockets.size() > 0) {
                            pw.println("Maze shut down successfully");
                            pw.flush();

                            if (!(currentSocket == sockets.get(0))) {
                            try {
                                PrintWriter pw2 = new PrintWriter(sockets.get(0).getOutputStream());
                                pw2.println("ERROR: connection reset");
                                pw2.flush();
                                pw2.close();
                                
                            }
                            catch (SocketException e) {
                            }
                            }
                            pw.close();
                            sockets.get(0).close();
                            if (requests.size() > 0)
                                requests.remove(0);
                            sockets.remove(0);
                    }
                    exit = true;
                }
                else {
                    pw.println("ERROR: invalid request");
                    pw.flush();
                }   
            if (exit) {
                pw.close();
                in.close();
                currentSocket.close();
            }
        } catch (IOException e) {
        }
        
    }
    public static void main(String[] args) throws SocketException, IOException, FileNotFoundException {
        String filename = "maze.txt";
        MazeServer server;
        int i;
        if (args.length == 2) {
            try {
                filename = args[1];
                i = Integer.parseInt(args[0]);
                server = new MazeServer(i, readMaze(filename));
            }
            catch(NumberFormatException|BindException b) {
                System.out.println("Incorrect port specification.");
                return;
            }
        }
        else {
            server = new MazeServer(readMaze(filename));
            System.out.printf("Port not specified. Using free port %s%n",server.port);
        }
        server.maze[server.curRow][server.curCol] = '*';
        server.print(server.curRow, server.curCol);
        server.run();
    }

    private boolean checkUp() {
        int row = curRow - 1;
        int col = curCol;
        if (row < 0 || col < 0 || row >= rows || col >= cols || maze[row][col] != ' ')
            return false;
        return true;
    }

    private boolean checkDown() {
        int row = curRow + 1;
        int col = curCol;
        if (row < 0 || col < 0 || row >= rows || col >= cols || maze[row][col] != ' ')
            return false;
        return true;
    }

    private boolean checkRight() {
        int row = curRow;
        int col = curCol + 1;
        if (row < 0 || col < 0 || row >= rows || col >= cols || maze[row][col] != ' ')
            return false;
        return true;
    }

    private boolean checkLeft() {
        int row = curRow;
        int col = curCol - 1;
        if (row < 0 || col < 0 || row >= rows || col >= cols || maze[row][col] != ' ')
            return false;
        return true;
    }

    private boolean moveUp() {
        if (!checkUp())
            return false;
        curRow = curRow - 1;
        maze[curRow][curCol] = '*';
        return true;
    }

    private boolean moveDown() {
        if (!checkDown())
            return false;
        curRow = curRow + 1;
        maze[curRow][curCol] = '*';
        return true;
    }

    private boolean moveLeft() {
        if (!checkLeft())
            return false;
        curCol--;
        maze[curRow][curCol] = '*';
        return true;
    }

    private boolean moveRight() {
        if (!checkRight())
            return false;
        curCol++;
        maze[curRow][curCol] = '*';
        return true;
    }

    private boolean checkBackUp() {
        int row = curRow - 1;
        int col = curCol;
        if (row < 0 || col < 0 || row >= rows || col >= cols || (maze[row][col] != ' ' && maze[row][col] != '*'))
            return false;
        return true;
    }

    private boolean checkBackDown() {
        int row = curRow + 1;
        int col = curCol;
        if (row < 0 || col < 0 || row >= rows || col >= cols || (maze[row][col] != ' ' && maze[row][col] != '*'))
            return false;
        return true;
    }

    private boolean checkBackRight() {
        int row = curRow;
        int col = curCol + 1;
        if (row < 0 || col < 0 || row >= rows || col >= cols || (maze[row][col] != ' ' && maze[row][col] != '*'))
            return false;
        return true;
    }

    private boolean checkBackLeft() {
        int row = curRow;
        int col = curCol - 1;
        if (row < 0 || col < 0 || row >= rows || col >= cols || (maze[row][col] != ' ' && maze[row][col] != '*'))
            return false;
        return true;
    }


    private boolean moveBackUp() {
        if (!checkBackUp())
            return false;
        curRow = curRow - 1;
        maze[curRow][curCol] = 'x';
        return true;
    }

    private boolean moveBackDown() {
        if (!checkBackDown())
            return false;
        curRow = curRow + 1;
        maze[curRow][curCol] = 'x';
        return true;
    }

    private boolean moveBackLeft() {
        if (!checkBackLeft())
            return false;
        curCol--;
        maze[curRow][curCol] = 'x';
        return true;
    }

    private boolean moveBackRight() {
        if (!checkBackRight())
            return false;
        curCol++;
        maze[curRow][curCol] = 'x';
        return true;
    }

    private boolean isSolved() {
        return (curRow == endRow && curCol == endCol);
    }

    private boolean undo() {
        int count = 0;
        maze[curRow][curCol] = 'x';
        while (!checkDown() && !checkUp() && !checkLeft() && !checkRight()) {
            boolean changed = false;
            if (count > 500)
                return false;
            if (checkBackUp()) {
                moveBackUp();
                count++;
                changed = true;
            }
            if (changed)
                continue;
            if (checkBackLeft()) {
                moveBackLeft();
                count++;
                changed = true;
            }
            if (changed)
                continue;
            if (checkBackDown()) {
                moveBackDown();
                count++;
                changed = true;
            }
            if (changed)
                continue;
            if (checkBackRight()) {
                moveBackRight();
                count++;
                changed = true;
            }
        }
        maze[curRow][curCol] = '*';
        return true;
    }

    private void print(int row, int col) {
        System.out.print("\u001B[H\u001B[2J");  // clear screen (system dependent)
        for (int r = 0; r < maze.length; r++) {
            for (int c = 0; c < maze[0].length; c++)
                System.out.printf("%c", maze[r][c]);
            System.out.println();
        }
        System.out.printf("%3d: entering (%2d,%2d)\n", level, row, col);
        if (maze.length > 1) {
            try { Thread.sleep(75); } catch (InterruptedException e) { 
e.printStackTrace(); };
        } else {
            System.out.print("press enter...");
            try { System.in.read(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private static char[][] readMaze(String filename) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<String>();
        Scanner in = new Scanner(new File(filename));
        while (in.hasNextLine())
            lines.add(in.nextLine());
        int rows = lines.size();
        int cols = lines.get(0).length();
        char[][] maze = new char[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                maze[r][c] = lines.get(r).charAt(c);
        return maze;
    }


}
