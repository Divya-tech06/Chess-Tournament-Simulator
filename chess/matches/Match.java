package chess.matches;

import chess.exceptions.TournamentException;
import chess.players.Player;

import java.util.Random;

public abstract class Match implements MatchPlayable {
    private static final Object RESULT_LOCK = new Object();
    private static final Random RANDOM = new Random();

    private final String matchId;
    private final int roundNumber;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private final String timeControl;
    private MatchResult result;
    private boolean finished;

    protected Match(String matchId, int roundNumber, Player whitePlayer, Player blackPlayer, String timeControl) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.timeControl = timeControl;
        this.result = MatchResult.PENDING;
    }

    public String getMatchId() {
        return matchId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public String getTimeControl() {
        return timeControl;
    }

    public MatchResult getResult() {
        return result;
    }

    public boolean isFinished() {
        return finished;
    }

    protected void setResult(MatchResult result) {
        this.result = result;
        this.finished = result != MatchResult.PENDING;
    }

    public void restoreResult(MatchResult restoredResult) {
        setResult(restoredResult);
    }

    protected int simulatedDelay() {
        return 500 + RANDOM.nextInt(800);
    }

    protected MatchResult decideResultFromRatings() {
        if (blackPlayer == null) {
            return MatchResult.BYE;
        }
        double whiteExpected = 1.0 / (1.0 + Math.pow(10.0, (blackPlayer.getRating() - whitePlayer.getRating()) / 400.0));
        double drawChance = 0.18 + Math.max(0.0, 0.10 - Math.abs(whitePlayer.getRating() - blackPlayer.getRating()) / 2000.0);
        double roll = RANDOM.nextDouble();
        if (roll < drawChance) {
            return MatchResult.DRAW;
        }
        double remaining = 1.0 - drawChance;
        double whiteWinMark = drawChance + whiteExpected * remaining;
        if (roll < whiteWinMark) {
            return MatchResult.WHITE_WIN;
        }
        return MatchResult.BLACK_WIN;
    }

    @Override
    public void recordResult() throws TournamentException {
        synchronized (RESULT_LOCK) {
            if (finished) {
                return;
            }
            MatchResult finalResult = decideResultFromRatings();
            if (finalResult == MatchResult.BYE) {
                whitePlayer.recordBye("Round " + roundNumber + " bye");
                setResult(MatchResult.BYE);
                return;
            }
            whitePlayer.addOpponent(blackPlayer.getId());
            blackPlayer.addOpponent(whitePlayer.getId());
            if (finalResult == MatchResult.WHITE_WIN) {
                whitePlayer.recordWin("Round " + roundNumber + " beat " + blackPlayer.getName());
                blackPlayer.recordLoss("Round " + roundNumber + " lost to " + whitePlayer.getName());
            } else if (finalResult == MatchResult.BLACK_WIN) {
                blackPlayer.recordWin("Round " + roundNumber + " beat " + whitePlayer.getName());
                whitePlayer.recordLoss("Round " + roundNumber + " lost to " + blackPlayer.getName());
            } else if (finalResult == MatchResult.DRAW) {
                whitePlayer.recordDraw("Round " + roundNumber + " drew with " + blackPlayer.getName());
                blackPlayer.recordDraw("Round " + roundNumber + " drew with " + whitePlayer.getName());
            } else {
                throw new TournamentException("Unable to record match result.");
            }
            setResult(finalResult);
        }
    }

    public String getDisplayLine() {
        if (blackPlayer == null) {
            return String.format("%-8s %-18s %-18s %-10s", matchId, whitePlayer.getName(), "BYE", result.getNotation());
        }
        return String.format("%-8s %-18s %-18s %-10s", matchId, whitePlayer.getName(), blackPlayer.getName(), result.getNotation());
    }

    public String toDataString() {
        String blackId = blackPlayer == null ? "BYE" : blackPlayer.getId();
        return String.join("|",
                matchId,
                String.valueOf(roundNumber),
                getClass().getSimpleName(),
                whitePlayer.getId(),
                blackId,
                result.name(),
                timeControl);
    }
}
