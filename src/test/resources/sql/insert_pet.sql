INSERT INTO person (id, first_name, last_name, date_of_birth, street, house_number, house_number_additions, postal_code, city, country)
VALUES ('0fa281f4-9507-40dd-9165-7d6f49631cab', 'Voornaam', 'Achternaam', '1993-04-05', 'Straat', 25, null, '9876AB', 'Stad','Netherlands');

insert into pet (id, name, age, person_id)
values ('0fa281f4-9507-40dd-9165-7d6f49631cac', 'Lester', 12, '0fa281f4-9507-40dd-9165-7d6f49631cab'),
       ('0fa281f4-9507-40dd-9165-7d6f49631cad', 'Bas', 14, null);