/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  User
 * Created: 5 Φεβ 2026
 */
-- Δημιουργία πίνακα Κατηγοριών
CREATE TABLE CATEGORY (
    ID INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
);

-- Δημιουργία πίνακα Άρθρων
CREATE TABLE ARTICLE (
    ID INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    TITLE VARCHAR(255) NOT NULL,
    SNIPPET VARCHAR(2000),
    TEXT CLOB(100000),
    ARTICLE_TIMESTAMP VARCHAR(50),
    USER_COMMENTS VARCHAR(1000),
    RATING INT,
    CATEGORY_ID INT,
    PRIMARY KEY (ID),
    CONSTRAINT FK_ARTICLE_CATEGORY FOREIGN KEY (CATEGORY_ID) REFERENCES CATEGORY(ID)
);

-- Δημιουργία πίνακα για το Ιστορικό Αναζητήσεων
CREATE TABLE SEARCH_HISTORY (
    ID INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    KEYWORD VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
);
