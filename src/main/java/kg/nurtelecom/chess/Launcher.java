package kg.nurtelecom.chess;

import javafx.application.Application;

/**
 * Точка входа приложения (запускай именно этот класс).
 * <p>
 * Он намеренно НЕ наследует {@link Application} — только вызывает
 * {@link Application#launch}. Если бы main() был прямо в ChessApplication
 * (который extends Application), JVM потребовала бы JavaFX на module-path,
 * даже несмотря на то, что все нужные jar-файлы и так есть в classpath.
 * Разделение на два класса — стандартный обходной путь для non-modular
 * JavaFX-проектов.
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(ChessApplication.class, args);
    }
}
