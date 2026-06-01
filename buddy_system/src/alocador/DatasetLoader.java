package alocador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Carrega e processa o arquivo dataset.txt.
 *
 * Formato aceito (uma operação por linha):
 *   ALOCAR <id> <tamanho_kb>
 *   LIBERAR <id>
 *   # comentários são ignorados
 *   linhas em branco são ignoradas
 */
public class DatasetLoader {

    private final BuddyAlocador alocador;
    private final Visualizador  visualizador;

    public DatasetLoader(BuddyAlocador alocador, Visualizador visualizador) {
        this.alocador     = alocador;
        this.visualizador = visualizador;
    }

    public void carregar(String caminho) {
        System.out.println("\n=== Carregando dataset: " + caminho + " ===");
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            int numLinha = 0;
            while ((linha = br.readLine()) != null) {
                numLinha++;
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;

                String[] partes = linha.split("\\s+");
                if (partes.length == 0) continue;

                String cmd = partes[0].toUpperCase();
                System.out.println("\n[Linha " + numLinha + "] " + linha);

                switch (cmd) {
                    case "ALOCAR":
                        if (partes.length < 3) {
                            System.out.println("  [ERRO] Sintaxe: ALOCAR <id> <tamanho_kb>");
                            break;
                        }
                        try {
                            String id  = partes[1];
                            int    tam = Integer.parseInt(partes[2]);
                            alocador.alocar(id, tam);
                        } catch (NumberFormatException e) {
                            System.out.println("  [ERRO] Tamanho inválido: " + partes[2]);
                        }
                        break;

                    case "LIBERAR":
                        if (partes.length < 2) {
                            System.out.println("  [ERRO] Sintaxe: LIBERAR <id>");
                            break;
                        }
                        alocador.liberar(partes[1]);
                        break;

                    default:
                        System.out.println("  [AVISO] Comando desconhecido: " + cmd);
                }

                // Exibe estado após cada operação
                visualizador.exibirBuddyInfo();
            }
        } catch (IOException e) {
            System.out.println("[ERRO] Não foi possível abrir o arquivo: " + caminho);
            System.out.println("       " + e.getMessage());
        }
        System.out.println("\n=== Dataset processado. ===");
    }
}
