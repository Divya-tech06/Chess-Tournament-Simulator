package chess.tournament;

import chess.matches.Match;
import chess.players.Player;

import java.util.ArrayList;
import java.util.List;

public class RoundRobinScheduler {
    public List<List<Match>> createSchedule(List<Player> players) {
        List<Player> rotation = new ArrayList<>(players);
        if (rotation.size() % 2 != 0) {
            rotation.add(null);
        }

        List<List<Match>> schedule = new ArrayList<>();
        int rounds = rotation.size() - 1;
        int matchNumber = 1;

        for (int round = 1; round <= rounds; round++) {
            List<Match> roundMatches = new ArrayList<>();
            for (int index = 0; index < rotation.size() / 2; index++) {
                Player first = rotation.get(index);
                Player second = rotation.get(rotation.size() - 1 - index);

                if (first == null && second == null) {
                    continue;
                }

                if (first == null || second == null) {
                    Player byePlayer = first == null ? second : first;
                    roundMatches.add(new Match("M" + matchNumber++, round, byePlayer, null));
                } else if (round % 2 == 0) {
                    roundMatches.add(new Match("M" + matchNumber++, round, second, first));
                } else {
                    roundMatches.add(new Match("M" + matchNumber++, round, first, second));
                }
            }
            schedule.add(roundMatches);
            rotatePlayers(rotation);
        }

        return schedule;
    }

    private void rotatePlayers(List<Player> rotation) {
        Player lastPlayer = rotation.remove(rotation.size() - 1);
        rotation.add(1, lastPlayer);
    }
}
