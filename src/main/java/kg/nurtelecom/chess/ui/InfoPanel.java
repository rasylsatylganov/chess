package kg.nurtelecom.chess.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import kg.nurtelecom.chess.models.PieceColor;

/**
 * Правая панель: часы сверху, статус ("Ваш ход" и т.п.) под ними,
 * история ходов снизу — на всю оставшуюся высоту.
 */
public class InfoPanel extends VBox {

    private final ClockPanel clockPanel = new ClockPanel();
    private final Label statusLabel = new Label("Игра ещё не начата.\nВыберите: Игра \u2192 Одиночная.");
    private final TextArea historyArea = new TextArea();

    private int moveNumber = 1;

    public InfoPanel() {
        setPadding(new Insets(14));
        setSpacing(10);
        setPrefWidth(230);
        setStyle("-fx-background-color: #333333;");

        statusLabel.setStyle("-fx-text-fill: #DDDDDD; -fx-font-size: 14px;");
        statusLabel.setWrapText(true);

        Label historyTitle = new Label("История ходов");
        historyTitle.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 12px; -fx-font-weight: bold;");

        historyArea.setEditable(false);
        historyArea.setFocusTraversable(false);
        historyArea.setStyle(
                "-fx-control-inner-background: #2b2b2b;"
                        + "-fx-text-fill: #DDDDDD;"
                        + "-fx-font-family: 'Consolas', monospace;"
                        + "-fx-font-size: 13px;");
        VBox.setVgrow(historyArea, Priority.ALWAYS);

        getChildren().addAll(clockPanel, statusLabel, historyTitle, historyArea);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    /** Добавить строку к уже показанному статусу, не стирая его (например, техническую подробность от сервера). */
    public void appendStatusLine(String extraLine) {
        statusLabel.setText(statusLabel.getText() + "\n" + extraLine);
    }

    /** Очистить историю и начать нумерацию ходов заново (вызывается при новой партии). */
    public void clearHistory() {
        historyArea.clear();
        moveNumber = 1;
    }

    /**
     * Добавить сделанный ход в историю. Ходы белых и чёрных пишутся парой
     * на одной строке: "1. e2-e4   e7-e5".
     */
    public void addMove(PieceColor color, String moveText) {
        if (color == PieceColor.WHITE) {
            historyArea.appendText(moveNumber + ". " + moveText + "   ");
        } else {
            historyArea.appendText(moveText + "\n");
            moveNumber++;
        }
    }

    /** Сбросить оба счётчика времени и запустить их заново (часы белых) — новая партия. */
    public void resetClocks() {
        clockPanel.reset();
    }

    /** Переключить, чьи часы сейчас тикают — вызывать после каждого хода. */
    public void setActiveClock(PieceColor color) {
        clockPanel.switchActiveSide(color);
    }

    /** Остановить часы — партия закончилась (мат/пат). */
    public void stopClocks() {
        clockPanel.stop();
    }

    /** Возобновить часы без сброса счётчиков (например, после отмены хода из позиции мата). */
    public void resumeClocks() {
        clockPanel.resume();
    }
}
