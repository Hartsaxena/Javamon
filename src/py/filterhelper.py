
import json
import re
from pathlib import Path


def load_moves(json_path: Path):
    """Load the list of moves from the JSON file."""
    with json_path.open("r", encoding="utf-8") as f:
        return json.load(f)


def moves_without_effect(moves):
    """
    Return all moves that have no effect text.

    A move is considered to have 'no effect' if:
    - the 'effect' key is missing, or
    - the 'effect' value is None, or
    - the 'effect' value is an empty/whitespace-only string.
    """
    result = []
    for move in moves:
        effect = move.get("effect", None)
        if effect is None or (isinstance(effect, str) and effect.strip() == ""):
            result.append(move)
    return result


def print_moves_without_effect(moves):
    """Pretty-print the names (and details) of moves without an effect."""
    no_effect_moves = moves_without_effect(moves)

    print(f"Found {len(no_effect_moves)} moves without an effect.\n")
    for move in no_effect_moves:
        name = move.get("name", "<unknown>")
        move_type = move.get("type", "<unknown>")
        category = move.get("category", "<unknown>")
        stats = move.get("stats", {}) or {}
        power = stats.get("power", None)
        accuracy = stats.get("accuracy", None)
        pp = stats.get("pp", None)

        print(
            f"- {name} "
            f"(Type: {move_type}, Category: {category}, "
            f"Power: {power}, Accuracy: {accuracy}, PP: {pp})"
        )


def escape_java_string(s: str) -> str:
    """Escape a Python string so it is safe inside Java double quotes."""
    return s.replace("\\", "\\\\").replace('"', '\\"')


def print_java_for_moves_without_effect(moves):
    """Print Java `MoveRegistry` snippets for moves without an effect."""
    no_effect_moves = moves_without_effect(moves)

    print("\n// ========= Java snippets for MoveRegistry =========")
    for move in no_effect_moves:
        name = move.get("name", "<unknown>")
        move_type = move.get("type", "Normal")
        category = (move.get("category") or "").strip()
        stats = move.get("stats", {}) or {}
        power = stats.get("power", None)
        accuracy = stats.get("accuracy", None)
        pp = stats.get("pp", None)

        # Build comment-friendly numeric fields
        power_val = power if power is not None else 0
        # Handle accuracy: null, "Infinity", or numeric values
        if accuracy is None or (isinstance(accuracy, str) and accuracy.lower() in ("infinity", "inf")):
            acc_val = 100  # Default for always-hit moves
        else:
            acc_val = accuracy
        pp_val = pp if pp is not None else 10

        name_java = escape_java_string(name)

        # Decide whether to use DamagingMove or Move
        is_damaging = category.lower() in ("physical", "special") and power is not None

        if is_damaging:
            damage_type = "Physical" if category.lower() == "physical" else "Special"
            print(f'moves.put("{name_java}", new DamagingMove(')
            print(f'    "{name_java}",')
            print(f"    {move_type},")
            print(f"    {damage_type},")
            print(f"    {power_val},  // power")
            print(f"    {acc_val}, // accuracy")
            print(f"    {pp_val},  // pp")
            print(f"    Collections.emptyList()")
            print("));\n")
        else:
            # Status / non-damaging move
            print(f'moves.put("{name_java}", new Move(')
            print(f'    "{name_java}",')
            print(f"    {move_type},")
            print(f"    {acc_val}, // accuracy")
            print(f"    {pp_val},  // pp")
            print(f"    Collections.emptyList()")
            print("));\n")


def parse_priorities_file(priorities_path: Path) -> set[str]:
    """
    Parse priorities.txt and return a set of move names that have non-zero priority.
    
    Lines with "0" or "None" are ignored. Only moves with actual priority values
    (positive or negative) are included.
    Handles line continuations where moves span multiple lines.
    """
    priority_moves = set()
    
    if not priorities_path.exists():
        return priority_moves
    
    with priorities_path.open("r", encoding="utf-8") as f:
        current_priority_line = None
        for line in f:
            line = line.strip()
            if not line:
                current_priority_line = None
                continue
            
            # Check if this is a priority line (starts with +/- number)
            match = re.match(r'^([+-]?\d+)\s+(.+)$', line)
            if match:
                # New priority line
                moves_str = match.group(2)
                # Remove footnote markers like [a]
                moves_str = re.sub(r'\[[a-z]+\]', '', moves_str)
                # Split by comma and clean up move names
                moves = [m.strip() for m in moves_str.split(',') if m.strip()]
                priority_moves.update(moves)
                # Check if line ends with comma (continuation expected)
                if line.endswith(','):
                    current_priority_line = True
                else:
                    current_priority_line = None
            elif current_priority_line and not (line.startswith("0") or "None" in line):
                # Continuation line - contains more moves for the previous priority
                # Remove footnote markers like [a]
                moves_str = re.sub(r'\[[a-z]+\]', '', line)
                # Split by comma and clean up move names
                moves = [m.strip() for m in moves_str.split(',') if m.strip()]
                priority_moves.update(moves)
                # Check if this continuation also ends with comma
                if not line.endswith(','):
                    current_priority_line = None
    
    return priority_moves


