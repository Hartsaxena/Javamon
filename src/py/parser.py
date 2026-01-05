import json
from bs4 import BeautifulSoup

def clean_stat(value):
    """
    Helper function to clean numerical stats.
    Converts strings like '100' to integers.
    Converts '—' to None (JSON null).
    Keeps '∞' as a string or handles specific edge cases.
    """
    if not value or value.strip() == "—":
        return None
    
    value = value.strip()
    
    # Handle Infinity symbol found in accuracy for moves like Swift/Aerial Ace
    if "∞" in value:
        return "Infinity"
    
    try:
        return int(value)
    except ValueError:
        return value  # Return original string if it's not a clear number

def parse_pokemon_moves_json(html_file, output_file):
    try:
        with open(html_file, 'r', encoding='utf-8') as f:
            soup = BeautifulSoup(f, 'html.parser')

        table = soup.find('table', id='moves')
        if not table:
            print("Error: Could not find the moves table in the HTML file.")
            return

        moves_data = []
        
        # Select all rows in the table body
        rows = table.find('tbody').find_all('tr')

        for row in rows:
            cols = row.find_all('td')
            if not cols:
                continue

            # Extract Raw Text
            name_tag = cols[0].find('a', class_='ent-name')
            name = name_tag.text.strip() if name_tag else "Unknown"

            type_tag = cols[1].find('a', class_='type-icon')
            move_type = type_tag.text.strip() if type_tag else "Unknown"

            # Extract Category (Physical/Special/Status)
            category = cols[2].get('data-filter-value', '').capitalize()
            if not category and cols[2].find('img'):
                category = cols[2].find('img').get('alt', 'Unknown')

            # Extract Text for cleaning
            power_text = cols[3].text.strip()
            accuracy_text = cols[4].text.strip()
            pp_text = cols[5].text.strip()
            effect_text = cols[6].text.strip()
            prob_text = cols[7].text.strip() if len(cols) > 7 else "—"

            # Build Dictionary
            move_entry = {
                "name": name,
                "type": move_type,
                "category": category,
                "stats": {
                    "power": clean_stat(power_text),
                    "accuracy": clean_stat(accuracy_text),
                    "pp": clean_stat(pp_text),
                    "probability_percent": clean_stat(prob_text)
                },
                "effect": effect_text
            }
            
            moves_data.append(move_entry)

        # Write to JSON file
        with open(output_file, 'w', encoding='utf-8') as out:
            json.dump(moves_data, out, indent=4, ensure_ascii=False)

        print(f"Successfully exported {len(moves_data)} moves to '{output_file}'.")

    except FileNotFoundError:
        print(f"Error: The file '{html_file}' was not found.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

# --- Configuration ---
html_filename = "movedatabase.html" 
output_filename = "pokemon_moves.json"

if __name__ == "__main__":
    parse_pokemon_moves_json(html_filename, output_filename)