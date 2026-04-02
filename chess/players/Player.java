package chess.players;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Person {
    private int rating;
    private double score;
    private int wins;
    private int losses;
    private int draws;
    private double buchholz;
    private boolean byeReceived;
    private final Set<String> opponents;
    private final List<String> resultHistory;

    public Player(String id, String name, int rating) {
        super(id, name);
        this.rating = rating;
        this.opponents = new HashSet<>();
        this.resultHistory = new ArrayList<>();
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

    public double getBuchholz() {
        return buchholz;
    }

    public void setBuchholz(double buchholz) {
        this.buchholz = buchholz;
    }

    public boolean hasReceivedBye() {
        return byeReceived;
    }

    public Set<String> getOpponents() {
        return opponents;
    }

    public List<String> getResultHistory() {
        return resultHistory;
    }

    public void addOpponent(String opponentId) {
        if (opponentId != null && !opponentId.isBlank()) {
            opponents.add(opponentId);
        }
    }

    public boolean hasPlayed(String opponentId) {
        return opponents.contains(opponentId);
    }

    public void recordWin(String summary) {
        wins++;
        score += 1.0;
        resultHistory.add(summary);
    }

    public void recordLoss(String summary) {
        losses++;
        resultHistory.add(summary);
    }

    public void recordDraw(String summary) {
        draws++;
        score += 0.5;
        resultHistory.add(summary);
    }

    public void recordBye(String summary) {
        byeReceived = true;
        wins++;
        score += 1.0;
        resultHistory.add(summary);
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
                String.valueOf(buchholz),
                String.valueOf(byeReceived),
                String.join(",", opponents),
                String.join("~", resultHistory));
    }

    public static Player fromDataString(String line) {
        String[] parts = line.split("\\|", -1);
        Player player = new Player(parts[0], parts[1], Integer.parseInt(parts[2]));
        player.score = Double.parseDouble(parts[3]);
        player.wins = Integer.parseInt(parts[4]);
        player.losses = Integer.parseInt(parts[5]);
        player.draws = Integer.parseInt(parts[6]);
        player.buchholz = Double.parseDouble(parts[7]);
        player.byeReceived = Boolean.parseBoolean(parts[8]);
        if (!parts[9].isBlank()) {
            String[] rivalIds = parts[9].split(",");
            for (String rivalId : rivalIds) {
                player.opponents.add(rivalId);
            }
        }
        if (parts.length > 10 && !parts[10].isBlank()) {
            String[] history = parts[10].split("~");
            for (String entry : history) {
                player.resultHistory.add(entry);
            }
        }
        return player;
    }
}
