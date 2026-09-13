package kg.nurtelecom.chess.game;

import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import kg.nurtelecom.chess.api.ChessApiClient;
import kg.nurtelecom.chess.api.EngineDifficulty;
import kg.nurtelecom.chess.api.EngineMove;
import kg.nurtelecom.chess.enums.PieceType;
import kg.nurtelecom.chess.fen.FenConverter;
import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.models.PieceColor;
import kg.nurtelecom.chess.notation.MoveNotation;
import kg.nurtelecom.chess.records.Piece;
import kg.nurtelecom.chess.rules.CheckDetector;
import kg.nurtelecom.chess.rules.MoveValidator;
import kg.nurtelecom.chess.ui.BoardView;
import kg.nurtelecom.chess.ui.InfoPanel;

/**
 * Соединяет модель (Board), отрисовку (BoardView), правила (MoveValidator,
 * CheckDetector), нотацию хода, часы, мышь пользователя — и теперь ещё
 * и "думающего" соперника через {@link ChessApiClient}.
 * <p>
 * Игрок двигает только фигуры выбранного им цвета (humanColor). После
 * каждого хода, если очередь оказывается за другим цветом, GameController
 * сам запрашивает ход у chess-api.com и применяет его — асинхронно,
 * чтобы окно не "зависало" в ожидании ответа сервера.
 */
public class GameController {

    private final Board board;
    private final BoardView boardView;
    private final InfoPanel infoPanel;
    private final ChessApiClient chessApiClient = new ChessApiClient();

    private PieceColor sideToMove = PieceColor.WHITE;
    private PieceColor humanColor = PieceColor.WHITE;
    private int difficultyLevel = 5;
    private boolean gameOver = false;
    private boolean waitingForComputer = false;

    // Растёт с каждой новой партией — так пришедший с опозданием ответ от
    // сервера от УЖЕ ЗАКОНЧЕННОЙ/ПЕРЕЗАПУЩЕННОЙ партии просто игнорируется.
    private int gameGeneration = 0;

    private boolean dragging = false;
    private int dragFromRow = -1;
    private int dragFromCol = -1;

    public GameController(Board board, BoardView boardView, InfoPanel infoPanel) {
        this.board = board;
        this.boardView = boardView;
        this.infoPanel = infoPanel;
        attachMouseHandlers();
    }

    private void attachMouseHandlers() {
        boardView.setOnMousePressed(this::handleMousePressed);
        boardView.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMousePressed(MouseEvent event) {
        if (gameOver || waitingForComputer) {
            return;
        }

        int[] square = boardView.pointToSquare(event.getX(), event.getY());
        if (square == null) {
            return;
        }
        int row = square[0];
        int col = square[1];

        Piece piece = board.get(row, col);
        // Двигать можно только СВОИ фигуры (humanColor) и только в свой ход.
        if (piece == null || piece.color() != humanColor || sideToMove != humanColor) {
            return;
        }

        dragging = true;
        dragFromRow = row;
        dragFromCol = col;
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!dragging) {
            return;
        }
        dragging = false;

        int[] square = boardView.pointToSquare(event.getX(), event.getY());
        if (square != null) {
            int toRow = square[0];
            int toCol = square[1];
            if (MoveValidator.isLegalMove(board, dragFromRow, dragFromCol, toRow, toCol)) {
                applyMove(dragFromRow, dragFromCol, toRow, toCol, null);
            }
        }

        dragFromRow = -1;
        dragFromCol = -1;
        boardView.draw(board);
    }

    /**
     * Переставляет фигуру, обновляет часы/историю/статус и, если теперь
     * очередь соперника — запрашивает его ход у chess-api.com.
     * promotion — во что превратить пешку (только для ходов от chess-api.com;
     * свои собственные превращения пешки мы пока не поддерживаем, см. MoveValidator).
     */
    private void applyMove(int fromRow, int fromCol, int toRow, int toCol, PieceType promotion) {
        Piece piece = board.get(fromRow, fromCol);
        Piece captured = board.get(toRow, toCol);
        Piece placedPiece = promotion != null ? new Piece(promotion, piece.color()) : piece;

        board.set(toRow, toCol, placedPiece);
        board.set(fromRow, fromCol, null);
        sideToMove = sideToMove.opposite();

        CheckDetector.Status status = CheckDetector.evaluate(board, sideToMove);

        String moveText = MoveNotation.format(placedPiece, fromRow, fromCol, toRow, toCol, captured != null, status);
        infoPanel.addMove(piece.color(), moveText);
        infoPanel.setActiveClock(sideToMove);

        applyStatus(status);

        if (!gameOver && sideToMove != humanColor) {
            requestComputerMove(gameGeneration);
        }
    }

    /** Начать новую партию: сбросить позицию, выбрать сторону игрока и сложность компьютера. */
    public void startNewGame(PieceColor humanColor, int difficultyLevel) {
        gameGeneration++;
        board.setupStandardPosition();
        sideToMove = PieceColor.WHITE;
        gameOver = false;
        waitingForComputer = false;
        this.humanColor = humanColor;
        this.difficultyLevel = Math.max(1, Math.min(10, difficultyLevel));

        boardView.setFlipped(humanColor == PieceColor.BLACK);
        boardView.draw(board);
        infoPanel.clearHistory();
        infoPanel.resetClocks();

        String colorLabel = humanColor == PieceColor.WHITE ? "белыми" : "чёрными";
        infoPanel.setStatus("Вы играете " + colorLabel + ".\nСложность: " + this.difficultyLevel
                + "/10.\nХод белых.");

        if (sideToMove != humanColor) {
            requestComputerMove(gameGeneration);
        }
    }

    private void requestComputerMove(int requestGeneration) {
        waitingForComputer = true;
        infoPanel.setStatus("Компьютер думает...");

        String fen = FenConverter.toFen(board, sideToMove);
        EngineDifficulty difficulty = EngineDifficulty.forLevel(difficultyLevel);

        chessApiClient.requestBestMoveAsync(fen, difficulty)
                .thenAccept(engineMove -> Platform.runLater(() -> {
                    if (requestGeneration != gameGeneration) {
                        return; // партия уже перезапущена — этот ответ больше не актуален
                    }
                    waitingForComputer = false;
                    applyMove(engineMove.fromRow(), engineMove.fromCol(),
                            engineMove.toRow(), engineMove.toCol(), engineMove.promotionType());
                    boardView.draw(board);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        if (requestGeneration != gameGeneration) {
                            return;
                        }
                        waitingForComputer = false;
                        infoPanel.setStatus("Не удалось получить ход от сервера:\n" + rootMessage(error));
                    });
                    return null;
                });
    }

    private String rootMessage(Throwable error) {
        Throwable cause = error.getCause() != null ? error.getCause() : error;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    private void applyStatus(CheckDetector.Status status) {
        String turnLabel = sideToMove == PieceColor.WHITE ? "белых" : "чёрных";

        switch (status) {
            case NORMAL -> infoPanel.setStatus("Ход " + turnLabel + ".");
            case CHECK -> infoPanel.setStatus("Шах!\nХод " + turnLabel + ".");
            case CHECKMATE -> {
                gameOver = true;
                infoPanel.stopClocks();
                String winner = sideToMove == PieceColor.WHITE ? "Чёрные" : "Белые";
                infoPanel.setStatus("Мат!\n" + winner + " побеждают.");
            }
            case STALEMATE -> {
                gameOver = true;
                infoPanel.stopClocks();
                infoPanel.setStatus("Пат.\nНичья.");
            }
        }
    }
}
