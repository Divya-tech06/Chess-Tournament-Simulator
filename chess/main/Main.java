package chess.main;

import chess.exceptions.InvalidPlayerException;
import chess.exceptions.TournamentException;
import chess.players.Player;
import chess.players.PlayerManager;
import chess.tournament.Round;
import chess.tournament.Tournament;
import chess.utils.FileManager;
import chess.utils.IdGenerator;
import chess.utils.InputValidator;

import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner scanner;
    private final PlayerManager playerManager;
    private final FileManager fileManager;
    private Tournament tournament;

    public Main() {
        this.scanner = new Scanner(System.in);
        this.playerManager = new PlayerManager();
        this.fileManager = new FileManager("data");
    }

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        try {
            fileManager.ensureDataFiles();
            restoreSavedState();
            boolean exit = false;
            while (!exit) {
                printMenu();
                int choice = readInt("Choose an option: ");
                switch (choice) {
                    case 1 -> registerPlayer();
                    case 2 -> createTournament();
                    case 3 -> startNextRound();
                    case 4 -> viewStandings();
                    case 5 -> viewMatchHistory();
                    case 6 -> saveTournament();
                    case 7 -> exit = true;
                    default -> System.out.println("Please choose a valid menu option.");
                }
            }
            System.out.println("Program closed.");
        } catch (TournamentException exception) {
            System.out.println("Startup error: " + exception.getMessage());
        }
    }

    private void restoreSavedState() {
        try {
            List<Player> loadedPlayers = fileManager.loadPlayers();
            if (loadedPlayers.isEmpty()) {
                System.out.println("No players found in data file.");
                return;
            }
            playerManager.replaceAll(loadedPlayers);
            IdGenerator.syncPlayerCounter(loadedPlayers);
            System.out.println("Loaded " + loadedPlayers.size() + " players from file.");
            Tournament savedTournament = fileManager.loadSavedTournament(playerManager.getPlayers());
            if (savedTournament != null) {
                tournament = savedTournament;
                System.out.println("Restored saved tournament: " + tournament.getName());
            }
        } catch (TournamentException exception) {
            System.out.println("Saved tournament could not be restored.");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Swiss-System Chess Tournament Simulator");
        System.out.println("Players loaded: " + playerManager.getPlayerCount());
        System.out.println("1. Register new player");
        System.out.println("2. Create tournament");
        System.out.println("3. Start next round");
        System.out.println("4. View standings");
        System.out.println("5. View match history");
        System.out.println("6. Save tournament");
        System.out.println("7. Exit");
    }

    private void registerPlayer() {
        if (isTournamentInProgress()) {
            System.out.println("Cannot register new players after the tournament has started.");
            return;
        }
        String name = readText("Player name: ");
        if (!InputValidator.isValidName(name)) {
            System.out.println("Invalid name.");
            return;
        }
        int rating = readInt("Rating: ");
        if (!InputValidator.isValidRating(rating)) {
            System.out.println("Rating must be between 100 and 3500.");
            return;
        }
        Player player = new Player(IdGenerator.nextPlayerId(), name, rating);
        try {
            playerManager.registerPlayer(player);
            fileManager.savePlayers(playerManager.getPlayers());
            resetTournamentIfRosterChanged();
            System.out.println("Registered " + player.getName() + " with id " + player.getId());
        } catch (InvalidPlayerException | TournamentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void createTournament() {
        if (!InputValidator.isTournamentReady(playerManager.getPlayerCount())) {
            System.out.println("You need between 8 and 32 players to create a tournament.");
            return;
        }
        String name = readText("Tournament name: ");
        int suggested = InputValidator.suggestedRounds(playerManager.getPlayerCount());
        int totalRounds = readInt("Number of rounds [" + suggested + " suggested]: ");
        if (totalRounds <= 0) {
            System.out.println("Invalid round count.");
            return;
        }
        tournament = new Tournament(name, totalRounds, playerManager.getPlayers());
        System.out.println("Tournament created: " + tournament.getName());
    }

    private void startNextRound() {
        if (tournament == null) {
            System.out.println("Create a tournament first.");
            return;
        }
        if (tournament.isFinished()) {
            System.out.println("All rounds are already completed.");
            return;
        }
        try {
            Round round = tournament.pairNextRound();
            System.out.println("Pairings for round " + round.getRoundNumber());
            System.out.printf("%-8s %-18s %-18s %-10s%n", "Match", "White", "Black", "Result");
            round.getMatches().forEach(match -> System.out.println(match.getDisplayLine()));
            System.out.println("Simulating matches...");
            tournament.simulateLatestRound();
            System.out.println("Round " + round.getRoundNumber() + " finished.");
            System.out.println(tournament.standingsTable());
            fileManager.saveTournament(tournament);
            if (tournament.isFinished()) {
                System.out.println("Tournament completed.");
            }
        } catch (TournamentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void viewStandings() {
        if (tournament == null) {
            System.out.println("Create a tournament first.");
            return;
        }
        System.out.println(tournament.standingsTable());
    }

    private void viewMatchHistory() {
        if (tournament == null) {
            System.out.println("Create a tournament first.");
            return;
        }
        System.out.println(tournament.matchHistory());
    }

    private void saveTournament() {
        try {
            fileManager.savePlayers(playerManager.getPlayers());
            if (tournament == null) {
                fileManager.clearTournamentFiles();
                System.out.println("Player data saved.");
                return;
            }
            fileManager.saveTournament(tournament);
            System.out.println("Tournament data saved.");
        } catch (TournamentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private boolean isTournamentInProgress() {
        return tournament != null && tournament.getRoundsCompleted() > 0;
    }

    private void resetTournamentIfRosterChanged() throws TournamentException {
        if (tournament != null && tournament.getRoundsCompleted() == 0) {
            tournament = null;
            fileManager.clearTournamentFiles();
            System.out.println("Tournament setup cleared because the player list changed. Create the tournament again.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
