INSERT INTO owner (name, email) VALUES ('Ana Pop', 'ana.pop@example.com');
INSERT INTO owner (name, email) VALUES ('Bogdan Ionescu', 'bogdan.ionescu@example.com');

INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN12345', 'Dacia', 'Logan', 2018, 1);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN67890', 'VW', 'Golf', 2021, 2);

INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiry_notified) VALUES (1, 'Allianz', DATE '2024-01-01', DATE '2024-12-31', false);
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiry_notified) VALUES (1, 'Groupama', DATE '2025-01-01', DATE '2025-12-31', false);
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiry_notified) VALUES (2, 'Allianz', DATE '2025-03-01', DATE '2025-09-30', false);

INSERT INTO insuranceclaim (car_id, claim_date, description, amount) VALUES (1, DATE '2024-06-15', 'Minor collision', 1500.00);
INSERT INTO insuranceclaim (car_id, claim_date, description, amount) VALUES (2, DATE '2025-04-10', 'Windshield damage', 300.00);