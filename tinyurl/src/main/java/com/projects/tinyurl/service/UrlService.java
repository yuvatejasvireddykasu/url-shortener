package com.projects.tinyurl.service;

import com.projects.tinyurl.entity.UrlMapping;
import com.projects.tinyurl.exception.UrlNotFoundException;
import com.projects.tinyurl.repository.UrlRepository;
import com.projects.tinyurl.util.Base62Encoder;
import org.hibernate.dialect.function.DB2SubstringFunction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class UrlService {

    private final UrlRepository urlRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public UrlService(UrlRepository urlRepository, RedisTemplate<String, String> redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    private static final String CACHE_PREFIX = "short:";


    //2 functions: createShortUrl(longUrl) and getLongUrl(shortCode)

    public String createShortUrl(String longUrl) {

        UrlMapping urlMapping = new UrlMapping();

        urlMapping.setLongUrl(longUrl);

        urlRepository.save(urlMapping);

        String shortCode = Base62Encoder.encode(urlMapping.getId());

        urlMapping.setShortCode(shortCode);
        urlRepository.save(urlMapping);

        //save to redis
        try {
            redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, longUrl);
        } catch (Exception e) {
            // Log and continue
            System.err.println("Redis unavailable, skipping cache write");
        }
        return shortCode;
    }

    // STEP 2: Get original URL
    public String getLongUrl(String shortCode) {

        // check Redis cache first
        String cachedKey = CACHE_PREFIX + shortCode;
        try{
        String cachedLongUrl = redisTemplate.opsForValue().get(cachedKey);
        if (cachedLongUrl != null) {
            return cachedLongUrl;
        }}catch (Exception e){
            // Log and continue
            System.err.println("Redis unavailable, falling back to DB");
        }

        // Redis miss → DB lookup
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found"));

        //  Try to repopulate Redis
        try {
            redisTemplate.opsForValue()
                    .set(cachedKey, mapping.getLongUrl());
        } catch (Exception e) {
            // Ignore cache write failure
        }


        return mapping.getLongUrl();
    }
}
