package ru.yandex.practicum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты загрузчика словаря")
class WordleDictionaryLoaderTest {

    @TempDir
    Path tempDir;

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
        PrintWriter logger = new PrintWriter(logString);
        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary dict = loader.loadDictionary(dictFile.toString(), logger);

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
        PrintWriter logger = new PrintWriter(logString);
        WordleDictionaryLoader loader = new WordleDictionaryLoader();
        WordleDictionary dict = loader.loadDictionary(dictFile.toString(), logger);

        assertTrue(dict.getWords().isEmpty());
    }
}
