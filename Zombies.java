package dvakota.zombies;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Date: 11/1/14
 https://gist.github.com/anonymous/738e079f330053b6652f
 https://gist.github.com/anonymous/99e3789052c123fb2cad
 https://gist.github.com/anonymous/ffd6bd3af55bc89c9d49

 */

public class Zombies {
    static final Random RND = new Random();

    public static void start(int boardSize, int z, int v, int h, int cycles) {
        Board gameBoard = new Board(boardSize);
        gameBoard.initialize(z, v, h);
        gameBoard.show();

        for (int t = 0; t < cycles; t++) {
            gameBoard.tick();
            gameBoard.show();
            System.out.println("\n");
        }
        displayStats(gameBoard, cycles);
    }

    public static void displayStats(Board b, int ticks) {
        System.out.printf("Simulation stats after %d ticks\n\n", ticks);
        System.out.printf("%10s %10s %10s\n", "ZOMBIES", "VICTIMS", "HUNTERS");
        int[] totals = b.tally();
        for (int i = 0; i < totals.length; i++) {
            System.out.printf("%10d", totals[i]);
        }
        System.out.println();

        double[] stats = b.stats(totals);
        for (int i = 0; i < stats.length; i++) {
            System.out.printf("%10.2f%%", stats[i]);
        }
        System.out.println("\n");
        System.out.printf("Zombie points\tSTUMBLE: %d BITE: %d\n",
                                     Zombie.totalStumblePoints, Zombie.totalBitePoints);
        System.out.printf("Victim points\tFLEE: %d\n", Victim.totalFleePoints);
        System.out.printf("Hunter points\tSEEK: %d KILL: %d\n",
                                     Hunter.totalSeekPoints, Hunter.totalKillPoints);
        System.out.printf("Hunter double kills\t %d\n\n", Hunter.totalDoubleKills);
        System.out.printf("Victims bitten\t %10d\n", Victim.totalBitten);
        System.out.printf("Hunters bitten\t %10d\n", Hunter.totalBitten);
        //int totalSpawn = Victim.totalBitten + Hunter.totalBitten;
        int totalSpawn = Zombie.totalBitePoints;
        System.out.println("---------------------------------------------");
        System.out.printf("TOTAL HUMANS BITTEN BY ZOMBIES (new zombies spawn)\t%d\n", totalSpawn);
        System.out.printf("TOTAL ZOMBIES SLAYED BY HUMANS\t%d\n", Hunter.totalKillPoints);
        System.out.println("----------------------------------------------");
        int decay = Hunter.totalKillPoints - totalSpawn;
        if (decay == 5) System.out.printf("The simulation is in balance (decay = %d)\n", decay);
        else
            System.out.printf("The tally is %d in favor of %s\n", Math.abs(decay),
                                         decay > 5 ? "HUMANS" : "ZOMBIES");
        if (stats[0] > 60) System.out.printf("%.2f%% are now zombies. We're fucked. " +
                                                         "Happy Halloweeeeeen!..\n", stats[0]);

    }

    public static void main(String[] args) {
        int ticks = Zombies.RND.nextInt(50);
        int boardSize = 20;
        int z, v, h;
        z = v = h = 400;
        while (z + v + h >= 400) {
            z = Zombies.RND.nextInt(boardSize * boardSize);
            v = Zombies.RND.nextInt(boardSize * boardSize);
            h = Zombies.RND.nextInt(boardSize * boardSize);
        }
        System.out.printf("Starting simulation on %d x %d map\n", boardSize, boardSize);
        //System.out.printf("initial setting: ZOMBIES %d, VICTIMS %d, HUNTERS %d\n\n",
                   //                  z, v, h);

        System.out.printf("initial setting: ZOMBIES %d, VICTIMS %d, HUNTERS %d\n\n",
                                     7, 6, 3);

        //Zombies.start(boardSize, z, v, h, ticks);
        Zombies.start(5, 7, 6, 3, 10);
    }
}
