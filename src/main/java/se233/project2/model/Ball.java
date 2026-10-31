package se233.project2.model;

// The ball rests on the paddle until launched, then moves at a constant speed.
public class Ball extends GameObject {

    public static final double RADIUS = 7;
    public static final double DEFAULT_SPEED = 360;
    // Launch direction, measured from straight up towards the right.
    public static final double LAUNCH_ANGLE = Math.toRadians(30);
    public static final double MAX_BOUNCE_ANGLE = Math.toRadians(60);

    private double vx;
    private double vy;
    private final double speed;
    private boolean launched;
    private int pierceLeft;
    // Time since the last bounce; drives the ball's squash animation.
    private double sinceBounce = Double.MAX_VALUE;
    private boolean bouncedVertically;

    public Ball() {
        this(DEFAULT_SPEED);
    }

    public Ball(double speed) {
        super(0, 0, RADIUS * 2, RADIUS * 2);
        this.speed = speed;
    }

    public void followPaddle(Paddle paddle) {
        x = paddle.getCenterX() - RADIUS;
        y = paddle.getY() - height - 1;
    }

    public void launch() {
        launch(LAUNCH_ANGLE);
    }

    // angle is measured from straight up; positive goes right.
    public void launch(double angle) {
        launched = true;
        vx = speed * Math.sin(angle);
        vy = -speed * Math.cos(angle);
    }

    public void move(double dt) {
        moveX(dt);
        moveY(dt);
    }

    // Moving one axis at a time tells Physics which side of a brick the ball hit.
    public void moveX(double dt) {
        x += vx * dt;
    }

    public void moveY(double dt) {
        y += vy * dt;
    }

    public void bounceOffWalls(double left, double top, double right) {
        if (x < left) {
            x = left;
            vx = Math.abs(vx);
            markBounce(false);
        } else if (x + width > right) {
            x = right - width;
            vx = -Math.abs(vx);
            markBounce(false);
        }
        if (y < top) {
            y = top;
            vy = Math.abs(vy);
            markBounce(true);
        }
    }

    // Hit while moving sideways: push out of the side it came from and reverse.
    public void bounceOffSide(GameObject obstacle) {
        if (vx > 0) {
            x = obstacle.getX() - width;
        } else {
            x = obstacle.getX() + obstacle.getWidth();
        }
        vx = -vx;
        markBounce(false);
    }

    // Hit while moving up or down: push out of the top or bottom and reverse.
    public void bounceOffTopOrBottom(GameObject obstacle) {
        if (vy > 0) {
            y = obstacle.getY() - height;
        } else {
            y = obstacle.getY() + obstacle.getHeight();
        }
        vy = -vy;
        markBounce(true);
    }

    // The further from the paddle's center the ball lands, the steeper it leaves:
    // straight up in the middle, up to MAX_BOUNCE_ANGLE at the edges.
    public void bounceOffPaddle(Paddle paddle) {
        double offset = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
        double angle = Math.clamp(offset, -1, 1) * MAX_BOUNCE_ANGLE;
        vx = speed * Math.sin(angle);
        vy = -speed * Math.cos(angle);
        y = paddle.getY() - height;
        markBounce(true);
    }

    private void markBounce(boolean vertical) {
        sinceBounce = 0;
        bouncedVertically = vertical;
    }

    public void tick(double dt) {
        sinceBounce += dt;
    }

    public double getSinceBounce() {
        return sinceBounce;
    }

    // True if the last bounce was off a floor or ceiling (squash flat), false for a wall (squash thin).
    public boolean isBouncedVertically() {
        return bouncedVertically;
    }

    public void reset() {
        launched = false;
        vx = 0;
        vy = 0;
    }

    // Bricks it can still smash through without bouncing (Piercing Ball upgrade).
    public int getPierceLeft() {
        return pierceLeft;
    }

    public void setPierceLeft(int pierceLeft) {
        this.pierceLeft = pierceLeft;
    }

    public boolean usePierce() {
        if (pierceLeft <= 0) {
            return false;
        }
        pierceLeft--;
        return true;
    }

    public boolean isLaunched() {
        return launched;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getSpeed() {
        return speed;
    }
}
