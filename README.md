# DND Companion

DND Companion is an Android application for keeping Dungeons & Dragons character information, ability scores, inventory, equipment, coins, and spells in one place.

## Features

### Character Management

- Create new characters.
- Select previously created characters.
- Delete characters from the character selection screen.
- Limit the local character list to 15 characters.
- Choose a class, race, background, and alignment.
- Choose personality traits, ideals, bonds, and flaws.
- Choose languages and proficiencies.
- Fill and adjust ability scores.
- Apply class-based preset ability scores when a class is selected.
- Save character data locally. Character creation is also written into the project SQLite schema, while the current character-sheet state used by the UI is persisted locally through the app's character repository.

### Character Sheet

- Display the main character identity information.
- Display ability scores and related saving throws or skills.
- Display combat values such as armor class, initiative, speed, hit points, proficiency bonus, passive wisdom, and hit dice.
- Display selected languages.
- Display saving throws and proficiencies.
- Display personality traits, ideals, bonds, flaws, and additional character notes.

### Inventory and Equipment

- Load inventory data for the selected character.
- Automatically create an empty inventory, bag, and equipment record when a character does not have inventory data yet.
- Display inventory items by category.
- Search items by name in the Add Item dialog.
- Filter item search results by category.
- Add items to the selected character's bag.
- Remove items from inventory.
- Decrease the quantity of stacked items.
- Show an additional confirmation before removing quest items.
- View item details, including icon, name, category, description, and quantity.
- Equip and unequip supported item types.
- Support equipment slots for weapons, armor pieces, rings, earrings, gloves, pauldrons, boots, belts, pants, chest armor, neck items, and off-hand items.
- Handle one-handed and two-handed weapons. Two-handed weapons use the main hand and block the off-hand slot.
- Manage platinum, gold, silver, and copper coin totals.
- Display equipped item icons over the character equipment layout.

Inventory categories:

- Weapons
- Armor
- Accessories
- Tools
- Materials
- Other
- Quest Items

### Spellbook

- View spells in the character spellbook.
- Display spells grouped by spell level from 0 to 9.
- Add available spells from the built-in spell library.
- View spell details.
- Track spell uses.
- Restore spell uses after short rest and long rest actions.
- Filter available spells by the selected character class and character level.

## Technology Stack

- Java
- Android SDK
- Gradle
- Android Studio
- SQLite
- Material Components
- RecyclerView
- AndroidX AppCompat

## Database

The project uses a prepared local SQLite database for reference data, inventory items, item icons, equipment, bags, and spell-related data. The repository keeps the main database file at:

```text
datbase/dnd_project.db
```

The Android app also includes an asset copy used by the inventory database loader:

```text
app/src/main/assets/database/dnd_project.db
```

On first use, the app copies the asset database into the application's private database directory. The inventory repository also validates the runtime copy and applies the `item.equipment_slot` migration when needed.

The main database groups include:

- Characters and stats.
- Classes, races, backgrounds, languages, and proficiencies.
- Inventory, bag, equipment, and item records.
- Spells and spell-related tables.
- Character detail options such as alignment, personality traits, ideals, bonds, and flaws.

When changing the database schema or seed data, keep `datbase/dnd_project.db` and `app/src/main/assets/database/dnd_project.db` synchronized.

## Project Structure

```text
app/
└── src/main/
    ├── assets/database/
    │   └── dnd_project.db
    ├── java/com/example/dna_project/
    │   ├── MainActivity.java
    │   ├── data/
    │   └── ui/
    └── res/

datbase/
└── dnd_project.db

gradle/
└── wrapper/
```

## Getting Started

Prerequisites:

- Android Studio.
- Android SDK.
- JDK compatible with the project configuration. The project uses Java 11 source and target compatibility.
- Android Emulator or a physical Android device.

Steps:

1. Clone the repository.
2. Open the project in Android Studio.
3. Wait for Gradle synchronization to finish.
4. Select an emulator or connect an Android device.
5. Run the application from Android Studio.

Build check on Windows:

```powershell
.\gradlew.bat assembleDebug
```

Build check on macOS and Linux:

```bash
./gradlew assembleDebug
```

## Usage

1. Create a character from the character selection screen.
2. Select the character's class, race, background, alignment, traits, languages, proficiencies, and ability scores.
3. Open the character sheet to review stats and character details.
4. Use the bottom navigation to switch between Stats, Bag, and Spells.
5. Add items to the bag, equip gear, remove items, and track coins in the inventory screen.
6. Add spells to the spellbook and track spell uses during play.

## Development Notes

- Do not edit only one SQLite database copy and forget to synchronize the other copy.
- Run a Gradle build after code, resource, or database-related changes.
- Check SQLite binary conflicts manually before merging database changes.
- Do not commit local IDE settings, machine-specific configuration, temporary database backups, or generated build output.

## Status

This project is in active development.

## Screenshots

Screenshots will be added later.

## License

This project is intended for educational purposes.
