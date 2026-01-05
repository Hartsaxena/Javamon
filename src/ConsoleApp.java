import java.io.*;
import java.util.*;
import javamon.entities.*;
import javamon.entities.moves.Move;
import javamon.battle.*;
import ai.BattleBot;
import ai.strategies.*;

class ConsoleApp {

    static boolean gameOver = false;
    static final boolean twoPlayerMode = false;
    static final BattleBot enemyAI = new BattleBot(new RandomStrategy(), 2);

    private static int promptMenu(List<String> options, Scanner input ,PrintStream output) {
        return promptMenu(options, input, output, "Please input your choice: ");
    }
    private static int promptMenu(List<String> options, Scanner input, PrintStream output, String prompt) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Cannot prompt menu with no options!");
        }

        // Output options (1-indexed)
        for (int i = 0; i < options.size(); i++) {
            output.println("(" + (i + 1) + ")" + " " + options.get(i));
        }

        // Prompt integer response
        int response = -1;
        while (response == -1) {
            output.print(prompt);
            String choice = input.nextLine().trim();
            try {
                response = Integer.parseInt(choice) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Please input a valid choice.");
                continue;
            }

            if (response < 0 || response >= options.size()) {
                response = -1;
            }
        }

        return response;
    }

    private static void promptTurns(BattleEngine engine, Scanner input, PrintStream output, int playerN) {
        Pokemon playerActive = engine.getActivePokemon(playerN);
        List<String> mainOptions = new ArrayList<>(List.of("Attack", "Switch", "Exit"));
        List<String> attackOptions = new ArrayList<>();
        for (Move m : playerActive.getMoves()) {
            attackOptions.add(m.getName() + " PP: " + playerActive.getRemainingPP(m.getName()));
        }
        attackOptions.add("Back");

        List<String> switchOptions = new ArrayList<>();
        for (Pokemon poke : engine.getPokemon(playerN)) {
            String status;
            if (poke.isKnockedOut()) {
                status = "FAINTED";
            } else {
                status = "HP: " + poke.getCurrentHp();
            }
            switchOptions.add(poke.getSpeciesName() + " " + status);
        }
        switchOptions.add("Back");

        boolean finished = false;
        while (!finished) {
            int choice = promptMenu(mainOptions, input, output);
            String chosen = mainOptions.get(choice);
            
            if (chosen.equals("Attack")) {
                choice = promptMenu(attackOptions, input, output);
                chosen = attackOptions.get(choice);
                if (chosen.equals("Back")) {
                    continue;
                }

                // Extract just the move name (before " PP: ")
                String moveName = chosen.split(" PP: ")[0];
                engine.queueTurn(playerN, moveName);
                finished = true;
            } else if (chosen.equals("Switch")) {
                choice = promptMenu(switchOptions, input, output);
                chosen = switchOptions.get(choice);
                if (chosen.equals("Back")) {
                    continue;
                }

                engine.queueTurn(playerN, choice);
                finished = true;
            } else if (chosen.equals("Exit")) {
                gameOver = true;
                return;
            } else {
                // Something's wrong
                throw new IllegalStateException("Chose invalid option");
            }
        }
    }
    
    public static void main(String[] args) {
        Pokemon charmander = PokemonRegistry.create("Charmander", 32, new String[]{"Ember", "Tackle"});
        Pokemon bulbasaur = PokemonRegistry.create("Bulbasaur", 31, new String[]{"Vine Whip", "Tackle"});

        List<Pokemon> playerTeam = new ArrayList<>(List.of(charmander, bulbasaur));
        List<Pokemon> oppTeam = new ArrayList<>(List.of(bulbasaur, charmander));
        BattleEngine battleEngine = new BattleEngine(playerTeam, oppTeam);

        PrintStream output = System.out;
        Scanner input = new Scanner(System.in);
        ByteArrayOutputStream battleBuffer = new ByteArrayOutputStream();
        PrintStream battleStream = new PrintStream(battleBuffer);

        while (true) {
            if (battleEngine.isFinished()) {
                break;
            }
            
            promptTurns(battleEngine, input, output, 1);
            if (gameOver) {
                break;
            }

            if (twoPlayerMode) {
                promptTurns(battleEngine, input, output, 2);
                if (gameOver) {
                    break;
                }
            } else {
                enemyAI.queueTurn(battleEngine);
            }

            battleBuffer.reset();
            battleEngine.playOutTurns(battleStream);
            battleStream.flush();

            String[] lines = battleBuffer.toString().split("\\r?\\n");
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                output.print(line);
                input.nextLine();
            }

            System.out.print("\n\n");
        }
        
        System.out.println("Reached end of program.");
    }
}