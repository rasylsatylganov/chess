package kg.nurtelecom.chess.rules;


import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.models.PieceColor;
import kg.nurtelecom.chess.records.Piece;

/**
 * Проверка "может ли фигура так пойти" — по правилам конкретной фигуры,
 * с учётом других фигур на пути (кроме коня — он прыгает).
 * <p>
 * ЧЕГО ЗДЕСЬ ПОКА НЕТ (сознательно, это следующие шаги):
 * - не проверяется, не остаётся ли после хода свой король под шахом;
 * - нет взятия на проходе, рокировки, превращения пешки.
 */
public final class MoveValidator {

    private MoveValidator() {
    }

    public static boolean isLegalMove(Board board, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }

        Piece piece = board.get(fromRow, fromCol);
        if (piece == null) {
            return false;
        }

        Piece target = board.get(toRow, toCol);
        if (target != null && target.color() == piece.color()) {
            return false; // нельзя бить свою же фигуру
        }

        return switch (piece.type()) {
            case PAWN -> isPawnMove(board, piece, fromRow, fromCol, toRow, toCol, target);
            case KNIGHT -> isKnightMove(fromRow, fromCol, toRow, toCol);
            case BISHOP -> isSlidingMove(board, fromRow, fromCol, toRow, toCol, true, false);
            case ROOK -> isSlidingMove(board, fromRow, fromCol, toRow, toCol, false, true);
            case QUEEN -> isSlidingMove(board, fromRow, fromCol, toRow, toCol, true, true);
            case KING -> isKingMove(fromRow, fromCol, toRow, toCol);
        };
    }

    private static boolean isPawnMove(Board board, Piece piece, int fromRow, int fromCol,
                                      int toRow, int toCol, Piece target) {
        // Белые идут "вверх" (к row 0, где в модели восьмая горизонталь), чёрные — вниз.
        int direction = piece.color() == PieceColor.WHITE ? -1 : 1;
        int startRow = piece.color() == PieceColor.WHITE ? 6 : 1;
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        if (colDiff == 0) {
            if (target != null) {
                return false; // прямо вперёд можно только на пустую клетку
            }
            if (rowDiff == direction) {
                return true;
            }
            if (rowDiff == 2 * direction && fromRow == startRow) {
                int middleRow = fromRow + direction;
                return board.get(middleRow, fromCol) == null; // клетка перед конечной тоже должна быть пустой
            }
            return false;
        }

        if (Math.abs(colDiff) == 1 && rowDiff == direction) {
            return target != null; // взятие по диагонали — обязательно есть что бить
        }

        return false;
    }

    private static boolean isKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 1 && colDiff == 2) || (rowDiff == 2 && colDiff == 1);
    }

    private static boolean isKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;
    }

    /**
     * Общая проверка для слона/ладьи/ферзя: идём по прямой линии от from к to
     * и убеждаемся, что все клетки МЕЖДУ ними свободны — "без перепрыгивания".
     */
    private static boolean isSlidingMove(Board board, int fromRow, int fromCol, int toRow, int toCol,
                                         boolean allowDiagonal, boolean allowStraight) {
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        boolean isDiagonal = Math.abs(rowDiff) == Math.abs(colDiff);
        boolean isStraight = rowDiff == 0 || colDiff == 0;

        if (isDiagonal) {
            if (!allowDiagonal) {
                return false;
            }
        } else if (isStraight) {
            if (!allowStraight) {
                return false;
            }
        } else {
            return false; // не по прямой и не по диагонали — фигура так не ходит в принципе
        }

        int stepRow = Integer.signum(rowDiff);
        int stepCol = Integer.signum(colDiff);
        int steps = Math.max(Math.abs(rowDiff), Math.abs(colDiff));

        for (int i = 1; i < steps; i++) {
            int r = fromRow + stepRow * i;
            int c = fromCol + stepCol * i;
            if (board.get(r, c) != null) {
                return false; // на пути стоит фигура — прыгать через неё нельзя
            }
        }
        return true;
    }
}

