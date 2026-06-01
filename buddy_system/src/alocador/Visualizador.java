package alocador;

import estruturas.NovaFila;
import estruturas.NovaLista;

/**
 * Responsável por exibir a árvore hierarquizada, fila de pendentes,
 * listas de blocos livres e informações gerais do alocador.
 */
public class Visualizador {

    private final BuddyAlocador alocador;

    public Visualizador(BuddyAlocador alocador) {
        this.alocador = alocador;
    }

    //  Árvore 

    public void exibirArvore() {
        System.out.println("\n");
        System.out.println("  ÁRVORE BUDDY BINÁRIA ");
        System.out.println("");
        exibirNoRec(alocador.getRaiz(), "", true);
    }

    private void exibirNoRec(NoBinario no, String prefixo, boolean ehUltimo) {
        if (no == null) return;
        String conector = ehUltimo ? "└── " : "├── ";
        String info = formatarNo(no);
        System.out.println(prefixo + conector + info);

        String novoPrefixo = prefixo + (ehUltimo ? "    " : "│   ");
        if (no.filhoEsquerdo != null || no.filhoDireito != null) {
            exibirNoRec(no.filhoEsquerdo, novoPrefixo, no.filhoDireito == null);
            exibirNoRec(no.filhoDireito,  novoPrefixo, true);
        }
    }

    private String formatarNo(NoBinario no) {
        String tamanho = no.tamanho >= 1024
                ? (no.tamanho / 1024) + " MB"
                : no.tamanho + " KB";
        String end = "@" + no.endereco + "KB";
        switch (no.estado) {
            case LIVRE:
                return "[" + tamanho + end + " LIVRE]";
            case OCUPADO:
                return "[" + tamanho + end + " OCUPADO:" + no.identificador + "]";
            case DIVIDIDO:
                return "[" + tamanho + end + " DIVIDIDO]";
            default:
                return "[?]";
        }
    }

    //  Listas de blocos livres 

    public void exibirListasLivres() {
        System.out.println("\n");
        System.out.println(" LISTAS DE BLOCOS LIVRES║");
        System.out.println("");
        NovaLista[] listas = alocador.getListasLivres();
        int totalLivre = 0;
        for (int i = 0; i < BuddyAlocador.NUM_NIVEIS; i++) {
            int tam = alocador.tamanhoDeNivel(i);
            String tamStr = tam >= 1024 ? (tam / 1024) + " MB" : tam + " KB";
            if (!listas[i].estaVazia()) {
                System.out.printf("  [%2d] %6s : %d bloco(s) → %s%n",
                        i, tamStr, listas[i].tamanho(), listas[i].toString());
                totalLivre += tam * listas[i].tamanho();
            } else {
                System.out.printf("  [%2d] %6s : vazio%n", i, tamStr);
            }
        }
        System.out.println("  ");
        String totStr = totalLivre >= 1024 ? (totalLivre / 1024) + " MB" : totalLivre + " KB";
        System.out.println("  Total livre: " + totStr);
    }

    //  Fila de pendentes

    public void exibirFilaPendentes() {
        System.out.println("\n");
        System.out.println("  FILA DE REQUISIÇÕES PENDENTES ");
        System.out.println("");
        NovaFila<RequisicaoPendente> fila = alocador.getFilaPendentes();
        if (fila.estaVazia()) {
            System.out.println("  (fila vazia)");
        } else {
            System.out.println("  " + fila.toString());
        }
    }

    //  Visão geral 

    public void exibirBuddyInfo() {
        System.out.println("\n");
        System.out.println(" VISÃO GERAL DA MEMÓRIA ");
        System.out.println("");
        exibirArvore();
        exibirListasLivres();
        exibirFilaPendentes();
    }
}
