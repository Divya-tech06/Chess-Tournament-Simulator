package chess.utils;

import chess.exceptions.TournamentException;
import chess.matches.Match;
import chess.matches.MatchResult;
import chess.matches.RapidMatch;
import chess.matches.StandardMatch;
import chess.players.Player;
import chess.tournament.Round;
import chess.tournament.Tournament;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager {
    private final Path dataDirectory;
    private final Path playersFile;
    private final Path matchesFile;
    private final Path standingsFile;
    private final Path tournamentFile;

    public FileManager(String dataDirectory) {
        this.dataDirectory = Path.of(dataDirectory);
        this.playersFile = this.dataDirectory.resolve("players.txt");
        this.matchesFile = this.dataDirectory.resolve("matches.txt");
        this.standingsFile = this.dataDirectory.resolve("standings.txt");
        this.tournamentFile = this.dataDirectory.resolve("tournament.txt");
    }

    public void ensureDataFiles() throws TournamentException {
        try {
            Files.createDirectories(dataDirectory);
            createIfMissing(playersFile);
            createIfMissing(matchesFile);
            createIfMissing(standingsFile);
            createIfMissing(tournamentFile);
        } catch (IOException exception) {
            throw new TournamentException("Could not prepare data files.");
        }
    }

    public void savePlayers(List<Player> players) throws TournamentException {
        List<String> lines = new ArrayList<>();
        for (Player player : players) {
            lines.add(player.toDataString());
        }
        writeLines(playersFile, lines);
    }

    public List<Player> loadPlayers() throws TournamentException {
        try {
            ensureDataFiles();
            List<String> lines = Files.readAllLines(playersFile, StandardCharsets.UTF_8);
            List<Player> players = new ArrayList<>();
            for (String line : lines) {
                if (!line.isBlank()) {
                    players.add(Player.fromDataString(line));
                }
            }
            return players;
        } catch (IOException exception) {
            throw new TournamentException("Could not load players from file.");
        }
    }

    public void saveTournament(Tournament tournament) throws TournamentException {
        ensureDataFiles();
        savePlayers(tournament.getPlayers());
        saveMatches(tournament);
        saveStandings(tournament);
        List<String> meta = List.of(
                tournament.getName(),
                String.valueOf(tournament.getTotalRounds()),
                String.valueOf(tournament.getRoundsCompleted()));
        writeLines(tournamentFile, meta);
    }

    public Tournament loadSavedTournament(List<Player> players) throws TournamentException {
        try {
            ensureDataFiles();
            List<String> meta = Files.readAllLines(tournamentFile, StandardCharsets.UTF_8);
            if (meta.size() < 3 || meta.get(0).isBlank()) {
                return null;
            }
            String name = meta.get(0);
            int totalRounds = Integer.parseInt(meta.get(1));
            int roundsCompleted = Integer.parseInt(meta.get(2));
            Tournament tournament = new Tournament(name, totalRounds, players);
            List<Round> savedRounds = loadRounds(players);
            tournament.restoreRounds(savedRounds, roundsCompleted);
            return tournament;
        } catch (IOException | NumberFormatException exception) {
            throw new TournamentException("Could not load saved tournament data.");
        }
    }

    public void saveMatches(Tournament tournament) throws TournamentException {
        List<String> lines = new ArrayList<>();
        for (Round round : tournament.getRounds()) {
            for (Match match : round.getMatches()) {
                lines.add(match.toDataString());
            }
        }
        writeLines(matchesFile, lines);
    }

    public void saveStandings(Tournament tournament) throws TournamentException {
        writeLines(standingsFile, List.of(tournament.standingsTable()));
    }

    public void clearTournamentFiles() throws TournamentException {
        ensureDataFiles();
        writeLines(matchesFile, List.of());
        writeLines(standingsFile, List.of());
        writeLines(tournamentFile, List.of());
    }

    private List<Round> loadRounds(List<Player> players) throws IOException, TournamentException {
        Map<String, Player> playerMap = new HashMap<>();
        for (Player player : players) {
            playerMap.put(player.getId(), player);
        }
        Map<Integer, Round> rounds = new HashMap<>();
        List<String> lines = Files.readAllLines(matchesFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\\|", -1);
            String matchId = parts[0];
            int roundNumber = Integer.parseInt(parts[1]);
            String matchType = parts[2];
            Player white = playerMap.get(parts[3]);
            Player black = "BYE".equals(parts[4]) ? null : playerMap.get(parts[4]);
            Match match = "RapidMatch".equals(matchType)
                    ? new RapidMatch(matchId, roundNumber, white, black)
                    : new StandardMatch(matchId, roundNumber, white, black);
            match.restoreResult(MatchResult.valueOf(parts[5]));
            Round round = rounds.computeIfAbsent(roundNumber, Round::new);
            round.addMatch(match);
        }
        List<Round> ordered = new ArrayList<>();
        for (int roundNumber = 1; roundNumber <= rounds.size(); roundNumber++) {
            if (rounds.containsKey(roundNumber)) {
                ordered.add(rounds.get(roundNumber));
            }
        }
        return ordered;
    }

    private void createIfMissing(Path file) throws IOException {
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
    }

    private void writeLines(Path file, List<String> lines) throws TournamentException {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new TournamentException("Could not write file: " + file.getFileName());
        }
    }
}
