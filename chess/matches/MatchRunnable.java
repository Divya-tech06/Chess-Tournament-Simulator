package chess.matches;

import chess.exceptions.TournamentException;

public class MatchRunnable implements Runnable {
    private final Match match;
    private TournamentException error;

    public MatchRunnable(Match match) {
        this.match = match;
    }

    @Override
    public void run() {
        try {
            MatchPlayable playable = match;
            playable.startMatch();
        } catch (TournamentException exception) {
            error = exception;
        }
    }

    public TournamentException getError() {
        return error;
    }
}
