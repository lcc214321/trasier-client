package com.trasier.client.spring.spancontrol;

import java.util.regex.Pattern;

public class TrasierSampleByUrlPatternConfiguration {

    private static final Pattern DEFAULT_SKIP_PATTERN = Pattern.compile(
            "/api-docs.*|/autoconfig|/configprops|/dump|/health|/info|/metrics.*|" +
                    "/mappings|/swagger.*|.*\\.png|.*\\.css|.*\\.js|.*\\.html|/favicon.ico|/hystrix.stream|" +
                    ".*/checkServlet|/admin/check");

    private Pattern skipPattern;
    private Pattern defaultSkipPattern = DEFAULT_SKIP_PATTERN;

    public Pattern getSkipPattern() {
        return skipPattern;
    }

    public void setSkipPattern(String skipPattern) {
        this.skipPattern = Pattern.compile(skipPattern);
    }

    public Pattern getDefaultSkipPattern() {
        return defaultSkipPattern;
    }

    public void setDefaultSkipPattern(String pattern) {
        this.defaultSkipPattern = Pattern.compile(pattern);
    }

}
