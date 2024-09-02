DROP DATABASE IF EXISTS conference;

CREATE DATABASE conference;

\c conference;

create sequence attendee_SEQ start with 1 increment by 50;

CREATE TABLE IF NOT EXISTS Attendee (
    id INT PRIMARY KEY,
    surname VARCHAR(255),
    givenName VARCHAR(255),
    email VARCHAR(255)
);



