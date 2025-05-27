package isep.crescendo.model.crescendo.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TokenInfo {
    private final String email;
    private final LocalDateTime createdAt;
    private static final Duration VALIDITY = Duration.ofMinutes(5);

    public TokenInfo(String email) {
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public boolean isExpired() {
        return createdAt.plus(VALIDITY).isBefore(LocalDateTime.now());
    }
}
