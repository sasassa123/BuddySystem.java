package alocador;

/**
 * Nó da árvore binária do Buddy System.
 * Cada nó representa um bloco de memória.
 *
 * Estado:
 *   LIVRE     — bloco disponível para alocação
 *   OCUPADO   — bloco alocado para uma requisição
 *   DIVIDIDO  — bloco dividido em dois filhos
 */
public class NoBinario {
    public enum Estado { LIVRE, OCUPADO, DIVIDIDO }

    public int tamanho;          // tamanho do bloco em KB
    public int endereco;         // endereço base (offset em KB a partir de 0)
    public Estado estado;
    public String identificador; // ID da alocação (ex: "a1"), null se livre/dividido
    public NoBinario filhoEsquerdo;
    public NoBinario filhoDireito;
    public NoBinario pai;

    public NoBinario(int tamanho, int endereco, NoBinario pai) {
        this.tamanho = tamanho;
        this.endereco = endereco;
        this.estado = Estado.LIVRE;
        this.identificador = null;
        this.filhoEsquerdo = null;
        this.filhoDireito = null;
        this.pai = pai;
    }

    public boolean ehFolha() {
        return filhoEsquerdo == null && filhoDireito == null;
    }

    @Override
    public String toString() {
        return "[" + tamanho + "KB@" + endereco + " " + estado
                + (identificador != null ? " id=" + identificador : "") + "]";
    }
}
