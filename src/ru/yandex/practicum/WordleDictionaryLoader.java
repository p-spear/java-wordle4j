package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {
    public WordleDictionary loadDictionary(String filePath) throws IOException {
        List<String> words = new ArrayList<>();

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

        return new WordleDictionary(words);
    }

    private String normalizeWord(String word) {
        return word.toLowerCase()
                .replace('ё', 'е')
                .trim();
    }

    private boolean isValidWord(String word) {
        return word.length() == 5 && word.matches("[а-я]+");
    }
}
