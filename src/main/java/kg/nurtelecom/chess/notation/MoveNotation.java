package kg.nurtelecom.chess.notation;


import kg.nurtelecom.chess.enums.PieceType;
import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.records.Piece;
import kg.nurtelecom.chess.rules.CheckDetector;

/**
 * Текст хода в упрощённой алгебраической нотации: "e2-e4", "Ng1-f3", "Qd8xh4+".
 * <p>
 * Упрощение по сравнению с настоящей шахматной нотацией: указываем и клетку
 * "откуда", и клетку "куда" (в настоящей нотации "откуда" пишут только когда
 * иначе непонятно, какая из двух одинаковых фигур ходила) — так проще и
 * однозначнее читать историю партии, не заглядывая в саму позицию.
 */
public final class MoveNotation {

    private MoveNotation() {
    }

    public static String format(Piece piece, int fromRow, int fromCol, int toRow, int toCol,
                                boolean isCapture, CheckDetector.Status statusAfterMove) {
        String letter = pieceLetter(piece.type());
        String separator = isCapture ? "x" : "-";
        String from = square(fromRow, fromCol);
        String to = square(toRow, toCol);
        String suffix = switch (statusAfterMove) {
            case CHECK -> "+";
            case CHECKMATE -> "#";
            default -> "";
        };
        return letter + from + separator + to + suffix;
    }

    private static String square(int row, int col) {
        char file = (char) ('a' + col);
        int rank = Board.SIZE - row;
        return "" + file + rank;
    }

    private static String pieceLetter(PieceType type) {
        return switch (type) {
            case PAWN -> "";
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case ROOK -> "R";
            case QUEEN -> "Q";
            case KING -> "K";
        };
    }
}

