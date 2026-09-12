package kg.nurtelecom.chess.records;


import kg.nurtelecom.chess.enums.PieceType;
import kg.nurtelecom.chess.models.PieceColor;

/**
 * Фигура — неизменяемый объект (record): тип + цвет.
 */
public record Piece(PieceType type, PieceColor color) {

    /** Буква цвета для имени файла картинки: 'w' или 'b'. */
    public char colorCode() {
        return color == PieceColor.WHITE ? 'w' : 'b';
    }

    /** Буква типа фигуры для имени файла картинки: p/n/b/r/q/k. */
    public char typeCode() {
        return switch (type) {
            case PAWN -> 'p';
            case KNIGHT -> 'n';
            case BISHOP -> 'b';
            case ROOK -> 'r';
            case QUEEN -> 'q';
            case KING -> 'k';
        };
    }
}
