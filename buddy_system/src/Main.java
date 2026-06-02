import alocador.*;
import java.util.Scanner;

/**
 * Ponto de entrada do simulador Buddy System.
 * Menu interativo com 8 opções.
 *
 * Uso pela linha de comando:
 *   java Main                         menu interativo
 *   java Main ALOCAR id tamanho_kb    aloca diretamente
 *   java Main LIBERAR id              libera diretamente
 */
public class Main {

    private static BuddyAlocador alocador;
    private static Visualizador  visualizador;
    private static Scanner       scanner;

    public static void main(String[] args) {
        alocador     = new BuddyAlocador();
        visualizador = new Visualizador(alocador);
        scanner      = new Scanner(System.in);

        // Modo argumento de linha de comando
        if (args.length >= 2) {
            processarArgumento(args);
            return;
        }

        // Modo menu interativo
        boolean executando = true;
        while (executando) {
            exibirMenu();
            String entrada = scanner.nextLine().trim();
            switch (entrada) {
                case "1": opcaoAlocar();           break;
                case "2": opcaoLiberar();          break;
                case "3": opcaoDesfazer();         break;
                case "4": visualizador.exibirBuddyInfo(); break;
                case "5": visualizador.exibirFilaPendentes(); break;
                case "6": visualizador.exibirListasLivres();  break;
                case "7": opcaoCarregarDataset();  break;
                case "8": executando = false; System.out.println("Encerrando..."); break;
                default:  System.out.println(" Opção inválida.");
            }
        }
        scanner.close();
    }

    //  Menu

    private static void exibirMenu() {
        System.out.println("\n");
        System.out.println("  SIMULADOR BUDDY SYSTEM — 32 MB ");
        System.out.println("");
        System.out.println("  1. Alocar bloco  ");
        System.out.println("  2. Liberar bloco ");
        System.out.println(" 3. Desfazer (Undo) ");
        System.out.println(" 4. Exibir memória (árvore + listas + fila)");
        System.out.println("5. Fila de pendentes ");
        System.out.println(" 6. Listas de blocos livres ");
        System.out.println("7. Carregar dataset ");
        System.out.println(" 8. Sair  ");
        System.out.print("Escolha: ");
    }

    //  Opções 

    private static void opcaoAlocar() {
        System.out.print("ID da alocação: ");
        String id = scanner.nextLine().trim();
        System.out.print("Tamanho (KB): ");
        String tamStr = scanner.nextLine().trim();
        try {
            int tam = Integer.parseInt(tamStr);
            alocador.alocar(id, tam);
        } catch (NumberFormatException e) {
            System.out.println("[ERRO] Tamanho inválido.");
        }
    }

    private static void opcaoLiberar() {
        System.out.print("ID do bloco a liberar: ");
        String id = scanner.nextLine().trim();
        alocador.liberar(id);
    }

    private static void opcaoDesfazer() {
        alocador.desfazer();
    }

    private static void opcaoCarregarDataset() {
        System.out.print("Caminho do arquivo (padrão: dataset.txt): ");
        String caminho = scanner.nextLine().trim();
        if (caminho.isEmpty()) caminho = "dataset.txt";
        DatasetLoader loader = new DatasetLoader(alocador, visualizador);
        loader.carregar(caminho);
    }

    //  Argumento de linha de comando 

    private static void processarArgumento(String[] args) {
        String cmd = args[0].toUpperCase();
        if (cmd.equals("ALOCAR") && args.length >= 3) {
            try {
                int tam = Integer.parseInt(args[2]);
                alocador.alocar(args[1], tam);
                visualizador.exibirBuddyInfo();
            } catch (NumberFormatException e) {
                System.out.println("[ERRO] Tamanho inválido: " + args[2]);
            }
        } else if (cmd.equals("LIBERAR") && args.length >= 2) {
            alocador.liberar(args[1]);
            visualizador.exibirBuddyInfo();
        } else {
            System.out.println("Uso: java Main [ALOCAR id tamanho_kb | LIBERAR id]");
        }
    }
}
