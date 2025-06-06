INSERT INTO pests (
    common_name,
    scientific_name,
    description,
    damage_symptoms,
    life_cycle,
    season_active,
    organic_control,
    chemical_control,
    prevention_tips,
    image_url
) VALUES
-- Aphids
('Aphids',
 'Aphidoidea',
 'Small, soft-bodied insects, often green or black, found in clusters on new growth.',
 'Curling leaves, sticky honeydew, stunted growth, and potential transmission of plant viruses.',
 'Aphids reproduce rapidly, with females giving live birth; multiple generations occur per season.',
 'Spring through late summer.',
 'Spray with water to dislodge, apply neem oil, or introduce ladybugs.',
 'Use insecticidal soaps or pyrethrin sprays; ensure thorough coverage.',
 'Avoid excess nitrogen fertilization and encourage beneficial insects.',
 'https://killroy.com/wp-content/uploads/2023/04/Aphids-can-be-very-hard-to-see-scaled.webp'),

-- Whiteflies
('Whiteflies',
 'Aleyrodidae',
 'Tiny, white, moth-like insects that congregate on the undersides of leaves.',
 'Yellowing foliage, leaf stippling, premature leaf drop, and sooty mold from honeydew.',
 'Life cycle includes egg, nymph, and adult stages; multiple generations can coexist.',
 'Warm seasons, especially late spring to early fall.',
 'Introduce natural predators like lacewings and lady beetles; use yellow sticky traps.',
 'Apply insecticidal soaps or horticultural oils; avoid broad-spectrum insecticides.',
 'Inspect new plants before introducing them; maintain garden hygiene.',
 'https://www.centralgrower.com/media/blog/cottom-whitefly.jpg'),

-- Tomato Hornworm
('Tomato Hornworm',
 'Manduca quinquemaculata',
 'Large green caterpillars with a horn-like tail, feeding on tomato and related plants.',
 'Defoliation of plants, chewed fruits, and dark green or black droppings.',
 'Eggs hatch into larvae that feed for 3-4 weeks before pupating in the soil.',
 'Late spring through summer.',
 'Handpick caterpillars and drop into soapy water; encourage natural predators like braconid wasps.',
 'Use Bacillus thuringiensis (Bt) or spinosad-based insecticides as a last resort.',
 'Remove weeds and till soil after harvest to destroy overwintering pupae.',
 'https://www.aces.edu/wp-content/uploads/2022/06/shutterstock_103376177-scaled.jpg'),

-- Spittlebugs
('Spittlebugs',
 'Cercopidae',
 'Small insects that produce frothy spittle masses on plant stems for protection.',
 'Discolored leaves, wilting stems, and potential plant death under heavy infestation.',
 'Undergo egg, nymph, and adult stages; nymphs produce spittle for protection.',
 'Late spring through summer.',
 'Remove spittle masses manually; maintain healthy turf to deter infestations.',
 'Apply horticultural oils during egg stage; use insecticidal soaps if necessary.',
 'Keep lawn well-maintained and reduce excessive thatch.',
 'https://tasmanianinsectfieldguide.com/wp-content/uploads/2021/03/IMG_2415-1.jpg'),

-- Mealybugs
('Mealybugs',
 'Pseudococcidae',
 'Small, oval insects covered with white, cottony wax, feeding on plant sap.',
 'Yellowing leaves, stunted growth, honeydew secretion leading to sooty mold.',
 'Females lay eggs in cottony masses; multiple generations per year.',
 'Year-round in greenhouses; spring through fall outdoors.',
 'Introduce natural predators like lady beetles; apply neem oil or insecticidal soap.',
 'Use systemic insecticides for severe infestations; ensure coverage of hidden areas.',
 'Inspect new plants before introduction; isolate infested plants.',
 'https://apal.org.au/wp-content/uploads/2024/11/IMG_2190-Longtailed-Mealybug-scaled.jpg'),

-- Spider Mites
('Spider Mites',
 'Tetranychidae',
 'Tiny arachnids that feed on plant sap, often producing fine webbing on leaves.',
 'Stippling, bronzing, leaf drop, and reduced plant vigor; severe infestations can kill plants.',
 'Rapid reproduction with multiple generations in warm, dry conditions.',
 'Late spring through early fall, especially during hot, dry weather.',
 'Increase humidity, regularly hose down plants, and introduce predatory mites.',
 'Apply horticultural oils or miticides; avoid broad-spectrum insecticides.',
 'Maintain adequate irrigation and reduce dust around plants.',
 'https://hips.hearstapps.com/hmg-prod/images/scouting-for-spider-mite-royalty-free-image-1711635627.jpg?crop=1.00xw:1.00xh;0,0&resize=1200:*'),

-- Flea Beetles
('Flea Beetles',
 'Chrysomelidae',
 'Small, jumping beetles that chew small holes in leaves, giving a "shot-hole" appearance.',
 'Wilting, stunted growth, and reduced yields, especially in seedlings.',
 'Adults lay eggs in soil; larvae feed on roots; one to two generations per year.',
 'Spring through early summer.',
 'Use row covers to protect young plants; implement crop rotation.',
 'Apply appropriate insecticides when damage is severe; follow label instructions.',
 'Maintain weed-free zones and remove plant debris to reduce overwintering sites.',
 'https://www.gardenia.net/wp-content/uploads/2023/05/flea-beetle-780x520.webp'),

-- Japanese Beetles
('Japanese Beetles',
 'Popillia japonica',
 'Metallic green beetles with coppery wings, feeding on a wide range of plants.',
 'Skeletonized leaves, damaged flowers and fruits; grubs damage turf by feeding on roots.',
 'One-year life cycle: eggs laid in soil hatch into grubs, which overwinter and emerge as adults.',
 'Adult beetles active from late June through August.',
 'Handpick beetles early in the morning; use traps cautiously to avoid attracting more beetles.',
 'Apply systemic insecticides for adults; use grub control products in late summer.',
 'Plant beetle-resistant species and maintain healthy, well-irrigated lawns.',
 'https://plantpono.org/wp-content/uploads/Japanese-beetle_-on_the_thumb_-_2-KKPCW-via-Wikimedia-Commons.jpg'),

-- Scale Insects
('Scale Insects',
 'Coccoidea',
 'Small, immobile insects with protective coverings, feeding on plant sap.',
 'Yellowing leaves, stunted growth, branch dieback, and honeydew leading to sooty mold.',
 'Life cycle includes egg, crawler (mobile nymph), and adult stages; multiple generations possible.',
 'Spring through summer.',
 'Introduce natural enemies like parasitic wasps; prune and destroy infested branches.',
 'Apply horticultural oils during crawler stage; systemic insecticides for severe cases.',
 'Inspect plants regularly and maintain plant health to prevent infestations.',
 'https://moowy.co.uk/wp-content/uploads/2023/09/Scale-insects-clustered-on-a-plant-1285x492.jpg');
