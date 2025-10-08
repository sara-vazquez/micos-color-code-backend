INSERT INTO roles (id, name) VALUES (1, "ROLE_ADMIN");
INSERT INTO roles (id, name) VALUES (2, "ROLE_USER");

/* Test: admin and user */

INSERT INTO users (id, username, password, enabled) 
VALUES (1, 'admin_test', '$2a$10$wT0XhI3H9/jN05s.m6h3Q.eL4g2X0B2/h5i/eD5gJ8/eI6x6H7v7I', TRUE);

INSERT INTO users(id, username, password, enabled)
VALUES(2, 'user_test','$2a$10$wT0XhI3H9/jN05s.m6h3Q.eL4g2X0B2/h5i/eD5gJ8/eI6x6H7v7I', TRUE)

INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); 
