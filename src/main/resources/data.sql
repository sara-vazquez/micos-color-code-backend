-- ROLES
INSERT INTO roles (id_role, name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO roles (id_role, name) VALUES (2, 'ROLE_USER');

-- TEST: USER AND ADMIN 
INSERT INTO users (id, username, email, password, enabled)
VALUES (1, 'admin_test', 'admin@test.com', 'admin12345678!', TRUE);

INSERT INTO users (id, username, email, password, enabled)
VALUES (2, 'user_test', 'user@test.com', 'user12345678!', TRUE);

-- REL.
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
