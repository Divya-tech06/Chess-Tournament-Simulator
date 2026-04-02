package chess.matches;

import chess.exceptions.TournamentException;

public class MatchThread extends Thread {
    private final MatchPlayable playable;
    private TournamentException failure;

    public MatchThread(MatchPlayable playable, String name) {
        super(name);
        this.playable = playable;
    }

    @Override
    public void run() {
        try {
            playable.startMatch();
        } catch (TournamentException exception) {
            this.failure = exception;
        }
    }

    public TournamentException getFailure() {
        return failure;
    }
}
