DROP DATABASE IF EXISTS libraryDB;

CREATE DATABASE IF NOT EXISTS libraryDB;
USE libraryDB;

CREATE TABLE users (
    userID INT PRIMARY KEY AUTO_INCREMENT,
    fullName VARCHAR(100) NOT NULL,
	username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    phoneNumber VARCHAR(15),
    email VARCHAR(100),
    role ENUM('CUSTOMER', 'ADMIN') DEFAULT 'CUSTOMER',
    avatarImage VARCHAR(2000)
);

CREATE TABLE books (
    availableNumber INT DEFAULT 0,
    borrowCount INT DEFAULT 0,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    isbn VARCHAR(20) NOT NULL PRIMARY KEY UNIQUE,
    thumbnail VARCHAR(10000),
    description VARCHAR(1000)
);
CREATE TABLE transactions (
    userID INT,
    isbn VARCHAR(20)
);
INSERT INTO books (title, author, isbn, description, thumbnail, availableNumber)
VALUES (
    'Harry Potter',
    'S. Gunelius',
    '9780230594104',
    'The Harry Potter books are the bestselling books of all time. In this fascinating study, Susan Gunel...',
    'http://books.google.com/books/content?id=abYKXvCwEToC&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api',
    5
);

-- Sách #2
INSERT INTO books (title, author, isbn, description, thumbnail, availableNumber)
VALUES (
    'Teaching with Harry Potter',
    'Valerie Estelle Frankel',
    '9781476601229',
    'The Harry Potter phenomenon created a surge in reading with a lasting effect on all areas of culture...',
    'http://books.google.com/books/content?id=sKW7u4786yUC&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api',
    5
);

-- Sách #3
INSERT INTO books (title, author, isbn, description, thumbnail, availableNumber)
VALUES (
    'Harry Potter and the Other',
    'Sarah Park Dahlen, Ebony Elizabeth Thomas',
    '9781496840530',
    'Named a 2023 Honour Book by the International Research Society for Children''s Literature Contributio...',
    'http://books.google.com/books/content?id=2YR0EAAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api',
    5
);

-- Sách #4
INSERT INTO books (title, author, isbn, description, thumbnail, availableNumber)
VALUES (
    'The Harry Potter Generation',
    'Emily Lauer, Balaka Basu',
    '9781476670034',
    'The generation of readers most heavily impacted by J.K. Rowling''s Harry Potter series--those who gre...',
    'http://books.google.com/books/content?id=1nOPDwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api',
    5
);

-- Sách #5
INSERT INTO books (title, author, isbn, description, thumbnail, availableNumber)
VALUES (
    'Inside the World of Harry Potter',
    'Christopher E. Bell',
    '9781476634135',
    'Many scholars recognize the importance of Harry Potter as a vehicle for discussions about society--f...',
    'http://books.google.com/books/content?id=j_p1DwAAQBAJ&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api',
    5
);
INSERT INTO users (fullName, username, password, phoneNumber, email, role)
VALUES (
    'cuong',
    'cuong',
    '123456',
    '1',
    'cuong@gmail.com',
    'ADMIN'
);


