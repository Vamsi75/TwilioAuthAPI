USE achievers;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    mobileNumber VARCHAR(15) NOT NULL UNIQUE,
    loginOtp VARCHAR(6),
    createdDate DATETIME,
    lastLogin DATETIME,
    isActive BOOLEAN NOT NULL);
