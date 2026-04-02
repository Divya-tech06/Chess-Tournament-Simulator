package chess.utils;

import chess.players.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private static final AtomicInteger PLAYER_COUNTER = new AtomicInteger(1000);
    private static final AtomicInteger MATCH_COUNTER = new AtomicInteger(1);

    private IdGenerator() {
    }

    public static String nextPlayerId() {
        return "P" + PLAYER_COUNTER.incrementAndGet();
    }

    public static String nextMatchId() {
        return "M" + MATCH_COUNTER.getAndIncrement();
    }

    public static void syncPlayerCounter(List<Player> players) {
        int max = 1000;
        for (Player player : players) {
            String id = player.getId();
            if (id != null && id.startsWith("P")) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(1)));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        PLAYER_COUNTER.set(max);
    }
}
