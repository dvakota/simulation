package dvakota.zombies;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;


import dvakota.zombies.Zombie;


/**
 * Date: 11/2/14
 */
public class Board {
    private static final int GRID_WIDTH = 100;
    private static final int CELL_WIDTH = 6;
    private volatile long ticks;
    private volatile long tickDelay;


    final Random RND = new Random();

    Queue<Player> zombies = new LinkedList<Player>();
    Queue<Player> victims = new LinkedList<Player>();
    Queue<Player> hunters = new LinkedList<Player>();

    Queue<Player> newZombies = new LinkedList<Player>();
    Queue<Player> newVictims = new LinkedList<Player>();
    Queue<Player> newHunters = new LinkedList<Player>();

    // XXX add a comment here to say what side means, how an integer represents it
    int side;
    Player[][] board;
    final Empty EMPTY = new Empty(this);

    public Board(int s) {
        side = s;
        board = new Player[side][side];
        init(board);
    }

    // XXX add comments here to say what z h and h represent (side addressed above)
    // i would assume it is zombies victims and humans but idk
    public void initialize(int z, int v, int h) {
        if (z + v + h > side * side - 1)
            throw new IllegalArgumentException("Too many players!");

        int edge = side * side;
        int[] positions = new int[edge];
        for (int i = 0; i < edge; i++) positions[i] = i;

        while (v > 0) {
            Player player = new Victim(this, findAvailablaPosition(edge, side, positions));
            board[player.position.x][player.position.y] = player;
            victims.offer(player);
            v--; edge--;
        }
        while (z > 0) {
            Player player = new Zombie(this, findAvailablaPosition(edge, side, positions));
            board[player.position.x][player.position.y] = player;
            zombies.offer(player);
            z--; edge--;
        }
        while (h > 0) {
            Player player = new Hunter(this, findAvailablaPosition(edge, side, positions));
            board[player.position.x][player.position.y] = player;
            hunters.offer(player);
            h--; edge--;
        }
    }

    /* Locates a random element (board index), then removes it
    from the available set (aka Fished-Yates shuffle) */
    // XXX what is edge? edge of a graph? side of a plane?
    private Tuple findAvailablaPosition(int edge, int side, int[] positions) {
        int index = RND.nextInt(edge);
        Tuple xy = new Tuple(positions[index] % side, positions[index] / side);
        int tmp = positions[index];
        positions[index] = positions[edge - 1];
        positions[edge - 1] = tmp;
        return xy;
    }

    private void init(Player[][] board) {
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                board[i][j] = EMPTY;
            }
        }
    }

    /* Checks for available position to move to in current board state */
    public boolean isAvailable(Tuple at) {
        if (at.x < 0 || at.y < 0) return false;
        if (at.x >= side || at.y >= side) return false;
        return board[at.x][at.y] == EMPTY;
    }

    /* get(): fetch Player from board */
    public Player getPlayer(Tuple at) {
        if (at.x < 0 || at.y < 0) return null;
        if (at.x >= side || at.y >= side) return null;
        return board[at.x][at.y];
    }

    /* put(): updates the current board state and future Player state */
    public void put(Player p) {
        board[p.position.x][p.position.y] = p;
        if (p instanceof Zombie) newZombies.offer(p);
        if (p instanceof Victim) newVictims.offer(p);
        if (p instanceof Hunter) newHunters.offer(p);
    }

    /* Three board updates per tick: 1) Movement 2) Hunters attack 3) Zombies bite */
    void tick() {
        moveZombies();
        show();
        moveVictims();
        show();
        moveHunters();
        show();
        huntersAttack();
        show();
        zombiesAttack();
        show();
        refreshAll();
    }
     void moveZombies() {
            /* Board state is updated continuously, while Player locations/numbers
            are changed once per cycle (to avoid collisions but also
            not relocate a single Player multiple times) */
        for (Player p : zombies) {
            p.move();
        }
    }

     void moveVictims() {

        for (Player p : victims) {
            p.move();
        }
    }

     void moveHunters() {
        for (Player p : hunters) {
            p.move();
        }
    }


     void huntersAttack() {
        /* Refrresh Hunter locations after the move for next iteration */
        hunters = newHunters;
        newHunters = new LinkedList<Player>();

        //Hunters attack, zombies chill
        for (Player p : hunters) {
            p.attack();
        }
    }

     void zombiesAttack() {
        /* Refresh Zombie locations and numbers after the hunt for next iteration */
        zombies  = newZombies;
        newZombies = new LinkedList<Player>();

        //Zombies attack, hunters chill
        for (Player p : zombies)  {
            p.attack();
        }

    }

     void refreshAll() {
        hunters = newHunters; newHunters = new LinkedList<Player>();
        zombies = newZombies; newZombies = new LinkedList<Player>();
        victims = newVictims; newVictims = new LinkedList<Player>();
    }

    public void show() {
        for (Player[] row : board) {
            for (Player p : row) {
                System.out.printf("%3s", p.toString());
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    /* Total amount of each Player type // 0 - Zombie, 1 - Victim, 2 - Hunter
    * currently on board */
    public int[] tally() {
        int tally[] = new int[3];
        for (Player[] row : board) {
            for (Player p : row) {
                if (p instanceof Zombie) tally[0] += 1;
                if (p instanceof Victim) tally[1] += 1;
                if (p instanceof Hunter) tally[2] += 1;
            }
        }

            /* Sanity check, boards and lists are updated independently */
        assert(tally[0] == zombies.size());
        assert(tally[1] == victims.size());
        assert(tally[2] == hunters.size());
        return tally;
    }

    /* Percentage of each Player type */
    public double[] stats(int[] tally) {
        double sum = 0;  double[] result = new double[3];
        for (int i = 0; i < tally.length; i++) {
            sum += tally[i];
        }
        if (sum == 0) return result;
        for (int i = 0; i < tally.length; i++) {
            result[i] = tally[i] / sum * 100;
        }
        return result;
    }

    /* Sets Empty element at a given location on board*/
    void put(Empty empty, int x, int y) {
        board[x][y] = empty;
    }

    /* Update future Player states if killed/turned */
    public void remove(Player toKill) {
        if (toKill instanceof Zombie) newZombies.remove(toKill);
        if (toKill instanceof Hunter) {
            newHunters.remove(toKill);
            Hunter.totalBitten++;
        }
        if (toKill instanceof Victim) {
            newVictims.remove(toKill);
            Victim.totalBitten++;
        }
    }
}

class Tuple {
    int x;
    int y;
    public Tuple(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

