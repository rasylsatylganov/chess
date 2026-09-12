package kg.nurtelecom.chess.game;

import javafx.scene.input.MouseEvent;
import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.models.PieceColor;
import kg.nurtelecom.chess.records.Piece;
import kg.nurtelecom.chess.rules.MoveValidator;
import kg.nurtelecom.chess.ui.BoardView;
import kg.nurtelecom.chess.ui.InfoPanel;

/**
 * Соединяет модель (Board), отрисовку (BoardView), правила (MoveValidator)
 * и мышь пользователя. Здесь же хранится, чей сейчас ход.
 * <p>
 * Взаимодействие: нажал на свою фигуру (запоминаем клетку), отпустил —
 * если ход по правилам возможен, фигура переставляется и доска
 * перерисовывается целиком. Визуального "полёта" фигуры за курсором нет
 * (картинки без прозрачности, это выглядело бы криво — фон клетки ехал бы вместе с фигурой).
 */
public class GameController {

    private final Board board;
    private final BoardView boardView;
    private final InfoPanel infoPanel;

    private PieceColor sideToMove = PieceColor.WHITE;

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
        int[] square = boardView.pointToSquare(event.getX(), event.getY());
        if (square == null) {
            return;
        }
        int row = square[0];
        int col = square[1];

        Piece piece = board.get(row, col);
        if (piece == null || piece.color() != sideToMove) {
            return; // пустая клетка или чужая фигура — выбирать нечего
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
                applyMove(dragFromRow, dragFromCol, toRow, toCol);
            }
        }

        dragFromRow = -1;
        dragFromCol = -1;
        boardView.draw(board);
        updateStatus();
    }

    private void applyMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board.get(fromRow, fromCol);
        board.set(toRow, toCol, piece);
        board.set(fromRow, fromCol, null);
        sideToMove = sideToMove.opposite();
    }

    /** Начать новую партию: сбросить позицию, выбрать сторону игрока, обновить отображение. */
    public void startNewGame(PieceColor humanColor) {
        board.setupStandardPosition();
        sideToMove = PieceColor.WHITE;
        boardView.setFlipped(humanColor == PieceColor.BLACK);
        boardView.draw(board);

        String colorLabel = humanColor == PieceColor.WHITE ? "белыми" : "чёрными";
        infoPanel.setStatus("Вы играете " + colorLabel + ".\nХод белых.");
    }

    private void updateStatus() {
        String turn = sideToMove == PieceColor.WHITE ? "белых" : "чёрных";
        infoPanel.setStatus("Ход " + turn + ".");
    }
}
