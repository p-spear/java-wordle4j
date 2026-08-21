package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {
    public WordleDictionary loadDictionary(String filePath, PrintWriter logger) throws IOException {
        List<String> words = new ArrayList<>();
        logger.println("[WordleDictionaryLoader] Начало загрузки словаря из файла: " + filePath);

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {

            while (reader.ready()) {
                String line = reader.readLine();
                String normalized = normalizeWord(line);
                if (isValidWord(normalized)) {
                    words.add(normalized);
                }
            }
        }
        logger.println("[WordleDictionaryLoader] Словарь успешно загружен. Количество слов: " + words.size());

        return new WordleDictionary(words);
    }

    private String normalizeWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .trim();
    }

    private boolean isValidWord(String word) {
        return word.length() == WordleGame.WORD_LENGTH && word.matches("[а-я]+");
    }
}
