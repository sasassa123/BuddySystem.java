package estruturas;

/**
 * Fila encadeada FIFO (First In, First Out).
 * Implementada manualmente com nós encadeados — sem ArrayList, LinkedList ou classes prontas.
 * Operações: enfileirar, desenfileirar, espiar, estaVazia, tamanho.
 */
public class NovaFila<T> {
    private NoFila<T> cabeca;
    private NoFila<T> cauda;
    private int tamanho;

    public NovaFila() {
        this.cabeca = null;
        this.cauda = null;
        this.tamanho = 0;
    }

    /** Insere elemento no final da fila. */
    public void enfileirar(T dado) {
        NoFila<T> novo = new NoFila<>(dado);
        if (cauda == null) {
            cabeca = novo;
            cauda = novo;
        } else {
            cauda.proximo = novo;
            cauda = novo;
        }
        tamanho++;
    }

    /** Remove e retorna o elemento do início da fila. */
    public T desenfileirar() {
        if (estaVazia()) throw new RuntimeException("Fila vazia");
        T dado = cabeca.dado;
        cabeca = cabeca.proximo;
        if (cabeca == null) cauda = null;
        tamanho--;
        return dado;
    }

    /** Retorna (sem remover) o elemento do início da fila. */
    public T espiar() {
        if (estaVazia()) throw new RuntimeException("Fila vazia");
        return cabeca.dado;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        NoFila<T> atual = cabeca;
        while (atual != null) {
            sb.append(atual.dado);
            if (atual.proximo != null) sb.append(", ");
            atual = atual.proximo;
        }
        sb.append("]");
        return sb.toString();
    }
}
