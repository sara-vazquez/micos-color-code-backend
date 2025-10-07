package dev.sara.micos_color_code.Captcha;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private record CaptchaRecord(int answer, Instant expiresAt) {}

    private final Map<String, CaptchaRecord> store = new HashMap<>();
    private record Operation(String question, int answer){}

    private final List<Supplier<Operation>> generators = List.of(
        () -> {
            int a = rand(1, 10);
            int b = rand(1, 10);
            int c = rand(1, 10);
            return new Operation(String.format("¿Cuánto es %d × %d + %d?", a, b, c), a * b + c);
        },
        () -> {
            int a = rand(1, 10);
            int b = rand(1, 5);
            int c = b * rand(1, 5);
            return new Operation(String.format("¿Cuánto es %d − %d ÷ %d?", c * b, c * b, b), c * b - (c * b / b));
        },
        () -> {
            int a = rand(1, 10);
            int b = rand(1, 10);
            int c = rand(1, 10);
            return new Operation(String.format("¿Cuánto es %d + %d × %d?", a, b, c), a + (b * c));
        }
    );

    public CaptchaChallenge generate() {
        Operation op = generators.get(rand(0, generators.size() - 1)).get();

        String id = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(60);

        store.put(id, new CaptchaRecord(op.answer(), expiresAt));
        return new CaptchaChallenge(id, op.question());
    }

    public boolean validate(String id, int answer) {
        CaptchaRecord record = store.remove(id);
        if (record == null) return false;
        return Instant.now().isBefore(record.expiresAt()) && record.answer() == answer;
    }

    private static int rand(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public record CaptchaChallenge(String id, String question) {}
}
