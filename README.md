# 🔐 JWT Auth Starter
### A Custom Spring Boot Starter for Zero-Boilerplate JWT Security

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange)](https://adoptium.net/)
[![JJWT](https://img.shields.io/badge/JJWT-0.12.5-blue)](https://github.com/jwtk/jjwt)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 📌 What Is This?

**JWT Auth Starter** is a reusable Spring Boot library (custom starter) that
automatically configures complete JWT-based authentication and authorization
for any Spring Boot application.

Instead of writing the same JWT security code in every project, simply add
**one dependency** — and the entire security layer is ready.

---

## 🎯 Who Should Use This?

| You Are | Use Case |
|---|---|
| **Student / Beginner** | Learning how Spring Security and JWT work together |
| **Backend Developer** | Building REST APIs that need token-based authentication |
| **Team Lead** | Standardizing JWT security across multiple microservices |
| **Freelancer** | Quickly bootstrapping secure Spring Boot projects |

---

## ✅ What You Get Automatically

After adding the dependency, your project instantly has:

- 🔑 **JWT Token Generation** — create signed tokens after login
- 🛡️ **Request Interception** — every API call is automatically validated
- 🚫 **401 Unauthorized** — clean JSON response when token is missing
- ⛔ **403 Forbidden** — clean JSON response when user lacks permission
- 🔒 **BCrypt Password Encoder** — for secure password hashing
- ⚙️ **Configurable Properties** — control everything from `application.properties`
- 🧩 **Full Extensibility** — override any component with your own implementation

---

## 📋 Prerequisites

Before using this library, make sure you have:

```
✅ Java 17 or higher
✅ Maven 3.6 or higher
✅ Spring Boot 3.x project
✅ Library installed locally (see Step 1 below)
```

Verify your setup:
```bash
java -version    # Should show 17.x.x or higher
mvn -version     # Should show 3.6.x or higher
```

---

## 🚀 Quick Start — 4 Steps

### Step 1 — Install the Library Locally

Download the source and run this command inside `jwt-auth-starter-parent/` folder:

```bash
mvn clean install -DskipTests
```

Wait for:
```
[INFO] BUILD SUCCESS
```

---

### Step 2 — Add Dependency to Your Project

Open your project's `pom.xml` and add inside :

```xml

<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>

<!-- JWT Auth Starter — One dependency for complete JWT security -->
<dependency>
    <groupId>com.github.HimanshuSingh924.jwt-auth-starter</groupId>
    <artifactId>jwt-auth-autoconfigure</artifactId>
    <version>1.0.1</version>   <!-- update version -->
</dependency>
```

> ⚠️ **No other security dependencies needed.** This starter automatically
> pulls in Spring Security, JJWT, and Spring Web.

---

### Step 3 — Configure `application.properties`

Add these properties to your `src/main/resources/application.properties`:

```properties
# ════════════════════════════════════════════════════════════
#  JWT Auth Starter — Minimum Required Configuration
# ════════════════════════════════════════════════════════════

# Your signing secret — Base64-encoded, minimum 32 characters
# ⚠️ CHANGE THIS in production! Use environment variables.
auth.jwt.secret=${JWT_SECRET}

# Token expiry in milliseconds (3600000 = 1 hour)
auth.jwt.expiration=3600000

# URL patterns that do NOT require a JWT token (comma-separated)
auth.jwt.public-endpoints=/api/auth/**,/api/public/**,/actuator/health
```

That's it. Your app is now JWT-secured. 🎉

---

### Step 4 — Provide Your UserDetailsService

This is the **only class you must write** — it tells the library how to
load your users from your database:

```java
@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Your JPA repository

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return userRepository.findByEmail(email)
                .map(user -> User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())  // BCrypt hash
                        .authorities(List.of(
                            new SimpleGrantedAuthority(user.getRole())))
                        .build())
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not found: " + email));
    }
}
```

> 💡 The `@Service` annotation is enough — the starter detects it automatically
> and uses your implementation instead of the built-in placeholder.

---

## ⚙️ All Configuration Properties

Copy this complete block into your `application.properties` and customize as needed:

```properties
# ════════════════════════════════════════════════════════════
#  JWT Auth Starter — Complete Configuration Reference
#  All properties have defaults. Only 'secret' is mandatory.
# ════════════════════════════════════════════════════════════

# ── Required ──────────────────────────────────────────────────────────────
# Base64-encoded HMAC-SHA256 secret key
# Minimum 32 characters (256-bit) for HS256 algorithm
# Production: set via environment variable → AUTH_JWT_SECRET=...
auth.jwt.secret=dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWtleS1mb3ItZGV2bGliLXN0YXJ0ZXIh

# ── Optional (defaults shown) ──────────────────────────────────────────────
# Token validity in milliseconds
# 3600000   = 1 hour
# 86400000  = 24 hours  (default)
# 604800000 = 7 days
auth.jwt.expiration=86400000

# Prefix expected before token in the Authorization header
# Default: Bearer  →  Authorization: Bearer eyJhbGci...
auth.jwt.token-prefix=Bearer

# HTTP header name that carries the JWT
# Default: Authorization
auth.jwt.header-name=Authorization

# Comma-separated URL patterns that bypass JWT authentication
# Supports Ant-style patterns: **, *, ?
# Default: /api/auth/**, /actuator/health
auth.jwt.public-endpoints=/api/auth/**,/api/public/**,/actuator/health,/h2-console/**
```

---

## 🔑 Issuing Tokens — Login Endpoint Example

Inject `JwtTokenProvider` from the library to generate tokens:

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // ✅ Auto-injected from the starter — no extra config needed
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // 1. Load user from your database
        UserDetails user = userDetailsService
                .loadUserByUsername(request.email());

        // 2. Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }

        // 3. Build Authentication object
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        // 4. Generate JWT using the starter's token provider
        String token = jwtTokenProvider.generateToken(auth);

        // 5. Return token to the client
        return ResponseEntity.ok(Map.of(
                "token", token,
                "type",  "Bearer",
                "email", user.getUsername()
        ));
    }

    public record LoginRequest(String email, String password) {}
}
```

**Request:**
```http
POST /api/auth/login
Content-Type: application/json

{
    "email": "alice@example.com",
    "password": "password123"
}
```

**Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZ...",
    "type":  "Bearer",
    "email": "alice@example.com"
}
```

---

## 🛡️ Protecting Endpoints

### Method 1 — URL-level (via properties)

Everything NOT in `auth.jwt.public-endpoints` is automatically protected:

```properties
# These need no token:
auth.jwt.public-endpoints=/api/auth/**,/api/public/**

# Everything else → automatically requires valid JWT
```

### Method 2 — Role-level (via @PreAuthorize)

```java
@RestController
@RequestMapping("/api")
public class ResourceController {

    // Any valid token
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(Map.of(
                "email", user.getUsername(),
                "roles", user.getAuthorities()
        ));
    }

    // Only ROLE_ADMIN can access
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> adminDashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome Admin!"));
    }

    // ROLE_USER or ROLE_ADMIN
    @GetMapping("/user/data")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> userData() {
        return ResponseEntity.ok(Map.of("data", "Your data here"));
    }
}
```

**Using the token in requests:**
```http
GET /api/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 🔄 Error Responses

The library returns clean JSON for all security errors — no HTML pages:

**401 Unauthorized** (missing or invalid token):
```json
{
    "status":  401,
    "error":   "Unauthorized",
    "message": "Authentication token is missing or invalid.",
    "path":    "/api/me"
}
```

**403 Forbidden** (valid token but wrong role):
```json
{
    "status":  403,
    "error":   "Forbidden",
    "message": "You do not have permission to access this resource.",
    "path":    "/api/admin/dashboard"
}
```

---

## 🧩 Overriding Default Behavior

Every component is replaceable. Just declare your own bean — the starter detects it and skips its default.

### Override SecurityFilterChain (Custom URL rules)

```java
@Configuration
public class MySecurityConfig {

    @Bean  // ← This prevents the starter's default chain from loading
    public SecurityFilterChain customChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {

        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### Override PasswordEncoder

```java
@Bean
public PasswordEncoder argon2Encoder() {
    return new Argon2PasswordEncoder(16, 32, 1, 65536, 10);
}
```

### Override JwtTokenProvider (e.g., RS256 instead of HS256)

```java
@Bean
public JwtTokenProvider customTokenProvider(JwtProperties props) {
    return new RS256JwtTokenProvider(props);
}
```

---

## 📁 Project Structure (For Developers)

```
jwt-auth-starter-parent/
│
├── jwt-auth-autoconfigure/           ← All logic and beans
│   └── src/main/
│       ├── java/com/devlib/auth/
│       │   ├── JwtAuthAutoConfiguration.java   ← Master @AutoConfiguration
│       │   ├── config/
│       │   │   ├── JwtProperties.java           ← @ConfigurationProperties
│       │   │   └── SecurityConfig.java          ← Default SecurityFilterChain
│       │   ├── provider/
│       │   │   └── JwtTokenProvider.java        ← Token create/validate/extract
│       │   ├── filter/
│       │   │   └── JwtAuthenticationFilter.java ← OncePerRequestFilter
│       │   └── service/
│       │       └── DefaultUserDetailsService.java ← No-op fallback
│       └── resources/META-INF/spring/
│           └── *.AutoConfiguration.imports      ← Spring Boot discovery file
│
└── jwt-auth-starter/                 ← Thin POM — what consumers depend on
    └── pom.xml
```

---

## 🧪 Quick Test Checklist

After setup, test these scenarios with Postman or curl:

| # | Test | Expected |
|---|---|---|
| 1 | `GET /api/public/hello` | ✅ 200 — no token needed |
| 2 | `GET /api/me` — no token | ✅ 401 JSON |
| 3 | `POST /api/auth/login` — valid creds | ✅ JWT token in response |
| 4 | `GET /api/me` — valid token | ✅ 200 user info |
| 5 | `GET /api/admin/dashboard` — ROLE_USER token | ✅ 403 JSON |
| 6 | `GET /api/admin/dashboard` — ROLE_ADMIN token | ✅ 200 success |
| 7 | `GET /api/me` — expired/wrong token | ✅ 401 JSON |

---

## 🚨 Production Checklist

Before going live, make sure you have:

```
☐ Set auth.jwt.secret via environment variable (not hardcoded)
☐ Secret is at least 32 characters, Base64-encoded
☐ HTTPS enabled — JWT in plain HTTP is vulnerable
☐ Set appropriate token expiration (15–60 min for sensitive apps)
☐ Add rate limiting on /api/auth/login (prevent brute force)
☐ Cache loadUserByUsername() result with @Cacheable (performance)
☐ Implement refresh token mechanism for long sessions
```

**Production `application.properties` example:**
```properties
# Use environment variable — never commit secrets to Git!
auth.jwt.secret=${JWT_SECRET}
auth.jwt.expiration=1800000
auth.jwt.public-endpoints=/api/auth/login,/api/auth/register
```

**Set environment variable:**
```bash
# Windows
set JWT_SECRET=your-super-secret-base64-encoded-key-here

# Linux / macOS
export JWT_SECRET=your-super-secret-base64-encoded-key-here
```

---

## ❓ Frequently Asked Questions

**Q: Do I need to add Spring Security separately?**
No. The starter pulls it in automatically as a transitive dependency.

**Q: Can I use this with Spring Data JPA?**
Yes. Add `spring-boot-starter-data-jpa` to your own `pom.xml` and use your
JPA repository inside `UserDetailsService`.

**Q: What if I want a different token expiry per user?**
Override `JwtTokenProvider` with your own bean and implement custom logic.

**Q: Can I use this with a MySQL or PostgreSQL database?**
Yes. Replace the H2 dependency with your database driver and update
`application.properties` with your connection URL.

**Q: Is this compatible with Spring Boot 3.2.x?**
Yes. It works with any Spring Boot 3.x version.

---

## 📦 Dependencies Included (Transitive)

When you add `jwt-auth-starter`, these are pulled in automatically:

| Library | Version | Purpose |
|---|---|---|
| spring-boot-starter-security | 3.3.0 | Security framework |
| spring-boot-starter-web | 3.3.0 | Servlet API support |
| jjwt-api | 0.12.5 | JWT creation and parsing |
| jjwt-impl | 0.12.5 | JWT implementation |
| jjwt-jackson | 0.12.5 | JSON serialization for JWT |

---

## 👨‍💻 Author

**DevLib** — Built as a portfolio project demonstrating advanced Spring Boot
internals: custom starters, auto-configuration, conditional beans, and
production-grade JWT security architecture.

---

*Last updated: 2025 | Spring Boot 3.3.0 | Java 17+*
