package chess.tournament;

import chess.players.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Standings {
    public List<Player> sortedPlayers(List<Player> players) {
        List<Player> copy = new ArrayList<>(players);
        copy.sort(Comparator.comparingDouble(Player::getScore).reversed()
                .thenComparing(Comparator.comparingDouble(Player::getBuchholz).reversed())
                .thenComparing(Comparator.comparingInt(Player::getWins).reversed())
                .thenComparing(Comparator.comparingInt(Player::getRating).reversed())
                .thenComparing(Player::getName));
        return copy;
    }

    public void refreshTieBreaks(List<Player> players) {
        for (Player player : players) {
            double buchholz = 0.0;
            for (String opponentId : player.getOpponents()) {
                for (Player rival : players) {
                    if (rival.getId().equals(opponentId)) {
                        buchholz += rival.getScore();
                        break;
                    }
                }
            }
            player.setBuchholz(buchholz);
        }
    }

    public String formatTable(List<Player> players) {
        List<Player> ranking = sortedPlayers(players);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-5s %-20s %-8s %-8s %-8s %-8s %-10s%n",
                "Rank", "Player", "Rating", "Score", "Wins", "Draws", "Buchholz"));
        int rank = 1;
        for (Player player : ranking) {
            builder.append(String.format("%-5d %-20s %-8d %-8.1f %-8d %-8d %-10.1f%n",
                    rank++,
                    player.getName(),
                    player.getRating(),
                    player.getScore(),
                    player.getWins(),
                    player.getDraws(),
                    player.getBuchholz()));
        }
        return builder.toString();
    }
}
