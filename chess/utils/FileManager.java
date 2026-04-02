package chess.utils;

import chess.exceptions.TournamentException;
import chess.matches.Match;
import chess.players.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private final Path dataFolder;
    private final Path playersFile;
    private final Path matchesFile;
    private final Path standingsFile;

    public FileManager(String folderName) {
        dataFolder = Path.of(folderName);
        playersFile = dataFolder.resolve("players.txt");
        matchesFile = dataFolder.resolve("matches.txt");
        standingsFile = dataFolder.resolve("standings.txt");
    }

    public void prepareFiles() throws TournamentException {
        try {
            Files.createDirectories(dataFolder);
            if (Files.notExists(playersFile)) {
                Files.createFile(playersFile);
            }
            if (Files.notExists(matchesFile)) {
                Files.createFile(matchesFile);
            }
            if (Files.notExists(standingsFile)) {
                Files.createFile(standingsFile);
            }
        } catch (IOException exception) {
            throw new TournamentException("Could not prepare data files.", exception);
        }
    }

    public List<Player> loadPlayers() throws TournamentException {
        prepareFiles();
        try {
            List<Player> players = new ArrayList<>();
            for (String line : Files.readAllLines(playersFile, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    players.add(Player.fromDataString(line));
                }
            }
            return players;
        } catch (IOException exception) {
            throw new TournamentException("Could not load players.", exception);
        }
    }

    public void savePlayers(List<Player> players) throws TournamentException {
        List<String> lines = new ArrayList<>();
        for (Player player : players) {
            lines.add(player.toDataString());
        }
        write(playersFile, lines, "Could not save players.");
    }

    public void saveMatches(List<Match> matches) throws TournamentException {
        List<String> lines = new ArrayList<>();
        for (Match match : matches) {
            lines.add(match.toDataString());
        }
        write(matchesFile, lines, "Could not save matches.");
    }

    public void saveStandings(String standings) throws TournamentException {
        write(standingsFile, List.of(standings), "Could not save standings.");
    }

    private void write(Path file, List<String> lines, String message) throws TournamentException {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new TournamentException(message, exception);
        }
    }
}
