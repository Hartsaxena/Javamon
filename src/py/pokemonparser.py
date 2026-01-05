from bs4 import BeautifulSoup

def generate_pokemon_java_code(html_file_path):
    """
    Parses the HTML file and generates Java register() calls for each Pokemon.
    """
    try:
        with open(html_file_path, 'r', encoding='utf-8') as f:
            soup = BeautifulSoup(f, 'html.parser')

        # Find the main pokedex table
        table = soup.find('table', id='pokedex')
        if not table:
            print("// Error: Could not find table with id 'pokedex'")
            return

        rows = table.find('tbody').find_all('tr')

        for row in rows:
            cols = row.find_all('td')
            
            # Column indices based on the specific HTML structure:
            # 0: # (Number)
            # 1: Name
            # 2: Type
            # 3: Total
            # 4: HP
            # 5: Attack
            # 6: Defense
            # 7: Sp. Atk
            # 8: Sp. Def
            # 9: Speed

            if len(cols) < 10:
                continue

            # 1. Extract Name
            # The name is inside an 'a' tag or text inside the cell.
            # Sometimes there are sub-labels (like 'Mega Venusaur'), represented by <small>
            name_cell = cols[1]
            name_text = name_cell.get_text(separator=" ").strip()
            
            # Clean up the name (e.g., remove duplicate spaces if any)
            name = " ".join(name_text.split())
            # Escape double quotes for Java strings if necessary
            name = name.replace('"', '\\"')
            # Handle weird gender symbols
            name = name.replace("♀", "F").replace("♂", "M")

            if ("Mega" in name) or ("Primal" in name) or ("Alolan" in name) or ("Galarian" in name):
                name = name.split()[1:]
                name = " ".join(name)

            # 2. Extract Types
            type_cell = cols[2]
            type_links = type_cell.find_all('a')
            
            types = [t.text.strip() for t in type_links]
            
            t1 = types[0] if len(types) > 0 else "Unknown"
            t2 = types[1] if len(types) > 1 else "null"

            # 3. Extract Stats
            # Parse integers from the text content
            try:
                hp = int(cols[4].text.strip())
                atk = int(cols[5].text.strip())
                deff = int(cols[6].text.strip())
                spa = int(cols[7].text.strip())
                spd = int(cols[8].text.strip())
                spe = int(cols[9].text.strip())
            except ValueError:
                # Skip rows where stats aren't numbers (headers, etc.)
                continue

            # 4. Generate Java Code
            # Format: register("Name", Type1, Type2, hp, atk, def, spa, spd, spe);
            java_line = (
                f'register("{name}", {t1}, {t2}, '
                f'{hp}, {atk}, {deff}, {spa}, {spd}, {spe});'
            )
            
            print(java_line)

    except Exception as e:
        print(f"// An error occurred: {e}")

# --- Execution ---
# Assumes the file is in the same directory. Change path if needed.
if __name__ == "__main__":
    generate_pokemon_java_code("pokemondatabase.html")