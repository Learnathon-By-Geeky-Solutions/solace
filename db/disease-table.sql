CREATE TABLE plant_diseases (
    id SERIAL PRIMARY KEY,
    common_name TEXT NOT NULL,
    scientific_name TEXT,
    description TEXT,
    symptoms TEXT,
    favorable_conditions TEXT,
    prevention_tips TEXT,
    organic_control TEXT,
    chemical_control TEXT,
    image_url TEXT,
    transmission_method TEXT,
    contagiousness TEXT CHECK (contagiousness IN ('low', 'moderate', 'high')),
    severity_rating TEXT CHECK (severity_rating IN ('mild', 'moderate', 'severe')),
    time_to_onset TEXT,
    recovery_chances TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