def parse_priorities_with_values(priorities_path: Path) -> dict[str, int]:
    """
    Parse priorities.txt and return a dict mapping move names to their priority values.
    
    Returns a dictionary like {"Quick Attack": 1, "Counter": -5, ...}
    """
    priority_map = {}
    
    if not priorities_path.exists():
        return priority_map
    
    with priorities_path.open("r", encoding="utf-8") as f:
        current_priority = None
        current_priority_line = None
        for line in f:
            line = line.strip()
            if not line:
                current_priority = None
                current_priority_line = None
                continue
            
            # Check if this is a priority line (starts with +/- number)
            match = re.match(r'^([+-]?\d+)\s+(.+)$', line)
            if match:
                # New priority line
                current_priority = int(match.group(1))
                moves_str = match.group(2)
                # Remove footnote markers like [a]
                moves_str = re.sub(r'\[[a-z]+\]', '', moves_str)
                # Split by comma and clean up move names
                moves = [m.strip() for m in moves_str.split(',') if m.strip()]
                for move in moves:
                    priority_map[move] = current_priority
                # Check if line ends with comma (continuation expected)
                if line.endswith(','):
                    current_priority_line = True
                else:
                    current_priority_line = None
            elif current_priority_line and not (line.startswith("0") or "None" in line):
                # Continuation line - contains more moves for the previous priority
                # Remove footnote markers like [a]
                moves_str = re.sub(r'\[[a-z]+\]', '', line)
                # Split by comma and clean up move names
                moves = [m.strip() for m in moves_str.split(',') if m.strip()]
                for move in moves:
                    priority_map[move] = current_priority
                # Check if this continuation also ends with comma
                if not line.endswith(','):
                    current_priority_line = None
    
    return priority_map


def is_priority_only_effect(effect: str) -> bool:
    """
    Check if an effect string describes ONLY priority mechanics.
    
    Returns True if the effect is empty/None or only describes priority
    (e.g., "User attacks first.").
    """
    if not effect or not isinstance(effect, str):
        return True
    
    effect_lower = effect.lower().strip()
    
    # Common priority-only effect patterns
    priority_only_patterns = [
        r'^user attacks first\.?$',
        r'^user attacks first$',
        r'^attacks first\.?$',
        r'^attacks first$',
        r'^always goes first\.?$',
        r'^always goes first$',
        r'^goes first\.?$',
        r'^goes first$',
    ]
    
    for pattern in priority_only_patterns:
        if re.match(pattern, effect_lower):
            return True
    
    return False


def moves_with_priority_only(moves, priority_moves: set[str]):
    """
    Return moves that have priority AND no other effect (or only priority effect).
    """
    result = []
    for move in moves:
        name = move.get("name", "")
        if name not in priority_moves:
            continue
        
        effect = move.get("effect", None)
        if is_priority_only_effect(effect):
            result.append(move)
    
    return result


def print_priority_only_moves(moves, priority_moves: set[str]):
    """Pretty-print moves that have priority but no other effect."""
    priority_only = moves_with_priority_only(moves, priority_moves)
    
    print(f"\nFound {len(priority_only)} moves with priority and no other effect:\n")
    for move in priority_only:
        name = move.get("name", "<unknown>")
        move_type = move.get("type", "<unknown>")
        category = move.get("category", "<unknown>")
        stats = move.get("stats", {}) or {}
        power = stats.get("power", None)
        accuracy = stats.get("accuracy", None)
        pp = stats.get("pp", None)
        effect = move.get("effect", "")
        
        print(
            f"- {name} "
            f"(Type: {move_type}, Category: {category}, "
            f"Power: {power}, Accuracy: {accuracy}, PP: {pp})"
        )
        if effect:
            print(f"  Effect: {effect}")


