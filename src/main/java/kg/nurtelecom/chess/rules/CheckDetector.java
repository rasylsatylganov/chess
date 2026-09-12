package kg.nurtelecom.chess.rules;

import kg.nurtelecom.chess.enums.PieceType;
import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.models.PieceColor;
import kg.nurtelecom.chess.records.Piece;

/**
 * Определяет: атакована ли клетка, стоит ли король под шахом,
 * и есть ли у стороны вообще хоть один легальный ход (для мата/пата).
 */
public final class CheckDetector {

    private CheckDetector() {
    }

    /** Итог позиции для стороны, которая должна ходить следующей. */
    public enum Status {
        NORMAL,
        CHECK,
        CHECKMATE,
        STALEMATE
    }

    public static Status evaluate(Board board, PieceColor sideToMove) {
        boolean inCheck = isInCheck(board, sideToMove);
        boolean hasMove = hasAnyLegalMove(board, sideToMove);

        if (hasMove) {
            return inCheck ? Status.CHECK : Status.NORMAL;
        }
        return inCheck ? Status.CHECKMATE : Status.STALEMATE;
    }

    public static boolean isInCheck(Board board, PieceColor color) {
        int[] kingSquare = findKing(board, color);
        if (kingSquare == null) {
            return false; // короля нет на доске — такого в норме не бывает, просто не падаем
        }
        return isSquareAttacked(board, kingSquare[0], kingSquare[1], color.opposite());
    }

    /** Есть ли у фигур цвета color хотя бы один по-настоящему легальный ход (с учётом шаха). */
    public static boolean hasAnyLegalMove(Board board, PieceColor color) {
        for (int fromRow = 0; fromRow < Board.SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < Board.SIZE; fromCol++) {
                Piece piece = board.get(fromRow, fromCol);
                if (piece == null || piece.color() != color) {
                    continue;
                }
                for (int toRow = 0; toRow < Board.SIZE; toRow++) {
                    for (int toCol = 0; toCol < Board.SIZE; toCol++) {
                        if (MoveValidator.isLegalMove(board, fromRow, fromCol, toRow, toCol)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Атакует ли хоть одна фигура цвета byColor клетку (row, col) — по правилам ходов (без учёта шаха). */
    public static boolean isSquareAttacked(Board board, int row, int col, PieceColor byColor) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.get(r, c);
                if (piece != null && piece.color() == byColor
                        && MoveValidator.isPseudoLegalMove(board, r, c, row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int[] findKing(Board board, PieceColor color) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = board.get(row, col);
                if (piece != null && piece.type() == PieceType.KING && piece.color() == color) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }
}
