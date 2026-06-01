package alocador;

/**
 * Representa uma operação bem-sucedida para o histórico (Undo).
 * Armazena o tipo da operação e os dados necessários para revertê-la.
 */
public class OperacaoHistorico {
    public enum Tipo { ALOCAR, LIBERAR }

    public final Tipo tipo;
    public final String identificador;
    public final int enderecoBloco;
    public final int tamanhoBloco;

    public OperacaoHistorico(Tipo tipo, String identificador, int enderecoBloco, int tamanhoBloco) {
        this.tipo = tipo;
        this.identificador = identificador;
        this.enderecoBloco = enderecoBloco;
        this.tamanhoBloco = tamanhoBloco;
    }

    @Override
    public String toString() {
        if (tipo == Tipo.ALOCAR) {
            return "ALOCAR id=" + identificador + " bloco=" + tamanhoBloco + "KB@" + enderecoBloco;
        } else {
            return "LIBERAR id=" + identificador + " bloco=" + tamanhoBloco + "KB@" + enderecoBloco;
        }
    }
}
