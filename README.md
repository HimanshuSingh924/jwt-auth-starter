# JWT Auth Starter — Complete Project

## Folder Structure
```
jwt-workspace/
├── jwt-auth-starter-parent/     ← LIBRARY (Import this in Eclipse)
│   ├── pom.xml
│   ├── jwt-auth-autoconfigure/  ← All JWT logic lives here
│   └── jwt-auth-starter/        ← Thin POM (what consumers depend on)
│
└── demo-consumer-app/           ← TEST APP (Import this separately)
    ├── pom.xml
    └── src/...
```

---

## STEP 1 — Build the Library

Open CMD in the `jwt-auth-starter-parent` folder and run:

```cmd
mvn clean install -DskipTests
```

Wait for: **BUILD SUCCESS**

---

## STEP 2 — Import in Eclipse

### Import Library:
```
File → Import → Maven → Existing Maven Projects
Root Directory → Browse → select jwt-auth-starter-parent folder
(All 3 projects should be checked) → Finish
```

### Import Demo App:
```
File → Import → Maven → Existing Maven Projects
Root Directory → Browse → select demo-consumer-app folder
→ Finish
```

If you see red errors → Right click any project → Maven → Update Project → OK

---

## STEP 3 — Run the Demo App

In Eclipse, expand `demo-consumer-app` → `src/main/java` → `com.example.demo`
→ Right click `DemoConsumerApplication.java` → **Run As → Java Application**

Look for this in the Console:
```
✅ jwt-auth-starter Demo — Test Users Ready!
👤 alice@example.com  / password123  → ROLE_USER
👑 admin@example.com  / admin123     → ROLE_ADMIN
Started DemoConsumerApplication in X.X seconds
```

---

## STEP 4 — Test with Postman

### 1. Public (no token needed)
```
GET http://localhost:8080/api/public/hello
```

### 2. Login → Get Token
```
POST http://localhost:8080/api/auth/login
Body (JSON): { "email": "alice@example.com", "password": "password123" }
```
Copy the "token" value from response.

### 3. Use Token
```
GET http://localhost:8080/api/me
Header → Authorization: Bearer <paste_token_here>
```

### 4. No token → 401
```
GET http://localhost:8080/api/me   (no header)
```

### 5. Wrong role → 403
```
GET http://localhost:8080/api/admin/dashboard
Header → Authorization: Bearer <alice_token>   (alice is ROLE_USER, not ROLE_ADMIN)
```

### 6. Admin access
```
POST http://localhost:8080/api/auth/login
Body: { "email": "admin@example.com", "password": "admin123" }

GET http://localhost:8080/api/admin/dashboard
Header → Authorization: Bearer <admin_token>
```

---

## Test Credentials
| Email | Password | Role |
|---|---|---|
| alice@example.com | password123 | ROLE_USER |
| admin@example.com | admin123 | ROLE_ADMIN |
