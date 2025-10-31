# MICOS COLOR CODE

MICOS es una web (mobile-first) dirigida a niños y niñas de entre 3 y 8 años con daltonismo dicromático. Surge tras la creación del sistema visual "Micos color code", pensado para facilitar la identificación de los colores por parte de los peques con esta alteración visual. 

Con la web se pretende que mediante una forma lúdica, visual y sencilla, continúen con el aprendizaje también a través de las pantallas. 

[Aquí encontrarás la documentación del proyecto ↗︎](https://www.notion.so/sara-vazquez/MICOS-PROYECTO-FINAL-23fd5565c5b68048a775fc74e9a9f749)


## 📓 Diagrama de clases UML
```mermaid
---
config:
  theme: 'forest'
---

classDiagram
    %% ===== ENTITIES =====
    class UserEntity {
        -Long id
        -String username
        -String email
        -String password
        -boolean enabled
        -String confirmationToken
        -LocalDateTime tokenCreationDate
        -Set~RoleEntity~ roles
        -Set~UserGameStatsEntity~ gameStats
        -Set~GameSessionEntity~ sessions
    }

    class RoleEntity {
        -Long id_role
        -String name
        -Set~UserEntity~ users
    }

    class GameEntity {
        -Long id
        -String gameName
        -Set~UserGameStatsEntity~ gameStats
        -Set~GameSessionEntity~ sessions
    }

    class UserGameStatsEntity {
        -Long id
        -UserEntity user
        -GameEntity game
        -Integer totalPoints
        -Integer gamesPlayed
        -Integer currentLevel
    }

    class GameSessionEntity {
        -Long id
        -UserEntity user
        -GameEntity game
        -int points
        -int timeCompleted
        -int levels
        -int currentLevel
    }

    class ResourceEntity {
        -Long id
        -String imageFile
        -String name
        -String intro
        -String description
        -String pdfFile
    }

    %% ===== RELATIONSHIPS: Entities =====
    UserEntity "1" --> "*" UserGameStatsEntity : has
    UserEntity "1" --> "*" GameSessionEntity : has
    UserEntity "*" --> "*" RoleEntity : has
    GameEntity "1" --> "*" UserGameStatsEntity : tracks
    GameEntity "1" --> "*" GameSessionEntity : tracks
    UserGameStatsEntity "*" --> "1" UserEntity : belongs to
    UserGameStatsEntity "*" --> "1" GameEntity : belongs to
    GameSessionEntity "*" --> "1" UserEntity : belongs to
    GameSessionEntity "*" --> "1" GameEntity : belongs to

```

## 📙 Diagrama entidad-relación
```mermaid
---
config:
  theme: 'forest'
---

erDiagram
    users ||--o{ game_sessions : "plays"
    users ||--o{ user_game_stats : "has"
    users ||--o{ user_roles : "has"
    games ||--o{ game_sessions : "contains"
    games ||--o{ user_game_stats : "register"
    roles ||--o{ user_roles : "assigns"

    users {
        int id PK
        string username
        string password
        string confirmation_token
        datetime token_creation_date
        boolean enabled
    }

    games {
        int id PK
        string game_name
    }

    game_sessions {
        int id PK
        int levels
        int points
        int time_completed_seconds
        int game_id FK
        int user_id FK
        int current_level
    }

    user_game_stats {
        int id PK
        int total_points
        int game_id FK
        int user_id FK
        int current_level
        int games_played
    }

    roles {
        int id_role PK
        string name
    }

    user_roles {
        int user_id FK
        int role_id FK
    }

    resources {
        int id PK
        string description
        string image
        string intro
        string name
        string pdf
    }
```

## 📂 Estructura de carpetas


## 🚀 Instalación y ejecución
1. Haz fork del repositorio

2. Crea una rama para tu feature/fix
	 ```
	 git checkout -b feature/nueva-funcionalidad
	 ```

3. Configura la base de datos MySQL (ver `application.properties`)
   
- [Consulta la guía con las instalaciones que necesitas ↗︎](https://www.notion.so/sara-vazquez/Instalaciones-back-28dd5565c5b6805e823dc9f9ec5170d9)

4. Haz tus cambios y crea un pull request
 	 ```
	 git commit -m "Descripción breve del cambio"
	 git push origin feature/nueva-funcionalidad
	 ```

5. Levanta los servicios con Docker Compose:
	 ```
	 docker compose up -d
	 ```

6. Ejecuta la aplicación Spring Boot:
	 ```
	 mvn spring-boot:run
	 ```

  La aplicación se levantará en:
👉 http://localhost:8080



## 🔗 Endpoints principales

1. 🔐 **AUTENTICACIÓN**

	- POST http://localhost:8080/auth/login - Login (USER, ADMIN)
	- POST http://localhost:8080/auth/logout - Logout (USER, ADMIN)
	- POST http://localhost:8080/register - Registration (First user registered: role ADMIN)
	- GET http://localhost:8080/captcha/generate - Generate captcha - register (USER, ADMIN)

2. 📚 **RECURSOS**

**Admin**
	- GET http://localhost:8080/admin/resources - Get all resources 
	- POST http://localhost:8080/admin/resources  - Add new resources
	- PUT http://localhost:8080/admin/resources/{id} - Update a resource 
	- DEL http://localhost:8080/admin/resources/{id} - Delete a resource

**User**
	- GET http://localhost:8080/users/resources - Get all resources

3. 📄 **FEEDBACK**

	- POST http://localhost:8080/feedback - Create feedback - email service (USER)

4. 🎮 **JUEGOS**

	- POST http://localhost:8080/users/play/{gameId}/sessions - Create game session (USER)
	- GET http://localhost:8080/users/play/{gameId}/ranking - Get game ranking chart (USER)



## 📯 Pruebas en Postman

## 📋 Funcionalidades principales del ADMIN
### Gestión de recursos:
  - Listar 📄
  - Añadir ➕
  - Editar ✏️
  - Eliminar 🗑


## 🧪 Test coverage
![Coverage provisional de los test de back](src/assets/back-coverage.png)


### Autora
Sara Vázquez
