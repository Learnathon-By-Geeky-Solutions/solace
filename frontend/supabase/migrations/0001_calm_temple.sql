/*
  # Initial Schema Setup for Balcony Garden Planner

  1. New Tables
    - `gardens`
      - `id` (uuid, primary key)
      - `user_id` (uuid, references auth.users)
      - `name` (text)
      - `location` (text)
      - `created_at` (timestamp)
    - `plants`
      - `id` (uuid, primary key)
      - `garden_id` (uuid, references gardens)
      - `name` (text)
      - `type` (text)
      - `planting_date` (date)
      - `health_status` (text)
      - `created_at` (timestamp)
    - `plant_images`
      - `id` (uuid, primary key)
      - `plant_id` (uuid, references plants)
      - `image_url` (text)
      - `analysis_result` (jsonb)
      - `created_at` (timestamp)

  2. Security
    - Enable RLS on all tables
    - Add policies for authenticated users to manage their own data
*/

-- Gardens table
CREATE TABLE IF NOT EXISTS gardens (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid REFERENCES auth.users NOT NULL,
  name text NOT NULL,
  location text NOT NULL,
  created_at timestamptz DEFAULT now()
);

ALTER TABLE gardens ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage their own gardens"
  ON gardens
  FOR ALL
  TO authenticated
  USING (auth.uid() = user_id);

-- Plants table
CREATE TABLE IF NOT EXISTS plants (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  garden_id uuid REFERENCES gardens ON DELETE CASCADE NOT NULL,
  name text NOT NULL,
  type text NOT NULL,
  planting_date date NOT NULL,
  health_status text DEFAULT 'healthy',
  created_at timestamptz DEFAULT now()
);

ALTER TABLE plants ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage plants in their gardens"
  ON plants
  FOR ALL
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM gardens
      WHERE gardens.id = plants.garden_id
      AND gardens.user_id = auth.uid()
    )
  );

-- Plant images table
CREATE TABLE IF NOT EXISTS plant_images (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  plant_id uuid REFERENCES plants ON DELETE CASCADE NOT NULL,
  image_url text NOT NULL,
  analysis_result jsonb DEFAULT '{}',
  created_at timestamptz DEFAULT now()
);

ALTER TABLE plant_images ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage images of their plants"
  ON plant_images
  FOR ALL
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM plants
      JOIN gardens ON gardens.id = plants.garden_id
      WHERE plants.id = plant_images.plant_id
      AND gardens.user_id = auth.uid()
    )
  );

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS gardens_user_id_idx ON gardens(user_id);
CREATE INDEX IF NOT EXISTS plants_garden_id_idx ON plants(garden_id);
CREATE INDEX IF NOT EXISTS plant_images_plant_id_idx ON plant_images(plant_id);