package dev.sara.micos_color_code.Captcha;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private record CaptchaRecord(int answer, Instant expiresAt) {}

    private final Map<String, CaptchaRecord> store = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public CaptchaService() {
        //periodic cleanup
    }
    
}
