CREATE TABLE test3rollback (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(50)
);

INSERT INTO test3rollback (name) VALUES ('Test 1'), ('Test 2');
-- INSERT INTO test_rollback (id, name) VALUES (NULL, NULL);
