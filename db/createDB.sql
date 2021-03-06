CREATE DATABASE IF NOT EXISTS aresonsomnium;

USE aresonsomnium;

create table if not exists somniumGuis (
    guiName varchar(255) primary key,
    guiTitle varchar(255) not null,
    shopItems text not null
);