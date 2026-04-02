package chess.matches;

import chess.exceptions.TournamentException;
import chess.players.Player;

import java.util.Random;

public class Match implements MatchPlayable {
    private static final Random RANDOM = new Random();

    private final String matchId;
    private final int roundNumber;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private String result;

    public Match(String matchId, int roundNumber, Player whitePlayer, Player blackPlayer) {
        this.matchId = matchId;
        this.roundNumber = roundNumber;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.result = "Pending";
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    private int simulatedDelay() {
        return 500 + RANDOM.nextInt(800);
    }

    private String decideResultFromRatings() {
        if (blackPlayer == null) {
            return "Bye";
        }
        double whiteExpected = 1.0 / (1.0 + Math.pow(10.0, (blackPlayer.getRating() - whitePlayer.getRating()) / 400.0));
        double drawChance = 0.18 + Math.max(0.0, 0.10 - Math.abs(whitePlayer.getRating() - blackPlayer.getRating()) / 2000.0);
        double roll = RANDOM.nextDouble();
        if (roll < drawChance) {
            return "Draw";
        }
        double remaining = 1.0 - drawChance;
        double whiteWinMark = drawChance + whiteExpected * remaining;
        if (roll < whiteWinMark) {
            return whitePlayer.getName() + " won";
        }
        return blackPlayer.getName() + " won";
    }

    @Override
    public void startMatch() throws TournamentException {
        try {
            Thread.sleep(simulatedDelay());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TournamentException("Match interrupted.");
        }
        applyResult();
    }

    private void applyResult() {
        String finalResult = decideResultFromRatings();
        result = finalResult;
        if (blackPlayer == null) {
            whitePlayer.recordBye();
            return;
        }
        whitePlayer.addOpponent(blackPlayer.getId());
        blackPlayer.addOpponent(whitePlayer.getId());
        if ("Draw".equals(finalResult)) {
            whitePlayer.recordDraw();
            blackPlayer.recordDraw();
        } else if ((whitePlayer.getName() + " won").equals(finalResult)) {
            whitePlayer.recordWin();
            blackPlayer.recordLoss();
        } else {
            blackPlayer.recordWin();
            whitePlayer.recordLoss();
        }
    }

    public String getDisplayLine() {
        if (blackPlayer == null) {
            return String.format("%-8s %-18s %-18s %-18s", matchId, whitePlayer.getName(), "BYE", result);
        }
        return String.format("%-8s %-18s %-18s %-18s", matchId, whitePlayer.getName(), blackPlayer.getName(), result);
    }

    public String toDataString() {
        String blackId = blackPlayer == null ? "BYE" : blackPlayer.getId();
        return String.join("|",
                matchId,
                String.valueOf(roundNumber),
                whitePlayer.getId(),
                blackId,
                result);
    }
}
