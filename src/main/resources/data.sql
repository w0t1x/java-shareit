-- Удаление в правильном порядке (сначала дочерние таблицы)
DELETE FROM comments;
DELETE FROM bookings;
DELETE FROM items;
DELETE FROM users;

-- Сброс счетчиков ID
ALTER TABLE comments ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE bookings ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE items ALTER COLUMN ID RESTART WITH 1;
ALTER TABLE users ALTER COLUMN ID RESTART WITH 1;