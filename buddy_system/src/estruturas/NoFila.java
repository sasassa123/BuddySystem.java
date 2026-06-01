package estruturas;

/**
 * Nó genérico para a fila encadeada (FIFO).
 */
public class NoFila<T> {
    public T dado;
    public NoFila<T> proximo;

    public NoFila(T dado) {
        this.dado = dado;
        this.proximo = null;
    }
}
