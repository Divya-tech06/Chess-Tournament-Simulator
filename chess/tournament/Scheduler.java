package chess.tournament;

import chess.exceptions.TournamentException;
import chess.matches.Match;
import chess.matches.RapidMatch;
import chess.matches.StandardMatch;
import chess.players.Player;
import chess.utils.IdGenerator;

import java.util.List;

public class Scheduler {
    private final SwissPairingEngine pairingEngine;

    public Scheduler() {
        this.pairingEngine = new SwissPairingEngine();
    }

    public Round createRound(int roundNumber, List<Player> players) throws TournamentException {
        SwissPairingEngine.PairingPlan plan = pairingEngine.buildRound(players);
        Round round = new Round(roundNumber);
        int boardNumber = 1;
        for (SwissPairingEngine.Pairing pairing : plan.getPairings()) {
            Player white = chooseWhite(pairing.getFirst(), pairing.getSecond(), boardNumber);
            Player black = white == pairing.getFirst() ? pairing.getSecond() : pairing.getFirst();
            Match match = roundNumber % 2 == 0
                    ? new RapidMatch(IdGenerator.nextMatchId(), roundNumber, white, black)
                    : new StandardMatch(IdGenerator.nextMatchId(), roundNumber, white, black);
            round.addMatch(match);
            boardNumber++;
        }
        if (plan.getByePlayer() != null) {
            round.addMatch(new StandardMatch(IdGenerator.nextMatchId(), roundNumber, plan.getByePlayer(), null));
        }
        return round;
    }

    private Player chooseWhite(Player first, Player second, int boardNumber) {
        if (boardNumber % 2 == 0) {
            return first.getRating() >= second.getRating() ? second : first;
        }
        return first.getRating() >= second.getRating() ? first : second;
    }
}
