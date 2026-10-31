package se233.project2.view;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import se233.project2.exception.GameException;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.IntPredicate;

// A sprite sheet image loaded from resources. The sheets have solid background colors
// instead of transparency, so pixels matching `background` are made transparent on load.
public class SpriteSheet {

    private final Image image;

    private SpriteSheet(Image image) {
        this.image = image;
    }

    public static SpriteSheet load(String path, IntPredicate background) {
        Image source;
        try (InputStream in = SpriteSheet.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new GameException("Missing sprite sheet " + path);
            }
            source = new Image(in);
        } catch (IOException e) {
            throw new GameException("Could not read sprite sheet " + path, e);
        }
        if (source.isError()) {
            throw new GameException("Could not decode sprite sheet " + path, source.getException());
        }

        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage keyed = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = keyed.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                writer.setArgb(x, y, background.test(argb & 0xFFFFFF) ? 0 : argb);
            }
        }
        return new SpriteSheet(keyed);
    }

    public Image getImage() {
        return image;
    }
}
