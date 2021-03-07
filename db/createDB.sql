CREATE DATABASE IF NOT EXISTS aresonSomnium;

USE aresonSomnium;

create table if not exists somniumGuis (
    guiName varchar(255) primary key,
    guiTitle varchar(255) not null,
    shopItems text not null
);