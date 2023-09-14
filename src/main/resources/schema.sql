drop database if exists fine_database;
create database fine_database;
use fine_database;
create table Car
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate VARCHAR(8)  NOT NULL,
    mark  VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    color ENUM ('WHITE',
        'BLACK',
        'SILVER',
        'RED',
        'BLUE',
        'GREEN',
        'BROWN',
        'YELLOW')     NOT NULL,
    CONSTRAINT plate_format_check CHECK (
        plate REGEXP '^(?=(.*[A-ZА-ЯІЇҐЄ]){2,})([A-ZА-ЯІЇҐЄ0-9]{3,8})$')
);

create table Violation
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(50)    NOT NULL,
    price       DECIMAL(10, 2) NOT NULL
);

create table Fine
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    longitude DOUBLE       NOT NULL,
    latitude  DOUBLE       NOT NULL,
    date_time DATETIME     NOT NULL,
    photo_url VARCHAR(255) NOT NULL,
    car_id    BIGINT       NOT NULL,
    FOREIGN KEY (car_id) REFERENCES car (id),
    CONSTRAINT fk_fine_car FOREIGN KEY (car_id) REFERENCES car (id)
);

create table Fine_Violation
(
    fine_id      BIGINT NOT NULL,
    violation_id BIGINT NOT NULL,
    FOREIGN KEY (fine_id) REFERENCES Fine (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (violation_id) REFERENCES Violation (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    PRIMARY KEY (fine_id, violation_id)
);

insert into car(plate, model, mark, color)
values ('AA1234KA', 'Opel', 'Vectra', 'GREEN'),
       ('AA1235KK', 'Opel', 'Insignia', 'BLUE');

insert into fine(date_time, latitude, longitude, photo_url, car_id)
values ('2023-08-29 22:59:52', 32.21, 45.34, 'url1', 1),
       ('2023-08-30 22:59:52', 45.21, 23.34, 'url2', 1);
insert into fine(date_time, latitude, longitude, photo_url, car_id)
values ('2023-08-29 22:59:52', 67.21, 54.34, 'url3', 2),
       ('2023-08-30 22:59:52', 23.21, 31.34, 'url4', 2);

insert ignore violation
values (1, 'VIOLATION OF LICENSE PLATE USE', 1190.0),
       (2, 'VIOLATION OF SIGNS', 340.0),
       (3, 'PARKED IN TWO LANES', 680.0),
       (4, 'PARKED IN FORBIDDEN AREAS', 680.0),
       (5, 'OBSTRUCTS TRAFFIC, PEDESTRIANS', 680.0),
       (6, 'PARKED ON PUBLIC TRANSPORT LANE', 680.0),
       (7, 'PARKED ON BIKE LANE', 680.0),
       (8, 'OBSTRUCTS MUNICIPAL TRANSPORT MOVEMENT', 680.0),
       (9, 'VIOLATES PARKING SCHEME', 680.0),
       (10, 'PARKED IN DISABLED ZONE', 1700.0);

insert ignore into Fine_Violation(fine_id, violation_id)
values (1, 6),
       (1, 4),
       (1, 5),
       (2, 9),
       (2, 10);
