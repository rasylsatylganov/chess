package kg.nurtelecom.chess.api;


/**
 * Переводит "уровень сложности" (1 — играет быстро и слабо, 10 — думает
 * дольше и считает на бОльшую глубину) в параметры запроса к chess-api.com.
 * <p>
 * По документации сервиса depth ограничен 18, maxThinkingTime — 100 мс
 * (значения выше доступны только спонсорам проекта, поэтому уровень 10
 * не гарантирует буквально максимум возможностей Stockfish — тут мы
 * ограничены тем, что даёт бесплатный тариф).
 */
public record EngineDifficulty(int depth, int maxThinkingTimeMs) {

    private static final EngineDifficulty[] LEVELS = {
            new EngineDifficulty(1, 10),   // 1 — совсем слабо и быстро
            new EngineDifficulty(2, 15),   // 2
            new EngineDifficulty(3, 20),   // 3
            new EngineDifficulty(5, 25),   // 4
            new EngineDifficulty(7, 30),   // 5 — средний уровень
            new EngineDifficulty(9, 40),  // 6
            new EngineDifficulty(11, 50),  // 7
            new EngineDifficulty(13, 60),  // 8
            new EngineDifficulty(15, 80),  // 9
            new EngineDifficulty(18, 100), // 10 — максимум бесплатного тарифа
    };

    public static EngineDifficulty forLevel(int level) {
        int clamped = Math.max(1, Math.min(10, level));
        return LEVELS[clamped - 1];
    }
}
