package kg.nurtelecom.chess.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;

/**
 * Заставка на весь экран — показывается при запуске приложения, пока
 * партия ещё не начата. Собрана из тех же картинок фигур, что и доска
 * (просто крупных и внахлёст, чисто для красоты).
 * Заменяется на реальную доску, когда игрок выбирает
 * "Игра -> Одиночная -> С компьютером (Web)".
 */
public class WelcomePanel extends StackPane {

    // Ладья, конь, слон, ферзь, король — на белых клетках, только белые фигуры для красивого набора.
    private static final String[] PIECE_FILES = {"wrw", "wnw", "wbw", "wqw", "wkw"};
    private static final double IMAGE_SIZE = 190;
    private static final double[] OFFSET_X = {0, 110, 220, 330, 440};
    private static final double[] OFFSET_Y = {40, 0, 55, 0, 35};
    private static final double[] ROTATION = {-10, 7, -5, 9, -12};

    public WelcomePanel() {
        setStyle("-fx-background-color: #2b2b2b;");

        Pane piecesLayer = new Pane();
        piecesLayer.setPrefSize(IMAGE_SIZE + OFFSET_X[OFFSET_X.length - 1], IMAGE_SIZE + 60);
        piecesLayer.setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        for (int i = 0; i < PIECE_FILES.length; i++) {
            ImageView imageView = new ImageView(loadImage(PIECE_FILES[i]));
            imageView.setFitWidth(IMAGE_SIZE);
            imageView.setFitHeight(IMAGE_SIZE);
            imageView.setLayoutX(OFFSET_X[i]);
            imageView.setLayoutY(OFFSET_Y[i]);
            imageView.setRotate(ROTATION[i]);
            piecesLayer.getChildren().add(imageView);
        }

        Label title = new Label("Шахматы");
        title.setFont(Font.font("Serif", FontWeight.BOLD, 44));
        title.setTextFill(Color.web("#EEEEEE"));

        Label hint = new Label("Игра \u2192 Одиночная \u2192 С компьютером (Web), чтобы начать партию");
        hint.setFont(Font.font("Sans Serif", 16));
        hint.setTextFill(Color.web("#AAAAAA"));

        VBox content = new VBox(24, piecesLayer, title, hint);
        content.setAlignment(Pos.CENTER);

        getChildren().add(content);
    }

    private Image loadImage(String key) {
        String path = "/png/" + key + ".png";
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Не найдена картинка для заставки: " + path);
        }
        return new Image(stream, 400, 400, false, true);
    }
}

