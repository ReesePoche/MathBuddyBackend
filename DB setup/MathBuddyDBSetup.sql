-- Following made by Reese Poche 11/16/2018
-- The following SQL Commands will set up the DB server for the project MathBuddy

-- ------------------------------------------------------

-- 
-- Make user that server will use to login to DB with
-- 
CREATE USER 'netBeans'@'localhost' IDENTIFIED BY 'beans';
GRANT ALL PRIVILEGES ON * . * TO 'netBeans'@'localhost';
GRANT SELECT ON *.* TO 'username'@'localhost';

-- ------------------------------------------------------
-- 
-- Make the DB
-- 

CREATE DATABASE mathbuddydb;

USE mathbuddydb;

-- ------------------------------------------------------

--
-- Create and populate userTypes table
-- 

CREATE TABLE userTypes (

	userType INT NOT NULL AUTO_INCREMENT,

    userTypeName varChar(255),
 
    PRIMARY KEY (userType)

);

INSERT INTO userTypes (userTypeName)

VALUES ('Student');



INSERT INTO userTypes (userTypeName)

VALUES ('Teacher');



INSERT INTO userTypes (userTypeName)

VALUES ('SGL');

-- ------------------------------------------------------

--
-- Create and populate activityTypes relation
-- 

CREATE TABLE activityTypes (

	activityType INT NOT NULL AUTO_INCREMENT,

    activityTypeName varChar(255),
 
    PRIMARY KEY (activityType)

);

INSERT INTO activityTypes (activityTypeName)
VALUES ('InClass');


INSERT INTO activityTypes (activityTypeName)
VALUES ('Addition');

INSERT INTO activityTypes (activityTypeName)
VALUES ('Subtraction');

INSERT INTO activityTypes (activityTypeName)
VALUES ('Multiplication');

INSERT INTO activityTypes (activityTypeName)
VALUES ('Division');

-- ------------------------------------------------------

--
-- Create users table
--

CREATE TABLE users(

	userID INT NOT NULL AUTO_INCREMENT,

    userName varchar(255) NOT NULL,
 
    userEmail varchar(255) NOT NULL,

    userType int,

    PRIMARY KEY (userID),
 
    FOREIGN KEY (userType) REFERENCES userTypes(userType)

);

-- ------------------------------------------------------

-- 
-- create classrooms table
--

CREATE TABLE classrooms (

	classroomID int NOT NULL AUTO_INCREMENT,

	classroomName varchar(255) NOT NULL,
    teacherID int,
 
    PRIMARY KEY (classroomID),
 
    FOREIGN KEY (teacherID) REFERENCES users(userID)

);

-- ------------------------------------------------------

-- 
-- create activities table
-- 

CREATE TABLE activities(
	
activityID int NOT NULL AUTO_INCREMENT,

    classroomID int,

    userID int,

    activityType int,
 
    score int,

    seed int,
 
    numProblems int,

    PRIMARY KEY (activityID),

    FOREIGN KEY (classroomID) REFERENCES classrooms(classroomID),

    FOREIGN KEY (userID) REFERENCES users(userID), 

    FOREIGN KEY (activityType) REFERENCES activityTypes(activityType) 

);
		

