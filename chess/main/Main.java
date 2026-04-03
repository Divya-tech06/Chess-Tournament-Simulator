package chess.main;

import chess.exceptions.TournamentException;
import chess.players.Player;
import chess.tournament.Tournament;
import chess.utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final Scanner scanner;
    private final FileManager fileManager;
    private final List<Player> players;
    private final Tournament tournament;

    public Main() throws TournamentException {
        scanner = new Scanner(System.in);
        fileManager = new FileManager("data");
        fileManager.prepareFiles();
        players = new ArrayList<>(fileManager.loadPlayers());
        tournament = new Tournament(players);
    }

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (TournamentException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void run() {
        System.out.println("Loaded " + players.size() + " players from file.");
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1 -> addPlayer();
                    case 2 -> printPlayers();
                    case 3 -> startTournament();
                    case 4 -> generatePairings();
                    case 5 -> runMatches();
                    case 6 -> viewStandings();
                    case 7 -> saveData();
                    case 8 -> running = false;
                    default -> System.out.println("Enter a valid option.");
                }
            } catch (TournamentException exception) {
                System.out.println(exception.getMessage());
            }
        }
        System.out.println("Program closed.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Round-Robin Chess Tournament Simulator");
        System.out.println("Players: " + players.size());
        System.out.println("1. Add player");
        System.out.println("2. Print players");
        System.out.println("3. Start tournament");
        System.out.println("4. Generate pairings");
        System.out.println("5. Run matches");
        System.out.println("6. View standings");
        System.out.println("7. Save data");
        System.out.println("8. Exit");
    }

    private void addPlayer() throws TournamentException {
        if (tournament.hasStarted()) {
            throw new TournamentException("Add players before starting the tournament.");
        }
        String name = readText("Player name: ");
        int rating = readInt("Rating: ");
        if (name.isBlank()) {
            throw new TournamentException("Player name cannot be empty.");
        }
        if (rating < 100 || rating > 3500) {
            throw new TournamentException("Rating must be between 100 and 3500.");
        }
        for (Player player : players) {
            if (player.getName().equalsIgnoreCase(name)) {
                throw new TournamentException("Player already exists.");
            }
        }
        String id = "P" + (players.size() + 1001);
        Player player = new Player(id, name, rating);
        players.add(player);
        fileManager.savePlayers(players);
        System.out.println("Added " + player.getName());
    }

    private void startTournament() throws TournamentException {
        if (players.size() < 2) {
            throw new TournamentException("Add at least two players first.");
        }
        String name = readText("Tournament name: ");
        tournament.startTournament(name);
        System.out.println("Tournament started: " + tournament.getName());
        System.out.println("Total rounds: " + tournament.getTotalRounds());
    }

    private void generatePairings() throws TournamentException {
        tournament.generateNextRound();
        System.out.println("Pairings for round " + tournament.getCurrentRound());
        System.out.println(tournament.currentPairingsText());
    }

    private void runMatches() throws TournamentException {
        tournament.runCurrentRound();
        System.out.println("Round completed.");
        System.out.println(tournament.resultsText());
        System.out.println(tournament.standingsText());
        saveData();
        if (tournament.isFinished()) {
            System.out.println("Tournament finished.");
        }
    }

    private void viewStandings() {
        System.out.println(tournament.standingsText());
    }

    private void printPlayers() {
        if (players.isEmpty()) {
            System.out.println("No players added.");
            return;
        }
        System.out.printf("%-8s %-20s %-8s%n", "ID", "Name", "Rating");
        for (Player player : players) {
            System.out.printf("%-8s %-20s %-8d%n",
                    player.getId(),
                    player.getName(),
                    player.getRating());
        }
    }

    private void saveData() throws TournamentException {
        fileManager.savePlayers(players);
        fileManager.saveMatches(tournament.getCompletedMatches());
        fileManager.saveStandings(tournament.standingsText());
        System.out.println("Data saved.");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
