package kg.nurtelecom.chess.fen;

import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.models.PieceColor;
import kg.nurtelecom.chess.records.Piece;

/**
 * Переводит нашу доску в стандартную FEN-строку — так внешние шахматные
 * сервисы (chess-api.com) понимают, какая сейчас позиция на доске.
 * <p>
 * Упрощение: поля "рокировка" и "взятие на проходе" всегда "-" (недоступны),
 * потому что мы сами эти правила ещё не реализовали (см. MoveValidator) —
 * так честнее, чем притворяться, что они есть.
 */
public final class FenConverter {

    private FenConverter() {
    }

    public static String toFen(Board board, PieceColor sideToMove) {
        StringBuilder fen = new StringBuilder();

        for (int row = 0; row < Board.SIZE; row++) {
            int emptyCount = 0;
            for (int col = 0; col < Board.SIZE; col++) {
                Piece piece = board.get(row, col);
                if (piece == null) {
                    emptyCount++;
                    continue;
                }
                if (emptyCount > 0) {
                    fen.append(emptyCount);
                    emptyCount = 0;
                }
                char letter = piece.typeCode(); // уже совпадает с обозначениями FEN (p/n/b/r/q/k)
                fen.append(piece.color() == PieceColor.WHITE ? Character.toUpperCase(letter) : letter);
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (row < Board.SIZE - 1) {
                fen.append('/');
            }
        }

        fen.append(' ').append(sideToMove == PieceColor.WHITE ? 'w' : 'b');
        fen.append(" - - 0 1"); // рокировка, взятие на проходе, счётчики — не отслеживаем

        return fen.toString();
    }
}
