INSERT INTO roles (id_role, name)
SELECT 1, 'ROLE_ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id_role = 1);

INSERT INTO roles (id_role, name)
SELECT 2, 'ROLE_USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE id_role = 2);

INSERT INTO games (id, game_name)
SELECT 1, 'Memory Cards' WHERE NOT EXISTS (SELECT 1 FROM games WHERE id = 1);
