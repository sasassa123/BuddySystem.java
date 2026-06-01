package estruturas;

/**
 * Nó para a lista encadeada de blocos livres.
 */
public class NoLista {
    public int endereco;      // endereço base do bloco livre
    public NoLista proximo;

    public NoLista(int endereco) {
        this.endereco = endereco;
        this.proximo = null;
    }

    @Override
    public String toString() {
        return "0x" + Integer.toHexString(endereco).toUpperCase();
    }
}
