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
 * Рисует доску картинками из src/main/resources/png и подписи координат.
 * <p>
 * Визуального "перетаскивания" фигуры за курсором сознательно нет: картинки
 * фигур содержат вшитый фон клетки (без прозрачности), поэтому при таком
 * перетаскивании вместе с фигурой "ехал" бы ещё и чужой фон. Вместо этого
 * фигура переставляется одним действием сразу в момент отпускания кнопки
 * мыши (см. GameController) — доска просто перерисовывается целиком.
 */
public class BoardView extends Canvas {

    private static final int CACHE_RESOLUTION = 256;
    private static final String IMAGE_FOLDER = "/png/";
    private static final double MARGIN_RATIO = 0.055;
    private static final Color LABEL_COLOR = Color.web("#DDDDDD");

    private final Map<String, Image> imageCache = new HashMap<>();

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

    /**
     * Переводит пиксельные координаты клика в клетку доски (row, col) в системе координат Board.
     * Возвращает null, если клик пришёлся на полосу с подписями или мимо доски.
     */
    public int[] pointToSquare(double x, double y) {
        double size = getWidth();
        double margin = size * MARGIN_RATIO;
        double boardPixelSize = size - margin;
        double squareSize = boardPixelSize / Board.SIZE;

        if (x < margin || x >= size || y < 0 || y >= boardPixelSize) {
            return null;
        }

        int screenCol = (int) ((x - margin) / squareSize);
        int screenRow = (int) (y / squareSize);
        screenCol = Math.max(0, Math.min(Board.SIZE - 1, screenCol));
        screenRow = Math.max(0, Math.min(Board.SIZE - 1, screenRow));

        int row = flipped ? Board.SIZE - 1 - screenRow : screenRow;
        int col = flipped ? Board.SIZE - 1 - screenCol : screenCol;
        return new int[]{row, col};
    }

    private void drawSquaresAndPieces(GraphicsContext gc, Board board, double offsetX, double squareSize) {
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

    // Без этого JavaFX считает ТЕКУЩИЙ размер канваса его минимальным размером —
    // а раз мы сами постоянно увеличиваем канвас через привязку, минимальный
    // размер окна незаметно растёт следом, и окно потом физически не сжимается
    // обратно. Явно говорим: минимум — 0, максимум — не ограничен.
    @Override
    public double minWidth(double height) {
        return 0;
    }

    @Override
    public double minHeight(double width) {
        return 0;
    }

    @Override
    public double maxWidth(double height) {
        return Double.MAX_VALUE;
    }

    @Override
    public double maxHeight(double width) {
        return Double.MAX_VALUE;
    }
}
