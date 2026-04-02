package chess.tournament;

import chess.matches.Match;

import java.util.ArrayList;
import java.util.List;

public class Round {
    private final int roundNumber;
    private final List<Match> matches;

    public Round(int roundNumber) {
        this.roundNumber = roundNumber;
        this.matches = new ArrayList<>();
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void addMatch(Match match) {
        matches.add(match);
    }
}
