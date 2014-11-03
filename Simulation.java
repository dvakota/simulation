package dvakota.zombies;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Date: 11/2/14
 */
public class Simulation extends JFrame {
    final static int CELL_SIZE = 20;
    final static int WINDOW_SIZE = 800;

    static int cellSize; static int windowSize;

    static int cells;
    public Simulation(int c, int w) {
        cellSize = c; windowSize = w;
        cells = windowSize / cellSize;
        windowSize = cells * cellSize;
    }

    public static void main(String[] args) {
        Simulation test = new Simulation(CELL_SIZE, WINDOW_SIZE);
        test.setTitle("Zombie Apocalypse");
        test.setSize(new Dimension(test.windowSize, test.windowSize));
        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        test.setResizable(false);

        Board gameBoard = new Board(test.cells);
        int z, v, h;
        z = v = h = test.cells * test.cells;
        Random r = new Random();
        while (z + v + h >= (test.cells * test.cells)) {
            z = r.nextInt(test.cells * Math.min(3, test.cells));
            v = r.nextInt(test.cells * Math.min(10, test.cells));
            h = r.nextInt(3);
        }
        gameBoard.initialize(z, v, h);

        final Game grid = new Game(gameBoard);
        grid.setLayout(null);
        test.setContentPane(grid);
        test.setVisible(true);
        grid.repaint();
        new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                grid.mBoard.moveZombies();
                grid.repaint();
                grid.mBoard.moveVictims();
                grid.repaint();
                grid.mBoard.moveHunters();
                grid.repaint();
                grid.mBoard.huntersAttack();
                grid.repaint();
                grid.mBoard.zombiesAttack();
                grid.repaint();
                grid.mBoard.refreshAll();
            }
        }).start();
    }
}


class Game extends JPanel {

    final Board mBoard;

    public Game(Board board) {
        mBoard = board;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < mBoard.side; i++) {
            for (int j = 0; j < mBoard.side; j++) {
                Player p = mBoard.board[i][j];
                int x = j * Simulation.cellSize;
                int y = i * Simulation.cellSize;
                g.setColor(Color.BLACK);
                g.drawRect(x, y, Simulation.cellSize, Simulation.cellSize);
                if (p instanceof Zombie) g.setColor(Color.BLACK);
                if (p instanceof Hunter) g.setColor(Color.RED);
                if (p instanceof Victim) g.setColor(Color.YELLOW);
                if (p instanceof Empty) g.setColor(Color.WHITE);
                g.fillRect(x, y, Simulation.cellSize, Simulation.cellSize);
            }
        }
    }
}
