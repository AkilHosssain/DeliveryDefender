package app.utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class switchToScene {

    public static void apply(Scene scene, String fxmlPath) {
        Parent currentRoot = scene.getRoot();
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), currentRoot);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(_ -> {
            try {
                URL resource = switchToScene.class.getResource(fxmlPath);
                if (resource == null) {
                    throw new IllegalArgumentException("FXML file not found: " + fxmlPath);
                }
                Parent newPage = FXMLLoader.load(resource);
                scene.setRoot(newPage);
                newPage.setOpacity(0);

                FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), newPage);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            } catch (IOException e) {
                System.err.println("ERROR: Failed to load FXML file: " + fxmlPath);
                System.err.println("Reason: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("WARNING: " + e.getMessage());
            }
        });

        fadeOut.play();
    }
}
