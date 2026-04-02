package chess.players;

import java.util.ArrayList;
import java.util.List;

public class Player extends Person {
    private int rating;
    private double score;
    private int wins;
    private int losses;
    private int draws;
    private boolean byeReceived;
    private final List<String> opponents;

    public Player(String id, String name, int rating) {
        super(id, name);
        this.rating = rating;
        this.opponents = new ArrayList<>();
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public double getScore() {
        return score;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public boolean hasReceivedBye() {
        return byeReceived;
    }

    public void setByeReceived(boolean byeReceived) {
        this.byeReceived = byeReceived;
    }

    public List<String> getOpponents() {
        return opponents;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public void addOpponent(String opponentId) {
        if (opponentId != null && !opponentId.isBlank() && !opponents.contains(opponentId)) {
            opponents.add(opponentId);
        }
    }

    public boolean hasPlayed(String opponentId) {
        return opponents.contains(opponentId);
    }

    public void recordWin() {
        wins++;
        score += 1.0;
    }

    public void recordLoss() {
        losses++;
    }

    public void recordDraw() {
        draws++;
        score += 0.5;
    }

    public void recordBye() {
        byeReceived = true;
        wins++;
        score += 1.0;
    }

    public void resetTournamentData() {
        score = 0.0;
        wins = 0;
        losses = 0;
        draws = 0;
        byeReceived = false;
        opponents.clear();
    }

    public String toDataString() {
        return String.join("|",
                getId(),
                getName(),
                String.valueOf(rating),
                String.valueOf(score),
                String.valueOf(wins),
                String.valueOf(losses),
                String.valueOf(draws),
                String.valueOf(byeReceived),
                String.join(",", opponents));
    }

    public static Player fromDataString(String line) {
        String[] parts = line.split("\\|", -1);
        Player player = new Player(parts[0], parts[1], Integer.parseInt(parts[2]));
        player.score = Double.parseDouble(parts[3]);
        player.wins = Integer.parseInt(parts[4]);
        player.losses = Integer.parseInt(parts[5]);
        player.draws = Integer.parseInt(parts[6]);
        int byeIndex = parts.length > 8 ? 8 : 7;
        int opponentsIndex = parts.length > 9 ? 9 : 8;
        player.byeReceived = Boolean.parseBoolean(parts[byeIndex]);
        if (parts.length > opponentsIndex && !parts[opponentsIndex].isBlank()) {
            String[] rivalIds = parts[opponentsIndex].split(",");
            for (String rivalId : rivalIds) {
                player.addOpponent(rivalId);
            }
        }
        return player;
    }
}
