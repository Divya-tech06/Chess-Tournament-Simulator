package chess.matches;

public enum MatchResult {
    WHITE_WIN("1-0"),
    BLACK_WIN("0-1"),
    DRAW("1/2-1/2"),
    BYE("1-bye"),
    PENDING("Pending");

    private final String notation;

    MatchResult(String notation) {
        this.notation = notation;
    }

    public String getNotation() {
        return notation;
    }
}
