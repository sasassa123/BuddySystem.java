package estruturas;

/**
 * Lista encadeada de blocos livres para um determinado tamanho.
 * Blocos são mantidos em ordem crescente de endereço.
 * Implementada manualmente com nós encadeados — sem ArrayList, LinkedList ou classes prontas.
 * Operações: inserir, remover, buscarPrimeiro, estaVazia, tamanho.
 */
public class NovaLista {
    private NoLista cabeca;
    private int tamanho;

    public NovaLista() {
        this.cabeca = null;
        this.tamanho = 0;
    }

    /**
     * Insere o endereço em ordem crescente.
     * Split remove da lista maior e insere dois na menor.
     */
    public void inserir(int endereco) {
        NoLista novo = new NoLista(endereco);
        // Insere ordenado por endereço
        if (cabeca == null || endereco < cabeca.endereco) {
            novo.proximo = cabeca;
            cabeca = novo;
        } else {
            NoLista atual = cabeca;
            while (atual.proximo != null && atual.proximo.endereco < endereco) {
                atual = atual.proximo;
            }
            novo.proximo = atual.proximo;
            atual.proximo = novo;
        }
        tamanho++;
    }

    /**
     * Remove o nó com o endereço especificado.
     * Retorna true se encontrou e removeu, false caso contrário.
     */
    public boolean remover(int endereco) {
        if (cabeca == null) return false;
        if (cabeca.endereco == endereco) {
            cabeca = cabeca.proximo;
            tamanho--;
            return true;
        }
        NoLista atual = cabeca;
        while (atual.proximo != null) {
            if (atual.proximo.endereco == endereco) {
                atual.proximo = atual.proximo.proximo;
                tamanho--;
                return true;
            }
            atual = atual.proximo;
        }
        return false;
    }

    /**
     * Retorna o endereço do primeiro bloco livre (sem remover).
     * Retorna -1 se vazia.
     */
    public int buscarPrimeiro() {
        if (estaVazia()) return -1;
        return cabeca.endereco;
    }

    /**
     * Verifica se existe um bloco com o endereço especificado.
     */
    public boolean contem(int endereco) {
        NoLista atual = cabeca;
        while (atual != null) {
            if (atual.endereco == endereco) return true;
            atual = atual.proximo;
        }
        return false;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    public int tamanho() {
        return tamanho;
    }

    @Override
    public String toString() {
        if (estaVazia()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        NoLista atual = cabeca;
        while (atual != null) {
            sb.append(atual.toString());
            if (atual.proximo != null) sb.append(", ");
            atual = atual.proximo;
        }
        sb.append("]");
        return sb.toString();
    }
}
