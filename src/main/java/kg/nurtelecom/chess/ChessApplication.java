package kg.nurtelecom.chess;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kg.nurtelecom.chess.models.Board;
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

		// Доска живёт в отдельном контейнере — так привязка размера считается
		// от места, оставшегося ПОД меню и СПРАВА от панели, а не от всего окна целиком.
		StackPane boardContainer = new StackPane(boardView);
		boardContainer.setStyle("-fx-background-color: #2b2b2b;");

		NumberBinding boardSize = Bindings.min(boardContainer.widthProperty(), boardContainer.heightProperty());
		boardView.widthProperty().bind(boardSize);
		boardView.heightProperty().bind(boardSize);
		boardSize.addListener((obs, oldVal, newVal) -> boardView.draw(board));

		BorderPane root = new BorderPane();
		root.setTop(buildMenuBar(board, boardView, infoPanel));
		root.setCenter(boardContainer);
		root.setRight(infoPanel);

		Scene scene = new Scene(root, 900, 700);

		primaryStage.setTitle("Шахматы");
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		primaryStage.show();

		boardView.draw(board); // первая отрисовка, когда размеры уже посчитаны
	}

	/**
	 * Меню: Игра -> Одиночная -> С компьютером (Web).
	 */
	private MenuBar buildMenuBar(Board board, BoardView boardView, InfoPanel infoPanel) {
		MenuItem vsComputerWeb = new MenuItem("С компьютером (Web)");
		vsComputerWeb.setOnAction(event -> startNewGame(board, boardView, infoPanel));

		Menu singlePlayerMenu = new Menu("Одиночная");
		singlePlayerMenu.getItems().add(vsComputerWeb);

		Menu gameMenu = new Menu("Игра");
		gameMenu.getItems().add(singlePlayerMenu);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(gameMenu);
		return menuBar;
	}

	/**
	 * Запрашивает у игрока цвет фигур и настраивает доску соответственно.
	 * Сама расстановка фигур в модели не меняется — переворачивается только
	 * визуальное отображение (BoardView.setFlipped), чтобы выбранный цвет
	 * игрока оказался снизу, ближе к нему, как за настоящей доской.
	 */
	private void startNewGame(Board board, BoardView boardView, InfoPanel infoPanel) {
		ButtonType whiteButton = new ButtonType("Белыми");
		ButtonType blackButton = new ButtonType("Чёрными");

		Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.setTitle("Новая игра");
		dialog.setHeaderText("Какими фигурами хотите играть?");
		dialog.getButtonTypes().setAll(whiteButton, blackButton);

		Optional<ButtonType> choice = dialog.showAndWait();
		if (choice.isEmpty()) {
			return; // закрыли диалог, ничего не выбрав
		}

		boolean playingBlack = choice.get() == blackButton;

		board.setupStandardPosition(); // свежая партия
		boardView.setFlipped(playingBlack);
		boardView.draw(board);

		infoPanel.setStatus(playingBlack
				? "Вы играете чёрными.\nХод белых."
				: "Вы играете белыми.\nВаш ход.");
	}

	@Override
	public void stop() {
		springContext.close();
		Platform.exit();
	}
}
