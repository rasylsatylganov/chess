package kg.nurtelecom.chess.api;

import kg.nurtelecom.chess.enums.PieceType;

/**
 * Ход, который прислал chess-api.com. from/to — в его собственном формате
 * ("b7", "e4"), методы ниже переводят их в систему координат нашей Board
 * (row 0 = восьмая горизонталь).
 */
public record EngineMove(String from, String to, String promotion) {

    public int fromRow() {
        return 8 - Character.getNumericValue(from.charAt(1));
    }

    public int fromCol() {
        return from.charAt(0) - 'a';
    }

    public int toRow() {
        return 8 - Character.getNumericValue(to.charAt(1));
    }

    public int toCol() {
        return to.charAt(0) - 'a';
    }

    /** Тип фигуры для превращения пешки, или null, если ход не является превращением. */
    public PieceType promotionType() {
        if (promotion == null) {
            return null;
        }
        return switch (promotion) {
            case "q" -> PieceType.QUEEN;
            case "r" -> PieceType.ROOK;
            case "b" -> PieceType.BISHOP;
            case "n" -> PieceType.KNIGHT;
            default -> null;
        };
    }
}
