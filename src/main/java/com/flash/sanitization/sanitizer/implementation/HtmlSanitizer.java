package com.flash.sanitization.sanitizer.implementation;

import org.springframework.stereotype.Component;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * A very simple html-sanitizer using {@link Jsoup}.
 * Added just so the sanitizer pipeline has some other options available to it.
 */
@Component("html-sanitizer")
public class HtmlSanitizer implements NoConfigSanitizer {

    @Override
    public String sanitize(String toSanitize) {
        return Jsoup.clean(toSanitize, Safelist.basic());
    }
}
