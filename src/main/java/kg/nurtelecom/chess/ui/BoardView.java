package kg.nurtelecom.chess.ui;


import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import kg.nurtelecom.chess.models.Board;
import kg.nurtelecom.chess.records.Piece;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Рисует доску картинками из src/main/resources/png, плюс подписи координат
 * по краям (цифры слева, буквы снизу — как на настоящей доске).
 * <p>
 * Поддерживает "переворот" ({@link #setFlipped}) — когда игрок играет чёрными,
 * доска показывается снизу-вверх (чёрные внизу, ближе к игроку), но модель
 * доски (Board) при этом не меняется — переворот чисто визуальный.
 */
public class BoardView extends Canvas {

    private static final int CACHE_RESOLUTION = 256;
    private static final String IMAGE_FOLDER = "/png/";
    private static final double MARGIN_RATIO = 0.055;
    private static final Color LABEL_COLOR = Color.web("#DDDDDD");

    private final Map<String, Image> imageCache = new HashMap<>();

    /** true, если доска показывается "с точки зрения чёрных" (чёрные внизу). */
    private boolean flipped = false;

    public BoardView() {
        super(8 * 90, 8 * 90);
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public void draw(Board board) {
        double size = getWidth();
        double margin = size * MARGIN_RATIO;
        double boardPixelSize = size - margin;
        double squareSize = boardPixelSize / Board.SIZE;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, size, size);

        drawSquaresAndPieces(gc, board, margin, squareSize);
        drawCoordinateLabels(gc, margin, boardPixelSize, squareSize);
    }

    private void drawSquaresAndPieces(GraphicsContext gc, Board board, double offsetX, double squareSize) {
        // screenRow/screenCol — позиция на экране (0,0 = левый верхний угол доски).
        // row/col — логическая клетка в модели Board. При flipped=true они идут в обратном порядке.
        for (int screenRow = 0; screenRow < Board.SIZE; screenRow++) {
            for (int screenCol = 0; screenCol < Board.SIZE; screenCol++) {
                int row = flipped ? Board.SIZE - 1 - screenRow : screenRow;
                int col = flipped ? Board.SIZE - 1 - screenCol : screenCol;

                boolean isDarkSquare = (row + col) % 2 != 0;
                Piece piece = board.get(row, col);

                String key = buildImageKey(piece, isDarkSquare);
                Image image = loadImage(key);

                double x = offsetX + screenCol * squareSize;
                double y = screenRow * squareSize;
                gc.drawImage(image, x, y, squareSize, squareSize);
            }
        }
    }

    private void drawCoordinateLabels(GraphicsContext gc, double marginX, double boardPixelSize, double squareSize) {
        double fontSize = Math.max(10, marginX * 0.5);
        gc.setFont(Font.font("Sans Serif", FontWeight.BOLD, fontSize));
        gc.setFill(LABEL_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        for (int screenRow = 0; screenRow < Board.SIZE; screenRow++) {
            int row = flipped ? Board.SIZE - 1 - screenRow : screenRow;
            int rank = Board.SIZE - row;
            double y = screenRow * squareSize + squareSize / 2;
            gc.fillText(String.valueOf(rank), marginX / 2, y);
        }

        for (int screenCol = 0; screenCol < Board.SIZE; screenCol++) {
            int col = flipped ? Board.SIZE - 1 - screenCol : screenCol;
            char file = (char) ('a' + col);
            double x = marginX + screenCol * squareSize + squareSize / 2;
            double y = boardPixelSize + marginX / 2;
            gc.fillText(String.valueOf(file), x, y);
        }
    }

    private String buildImageKey(Piece piece, boolean isDarkSquare) {
        char squareCode = isDarkSquare ? 'b' : 'w';
        if (piece == null) {
            return String.valueOf(squareCode);
        }
        return "" + piece.colorCode() + piece.typeCode() + squareCode;
    }

    private Image loadImage(String key) {
        return imageCache.computeIfAbsent(key, this::readImageFromResources);
    }

    private Image readImageFromResources(String key) {
        String path = IMAGE_FOLDER + key + ".png";
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException(
                    "Не найдена картинка фигуры: " + path
                            + " — проверь, что файл лежит в src/main/resources" + path
                            + " и называется точно так (регистр важен).");
        }
        return new Image(stream, CACHE_RESOLUTION, CACHE_RESOLUTION, false, true);
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
