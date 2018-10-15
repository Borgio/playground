
DELETE FROM TACO_ORDER_TACOS;
DELETE FROM TACO_INGREDIENTS;
DELETE FROM TACO;
DELETE FROM TACO_ORDER;
DELETE FROM INGREDIENT;

INSERT INTO INGREDIENT (id, name, type) VALUES ('FLTO', 'Flour Tortillas', 'WRAP');
INSERT INTO INGREDIENT (id, name, type) VALUES ('COTO', 'Corn Tortillas', 'WRAP');
INSERT INTO INGREDIENT (id, name, type) VALUES ('GRBF', 'Ground Beef', 'PROTEIN');
INSERT INTO INGREDIENT (id, name, type) VALUES ('CARN', 'Carnitas', 'PROTEIN');
INSERT INTO INGREDIENT (id, name, type) VALUES ('TMTO', 'Diced Tomatoes', 'VEGGIES');
INSERT INTO INGREDIENT (id, name, type) VALUES ('LETC', 'Lettuce', 'VEGGIES');
INSERT INTO INGREDIENT (id, name, type) VALUES ('CHED', 'Cheddar', 'CHEESE');
INSERT INTO INGREDIENT (id, name, type) VALUES ('JACK', 'Monterrey Jack', 'CHEESE');
INSERT INTO INGREDIENT (id, name, type) VALUES ('SLSA', 'Salsa', 'SAUCE');
INSERT INTO INGREDIENT (id, name, type) VALUES ('SRCR', 'Sour Cream', 'SAUCE');