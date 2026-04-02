package chess.exceptions;

public class TournamentException extends Exception {
    public TournamentException(String message) {
        super(message);
    }

    public TournamentException(String message, Throwable cause) {
        super(message, cause);
    }
}
