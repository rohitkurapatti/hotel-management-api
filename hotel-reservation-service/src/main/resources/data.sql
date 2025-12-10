-- Payment status reference data
INSERT INTO payment_status (payment_reference, status) VALUES ('PP111111', 'CONFIRMED');
INSERT INTO payment_status (payment_reference, status) VALUES ('PR222222', 'CANCELLED');
INSERT INTO payment_status (payment_reference, status) VALUES ('PN333333', 'PENDING');

-- Initial Users (password: "password" for all users, hashed with BCrypt)
-- Admin user: username=admin, password=admin123
INSERT INTO users (username, password, email, role, enabled)
VALUES ('admin', '$2a$10$xQ3.lqGZCYmIJH5RWYzEh.PnMCpvJbU8J6c8ZqLmYFVdxCqYwYP8S', 'admin@hotel.com', 'ADMIN', true);

-- Regular user: username=user, password=password
INSERT INTO users (username, password, email, role, enabled)
VALUES ('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user@hotel.com', 'USER', true);

-- System user for service-to-service calls: username=system, password=system123
INSERT INTO users (username, password, email, role, enabled)
VALUES ('system', '$2a$10$5A0VqhqhC3W.67z6e4p4dOMYqLQPUbMxMb4U5cLB0K5rJHH8bDfN2', 'system@hotel.com', 'SYSTEM', true);

-- Sample Reservations to demonstrate scheduler cancellation
INSERT INTO reservations (customer_name, room_number, start_date, end_date, room_segment, payment_mode, payment_reference, total_amount, reservation_status)
VALUES ('John Doe', '101', DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 3, CURRENT_DATE), 'MEDIUM', 'CASH', 'CASH001', 2000.00, 'CONFIRMED');

INSERT INTO reservations (customer_name, room_number, start_date, end_date, room_segment, payment_mode, payment_reference, total_amount, reservation_status)
VALUES ('Charlie Brown', '203', DATEADD('DAY', 2, CURRENT_DATE), DATEADD('DAY', 5, CURRENT_DATE), 'MEDIUM', 'BANK_TRANSFER', 'BT009012', 2000.00, 'PENDING_PAYMENT');

INSERT INTO reservations (customer_name, room_number, start_date, end_date, room_segment, payment_mode, payment_reference, total_amount, reservation_status)
VALUES ('Eva Davis', '205', DATEADD('DAY', 5, CURRENT_DATE), DATEADD('DAY', 8, CURRENT_DATE), 'MEDIUM', 'BANK_TRANSFER', 'BT007890', 2000.00, 'PENDING_PAYMENT');

INSERT INTO reservations (customer_name, room_number, start_date, end_date, room_segment, payment_mode, payment_reference, total_amount, reservation_status)
VALUES ('Frank Wilson', '301', DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 3, CURRENT_DATE), 'SMALL', 'BANK_TRANSFER', 'BT111111', 1200.00, 'CONFIRMED');

INSERT INTO reservations (customer_name, room_number, start_date, end_date, room_segment, payment_mode, payment_reference, total_amount, reservation_status)
VALUES ('Grace Lee', '302', CURRENT_DATE, DATEADD('DAY', 2, CURRENT_DATE), 'MEDIUM', 'BANK_TRANSFER', 'BT222222', 2000.00, 'CANCELLED');

INSERT INTO reservations (customer_name, room_number, start_date, end_date, room_segment, payment_mode, payment_reference, total_amount, reservation_status)
VALUES ('Henry Martinez', '303', DATEADD('DAY', -1, CURRENT_DATE), DATEADD('DAY', 1, CURRENT_DATE), 'SMALL', 'BANK_TRANSFER', 'BT333333', 1200.00, 'PENDING_PAYMENT');

