package com.qihang.campusmarket.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ViewDedupService {
    private final Cache<String, Boolean> viewed = Caffeine.newBuilder()
            .maximumSize(20_000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    public boolean firstView(Long productId, String viewerKey) {
        String safeViewer = viewerKey == null || viewerKey.isBlank() ? "anonymous" : viewerKey;
        String key = productId + ":" + safeViewer;
        Boolean existed = viewed.getIfPresent(key);
        if (existed != null) {
            return false;
        }
        viewed.put(key, Boolean.TRUE);
        return true;
    }
}
