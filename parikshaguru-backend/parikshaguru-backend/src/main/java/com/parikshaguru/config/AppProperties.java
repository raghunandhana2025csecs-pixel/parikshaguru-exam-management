package com.parikshaguru.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Getter @Setter
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Auth auth = new Auth();
    private final Cors cors = new Cors();
    private final Seed seed = new Seed();

    @Getter @Setter public static class Jwt { private String secret; private long expirationMinutes = 480; }
    @Getter @Setter public static class Auth {
        private List<String> allowedEmailDomains = new ArrayList<>();
        private String facultyInviteCode;
        private boolean studentAutoActivate = true;
        private boolean exposeResetToken = false;
        private int resetTokenMinutes = 30;
    }
    @Getter @Setter public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
        private List<String> allowedMethods = new ArrayList<>();
        private List<String> allowedHeaders = new ArrayList<>();
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }
    @Getter @Setter public static class Seed {
        private boolean enabled = true; private boolean demoData = true;
        private String adminName; private String adminEmail; private String adminPassword;
    }
}
