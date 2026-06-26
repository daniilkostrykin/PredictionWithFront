-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS prediction_schema;
/*
CREATE TABLE IF NOT EXISTS prediction_schema.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create events table
CREATE TABLE IF NOT EXISTS prediction_schema.events (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create event_options table
CREATE TABLE IF NOT EXISTS prediction_schema.event_options (
    id SERIAL PRIMARY KEY,
    event_id INTEGER REFERENCES prediction_schema.events(id),
    option_name VARCHAR(255) NOT NULL,
    odd DECIMAL(10, 2) DEFAULT 1.00,
    UNIQUE(event_id, option_name)
);

-- Create predictions table
CREATE TABLE IF NOT EXISTS prediction_schema.predictions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES prediction_schema.users(id),
    event_id INTEGER REFERENCES prediction_schema.events(id),
    event_option_id INTEGER REFERENCES prediction_schema.event_options(id),
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, event_id, event_option_id)
);

-- Create prediction_activities table
CREATE TABLE IF NOT EXISTS prediction_schema.prediction_activities (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES prediction_schema.users(id),
    prediction_id INTEGER REFERENCES prediction_schema.predictions(id),
    activity_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);*/