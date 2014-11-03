package dvakota.zombies;

import java.util.Random;

/**
 * Date: 11/2/14
 */
abstract class Player {
    final Random RND = new Random();
    Tuple position;
    Board board;

    public Player(Board b, Tuple pos) {
        board = b;
        position = pos;
    }

    public void move() {
        //clear Player's old board position before the potential move
        board.put(board.EMPTY, position.x, position.y);
        makeMove();
    }

    public void attack() {
        //keep the original Player position on board
        board.put(this);
        makeAttack();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().substring(0, 1).toUpperCase();
    }

    protected abstract void makeMove();
    protected abstract void makeAttack();
}

class Empty extends Player {

    public Empty(Board b) {
        super(b, new Tuple(-1, -1));
    }

    @Override
    public  void makeMove(){};

    @Override
    public void makeAttack(){};


    @Override
    public String toString() {
        return " ";
    }
}

class Zombie extends Player {
    static int totalStumblePoints;
    static int totalBitePoints;

    public Zombie(Board b, Tuple pos) {
        super(b, pos);
    }

    @Override
    public void makeMove() {
        Tuple[] legalMoves = new Tuple[4];
        int i = 0;
            /* Enumerate all legal moves for the current Zombie */
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx++) {
                if (dx == dy && dx == 0) continue; //skip self
                Tuple pos = new Tuple(position.x + dx, position.y + dy);
                if ((dx == 0 || dy == 0) && board.isAvailable(pos)) {
                    legalMoves[i++] = pos;
                }
            }
        }
            /* Select new legal position at random */
        if (i > 0) {
            Tuple newPos = legalMoves[RND.nextInt(i)];
            position.x = newPos.x; position.y = newPos.y;
            totalStumblePoints++;
        }
        board.put(this);
    }

    /* Bite all teh things! */
    @Override
    public void makeAttack() {
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx++) {
                if (dx == dy && dx == 0) continue;
                Tuple pos = new Tuple(position.x + dx, position.y + dy);
                if ((dx == 0 || dy == 0)  && board.getPlayer(pos) != board.EMPTY
                                && board.getPlayer(pos) != null
                                && (!(board.getPlayer(pos) instanceof Zombie))) {
                    board.remove(board.getPlayer(pos));
                    board.put(new Zombie(board, pos));
                    totalBitePoints++;
                }
            }
        }
    }

    public String toString() {
        return "☻";
    }
}

class Victim extends Player {
    static int totalFleePoints;
    static int totalBitten;

    public Victim(Board b, Tuple pos) {
        super(b, pos);
    }

    @Override
    public void makeMove() {
        Tuple[] legalMoves = new Tuple[8];

        boolean shouldMove = false;
        int i = 0;
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx ++) {
                if (dx == dy && dx == 0) continue;
                Tuple pos = new Tuple(position.x + dx, position.y + dy);
                if (board.getPlayer(pos) instanceof Zombie) {
                    shouldMove = true;
                }
                if (board.isAvailable(pos)) legalMoves[i++] = pos;
            }
        }
        if (shouldMove && i > 0)  {
            Tuple newPos = legalMoves[RND.nextInt(i)];
            totalFleePoints++;
            position.x = newPos.x; position.y = newPos.y;
        }
        board.put(this);
    }

    @Override
    public void makeAttack() {
        //just stand there like an idiot
    }

    public String toString() {
        return "웃";
    }
}

class Hunter extends Player {
    static int totalSeekPoints;
    static int totalKillPoints;
    static int totalDoubleKills;
    static int totalBitten;

    public Hunter(Board b, Tuple pos) {
        super(b, pos);
    }

    /* Hunter will makeMove arbitrarily in any available direction */
    @Override
    public void makeMove() {
        Tuple[] legalMoves = new Tuple[8];
        int i = 0;
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx++) {
                if (dx == dy && dx == 0) continue;
                Tuple newPos = new Tuple(position.x + dx, position.y + dy);
                if (board.isAvailable(newPos)) legalMoves[i++] = newPos;
            }
        }
        if (i > 0) {
            Tuple newPos = legalMoves[RND.nextInt(i)];
            position.x = newPos.x; position.y = newPos.y;
            totalSeekPoints++;
        }
        board.put(this);
    }

    /* Kill up to 2 zombies nearby  */
    @Override
    public void makeAttack() {
        Zombie[] zombies = new Zombie[8];
        int i = 0;
        for (int dy = -1; dy < 2; dy++) {
            for (int dx = -1; dx < 2; dx++) {
                if (dx == dy && dx == 0) continue;
                Tuple pos = new Tuple(position.x + dx, position.y + dy);
                Player nearby = board.getPlayer(pos);
                if (nearby instanceof Zombie) {
                    zombies[i++] = (Zombie) nearby;
                }
            }
        }
        if (i == 0) return; //noone to kill
        //otherwise, pick and slay at least two
        int howMany = Math.min(2, i);
        int remaining = i;
        //choose zombies at random
        for (int slayCount = 0; slayCount < howMany; slayCount++) {
            int index = RND.nextInt(remaining);

            Zombie toKill = zombies[index];
            board.put(board.EMPTY, toKill.position.x, toKill.position.y);
            board.remove(toKill);
            totalKillPoints++;

            //choose remaining zombie from the nearby group
            Zombie tmp = zombies[remaining - 1];
            zombies[remaining - 1] = zombies[index];
            zombies[index] = tmp;
            remaining--;
        }
        if (howMany == 2) totalDoubleKills++;
    }

    public String toString() {
        return " 유☭";
    }
}
