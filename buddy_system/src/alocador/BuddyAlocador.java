package alocador;

import estruturas.NoPilhaHistorico;
import estruturas.NovaFila;
import estruturas.NovaLista;

/**
 * Alocador Buddy Binário.
 *
 * Memória total: 32 MB (32.768 KB)
 * Buddy mínimo:   4 KB
 * Níveis:         14  (log2(32768/4) + 1 = 14)
 * As listas de blocos livres têm 14 entradas (índice 0 = 4KB, 13 = 32MB).
 */
public class BuddyAlocador {

    //  Constantes
    public static final int MEMORIA_TOTAL_KB = 32 * 1024;   // 32.768 KB
    public static final int BUDDY_MINIMO_KB  = 4;
    public static final int NUM_NIVEIS;

    static {
        int n = 0, t = MEMORIA_TOTAL_KB;
        while (t > BUDDY_MINIMO_KB) { t /= 2; n++; }
        NUM_NIVEIS = n + 1; // 14
    }

    // Estruturas de dados 
    private final NoBinario raiz;

    /** 14 listas de blocos livres: listasLivres[i] → blocos de tamanho tamanhoDoNivel(i) */
    private final NovaLista[] listasLivres;

    /** Fila FIFO de requisições pendentes */
    private final NovaFila<RequisicaoPendente> filaPendentes;

    /** Pilha LIFO de histórico para Undo */
    private final NoPilhaHistorico<OperacaoHistorico> pilhaHistorico;

    // Construtor 
    public BuddyAlocador() {
        raiz = new NoBinario(MEMORIA_TOTAL_KB, 0, null);

        listasLivres = new NovaLista[NUM_NIVEIS];
        for (int i = 0; i < NUM_NIVEIS; i++) {
            listasLivres[i] = new NovaLista();
        }
        // Memória começa totalmente livre: um bloco de 32MB no nível máximo
        listasLivres[NUM_NIVEIS - 1].inserir(0);

        filaPendentes = new NovaFila<>();
        pilhaHistorico = new NoPilhaHistorico<>();
    }

    // Utilitários

    /** Retorna o índice de nível para um dado tamanho em KB. */
    public int nivelDeTamanho(int tamanhoKB) {
        int t = BUDDY_MINIMO_KB;
        int nivel = 0;
        while (t < tamanhoKB) { t *= 2; nivel++; }
        return nivel;
    }

    /** Retorna o tamanho em KB para um dado nível. */
    public int tamanhoDeNivel(int nivel) {
        return BUDDY_MINIMO_KB << nivel;
    }

    /** Arredonda para a próxima potência de 2 ≥ valor, mínimo BUDDY_MINIMO_KB. */
    public int proximaPotenciaDe2(int valor) {
        if (valor <= BUDDY_MINIMO_KB) return BUDDY_MINIMO_KB;
        int p = BUDDY_MINIMO_KB;
        while (p < valor) p *= 2;
        return p;
    }

    /** Calcula o endereço do buddy de um bloco. */
    private int enderecoBuddy(int endereco, int tamanhoKB) {
        return endereco ^ tamanhoKB;
    }

    //  Localizar nó na árvore 

    /** Encontra o nó pelo endereço e tamanho. */
    public NoBinario encontrarNo(int endereco, int tamanhoKB) {
        return encontrarNoRec(raiz, endereco, tamanhoKB);
    }

    private NoBinario encontrarNoRec(NoBinario no, int endereco, int tamanhoKB) {
        if (no == null) return null;
        if (no.endereco == endereco && no.tamanho == tamanhoKB) return no;
        NoBinario esq = encontrarNoRec(no.filhoEsquerdo, endereco, tamanhoKB);
        if (esq != null) return esq;
        return encontrarNoRec(no.filhoDireito, endereco, tamanhoKB);
    }

    /** Encontra nó ocupado pelo identificador. */
    public NoBinario encontrarPorId(String id) {
        return encontrarPorIdRec(raiz, id);
    }

    private NoBinario encontrarPorIdRec(NoBinario no, String id) {
        if (no == null) return null;
        if (id.equals(no.identificador)) return no;
        NoBinario esq = encontrarPorIdRec(no.filhoEsquerdo, id);
        if (esq != null) return esq;
        return encontrarPorIdRec(no.filhoDireito, id);
    }

    // SPLIT 

