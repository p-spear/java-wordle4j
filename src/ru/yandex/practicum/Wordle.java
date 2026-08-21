package ru.yandex.practicum;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Wordle {
    private static final String LOG_FILE = "log.txt";
    private static final String DICTIONARY_FILE = "words_ru.txt";

    public static void main(String[] args) {
        try (PrintWriter logger = new PrintWriter(new FileWriter(LOG_FILE))) {

            logger.println("[Wordle] Лог-файл успешно создан: " + LOG_FILE);
            logger.println("[Wordle] === НАЧАЛО ИГРЫ ===");

            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.loadDictionary(DICTIONARY_FILE, logger);

            WordleGame game = new WordleGame(dictionary, logger);
            logger.println("[Wordle] Загадано слово: " + game.getAnswer());
            logger.println("[Wordle] Количество попыток: " + WordleGame.MAX_STEPS);

            Scanner scanner = new Scanner(System.in);
            System.out.println("Добро пожаловать в Wordle!");
            System.out.println("Угадайте слово из " + WordleGame.WORD_LENGTH + " букв. У вас " + WordleGame.MAX_STEPS + " попыток.");
            System.out.println("Подсказки: + (правильная позиция), ^ (есть в слове), - (нет)");
            System.out.println("Для подсказки нажмите Enter без ввода слова.");
            System.out.println();

            while (!game.isGameOver()) {
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) {
                    logger.println("[Wordle] Запрос подсказки от игрока");
                    try {
                        String hint = game.getHint();
                        System.out.println("Подсказка:\n" + hint);
                        logger.println("[Wordle] Подсказка выдана: " + hint);
                        input = hint;
                    } catch (WordleEmptyCandidatesException e) {
                        System.out.println("Нет подходящих слов для подсказки");
                        logger.println("[Wordle] Ошибка подсказки: " + e.getMessage());
                    }
                    //continue;
                }

                logger.println("[Wordle] Ввод игрока: " + input);

                try {
                    String feedback = game.makeGuess(input);
                    System.out.println(feedback);
                    logger.println("[Wordle] Результат анализа: " + feedback);
                    logger.println("[Wordle] Осталось попыток: " + game.getSteps());

                    if (game.isGameOver()) {
                        if (game.isWon()) {
                            System.out.println("Поздравляем! Вы угадали!");
                            logger.println("[Wordle] ИГРА ЗАВЕРШЕНА. РЕЗУЛЬТАТ: ПОБЕДА");
                        } else {
                            System.out.println("Проигрыш! Загадано: " + game.getAnswer());
                            logger.println("[Wordle] ИГРА ЗАВЕРШЕНА. РЕЗУЛЬТАТ: ПОРАЖЕНИЕ");
                        }
                    }
                } catch (WordleGameWrongWordLengthException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                    logger.println("[Wordle] Ошибка ввода (длина слова): " + e.getMessage() + ". Введено: " + input);
                } catch (WordleGameWrongWordException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                    logger.println("[Wordle] Ошибка ввода (недопустимые символы): " + e.getMessage() + ". Введено: " + input);
                } catch (WordNotFoundInDictionaryException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                    logger.println("[Wordle] Ошибка ввода (слово не в словаре): " + e.getMessage() + ". Введено: " + input);
                } catch (Exception e) {
                    logger.println("[Wordle] НЕОЖИДАННАЯ ОШИБКА: " + e);
                    e.printStackTrace(logger);
                }
            }

            logger.println("[Wordle] === КОНЕЦ ИГРЫ ===");
            System.out.println("Спасибо за игру!");

        } catch (FileNotFoundException e) {
            System.err.println("Файл словаря не найден: " + LOG_FILE);
        } catch (IOException e) {
            System.err.println("Ошибка создания лог-файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
