SELECT * FROM customer;

SELECT * FROM customer
WHERE state = 'AZ';

SELECT * FROM invoice
WHERE invoice_date < CURRENT_DATE - INTERVAL '6 months';


UPDATE customer
SET phone = NULL
WHERE phone !~ '^\+1\s\(\d{3}\)\s\d{3}-\d{4}$';

SELECT * FROM track
WHERE milliseconds > 180000;

UPDATE customer
SET country = 'USA',
    address = NULL,
    city = NULL,
    state = NULL
WHERE country NOT LIKE 'USA';

CREATE OR REPLACE FUNCTION get_customer_total_spending(p_customer_id INT)
RETURNS NUMERIC AS $$
DECLARE
    total_spent NUMERIC;
BEGIN
    SELECT COALESCE(SUM(total), 0)
    INTO total_spent
    FROM invoice
    WHERE customer_id = p_customer_id;

    RETURN total_spent;
END;
$$ LANGUAGE plpgsql;

SELECT get_customer_total_spending(7);

CREATE OR REPLACE PROCEDURE update_employee_manager(
    p_employee_id INT,
    p_new_manager_id INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_manager_exists BOOLEAN;
    v_creates_cycle BOOLEAN;
BEGIN
    IF p_employee_id = p_new_manager_id THEN
        RAISE EXCEPTION 'Employee % cannot report to themselves', p_employee_id;
    END IF;

    SELECT EXISTS (
        SELECT 1 FROM employee WHERE employee_id = p_new_manager_id
    ) INTO v_manager_exists;

    IF NOT v_manager_exists THEN
        RAISE EXCEPTION 'Manager with employee_id % does not exist', p_new_manager_id;
    END IF;

    WITH RECURSIVE subordinates AS (
        SELECT employee_id
        FROM employee
        WHERE reports_to = p_employee_id

        UNION ALL

        SELECT e.employee_id
        FROM employee e
        JOIN subordinates s ON e.reports_to = s.employee_id
    )
    SELECT EXISTS (
        SELECT 1 FROM subordinates WHERE employee_id = p_new_manager_id
    ) INTO v_creates_cycle;

    IF v_creates_cycle THEN
        RAISE EXCEPTION 'Assigning employee % to manager % would create a circular reporting relationship', p_employee_id, p_new_manager_id;
    END IF;

    UPDATE employee
    SET reports_to = p_new_manager_id
    WHERE employee_id = p_employee_id;

    RAISE NOTICE 'Employee % now reports to %', p_employee_id, p_new_manager_id;
END;
$$;

CALL update_employee_manager(7, 6);
/*
CREATE SCHEMA pets;

CREATE TABLE pets.customer (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    email VARCHAR(60) NOT NULL,
    phone VARCHAR(24)
);

CREATE TABLE pets.pet (
    pet_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL,
    name VARCHAR(40) NOT NULL,
    species VARCHAR(40) NOT NULL,
    breed VARCHAR(60),
    birth_date DATE,
    CONSTRAINT pet_customer_id_fkey
        FOREIGN KEY (customer_id)
        REFERENCES pets.customer (customer_id)
        ON DELETE CASCADE
);

INSERT INTO pets.customer (first_name, last_name, email, phone) VALUES
    ('Alice', 'Nguyen', 'alice.nguyen@example.com', '+1 555 111-2222'),
    ('Marcus', 'Reed', 'marcus.reed@example.com', '+1 555 333-4444'),
    ('Priya', 'Sharma', 'priya.sharma@example.com', '+1 555 555-6666');

INSERT INTO pets.pet (customer_id, name, species, breed, birth_date) VALUES
    (1, 'Biscuit', 'Dog', 'Golden Retriever', '2020-04-12'),
    (1, 'Whiskers', 'Cat', 'Domestic Shorthair', '2019-08-01'),
    (2, 'Rex', 'Dog', 'German Shepherd', '2021-01-20'),
    (3, 'Momo', 'Rabbit', 'Holland Lop', '2022-06-15');
*/
SELECT * FROM pets.customer;
SELECT * FROM pets.pet;