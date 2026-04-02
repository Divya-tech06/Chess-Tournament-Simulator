package chess.tournament;

import chess.exceptions.TournamentException;
import chess.matches.Match;
import chess.matches.MatchRunnable;
import chess.players.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Tournament {
    private final SwissPairing swissPairing;
    private final List<Player> players;
    private final List<Match> currentPairings;
    private final List<Match> completedMatches;
    private String name;
    private int totalRounds;
    private int currentRound;
    private int nextMatchNumber;
    private boolean started;

    public Tournament(List<Player> players) {
        this.players = players;
        this.currentPairings = new ArrayList<>();
        this.completedMatches = new ArrayList<>();
        this.swissPairing = new SwissPairing();
        this.nextMatchNumber = 1;
    }

    public void startTournament(String name, int totalRounds) throws TournamentException {
        if (players.size() < 2) {
            throw new TournamentException("At least two players are required.");
        }
        if (totalRounds <= 0) {
            throw new TournamentException("Round count must be positive.");
        }
        this.name = name;
        this.totalRounds = totalRounds;
        this.currentRound = 0;
        this.nextMatchNumber = 1;
        this.currentPairings.clear();
        this.completedMatches.clear();
        for (Player player : players) {
            player.resetTournamentData();
        }
        started = true;
    }

    public String getName() {
        return name;
    }

    public boolean hasStarted() {
        return started;
    }

    public boolean hasPendingRound() {
        return !currentPairings.isEmpty();
    }

    public boolean isFinished() {
        return started && currentRound >= totalRounds && currentPairings.isEmpty();
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Match> getCurrentPairings() {
        return currentPairings;
    }

    public List<Match> getCompletedMatches() {
        return completedMatches;
    }

    public void generateNextRound() throws TournamentException {
        if (!started) {
            throw new TournamentException("Start the tournament first.");
        }
        if (hasPendingRound()) {
            throw new TournamentException("Run the current round before generating a new one.");
        }
        if (currentRound >= totalRounds) {
            throw new TournamentException("All rounds are already completed.");
        }
        currentRound++;
        currentPairings.clear();
        currentPairings.addAll(swissPairing.generatePairings(players, currentRound, nextMatchNumber));
        nextMatchNumber += currentPairings.size();
    }

    public void runCurrentRound() throws TournamentException {
        if (currentPairings.isEmpty()) {
            throw new TournamentException("Generate pairings first.");
        }
        List<Thread> threads = new ArrayList<>();
        List<MatchRunnable> tasks = new ArrayList<>();
        for (Match match : currentPairings) {
            MatchRunnable task = new MatchRunnable(match);
            Thread thread = new Thread(task, match.getMatchId());
            tasks.add(task);
            threads.add(thread);
            thread.start();
        }
        for (int index = 0; index < threads.size(); index++) {
            try {
                threads.get(index).join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new TournamentException("Round execution interrupted.", exception);
            }
            if (tasks.get(index).getError() != null) {
                throw tasks.get(index).getError();
            }
        }
        completedMatches.addAll(currentPairings);
        currentPairings.clear();
    }

    public String standingsText() {
        List<Player> ranking = new ArrayList<>(players);
        ranking.sort(Comparator.comparingDouble(Player::getScore).reversed()
                .thenComparing(Comparator.comparingInt(Player::getWins).reversed())
                .thenComparing(Comparator.comparingInt(Player::getRating).reversed())
                .thenComparing(Player::getName));
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-5s %-20s %-8s %-8s %-8s %-8s %-8s%n",
                "Rank", "Player", "Rating", "Score", "Wins", "Draws", "Losses"));
        int rank = 1;
        for (Player player : ranking) {
            builder.append(String.format("%-5d %-20s %-8d %-8.1f %-8d %-8d %-8d%n",
                    rank++,
                    player.getName(),
                    player.getRating(),
                    player.getScore(),
                    player.getWins(),
                    player.getDraws(),
                    player.getLosses()));
        }
        return builder.toString();
    }

    public String currentPairingsText() {
        if (currentPairings.isEmpty()) {
            return "No pairings generated.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-8s %-18s %-18s %-18s%n", "Match", "White", "Black", "Result"));
        for (Match match : currentPairings) {
            builder.append(match.getDisplayLine()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    public String resultsText() {
        if (completedMatches.isEmpty()) {
            return "No results available.";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%-8s %-8s %-18s %-18s %-18s%n", "Match", "Round", "White", "Black", "Result"));
        for (Match match : completedMatches) {
            String blackName = match.getBlackPlayer() == null ? "BYE" : match.getBlackPlayer().getName();
            builder.append(String.format("%-8s %-8d %-18s %-18s %-18s%n",
                    match.getMatchId(),
                    match.getRoundNumber(),
                    match.getWhitePlayer().getName(),
                    blackName,
                    match.getResult()));
        }
        return builder.toString();
    }
}
