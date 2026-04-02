package chess.players;

import chess.exceptions.InvalidPlayerException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PlayerManager {
    private final List<Player> players;

    public PlayerManager() {
        this.players = new ArrayList<>();
    }

    public synchronized void registerPlayer(Player player) throws InvalidPlayerException {
        validatePlayer(player);
        boolean duplicateId = players.stream().anyMatch(existing -> existing.getId().equalsIgnoreCase(player.getId()));
        boolean duplicateName = players.stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(player.getName()));
        if (duplicateId || duplicateName) {
            throw new InvalidPlayerException("Player already exists.");
        }
        players.add(player);
    }

    public synchronized void replaceAll(List<Player> loadedPlayers) {
        players.clear();
        players.addAll(loadedPlayers);
    }

    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public synchronized int getPlayerCount() {
        return players.size();
    }

    public synchronized Optional<Player> findById(String playerId) {
        return players.stream().filter(player -> player.getId().equalsIgnoreCase(playerId)).findFirst();
    }

    public synchronized List<Player> sortedByRating() {
        List<Player> copy = new ArrayList<>(players);
        copy.sort(Comparator.comparingInt(Player::getRating).reversed().thenComparing(Player::getName));
        return copy;
    }

    private void validatePlayer(Player player) throws InvalidPlayerException {
        if (player.getName() == null || player.getName().isBlank()) {
            throw new InvalidPlayerException("Player name cannot be empty.");
        }
        if (player.getRating() < 100 || player.getRating() > 3500) {
            throw new InvalidPlayerException("Rating must be between 100 and 3500.");
        }
    }
}
