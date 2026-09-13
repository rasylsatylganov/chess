package kg.nurtelecom.chess;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kg.nurtelecom.chess.game.GameController;
import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.models.PieceColor;
import kg.nurtelecom.chess.ui.BoardView;
import kg.nurtelecom.chess.ui.InfoPanel;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;

/**
 * Один класс — два "запускателя" одновременно:
 * - @SpringBootApplication делает его точкой входа для Spring (DI-контейнер);
 * - extends Application делает его точкой входа для JavaFX (окно, сцена).
 * <p>
 * Порядок вызовов, которым управляет сам JavaFX:
 * main() -> Application.launch() -> init() -> start() -> (окно живёт) -> stop()
 */
@SpringBootApplication
public class ChessApplication extends Application {

	private ConfigurableApplicationContext springContext;

	/** Результат диалога "Новая игра": какой цвет выбрал игрок и на какой сложности играть. */
	private record NewGameSettings(PieceColor humanColor, int difficultyLevel) {
	}

	@Override
	public void init() {
		springContext = new SpringApplicationBuilder(ChessApplication.class)
				.run(getParameters().getRaw().toArray(new String[0]));
	}

	@Override
	public void start(Stage primaryStage) {
		Board board = new Board();
		BoardView boardView = new BoardView();
		InfoPanel infoPanel = new InfoPanel();
		GameController gameController = new GameController(board, boardView, infoPanel);

		StackPane boardContainer = new StackPane(boardView);
		boardContainer.setStyle("-fx-background-color: #2b2b2b;");

		NumberBinding boardSize = Bindings.min(boardContainer.widthProperty(), boardContainer.heightProperty());
		boardView.widthProperty().bind(boardSize);
		boardView.heightProperty().bind(boardSize);
		boardSize.addListener((obs, oldVal, newVal) -> boardView.draw(board));

		BorderPane root = new BorderPane();
		root.setTop(buildMenuBar(gameController));
		root.setCenter(boardContainer);
		root.setRight(infoPanel);

		Scene scene = new Scene(root, 900, 700);

		primaryStage.setTitle("Шахматы");
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		primaryStage.show();

		boardView.draw(board); // первая отрисовка, когда размеры уже посчитаны
	}

	private MenuBar buildMenuBar(GameController gameController) {
		MenuItem vsComputerWeb = new MenuItem("С компьютером (Web)");
		vsComputerWeb.setOnAction(event -> promptNewGameSettings(gameController));

		Menu singlePlayerMenu = new Menu("Одиночная");
		singlePlayerMenu.getItems().add(vsComputerWeb);

		Menu gameMenu = new Menu("Игра");
		gameMenu.getItems().add(singlePlayerMenu);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(gameMenu);
		return menuBar;
	}

	/**
	 * Диалог "Новая игра": выбор цвета фигур + ползунок сложности компьютера (1-10).
	 * Уровень сложности переводится в параметры depth/maxThinkingTime запроса
	 * к chess-api.com — см. {@link kg.nurtelecom.chess.api.EngineDifficulty}.
	 */
	private void promptNewGameSettings(GameController gameController) {
		Dialog<NewGameSettings> dialog = new Dialog<>();
		dialog.setTitle("Новая игра");
		dialog.setHeaderText("Настройки партии");

		ButtonType startButtonType = new ButtonType("Начать", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(startButtonType, ButtonType.CANCEL);

		ToggleGroup colorGroup = new ToggleGroup();
		RadioButton whiteRadio = new RadioButton("Белыми");
		RadioButton blackRadio = new RadioButton("Чёрными");
		whiteRadio.setToggleGroup(colorGroup);
		blackRadio.setToggleGroup(colorGroup);
		whiteRadio.setSelected(true);

		Slider difficultySlider = new Slider(1, 10, 5);
		difficultySlider.setShowTickLabels(true);
		difficultySlider.setShowTickMarks(true);
		difficultySlider.setMajorTickUnit(1);
		difficultySlider.setMinorTickCount(0);
		difficultySlider.setSnapToTicks(true);
		difficultySlider.setBlockIncrement(1);
		difficultySlider.setPrefWidth(200);

		Label difficultyValueLabel = new Label("5");
		difficultySlider.valueProperty().addListener((obs, oldVal, newVal) ->
				difficultyValueLabel.setText(String.valueOf(newVal.intValue())));

		GridPane grid = new GridPane();
		grid.setHgap(12);
		grid.setVgap(14);
		grid.setPadding(new Insets(10, 0, 0, 0));
		grid.add(new Label("Цвет фигур:"), 0, 0);
		grid.add(new HBox(10, whiteRadio, blackRadio), 1, 0);
		grid.add(new Label("Сложность (1-10):"), 0, 1);
		grid.add(new HBox(8, difficultySlider, difficultyValueLabel), 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(buttonType -> {
			if (buttonType != startButtonType) {
				return null;
			}
			PieceColor color = blackRadio.isSelected() ? PieceColor.BLACK : PieceColor.WHITE;
			int level = (int) Math.round(difficultySlider.getValue());
			return new NewGameSettings(color, level);
		});

		Optional<NewGameSettings> result = dialog.showAndWait();
		result.ifPresent(settings -> gameController.startNewGame(settings.humanColor(), settings.difficultyLevel()));
	}

	@Override
	public void stop() {
		springContext.close();
		Platform.exit();
	}
}
