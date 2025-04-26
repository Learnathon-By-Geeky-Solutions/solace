create table plants_library (
                                id uuid primary key default gen_random_uuid(),

    -- Text fields
                                common_name text,
                                other_name text,
                                scientific_name text,
                                short_description text,
                                origin text,
                                plant_type text,
                                climate text,
                                life_cycle text,
                                watering_frequency text,
                                soil_type text,
                                size text,
                                sunlight_requirement text,
                                growth_rate text,
                                ideal_place text,
                                care_level text,
                                image_url text,
                                best_planting_season text,
                                gardening_tips text,
                                pruning_guide text,

    -- Numeric fields
                                seed_depth numeric,
                                germination_time numeric,
                                time_to_harvest numeric,

    -- Boolean fields
                                flower boolean,
                                fruit boolean,
                                medicinal boolean,

    -- Range type for temperature
                                temperature_range numrange,

    -- Array (list) fields
                                common_pests text[],
                                common_diseases text[],
                                companion_plants text[],
                                avoid_planting_with text[],
                                pest_disease_prevention_tips text[],
                                cool_facts text[],
                                edible_parts text[],

    -- Timestamps
                                created_at timestamp with time zone default now(),
                                updated_at timestamp with time zone default now()
);
