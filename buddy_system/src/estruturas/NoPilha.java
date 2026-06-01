package estruturas;

/**
 * Nó genérico para a pilha encadeada (LIFO).
 */
public class NoPilha<T> {
    public T dado;
    public NoPilha<T> proximo;

    public NoPilha(T dado) {
        this.dado = dado;
        this.proximo = null;
    }
}
