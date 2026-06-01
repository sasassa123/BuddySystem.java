package alocador;

/**
 * Representa uma requisição de alocação pendente na fila FIFO.
 */
public class RequisicaoPendente {
    public final String identificador;
    public final int tamanhoSolicitado; // tamanho original pedido em KB

    public RequisicaoPendente(String identificador, int tamanhoSolicitado) {
        this.identificador = identificador;
        this.tamanhoSolicitado = tamanhoSolicitado;
    }

    @Override
    public String toString() {
        return "Pendente[id=" + identificador + ", tam=" + tamanhoSolicitado + "KB]";
    }
}
