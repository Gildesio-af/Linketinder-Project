ALTER TABLE addresses
ADD COLUMN user_id UUID;

ALTER TABLE addresses
ADD CONSTRAINT user_address_fk
FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE users
DROP CONSTRAINT country_user_fk;

ALTER TABLE users
DROP COLUMN address_id;