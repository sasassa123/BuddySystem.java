package estruturas;

/**
 * Pilha encadeada LIFO (Last In, First Out).
 * Usada para histórico de operações (Undo).
 * Implementada manualmente com nós encadeados — sem ArrayList, LinkedList ou classes prontas.
 * Operações: empilhar, desempilhar, topo, estaVazia.
 */
public class NoPilhaHistorico<T> {
    private NoPilha<T> topo;
    private int tamanho;

    public NoPilhaHistorico() {
        this.topo = null;
        this.tamanho = 0;
    }

    /** Empilha elemento no topo. */
    public void empilhar(T dado) {
        NoPilha<T> novo = new NoPilha<>(dado);
        novo.proximo = topo;
        topo = novo;
        tamanho++;
    }

    /** Remove e retorna o elemento do topo. */
    public T desempilhar() {
        if (estaVazia()) throw new RuntimeException("Pilha vazia");
        T dado = topo.dado;
        topo = topo.proximo;
        tamanho--;
        return dado;
    }

    /** Retorna (sem remover) o elemento do topo. */
    public T topo() {
        if (estaVazia()) throw new RuntimeException("Pilha vazia");
        return topo.dado;
    }

    public boolean estaVazia() {
        return topo == null;
    }

    public int tamanho() {
        return tamanho;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TOPO: ");
        NoPilha<T> atual = topo;
        while (atual != null) {
            sb.append(atual.dado);
            if (atual.proximo != null) sb.append(" -> ");
            atual = atual.proximo;
        }
        sb.append("]");
        return sb.toString();
    }
}
