USE photo_generate;
UPDATE user SET password='$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH' WHERE username='admin';
SELECT username, password, LENGTH(password) as length FROM user WHERE username='admin';