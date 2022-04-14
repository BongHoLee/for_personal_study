import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }


    @Test
    public void canRoll() {
        game.roll(0);
    }

    private void rollMany(int frames, int pins) {
        for (int i=0; i < frames; i++) {
            game.roll(pins);
        }
    }

    @Test
    public void gutterGame() {
        int frames = 20;
        int pins = 0;
        rollMany(frames, pins);
        assertThat(game.getScore()).isEqualTo(0);
    }

    @Test
    public void allOnes() {
        int frames = 20;
        int pins = 1;
        rollMany(frames, pins);
        assertThat(game.getScore()).isEqualTo(frames);
    }

    private void rollSpare() {
        game.roll(5);
        game.roll(5);
    }

    @Test
    public void oneSpare() {
        rollSpare();
        game.roll(3);
        rollMany(17, 0);
        assertThat(game.getScore()).isEqualTo(16);
    }

    @Test
    public void oneStrike() {
        game.roll(10);
        game.roll(5);
        game.roll(3);
        rollMany(16, 0);
        assertThat(game.getScore()).isEqualTo(26);
    }

    @Test
    public void perfectGame() {
        rollMany(10, 10);
        assertThat(game.getScore()).isEqualTo(300);
    }

}
