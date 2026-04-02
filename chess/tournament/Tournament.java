package chess.tournament;

import chess.exceptions.TournamentException;
import chess.matches.Match;
import chess.matches.MatchPlayable;
import chess.matches.MatchThread;
import chess.players.Player;

import java.util.ArrayList;
import java.util.List;

public class Tournament {
    private final String name;
    private final int totalRounds;
    private int roundsCompleted;
    private final List<Player> players;
    private final List<Round> rounds;
    private final Scheduler scheduler;
    private final Standings standings;

    public Tournament(String name, int totalRounds, List<Player> players) {
        this.name = name;
        this.totalRounds = totalRounds;
        this.players = players;
        this.rounds = new ArrayList<>();
        this.scheduler = new Scheduler();
        this.standings = new Standings();
    }

    public String getName() {
        return name;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public int getRoundsCompleted() {
        return roundsCompleted;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void restoreRounds(List<Round> savedRounds, int savedRoundsCompleted) {
        rounds.clear();
        rounds.addAll(savedRounds);
        this.roundsCompleted = savedRoundsCompleted;
    }

    public boolean isFinished() {
        return roundsCompleted >= totalRounds;
    }

    public Round pairNextRound() throws TournamentException {
        if (isFinished()) {
            throw new TournamentException("All rounds are already completed.");
        }
        standings.refreshTieBreaks(players);
        Round round = scheduler.createRound(roundsCompleted + 1, players);
        rounds.add(round);
        return round;
    }

    public void simulateLatestRound() throws TournamentException {
        if (rounds.isEmpty()) {
            throw new TournamentException("No round is scheduled.");
        }
        Round current = rounds.get(rounds.size() - 1);
        if (current.getRoundNumber() <= roundsCompleted) {
            throw new TournamentException("The latest round has already been played.");
        }
        List<MatchThread> threads = new ArrayList<>();
        int board = 1;
        for (Match match : current.getMatches()) {
            MatchPlayable playable = match;
            MatchThread thread = new MatchThread(playable, "Round-" + current.getRoundNumber() + "-Board-" + board++);
            threads.add(thread);
            thread.start();
        }
        for (MatchThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new TournamentException("Round simulation interrupted.");
            }
            if (thread.getFailure() != null) {
                throw thread.getFailure();
            }
        }
        roundsCompleted = current.getRoundNumber();
        standings.refreshTieBreaks(players);
    }

    public String standingsTable() {
        standings.refreshTieBreaks(players);
        return standings.formatTable(players);
    }

    public String matchHistory() {
        StringBuilder builder = new StringBuilder();
        for (Round round : rounds) {
            builder.append("Round ").append(round.getRoundNumber()).append(System.lineSeparator());
            builder.append(String.format("%-8s %-18s %-18s %-10s%n", "Match", "White", "Black", "Result"));
            for (Match match : round.getMatches()) {
                builder.append(match.getDisplayLine()).append(System.lineSeparator());
            }
            builder.append(System.lineSeparator());
        }
        if (builder.length() == 0) {
            return "No matches have been played yet.";
        }
        return builder.toString();
    }
}
