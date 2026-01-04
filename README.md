# GitHub Repos API

Spring Boot REST API that fetches non-forked GitHub repositories with branch information for a given user.

## Tech Stack

- Java 25
- Spring Boot 4.0.1
- Gradle
- Lombok

## API

```
GET /repos/{username}
```

Returns non-forked repositories with branches and commit SHAs.

**Response:**
```json
[
  {
    "name": "repo-name",
    "owner": "username",
    "branches": [
      {
        "name": "main",
        "sha": "abc123..."
      }
    ]
  }
]
```

**Errors:**
- `404` - User not found
- `429` - Rate limit exceeded
- `502` - Unable to fetch from upstream (includes timeouts)

## Run

```bash
./gradlew bootRun
```

## Test

```bash
./gradlew test
```
