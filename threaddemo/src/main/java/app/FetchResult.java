package app;

import lombok.Data;

public record FetchResult(
        String url,
        int statusCode,
        int responseSize,
        long durationMs,
        String threadName,
        String content
) {}