package chess.matches;

import chess.exceptions.TournamentException;
import chess.players.Player;

public class StandardMatch extends Match {
    public StandardMatch(String matchId, int roundNumber, Player whitePlayer, Player blackPlayer) {
        super(matchId, roundNumber, whitePlayer, blackPlayer, "Standard");
    }

    @Override
    public void startMatch() throws TournamentException {
        try {
            Thread.sleep(simulatedDelay());
            recordResult();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TournamentException("Standard match interrupted.");
        }
    }
}
