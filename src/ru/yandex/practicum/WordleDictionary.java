package ru.yandex.practicum;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class WordleDictionary {

    private final List<String> words = new ArrayList<>();
    private final Random random = new Random();

    public WordleDictionary(List<String> words) {
        this.words.addAll(words);
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    public String getRandomWord() {
        return words.get(random.nextInt(words.size()));
    }

    public boolean contains(String word) {
        return words.contains(word);
    }
}
