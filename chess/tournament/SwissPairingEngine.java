package chess.tournament;

import chess.exceptions.TournamentException;
import chess.players.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SwissPairingEngine {
    public PairingPlan buildRound(List<Player> originalPlayers) throws TournamentException {
        if (originalPlayers.size() < 2) {
            throw new TournamentException("At least two players are required.");
        }
        List<Player> sortedPlayers = new ArrayList<>(originalPlayers);
        sortedPlayers.sort(Comparator.comparingDouble(Player::getScore).reversed()
                .thenComparing(Comparator.comparingDouble(Player::getBuchholz).reversed())
                .thenComparing(Comparator.comparingInt(Player::getRating).reversed())
                .thenComparing(Player::getName));

        Player byePlayer = null;
        List<Player> working = new ArrayList<>(sortedPlayers);
        if (working.size() % 2 != 0) {
            byePlayer = chooseByePlayer(working);
            working.remove(byePlayer);
        }

        List<Pairing> pairings = backtrack(working, new ArrayList<>());
        if (pairings == null) {
            throw new TournamentException("Swiss pairing failed because no valid pairings were found.");
        }
        return new PairingPlan(pairings, byePlayer);
    }

    private Player chooseByePlayer(List<Player> players) {
        Player selected = players.get(players.size() - 1);
        for (int index = players.size() - 1; index >= 0; index--) {
            Player candidate = players.get(index);
            if (!candidate.hasReceivedBye()) {
                selected = candidate;
                break;
            }
        }
        return selected;
    }

    private List<Pairing> backtrack(List<Player> pool, List<Pairing> current) {
        if (pool.isEmpty()) {
            return new ArrayList<>(current);
        }
        Player first = pool.get(0);
        List<Integer> candidateIndexes = candidateIndexes(pool, first);
        for (Integer index : candidateIndexes) {
            Player second = pool.get(index);
            if (first.hasPlayed(second.getId())) {
                continue;
            }
            current.add(new Pairing(first, second));
            List<Player> next = new ArrayList<>(pool);
            next.remove(index.intValue());
            next.remove(0);
            List<Pairing> solved = backtrack(next, current);
            if (solved != null) {
                return solved;
            }
            current.remove(current.size() - 1);
        }
        return null;
    }

    private List<Integer> candidateIndexes(List<Player> pool, Player first) {
        List<Integer> indexes = new ArrayList<>();
        for (int index = 1; index < pool.size(); index++) {
            indexes.add(index);
        }
        indexes.sort((left, right) -> {
            Player a = pool.get(left);
            Player b = pool.get(right);
            int scoreGap = Double.compare(Math.abs(first.getScore() - a.getScore()), Math.abs(first.getScore() - b.getScore()));
            if (scoreGap != 0) {
                return scoreGap;
            }
            int ratingGap = Integer.compare(Math.abs(first.getRating() - a.getRating()), Math.abs(first.getRating() - b.getRating()));
            if (ratingGap != 0) {
                return ratingGap;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        return indexes;
    }

    public static class PairingPlan {
        private final List<Pairing> pairings;
        private final Player byePlayer;

        public PairingPlan(List<Pairing> pairings, Player byePlayer) {
            this.pairings = pairings;
            this.byePlayer = byePlayer;
        }

        public List<Pairing> getPairings() {
            return pairings;
        }

        public Player getByePlayer() {
            return byePlayer;
        }
    }

    public static class Pairing {
        private final Player first;
        private final Player second;

        public Pairing(Player first, Player second) {
            this.first = first;
            this.second = second;
        }

        public Player getFirst() {
            return first;
        }

        public Player getSecond() {
            return second;
        }
    }
}
