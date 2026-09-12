package kg.nurtelecom.chess.ui;


import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Правая панель. Пока показывает только текстовый статус
 * ("Ваш ход" и т.п.) — сюда же позже добавятся история ходов,
 * чат, часы (шаги 6-7 роадмапа).
 */
public class InfoPanel extends VBox {

    private final Label statusLabel = new Label("Игра ещё не начата.\nВыберите: Игра \u2192 Одиночная.");

    public InfoPanel() {
        setPadding(new Insets(14));
        setSpacing(10);
        setPrefWidth(230);
        setStyle("-fx-background-color: #333333;");

        statusLabel.setStyle("-fx-text-fill: #DDDDDD; -fx-font-size: 14px;");
        statusLabel.setWrapText(true);

        getChildren().add(statusLabel);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}
