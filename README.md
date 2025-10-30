# Micos color code

MICOS es una web (mobile-first) dirigida a niños y niñas de entre 3 y 8 años con daltonismo dicromático. Surge tras la creación del sistema visual "Micos color code", pensado para facilitar la identificación de los colores por parte de los peques con esta alteración visual. 

Con la web se pretende que mediante una forma lúdica, visual y sencilla, continúen con el aprendizaje también a través de las pantallas. 

## Documentación
- [Si accedes a este enlace te encontrarás con la documentación del proyecto](https://www.notion.so/sara-vazquez/MICOS-PROYECTO-FINAL-23fd5565c5b68048a775fc74e9a9f749)

## Diagrama de clases UML

## Diagrama entidad-relación

## Estructura de carpetas

## Instalación y ejecución
1. Haz fork del repositorio
2. Crea una rama para tu feature/fix
3. Configura la base de datos MySQL (ver `application.properties`) [Aquí tienes una guía](https://www.notion.so/sara-vazquez/Instalaciones-back-28dd5565c5b6805e823dc9f9ec5170d9)
4. Haz tus cambios y crea un pull request
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


## Endpoints principales

## 📯 Pruebas en Postman

## 📋 Funcionalidades principales del ADMIN

- Gestión de recursos:
  - Listar 📄
  - Añadir ➕
  - Editar ✏️
  - Eliminar 🗑

## 🧪 Test coverage
![Coverage provisional de los test de back](src/assets/back-coverage.png)