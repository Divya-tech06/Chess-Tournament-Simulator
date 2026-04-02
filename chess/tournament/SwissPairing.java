package chess.tournament;

import chess.matches.Match;
import chess.players.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SwissPairing {
    public List<Match> generatePairings(List<Player> players, int roundNumber, int nextMatchNumber) {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(Comparator.comparingDouble(Player::getScore).reversed()
                .thenComparing(Comparator.comparingInt(Player::getRating).reversed())
                .thenComparing(Player::getName));

        List<Match> matches = new ArrayList<>();
        Player byePlayer = null;
        if (sortedPlayers.size() % 2 != 0) {
            byePlayer = chooseByePlayer(sortedPlayers);
            sortedPlayers.remove(byePlayer);
        }

        for (int index = 0; index < sortedPlayers.size(); index += 2) {
            Player first = sortedPlayers.get(index);
            Player second = sortedPlayers.get(index + 1);
            if (first.hasPlayed(second.getId())) {
                for (int swapIndex = index + 2; swapIndex < sortedPlayers.size(); swapIndex++) {
                    Player candidate = sortedPlayers.get(swapIndex);
                    if (!first.hasPlayed(candidate.getId())) {
                        sortedPlayers.set(index + 1, candidate);
                        sortedPlayers.set(swapIndex, second);
                        second = candidate;
                        break;
                    }
                }
            }
            matches.add(new Match("M" + nextMatchNumber++, roundNumber, first, second));
        }

        if (byePlayer != null) {
            matches.add(new Match("M" + nextMatchNumber, roundNumber, byePlayer, null));
        }
        return matches;
    }

    private Player chooseByePlayer(List<Player> players) {
        for (int index = players.size() - 1; index >= 0; index--) {
            if (!players.get(index).hasReceivedBye()) {
                return players.get(index);
            }
        }
        return players.get(players.size() - 1);
    }
}
