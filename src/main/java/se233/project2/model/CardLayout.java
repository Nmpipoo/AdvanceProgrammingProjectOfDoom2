package se233.project2.model;

// Where the upgrade cards sit on screen. Shared by the view, which draws them,
// and by GameWorld, which works out which card the mouse is over.
public final class CardLayout {

    public static final double WIDTH = 220;
    public static final double HEIGHT = 150;
    public static final double GAP = 30;
    public static final double TOP = 240;

    private CardLayout() {
    }

    // Left edge of card `index` when `count` cards are centered in a row.
    public static double left(int index, int count) {
        double rowLeft = (GameWorld.WIDTH - count * WIDTH - (count - 1) * GAP) / 2;
        return rowLeft + index * (WIDTH + GAP);
    }

    // The card under (x, y), or -1 when the point is between or outside the cards.
    public static int cardAt(double x, double y, int count) {
        if (y < TOP || y > TOP + HEIGHT) {
            return -1;
        }
        for (int i = 0; i < count; i++) {
            double left = left(i, count);
            if (x >= left && x <= left + WIDTH) {
                return i;
            }
        }
        return -1;
    }
}
