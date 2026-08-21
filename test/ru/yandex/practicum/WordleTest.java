package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Общие тесты всех компонентов Wordle")
class WordleTest {

    private WordleDictionary dictionary;

    private PrintWriter logger;
    private StringWriter logWriter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dictionary = new WordleDictionary(Arrays.asList("герой", "книга", "кошка", "пенал", "полет"));
        logWriter = new StringWriter();
        logger = new PrintWriter(logWriter, true);
    }

    @Test
    @DisplayName("Загрузка корректного файла возвращает только слова нормализованные слова длинной 5 букв")
    void loadDictionary_ValidFile_ReturnsCorrectWords() throws IOException {
        Path dictFile = tempDir.resolve("words.txt");
        String content = """
                привет
                мир
                конёк
                дом
                книга
                стол
                яблоко
                apple
                """;
        Files.writeString(dictFile, content, StandardCharsets.UTF_8);

        StringWriter logString = new StringWriter();
        PrintWriter localLogger = new PrintWriter(logString);
        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary dict = loader.loadDictionary(dictFile.toString(), localLogger);

        List<String> words = dict.getWords();
        assertEquals(2, words.size());
        assertTrue(words.contains("конек"));
        assertTrue(words.contains("книга"));
    }

    @Test
    @DisplayName("Пустой файл возвращает пустой словарь")
    void loadDictionary_EmptyFile_ReturnsEmptyDictionary() throws IOException {
        Path dictFile = tempDir.resolve("empty.txt");
        Files.writeString(dictFile, "", StandardCharsets.UTF_8);

        StringWriter logString = new StringWriter();
        PrintWriter localLogger = new PrintWriter(logString);
        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary dict = loader.loadDictionary(dictFile.toString(), localLogger);

        assertTrue(dict.getWords().isEmpty());
    }

    @Test
    @DisplayName("getWords возвращает неизменяемый список")
    void getWords_ReturnsUnmodifiableList() {
        List<String> words = dictionary.getWords();
        try {
            words.add("новое");
            fail("Ожидалось UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertNotNull(e);
        }
        assertEquals(5, words.size());
    }

    @Test
    @DisplayName("contains возвращает true для существующего слова")
    void contains_ExistingWord_ReturnsTrue() {
        assertTrue(dictionary.contains("герой"));
        assertTrue(dictionary.contains("книга"));
    }

    @Test
    @DisplayName("contains возвращает false для отсутствующего слова")
    void contains_NotExistingWord_ReturnsFalse() {
        assertFalse(dictionary.contains("абвгд"));
        assertFalse(dictionary.contains("привет"));
    }

    @Test
    @DisplayName("getRandomWord возвращает слово из словаря")
    void getRandomWord_ReturnsWordFromDictionary() {
        String word = dictionary.getRandomWord();
        assertNotNull(word);
        assertTrue(dictionary.contains(word));
    }

    @Test
    @DisplayName("Правильное заполнение конструктора при создании игры")
    void constructor_ValidDictionary_CreatesGameWithMaxSteps() throws Exception {
        WordleGame game = new WordleGame(dictionary, logger);
        assertEquals(WordleGame.MAX_STEPS, game.getSteps());
        assertFalse(game.isGameOver());
        assertNotNull(game.getAnswer());
        assertEquals(WordleGame.WORD_LENGTH, game.getAnswer().length());
    }

    @Test
    @DisplayName("Угадывание правильного слова возвращает '+++++', присваивает победу и закрывает игру")
    void makeGuess_CorrectWord_WinsAndReturnsAllPlus() throws Exception {
        WordleDictionary singleDict = new WordleDictionary(Collections.singletonList("герой"));
        WordleGame game = new WordleGame(singleDict, logger);

        String feedback = game.makeGuess("герой");
        assertEquals("+++++", feedback);
        assertTrue(game.isGameOver());
        assertTrue(game.isWon());
    }

    @Test
    @DisplayName("Слово в верхнем регистре с буквой 'ё' нормализуется и побеждает")
    void makeGuess_CorrectWordWithYo_NormalizesAndWins() throws Exception {
        WordleDictionary singleDict = new WordleDictionary(Collections.singletonList("полет"));
        WordleGame game = new WordleGame(singleDict, logger);

        String feedback = game.makeGuess("ПОЛЁТ");
        assertEquals("+++++", feedback);
        assertTrue(game.isGameOver());
        assertTrue(game.isWon());
    }

    @Test
    @DisplayName("Частичное совпадение возвращает корректный feedback")
    void makeGuess_PartialMatch_ReturnsCorrectFeedback() throws Exception {
        WordleDictionary twoWords = new WordleDictionary(Arrays.asList("герой", "горец"));
        WordleGame game = new WordleGame(twoWords, logger);
        String answer = game.getAnswer();
        String guess = answer.equals("герой") ? "горец" : "герой";
        String feedback = game.makeGuess(guess);
        assertEquals("+^+^-", feedback);
        assertFalse(game.isGameOver());
        assertEquals(5, game.getSteps());
    }

    @Test
    @DisplayName("Слово отсутствует в словаре вызывает исключение WordNotFoundInDictionaryException")
    void makeGuess_WordNotInDictionary_ThrowsWordNotFoundInDictionaryException() throws Exception {
        WordleGame game = new WordleGame(dictionary, logger);

        try {
            game.makeGuess("абвгд");
            fail("Ожидалось WordNotFoundInDictionaryException");
        } catch (WordNotFoundInDictionaryException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("После хода кандидаты фильтруются, и подсказка берётся из них")
    void getHint_AfterSomeGuesses_FiltersCandidates() throws Exception {
        WordleDictionary twoWordDict = new WordleDictionary(Arrays.asList("герой", "книга"));
        WordleGame game = new WordleGame(twoWordDict, logger);
        String answer = game.getAnswer();

        String guess = "книга".equals(answer) ? "герой" : "книга";
        game.makeGuess(guess);

        String hint = game.getHint();
        assertNotNull(hint);
        assertEquals(answer, hint, "Подсказка должна быть равна загаданному слову, так как оно единственный кандидат");
    }

    @Test
    @DisplayName("После использования всех попыток игра завершена и присваивается поражение")
    void makeGuess_LossGameOver() throws Exception {
        WordleDictionary multiDict = dictionary;
        WordleGame game = new WordleGame(multiDict, logger);
        String answer = game.getAnswer();
        for (int i = 0; i < WordleGame.MAX_STEPS; i++) {
            if (!game.isGameOver()) {
                String guess = "книга".equals(answer) ? "кошка" : "книга";
                game.makeGuess(guess);
            }
        }
        assertTrue(game.isGameOver());
        assertFalse(game.isWon());
    }
}