    /**
     * Divide recursivamente um bloco livre do nível 'nivelAtual'
     * até atingir 'nivelDesejado'. Retorna o endereço do bloco resultante.
     * Retorna -1 se não for possível.
     */
    private int split(int nivelDesejado) {
        // Se já há bloco disponível no nível desejado, retorna
        if (!listasLivres[nivelDesejado].estaVazia()) {
            return listasLivres[nivelDesejado].buscarPrimeiro();
        }
        // Tenta obter bloco do nível superior (recursão)
        if (nivelDesejado >= NUM_NIVEIS - 1) return -1;
        int endPai = split(nivelDesejado + 1);
        if (endPai == -1) return -1;

        // Remove o bloco pai da lista e divide
        listasLivres[nivelDesejado + 1].remover(endPai);

        int tamFilho = tamanhoDeNivel(nivelDesejado);
        int endEsq = endPai;
        int endDir = endPai + tamFilho;

        // Atualiza a árvore: encontra o nó pai e cria os filhos
        int tamPai = tamFilho * 2;
        NoBinario noPai = encontrarNo(endPai, tamPai);
        if (noPai != null) {
            noPai.estado = NoBinario.Estado.DIVIDIDO;
            noPai.filhoEsquerdo = new NoBinario(tamFilho, endEsq, noPai);
            noPai.filhoDireito  = new NoBinario(tamFilho, endDir, noPai);
        }

        // Insere os dois filhos livres na lista do nível desejado
        listasLivres[nivelDesejado].inserir(endEsq);
        listasLivres[nivelDesejado].inserir(endDir);

        return endEsq; // retorna o primeiro (esquerdo)
    }

    // ALOCAR 

    /**
     * Tenta alocar um bloco para a requisição.
     * @return true se alocou, false se não há memória disponível (vai para fila).
     */
    public boolean alocar(String id, int tamanhoSolicitadoKB) {
        // Verifica duplicidade de ID
        if (encontrarPorId(id) != null) {
            System.out.println("   ID '" + id + "' já está em uso.");
            return false;
        }
        // Verifica se está na fila pendente
        // (simplificação: não verifica fila para evitar varredura O(n))

        int tamanhoReal = proximaPotenciaDe2(tamanhoSolicitadoKB);
        if (tamanhoReal > MEMORIA_TOTAL_KB) {
            System.out.println("   Requisição maior que a memória total.");
            return false;
        }
        int nivel = nivelDeTamanho(tamanhoReal);

        System.out.println("  Solicitado: " + tamanhoSolicitadoKB + " KB → alocado: "
                + tamanhoReal + " KB (fragmentação interna: "
                + (tamanhoReal - tamanhoSolicitadoKB) + " KB)");

        int endereco = split(nivel);
        if (endereco == -1) {
            System.out.println("   Requisição '" + id
                    + "' adicionada à fila de pendentes.");
            filaPendentes.enfileirar(new RequisicaoPendente(id, tamanhoSolicitadoKB));
            return false;
        }

        // Marca o bloco como ocupado
        listasLivres[nivel].remover(endereco);
        NoBinario no = encontrarNo(endereco, tamanhoReal);
        if (no != null) {
            no.estado = NoBinario.Estado.OCUPADO;
            no.identificador = id;
        }

        System.out.println("   '" + id + "' alocado: " + tamanhoReal
                + " KB no endereço " + endereco + " KB");

        // Registra na pilha de histórico
        pilhaHistorico.empilhar(
                new OperacaoHistorico(OperacaoHistorico.Tipo.ALOCAR, id, endereco, tamanhoReal));
        return true;
    }

    //  LIBERAR

    /**
     * Libera um bloco pelo identificador.
     * Após liberar, realiza merge em cascata e tenta atender pendentes.
     */
    public boolean liberar(String id) {
        NoBinario no = encontrarPorId(id);
        if (no == null) {
            System.out.println("   ID '" + id + "' não encontrado.");
            return false;
        }

        int endereco = no.endereco;
        int tamanho  = no.tamanho;

        System.out.println("  Liberando '" + id + "': " + tamanho + " KB @ " + endereco + " KB");

        // Marca como livre
        no.estado = NoBinario.Estado.LIVRE;
        no.identificador = null;
        int nivel = nivelDeTamanho(tamanho);
        listasLivres[nivel].inserir(endereco);

        // Merge em cascata
        mergeSubindo(no);

        System.out.println("  [OK] '" + id + "' liberado.");

        // Registra na pilha de histórico
        pilhaHistorico.empilhar(
                new OperacaoHistorico(OperacaoHistorico.Tipo.LIBERAR, id, endereco, tamanho));

        // Tenta atender pendentes
        tentarAtenderPendentes();
        return true;
    }

