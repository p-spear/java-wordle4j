package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

public class WordleGame {

    public static final int MAX_STEPS = 6;
    public static final int WORD_LENGTH = 5;
    private String answer;
    private int steps;
    private WordleDictionary dictionary;
    private PrintWriter logger;
    private boolean gameOver = false;
    private boolean won = false;

    private final List<String> candidates = new ArrayList<>();
    private final Random random = new Random();

    public WordleGame(WordleDictionary dictionary, PrintWriter logger) throws WordleEmptyCandidatesException {
        this.dictionary = dictionary;
        this.logger = logger;
        this.steps = MAX_STEPS;

        if (dictionary.getWords().isEmpty()) {
            logger.println("[WordleGame] ОШИБКА: Словарь пуст");
            throw new WordleEmptyCandidatesException("Словарь пуст");
        }
        this.answer = dictionary.getRandomWord();
        this.candidates.addAll(dictionary.getWords());
        logger.println("[WordleGame] Игра создана. Загадано слово: " + answer);
    }

    public String makeGuess(String guess) throws WordNotFoundInDictionaryException,
            WordleGameWrongWordLengthException,
            WordleGameWrongWordException {
        String normalizedGuess = guess.toLowerCase().replace('ё', 'е');
        logger.println("[WordleGame] Попытка ввода: " + normalizedGuess);

        if (normalizedGuess.length() != 5) {
            logger.println("[WordleGame] ОШИБКА: Неверная длина слова - " + normalizedGuess.length());
            throw new WordleGameWrongWordLengthException("Слово должно состоять из 5 букв");
        }

        if (!normalizedGuess.matches("[а-я]+")) {
            logger.println("[WordleGame] ОШИБКА: Недопустимые символы в слове - " + normalizedGuess);
            throw new WordleGameWrongWordException("Слово должно содержать только русские буквы");
        }

        if (!dictionary.contains(normalizedGuess)) {
            logger.println("[WordleGame] ОШИБКА: Слово не найдено в словаре - " + normalizedGuess);
            throw new WordNotFoundInDictionaryException("Слово не найдено в словаре");
        }

        if (normalizedGuess.equals(answer)) {
            gameOver = true;
            won = true;
            logger.println("[WordleGame] ПОБЕДА! Слово угадано: " + answer);

            return String.valueOf('+').repeat(WORD_LENGTH);
        }

        String feedback = generateFeedback(normalizedGuess);
        steps--;

        logger.println("[WordleGame] Результат анализа: " + feedback + ". Осталось попыток: " + steps);

        if (steps == 0) {
            gameOver = true;
            won = false;
            logger.println("[WordleGame] ПОРАЖЕНИЕ. Загадано: " + answer);
        } else {
            updateCandidates(normalizedGuess, feedback);
        }

        return feedback;
    }

    private String generateFeedback(String guess) {
        StringBuilder feedback = new StringBuilder();
        boolean[] answerUsed = new boolean[answer.length()];
        boolean[] guessUsed = new boolean[guess.length()];

        for (int i = 0; i < answer.length(); i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                feedback.append('+');
                answerUsed[i] = true;
                guessUsed[i] = true;
            } else {
                feedback.append(' ');
            }
        }

        for (int i = 0; i < guess.length(); i++) {
            if (guessUsed[i]) continue;

            char c = guess.charAt(i);
            boolean found = false;

            for (int j = 0; j < answer.length(); j++) {
                if (!answerUsed[j] && answer.charAt(j) == c) {
                    feedback.setCharAt(i, '^');
                    answerUsed[j] = true;
                    found = true;
                    break;
                }
            }

            if (!found) {
                feedback.setCharAt(i, '-');
            }
        }

        return feedback.toString();
    }

    private void updateCandidates(String guess, String feedback) {
        List<String> filtered = new ArrayList<>();

        for (String word : candidates) {
            if (matchesPattern(word, guess, feedback)) {
                filtered.add(word);
            }
        }

        candidates.clear();
        candidates.addAll(filtered);
        logger.println("[WordleGame] Обновлены кандидаты для подсказок. Количество: " + candidates.size());
    }

    private boolean matchesPattern(String word, String guess, String feedback) {
        // Сначала проверяем все зелёные
        for (int i = 0; i < feedback.length(); i++) {
            char f = feedback.charAt(i);
            if (f == '+') {
                if (word.charAt(i) != guess.charAt(i)) {
                    return false;
                }
            }
        }

        // Считаем вхождения букв в слове
        Map<Character, Integer> wordLetterCount = new HashMap<>();
        for (char c : word.toCharArray()) {
            wordLetterCount.put(c, wordLetterCount.getOrDefault(c, 0) + 1);
        }

        // Вычитаем зелёные буквы из счётчика
        for (int i = 0; i < feedback.length(); i++) {
            if (feedback.charAt(i) == '+') {
                char c = guess.charAt(i);
                wordLetterCount.put(c, wordLetterCount.get(c) - 1);
            }
        }

        // Проверяем жёлтые буквы
        for (int i = 0; i < feedback.length(); i++) {
            char f = feedback.charAt(i);
            if (f == '^') {
                char c = guess.charAt(i);
                // Буква не должна стоять на этой позиции
                if (word.charAt(i) == c) {
                    return false;
                }
                // Буква должна быть в слове
                if (!wordLetterCount.containsKey(c) || wordLetterCount.get(c) <= 0) {
                    return false;
                }
                // Уменьшаем счётчик для этой буквы
                wordLetterCount.put(c, wordLetterCount.get(c) - 1);
            }
        }

        // Проверяем серые буквы
        for (int i = 0; i < feedback.length(); i++) {
            char f = feedback.charAt(i);
            if (f == '-') {
                char c = guess.charAt(i);
                // Если буква осталась в счётчике, слово не подходит
                if (wordLetterCount.containsKey(c) && wordLetterCount.get(c) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getHint() throws WordleEmptyCandidatesException {
        if (candidates.isEmpty()) {
            logger.println("[WordleGame] ОШИБКА: Нет кандидатов для подсказки");
            throw new WordleEmptyCandidatesException("Нет подходящих слов");
        }
        String hint = candidates.get(random.nextInt(candidates.size()));
        logger.println("[WordleGame] Выдана подсказка: " + hint);
        return hint;
    }

    public String getAnswer() {
        return answer;
    }

    public int getSteps() {
        return steps;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }

}
