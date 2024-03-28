INSERT INTO rentals (id, rental_date, required_return_date, actual_return_date, car_id, user_id, status, is_deleted)
VALUES (1, '2024-04-01', '2024-04-10', null, 1, 1, 'LASTING', false);
INSERT INTO rentals (id, rental_date, required_return_date, actual_return_date, car_id, user_id, status, is_deleted)
VALUES (2, '2024-04-01', '2024-04-10', '2024-04-10', 1, 2, 'RETURNED', false);
INSERT INTO rentals (id, rental_date, required_return_date, actual_return_date, car_id, user_id, status, is_deleted)
VALUES (3, '2024-04-10', '2024-04-15', '2024-04-15', 1, 2, 'RETURNED', false);
INSERT INTO rentals (id, rental_date, required_return_date, actual_return_date, car_id, user_id, status, is_deleted)
VALUES (4, '2024-04-01', '2024-04-10', null, 1, 3, 'LASTING', false);