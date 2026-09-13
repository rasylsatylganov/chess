package kg.nurtelecom.chess.ui;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import kg.nurtelecom.chess.models.PieceColor;

/**
 * Простые шахматные часы: секундомер на каждую сторону — считают ВВЕРХ,
 * сколько времени сторона потратила на обдумывание (это не таймер с обратным
 * отсчётом и лимитом партии, просто счётчик потраченного времени).
 * <p>
 * Тикают по очереди: часы стороны, чей сейчас ход, идут, часы соперника —
 * на паузе (активные часы показываются ярче).
 */
public class ClockPanel extends HBox {

    private static final String ACTIVE_STYLE =
            "-fx-text-fill: #FFFFFF; -fx-font-size: 20px; -fx-font-family: 'Consolas', monospace;";
    private static final String INACTIVE_STYLE =
            "-fx-text-fill: #888888; -fx-font-size: 20px; -fx-font-family: 'Consolas', monospace;";
    private static final String TITLE_STYLE = "-fx-text-fill: #AAAAAA; -fx-font-size: 12px;";

    private final Label whiteDigits = new Label("00:00");
    private final Label blackDigits = new Label("00:00");

    private final Timeline timeline;

    private int whiteSeconds = 0;
    private int blackSeconds = 0;
    private PieceColor activeSide = PieceColor.WHITE;

    public ClockPanel() {
        setSpacing(16);

        Label whiteTitle = new Label("Белые");
        Label blackTitle = new Label("Чёрные");
        whiteTitle.setStyle(TITLE_STYLE);
        blackTitle.setStyle(TITLE_STYLE);

        VBox whiteBox = new VBox(2, whiteTitle, whiteDigits);
        VBox blackBox = new VBox(2, blackTitle, blackDigits);
        whiteBox.setAlignment(Pos.CENTER_LEFT);
        blackBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(whiteBox, Priority.ALWAYS);
        HBox.setHgrow(blackBox, Priority.ALWAYS);

        getChildren().addAll(whiteBox, blackBox);

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);

        refreshLabels();
    }

    /** Сбросить оба счётчика в ноль и запустить отсчёт заново (часы белых). */
    public void reset() {
        timeline.stop();
        whiteSeconds = 0;
        blackSeconds = 0;
        activeSide = PieceColor.WHITE;
        refreshLabels();
        timeline.play();
    }

    /** Переключить, чья сторона сейчас тикает — вызывать после каждого хода. */
    public void switchActiveSide(PieceColor newActiveSide) {
        this.activeSide = newActiveSide;
        refreshLabels();
    }

    /** Остановить оба счётчика — партия закончилась (мат/пат). */
    public void stop() {
        timeline.stop();
    }

    private void tick() {
        if (activeSide == PieceColor.WHITE) {
            whiteSeconds++;
        } else {
            blackSeconds++;
        }
        refreshLabels();
    }

    private void refreshLabels() {
        whiteDigits.setText(formatTime(whiteSeconds));
        blackDigits.setText(formatTime(blackSeconds));
        whiteDigits.setStyle(activeSide == PieceColor.WHITE ? ACTIVE_STYLE : INACTIVE_STYLE);
        blackDigits.setStyle(activeSide == PieceColor.BLACK ? ACTIVE_STYLE : INACTIVE_STYLE);
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
