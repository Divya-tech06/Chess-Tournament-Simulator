package chess.matches;

import chess.exceptions.TournamentException;
import chess.players.Player;

public class RapidMatch extends Match {
    public RapidMatch(String matchId, int roundNumber, Player whitePlayer, Player blackPlayer) {
        super(matchId, roundNumber, whitePlayer, blackPlayer, "Rapid");
    }

    @Override
    public void startMatch() throws TournamentException {
        try {
            Thread.sleep(Math.max(250, simulatedDelay() / 2));
            recordResult();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TournamentException("Rapid match interrupted.");
        }
    }
}
