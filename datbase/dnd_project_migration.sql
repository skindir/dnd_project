BEGIN TRANSACTION;

ALTER TABLE characters ADD COLUMN alignment TEXT;
ALTER TABLE characters ADD COLUMN personality_traits TEXT;
ALTER TABLE characters ADD COLUMN ideals TEXT;
ALTER TABLE characters ADD COLUMN bonds TEXT;
ALTER TABLE characters ADD COLUMN flaws TEXT;

CREATE TABLE IF NOT EXISTS class_base_stats (
    class_id     INTEGER PRIMARY KEY,
    strength     INTEGER NOT NULL,
    constitution INTEGER NOT NULL,
    dexterity    INTEGER NOT NULL,
    intelligence INTEGER NOT NULL,
    wisdom       INTEGER NOT NULL,
    charisma     INTEGER NOT NULL,
    FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS class_proficiency (
    class_id       INTEGER NOT NULL,
    proficiency_id INTEGER NOT NULL,
    PRIMARY KEY (class_id, proficiency_id),
    FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE,
    FOREIGN KEY (proficiency_id) REFERENCES proficiency(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS character_detail_option (
    id          INTEGER PRIMARY KEY,
    option_type TEXT NOT NULL,
    value       TEXT NOT NULL,
    UNIQUE(option_type, value)
);

CREATE TRIGGER IF NOT EXISTS characters_class_required_insert
BEFORE INSERT ON characters
WHEN NEW.class_id IS NULL
BEGIN
    SELECT RAISE(ABORT, 'characters.class_id is required');
END;

CREATE TRIGGER IF NOT EXISTS characters_class_required_update
BEFORE UPDATE OF class_id ON characters
WHEN NEW.class_id IS NULL
BEGIN
    SELECT RAISE(ABORT, 'characters.class_id is required');
END;

INSERT OR IGNORE INTO class(id, name, caster_type, hit_dice) VALUES
(1, 'Bard', 'full', 8),
(2, 'Barbarian', 'none', 12),
(3, 'Fighter', 'none', 10),
(4, 'Wizard', 'full', 6),
(5, 'Druid', 'full', 8),
(6, 'Cleric', 'full', 8),
(7, 'Artificer', 'half', 8),
(8, 'Warlock', 'pact', 8),
(9, 'Monk', 'none', 8),
(10, 'Paladin', 'half', 10),
(11, 'Rogue', 'none', 8),
(12, 'Ranger', 'half', 10),
(13, 'Sorcerer', 'full', 6);

INSERT OR IGNORE INTO race(id, name) VALUES
(1, 'Human'), (2, 'Dwarf'), (3, 'Elf'), (4, 'Halfling'), (5, 'Gnome'),
(6, 'Half-Elf'), (7, 'Half-Orc'), (8, 'Tiefling'), (9, 'Dragonborn');

INSERT OR IGNORE INTO background(id, name) VALUES
(1, 'Acolyte'), (2, 'Entertainer'), (3, 'Urchin'), (4, 'Noble'),
(5, 'Guild Artisan'), (6, 'Sailor'), (7, 'Sage'), (8, 'Folk Hero'),
(9, 'Hermit'), (10, 'Criminal'), (11, 'Retainer'), (12, 'Soldier'),
(13, 'Outlander'), (14, 'Charlatan');

INSERT OR IGNORE INTO language(id, name) VALUES
(1, 'Abyssal'), (2, 'Druidic'), (3, 'Giant'), (4, 'Infernal'),
(5, 'Thieves'' Cant'), (6, 'Celestial'), (7, 'Undercommon'),
(8, 'Primordial'), (9, 'Common'), (10, 'Orc'), (11, 'Elvish'),
(12, 'Gnomish'), (13, 'Goblin'), (14, 'Half-Elvish'), (15, 'Halfling'),
(16, 'Sylvan'), (17, 'Tiefling'), (18, 'Aquan');

INSERT OR IGNORE INTO proficiency(id, name) VALUES
(1, 'Saving Throw (Strength)'), (2, 'Athletics'),
(3, 'Saving Throw (Dexterity)'), (4, 'Acrobatics'),
(5, 'Sleight of Hand'), (6, 'Stealth'),
(7, 'Saving Throw (Constitution)'), (8, 'Saving Throw (Intelligence)'),
(9, 'Arcana'), (10, 'History'), (11, 'Investigation'), (12, 'Nature'),
(13, 'Religion'), (14, 'Saving Throw (Wisdom)'), (15, 'Animal Handling'),
(16, 'Insight'), (17, 'Medicine'), (18, 'Perception'), (19, 'Survival'),
(20, 'Saving Throw (Charisma)'), (21, 'Deception'), (22, 'Intimidation'),
(23, 'Performance'), (24, 'Persuasion');

INSERT OR IGNORE INTO character_detail_option(option_type, value) VALUES
('alignment', 'Lawful Good'), ('alignment', 'Neutral Good'), ('alignment', 'Chaotic Good'),
('alignment', 'Lawful Neutral'), ('alignment', 'True Neutral'), ('alignment', 'Chaotic Neutral'),
('alignment', 'Lawful Evil'), ('alignment', 'Neutral Evil'), ('alignment', 'Chaotic Evil'),
('personality_traits', 'I am always polite and respectful'),
('personality_traits', 'I trust my friends and protect them'),
('personality_traits', 'I am used to seeking profit in any situation'),
('personality_traits', 'I speak plainly, even when it is unpleasant'),
('personality_traits', 'I stay calm in the face of danger'),
('personality_traits', 'I love beautiful stories and glorious deeds'),
('personality_traits', 'I find it hard to trust strangers'),
('personality_traits', 'I am always seeking new knowledge'),
('ideals', 'Good'), ('ideals', 'Freedom'), ('ideals', 'Justice'), ('ideals', 'Honor'),
('ideals', 'Knowledge'), ('ideals', 'Power'), ('ideals', 'Tradition'), ('ideals', 'Redemption'),
('bonds', 'I protect my family'), ('bonds', 'I serve my people'),
('bonds', 'I owe my life to an old friend'), ('bonds', 'I seek a lost relic'),
('bonds', 'I must avenge the past'), ('bonds', 'I keep my mentor''s secret'),
('bonds', 'I want to restore my lost honor'), ('bonds', 'I am bound by oath to an order'),
('flaws', 'I am too trusting'), ('flaws', 'I am greedy for gold'),
('flaws', 'I am hot-tempered'), ('flaws', 'I fear losing control'),
('flaws', 'I often underestimate danger'), ('flaws', 'I envy the fame of others'),
('flaws', 'I have trouble admitting mistakes'), ('flaws', 'I easily give in to temptation');

INSERT OR IGNORE INTO class_base_stats(class_id, strength, constitution, dexterity, intelligence, wisdom, charisma) VALUES
(1, 10, 12, 14, 12, 13, 14),
(2, 16, 16, 12, 8, 10, 13),
(3, 16, 14, 14, 10, 10, 11),
(4, 8, 14, 12, 16, 14, 11),
(5, 8, 14, 12, 12, 16, 13),
(6, 10, 14, 10, 12, 16, 13),
(7, 10, 14, 12, 16, 12, 11),
(8, 8, 14, 14, 12, 12, 15),
(9, 12, 13, 16, 10, 16, 8),
(10, 16, 14, 10, 8, 12, 15),
(11, 10, 12, 16, 14, 10, 13),
(12, 12, 14, 16, 10, 14, 9),
(13, 8, 14, 12, 12, 13, 16);

INSERT OR IGNORE INTO class_proficiency(class_id, proficiency_id) VALUES
(1, 4), (1, 20),
(2, 1), (2, 7),
(3, 1), (3, 7),
(4, 8), (4, 14),
(5, 4), (5, 14),
(6, 14), (6, 20),
(7, 7), (7, 8),
(8, 14), (8, 20),
(9, 1), (9, 3),
(10, 14), (10, 20),
(11, 3), (11, 8),
(12, 1), (12, 3),
(13, 7), (13, 20);

COMMIT;