def print_java_for_priority_only_moves(moves, priority_moves: set[str], priority_map: dict[str, int]):
    """Print Java `MoveRegistry` snippets for moves with priority but no other effect."""
    priority_only = moves_with_priority_only(moves, priority_moves)
    
    print("\n// ========= Java snippets for MoveRegistry (Priority-only moves) =========")
    for move in priority_only:
        name = move.get("name", "<unknown>")
        move_type = move.get("type", "Normal")
        category = (move.get("category") or "").strip()
        stats = move.get("stats", {}) or {}
        power = stats.get("power", None)
        accuracy = stats.get("accuracy", None)
        pp = stats.get("pp", None)
        priority_value = priority_map.get(name, 0)
        
        # Build comment-friendly numeric fields
        power_val = power if power is not None else 0
        # Handle accuracy: null, "Infinity", or numeric values
        if accuracy is None or (isinstance(accuracy, str) and accuracy.lower() in ("infinity", "inf")):
            acc_val = 100  # Default for always-hit moves
        else:
            acc_val = accuracy
        pp_val = pp if pp is not None else 10
        
        name_java = escape_java_string(name)
        
        # Decide whether to use DamagingMove or Move
        is_damaging = category.lower() in ("physical", "special") and power is not None
        
        if is_damaging:
            damage_type = "Physical" if category.lower() == "physical" else "Special"
            print(f'moves.put("{name_java}", new DamagingMove(')
            print(f'    "{name_java}",')
            print(f"    {move_type},")
            print(f"    {damage_type},")
            print(f"    {power_val},  // power")
            print(f"    {acc_val}, // accuracy")
            print(f"    {pp_val},  // pp")
            print(f"    Collections.emptyList(),")
            print(f"    null,  // effect")
            print(f"    {priority_value} // priority")
            print("));\n")
        else:
            # Status / non-damaging move
            print(f'moves.put("{name_java}", new Move(')
            print(f'    "{name_java}",')
            print(f"    {move_type},")
            print(f"    {acc_val}, // accuracy")
            print(f"    {pp_val},  // pp")
            print(f"    Collections.emptyList(),")
            print(f"    null,  // effect")
            print(f"    {priority_value} // priority")
            print("));\n")


def remove_moves_without_effect_from_json(json_path: Path):
    """
    Remove moves without an effect from the JSON file in-place.

    A one-time backup is written next to the file as `pokemon_moves.backup.json`
    if it does not already exist.
    """
    moves = load_moves(json_path)
    to_remove = set(id(m) for m in moves_without_effect(moves))
    remaining_moves = [m for m in moves if id(m) not in to_remove]

    backup_path = json_path.with_suffix(".backup.json")
    if not backup_path.exists():
        # Write original content as backup
        backup_path.write_text(json.dumps(moves, ensure_ascii=False, indent=4), encoding="utf-8")

    # Overwrite original with remaining moves
    json_path.write_text(json.dumps(remaining_moves, ensure_ascii=False, indent=4), encoding="utf-8")

    print(f"\nRemoved {len(moves) - len(remaining_moves)} moves from {json_path.name}.")
    print(f"Remaining moves: {len(remaining_moves)}")
    if backup_path.exists():
        print(f"Original file backed up as: {backup_path.name}")


def remove_priority_only_moves_from_json(json_path: Path, priorities_path: Path):
    """
    Remove moves that have priority but no other effect from the JSON file in-place.
    """
    moves = load_moves(json_path)
    priority_moves = parse_priorities_file(priorities_path)
    
    to_remove = set(id(m) for m in moves_with_priority_only(moves, priority_moves))
    remaining_moves = [m for m in moves if id(m) not in to_remove]

    # Overwrite original with remaining moves
    json_path.write_text(json.dumps(remaining_moves, ensure_ascii=False, indent=4), encoding="utf-8")

    print(f"\nRemoved {len(moves) - len(remaining_moves)} priority-only moves from {json_path.name}.")
    print(f"Remaining moves: {len(remaining_moves)}")


def main():
    # Resolve JSON path relative to this script's directory
    here = Path(__file__).resolve().parent
    json_path = here / "pokemon_moves.json"
    priorities_path = here / "priorities.txt"

    if not json_path.exists():
        raise FileNotFoundError(f"Could not find 'pokemon_moves.json' at: {json_path}")

    # Load moves - try to get priority-only moves from current file or backup
    moves = load_moves(json_path)
    
    # Parse priority moves and their values
    priority_moves = parse_priorities_file(priorities_path)
    priority_map = parse_priorities_with_values(priorities_path)
    
    # Check if we have any priority-only moves in current file
    priority_only = moves_with_priority_only(moves, priority_moves)
    using_backup = False
    
    # If no priority-only moves found, try loading from backup
    if not priority_only:
        backup_path = json_path.with_suffix(".backup.json")
        if backup_path.exists():
            print("No priority-only moves found in current JSON. Loading from backup...")
            moves = load_moves(backup_path)
            priority_only = moves_with_priority_only(moves, priority_moves)
            using_backup = True
    
    # Print priority-only moves
    print_priority_only_moves(moves, priority_moves)
    
    # Print Java snippets
    print_java_for_priority_only_moves(moves, priority_moves, priority_map)
    
    # Only remove if we're working with the current file (not backup)
    if priority_only and not using_backup:
        remove_priority_only_moves_from_json(json_path, priorities_path)


if __name__ == "__main__":
    main()
