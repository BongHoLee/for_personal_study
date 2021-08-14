public class Game {

    private int currentRoll = 0;
    private final int[] rolls = new int[21];

    public void roll(int pins) {
        rolls[currentRoll++] = pins;
    }

    public int getScore() {
        int score = 0;
        int firstRoll = 0;
        for (int frame = 0; frame < 10; frame++) {
            if(isSpare(firstRoll)) {
                score += 10 + nextBallForSpare(firstRoll);
                firstRoll += 2;
            } else if (isStrike(firstRoll)) { // strike
                score += 10 + nextBallForStrike(firstRoll);
                firstRoll += 1;
            } else {
                score += nextBallForFrame(firstRoll);
                firstRoll += 2;
            }
        }
        return score;
    }

    private int nextBallForFrame(int firstRoll) {
        return rolls[firstRoll] + rolls[firstRoll + 1];
    }

    private int nextBallForSpare(int firstRoll) {
        return rolls[firstRoll + 2];
    }

    private int nextBallForStrike(int firstRoll) {
        return rolls[firstRoll + 1] + rolls[firstRoll + 2];
    }

    private boolean isStrike(int firstRoll) {
        return rolls[firstRoll] == 10;
    }

    private boolean isSpare(int eachRoll) {
        return rolls[eachRoll] + rolls[eachRoll + 1] == 10;
    }
}