    /**
     * Merge recursivo subindo até a raiz.
     * Verifica se o buddy do nó também está livre; se sim, funde e sobe.
     */
    private void mergeSubindo(NoBinario no) {
        if (no.pai == null) return; // chegou à raiz

        NoBinario pai = no.pai;
        NoBinario esq = pai.filhoEsquerdo;
        NoBinario dir = pai.filhoDireito;

        if (esq == null || dir == null) return;

        // Ambos os filhos devem estar livres (folha livre)
        if (esq.estado == NoBinario.Estado.LIVRE && esq.ehFolha()
                && dir.estado == NoBinario.Estado.LIVRE && dir.ehFolha()) {

            System.out.println("    Merge: " + esq.tamanho + "KB@" + esq.endereco
                    + " + " + dir.tamanho + "KB@" + dir.endereco
                    + " → " + pai.tamanho + "KB@" + pai.endereco);

            // Remove os dois buddies das listas
            int nivel = nivelDeTamanho(esq.tamanho);
            listasLivres[nivel].remover(esq.endereco);
            listasLivres[nivel].remover(dir.endereco);

            // Restaura o pai
            pai.estado = NoBinario.Estado.LIVRE;
            pai.filhoEsquerdo = null;
            pai.filhoDireito  = null;

            int nivelPai = nivelDeTamanho(pai.tamanho);
            listasLivres[nivelPai].inserir(pai.endereco);

            // Continua subindo
            mergeSubindo(pai);
        }
    }

    /** Tenta atender requisições pendentes na fila . */
    private void tentarAtenderPendentes() {
        while (!filaPendentes.estaVazia()) {
            RequisicaoPendente req = filaPendentes.espiar();
            int tamanhoReal = proximaPotenciaDe2(req.tamanhoSolicitado);
            int nivel = nivelDeTamanho(tamanhoReal);
            int endereco = split(nivel);
            if (endereco == -1) break; // sem memória, para

            filaPendentes.desenfileirar(); // remove da fila
            listasLivres[nivel].remover(endereco);

            NoBinario no = encontrarNo(endereco, tamanhoReal);
            if (no != null) {
                no.estado = NoBinario.Estado.OCUPADO;
                no.identificador = req.identificador;
            }

            System.out.println("   Pendente '" + req.identificador
                    + "' atendido: " + tamanhoReal + " KB @ " + endereco + " KB");
            pilhaHistorico.empilhar(new OperacaoHistorico(
                    OperacaoHistorico.Tipo.ALOCAR, req.identificador, endereco, tamanhoReal));
        }
    }

    //  DESFAZER 

    /**
     * Desfaz a última operação bem-sucedida.
     */
    public boolean desfazer() {
        if (pilhaHistorico.estaVazia()) {
            System.out.println("   Nenhuma operação para desfazer.");
            return false;
        }
        OperacaoHistorico op = pilhaHistorico.desempilhar();
        System.out.println("   Revertendo: " + op);

        if (op.tipo == OperacaoHistorico.Tipo.ALOCAR) {
            // Desfazer alocação → liberar (sem empilhar novo histórico)
            NoBinario no = encontrarPorId(op.identificador);
            if (no == null) {
                System.out.println("   Bloco não encontrado na árvore.");
                return false;
            }
            no.estado = NoBinario.Estado.LIVRE;
            no.identificador = null;
            int nivel = nivelDeTamanho(no.tamanho);
            listasLivres[nivel].inserir(no.endereco);
            mergeSubindo(no);
            System.out.println("   Alocação de '" + op.identificador + "' revertida.");
        } else {
            // Desfazer liberação → realocar (sem empilhar novo histórico)
            int nivel = nivelDeTamanho(op.tamanhoBloco);
            int endereco = split(nivel);
            if (endereco == -1) {
                System.out.println("   Sem memória para restaurar liberação.");
                return false;
            }
            listasLivres[nivel].remover(endereco);
            NoBinario no = encontrarNo(endereco, op.tamanhoBloco);
            if (no != null) {
                no.estado = NoBinario.Estado.OCUPADO;
                no.identificador = op.identificador;
            }
            System.out.println("   Liberação de '" + op.identificador + "' revertida.");
        }
        return true;
    }

    // GETTERS para exibição 

    public NoBinario getRaiz()                              { return raiz; }
    public NovaFila<RequisicaoPendente> getFilaPendentes()  { return filaPendentes; }
    public NoPilhaHistorico<OperacaoHistorico> getPilhaHistorico() { return pilhaHistorico; }
    public NovaLista[] getListasLivres()                    { return listasLivres; }
}
