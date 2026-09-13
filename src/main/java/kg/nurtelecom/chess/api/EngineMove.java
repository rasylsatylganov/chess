package kg.nurtelecom.chess.api;

import kg.nurtelecom.chess.enums.PieceType;

/**
 * Ход, который прислал chess-api.com. from/to — в его собственном формате
 * ("b7", "e4"), методы ниже переводят их в систему координат нашей Board
 * (row 0 = восьмая горизонталь).
 * <p>
 * depth/eval/text — фактические данные из ответа сервера. Полезны, чтобы
 * ПРОВЕРИТЬ, что уровень сложности реально дошёл до сервера: depth здесь
 * должен совпадать (или быть очень близким) с тем, что мы запросили в
 * EngineDifficulty — если да, значит параметр точно учитывается.
 */
public record EngineMove(String from, String to, String promotion, int depth, Double eval, String text) {

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
