package kg.nurtelecom.chess.models;


import kg.nurtelecom.chess.enums.PieceType;
import kg.nurtelecom.chess.records.Piece;

import java.util.Arrays;

/**
 * Модель доски. Пока только хранение фигур и начальная расстановка —
 * никакой логики ходов здесь ещё нет (это будущий шаг).
 * <p>
 * Договорённость по координатам: row=0 — восьмая горизонталь (чёрные сверху),
 * row=7 — первая горизонталь (белые снизу). col=0 — вертикаль "a".
 */
public class Board {

    public static final int SIZE = 8;

    private final Piece[][] squares = new Piece[SIZE][SIZE];

    public Board() {
        setupStandardPosition();
    }

    public void setupStandardPosition() {
        for (Piece[] row : squares) {
            Arrays.fill(row, null);
        }

        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int col = 0; col < SIZE; col++) {
            squares[0][col] = new Piece(backRank[col], PieceColor.BLACK);
            squares[1][col] = new Piece(PieceType.PAWN, PieceColor.BLACK);
            squares[6][col] = new Piece(PieceType.PAWN, PieceColor.WHITE);
            squares[7][col] = new Piece(backRank[col], PieceColor.WHITE);
        }
    }

    public Piece get(int row, int col) {
        checkBounds(row, col);
        return squares[row][col];
    }

    public void set(int row, int col, Piece piece) {
        checkBounds(row, col);
        squares[row][col] = piece;
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IndexOutOfBoundsException("Клетка вне доски: row=" + row + ", col=" + col);
        }
    }
}

