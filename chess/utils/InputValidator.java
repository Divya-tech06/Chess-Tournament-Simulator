package chess.utils;

public class InputValidator {
    public static boolean isValidName(String value) {
        return value != null && !value.isBlank() && value.length() <= 40;
    }

    public static boolean isValidRating(int value) {
        return value >= 100 && value <= 3500;
    }

    public static boolean isTournamentReady(int playerCount) {
        return playerCount >= 8 && playerCount <= 32;
    }

    public static int suggestedRounds(int playerCount) {
        int rounds = 1;
        int slots = 1;
        while (slots < playerCount) {
            slots *= 2;
            rounds++;
        }
        return Math.max(3, rounds - 1);
    }
}
