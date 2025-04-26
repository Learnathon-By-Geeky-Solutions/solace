CREATE TABLE pests (
    id SERIAL PRIMARY KEY,
    common_name TEXT NOT NULL,
    scientific_name TEXT,
    description TEXT,
    damage_symptoms TEXT,
    life_cycle TEXT,
    season_active TEXT,
    organic_control TEXT,
    chemical_control TEXT,
    prevention_tips TEXT,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
