package com.projects.tinyurl.service;

import com.projects.tinyurl.entity.UrlMapping;
import com.projects.tinyurl.exception.UrlNotFoundException;
import com.projects.tinyurl.repository.UrlRepository;
import com.projects.tinyurl.util.Base62Encoder;
import org.hibernate.dialect.function.DB2SubstringFunction;
import org.springframework.stereotype.Service;


@Service
public class UrlService {

    private final UrlRepository urlRepository;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    //2 functions: createShortUrl(longUrl) and getLongUrl(shortCode)

    public String createShortUrl(String longUrl) {

        UrlMapping urlMapping = new UrlMapping();

        urlMapping.setLongUrl(longUrl);

        urlRepository.save(urlMapping);

        String shortCode = Base62Encoder.encode(urlMapping.getId());

        urlMapping.setShortCode(shortCode);
        urlRepository.save(urlMapping);
        return shortCode;
    }

    // STEP 2: Get original URL
    public String getLongUrl(String shortCode) {
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found"));

        return mapping.getLongUrl();
    }
}
