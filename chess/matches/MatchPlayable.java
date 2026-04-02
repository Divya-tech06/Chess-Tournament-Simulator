package chess.matches;

import chess.exceptions.TournamentException;

public interface MatchPlayable {
    void startMatch() throws TournamentException;
}
