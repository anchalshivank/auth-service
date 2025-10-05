-- V1__create_users_table.sql
-- Business user table linked to Keycloak
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,

    -- Link to Keycloak user (must match entity field name and type)
    keycloak_user_id VARCHAR(255) UNIQUE NOT NULL,

    -- Business-specific fields
    phone_number VARCHAR(20) UNIQUE,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('tenant', 'owner')),
    score INT NOT NULL DEFAULT 500,

    -- Profile fields (cached from Keycloak for performance)
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),

    -- Status fields
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Add indexes for performance
CREATE INDEX idx_users_keycloak_id ON users(keycloak_user_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);
CREATE INDEX idx_users_type ON users(user_type);