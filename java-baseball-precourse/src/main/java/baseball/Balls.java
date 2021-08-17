package baseball;

import java.util.ArrayList;
import java.util.List;

public class Balls {
    private final List<Ball> myAnswers = new ArrayList<>();

    public Balls(List<Integer> myAnswers) {
        mapping(myAnswers);
    }

    private void mapping(List<Integer> answers) {
        for (int i = 0; i < 3; i++) {
            this.myAnswers.add(new Ball(i+1, answers.get(i)));
        }
    }

    public BallStatus play(Ball userBall) {
        return myAnswers.stream()
                .map(answer -> answer.play(userBall))
                .filter(BallStatus::isNotNothing)
                .findFirst()
                .orElse(BallStatus.NOTHING);
    }

    public PlayResult play(List<Integer> balls) {
        Balls userBalls = new Balls(balls);
        PlayResult result = new PlayResult();
        for (Ball answer : myAnswers) {
            BallStatus status = userBalls.play(answer);
            result.report(status);
        }
        return result;
    }
}
