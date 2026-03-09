# Nurun — AI Router with Persistent Memory

> A backend AI orchestration engine that solves the context-loss problem of free AI models.

---

## The Problem

Every free AI chatbot (Gemini, DeepSeek, etc.) has a message limit. When you hit that limit, you open a new chat — and the AI forgets everything. Your name. Your project. Your entire conversation history. Gone.

Most people just accept this. Nurun doesn't.

---

## The Solution

Nurun stores every message in a PostgreSQL database and reconstructs the full conversation history before every AI call. The model doesn't need to remember — **the system remembers for it**.

When one provider hits its rate limit, Nurun automatically routes to the next available provider. The user never notices.

---

## How It Works

```
Client
  └── POST /api/messages
        └── MessageService
              ├── 1. Identify user (SecurityContext → UserPrincipal → userId)
              ├── 2. Conversation management (new or existing)
              ├── 3. Reconstruct history from database
              ├── 4. Persist user message
              └── 5. AiRouter
                    └── AiProvider (Gemini → DeepSeek → Qwen)
                          └── External AI API
                                └── Store AI response
                                      └── Return DTO to client
```

### Step by Step

**1. User Identity**
Every request extracts the authenticated user from the Spring Security context. Every conversation and message is tied to a specific user. Nobody can read anyone else's chat.

**2. Conversation Management**
Two paths:
- `POST /api/messages` → creates a new conversation
- `POST /api/messages/{conversationId}` → continues an existing conversation

**3. Persistent Memory**
LLMs have no memory. They only know what you send them. Nurun solves this by loading the full conversation history from the database and sending it with every request. The AI always has full context.

**4. Message Persistence Before AI Call**
The user's message is saved to the database *before* the AI call. If the AI fails, the message is not lost.

**5. AI Router**
The router decides which provider handles the request. If a provider hits its rate limit, it is marked unavailable and the next provider is tried automatically.

**6. Response Storage**
The AI's response is stored with full metadata: model used, provider used, timestamp. The database contains a complete transcript of every conversation.

---

## Architecture

```
com.nurun
├── controller
│   └── MessageController       # REST endpoints
├── service
│   └── MessageService          # Core orchestration logic
├── router
│   ├── AiRouter                # Interface
│   └── AiRouterService         # Provider selection + fallback
├── provider
│   └── GeminiProvider          # Gemini API integration
├── model
│   ├── User
│   ├── Conversation
│   └── Message
├── security
│   ├── JwtAuthenticationFilter
│   └── UserPrincipal
└── exception
    └── GlobalExceptionHandler
```

---

## API Endpoints

### Authentication
```
POST /api/auth/register     Register new user
POST /api/auth/login        Login and receive JWT token
```

### Messages
```
POST /api/messages                      Start a new conversation
POST /api/messages/{conversationId}     Reply in an existing conversation
```

### Request Body
```json
{
  "content": "My name is Irfan Khan",
  "modelName": "gemini-flash"
}
```

### Response
```json
{
  "id": 34,
  "content": "Hello Irfan Khan! How can I help you?",
  "messageRole": "ASSISTANT",
  "userId": 1,
  "conversationId": 21,
  "modelUsed": "gemini-flash",
  "providerUsed": "Gemini",
  "sentAt": "2026-03-09T03:30:47Z"
}
```

The `conversationId` in the response is what you send on the next request to continue the conversation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4 |
| Security | Spring Security + JWT (jjwt 0.12.5) |
| Database | PostgreSQL |
| ORM | Hibernate 7 / Spring Data JPA |
| HTTP Client | RestClient (Spring Boot 3.2+) |
| AI Provider | Google Gemini API |

---

## Setup

### 1. Prerequisites
- Java 21+
- PostgreSQL running locally
- Gemini API key from [Google AI Studio](https://aistudio.google.com)

### 2. Database
```sql
CREATE DATABASE nurun;
CREATE USER nurun_user WITH PASSWORD 'yourpassword';
GRANT ALL ON SCHEMA public TO nurun_user;
```

### 3. Environment Variables
```bash
export GEMINI_API_KEY=your_gemini_key
export JWT_SECRET_KEY=your_base64_secret   # generate: openssl rand -base64 32
export DB_PASSWORD=yourpassword
```

### 4. Run
```bash
./mvnw spring-boot:run
```

---

## Roadmap

### V1 (Current)
- [x] JWT authentication
- [x] Persistent conversation history
- [x] Gemini provider integration
- [x] AI router with provider fallback
- [x] Per-user conversation isolation
- [x] Full message transcript in PostgreSQL

### V2 (Planned)
- [ ] Hierarchical memory — summarize old conversations so context fits in token limits
- [ ] Cross-conversation memory — user identity persists across all chats
- [ ] DeepSeek and Qwen providers
- [ ] Smart rate limit recovery with `retryAfter` timestamps
- [ ] Redis for distributed provider state

---

## Why Not Just Use OpenRouter?

OpenRouter is great but routes to paid models. Nurun is built specifically for **free model orchestration** — maximizing free tier usage across multiple providers before spending anything.

---

## Author

Built by [Irfan Khan](https://github.com/irfankhansajid) as a learning project to deeply 