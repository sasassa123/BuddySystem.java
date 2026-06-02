# Buddy System — Simulador de Alocação de Memória
Grupo:Felippe Matias Cardinot, João victor cracco.

Simulador do Alocador Buddy Binário implementado em Java, com todas as estruturas de dados encadeadas manualmente 
---

## Compilação e Execução

### Pré-requisito
JDK 11 ou superior instalado.

### Compilar
```bash
javac -d out src/estruturas/*.java src/alocador/*.java src/Main.java
```

### Executar
```bash
java -cp out Main
```

### Executar com argumento direto
```bash
java -cp out Main ALOCAR a1 5120
java -cp out Main LIBERAR a1
```

### Carregar dataset pelo menu
Ao abrir o menu, escolha a opção **7**, informe o caminho do arquivo (padrão: `dataset.txt`) e o programa processa cada operação exibindo o estado da memória após cada uma.

---

## Menu (8 opções)
```
1. Alocar bloco
2. Liberar bloco
3. Desfazer (Undo)
4. Exibir memória (árvore + listas + fila)
5. Fila de pendentes
6. Listas de blocos livres
7. Carregar dataset
8. Sair
```

---

## Estrutura do Projeto

```
buddy_system/
├── src/
│   ├── Main.java
│   ├── estruturas/
│   │   ├── NoFila.java
│   │   ├── NovaFila.java
│   │   ├── NoPilha.java
│   │   ├── NoPilhaHistorico.java
│   │   ├── NoLista.java
│   │   └── NovaLista.java
│   └── alocador/
│       ├── NoBinario.java
│       ├── OperacaoHistorico.java
│       ├── RequisicaoPendente.java
│       ├── BuddyAlocador.java
│       ├── Visualizador.java
│       └── DatasetLoader.java
├── dataset.txt
└── README.md
```

---

## Classes

### RA1 — Estruturas de Dados Lineares

| Classe | Tipo | Operações |
|--------|------|-----------|
| `NovaFila<T>` | Fila FIFO encadeada | `enfileirar`, `desenfileirar`, `espiar`, `estaVazia`, `tamanho` |
| `NoPilhaHistorico<T>` | Pilha LIFO encadeada | `empilhar`, `desempilhar`, `topo`, `estaVazia`, `tamanho` |
| `NovaLista` | Lista encadeada ordenada por endereço | `inserir`, `remover`, `buscarPrimeiro`, `contem`, `estaVazia`, `tamanho` |

**Fila de Requisições Pendentes:** quando não há memória disponível, a requisição entra na fila FIFO. Após cada liberação com merge, o alocador tenta atender as pendentes em ordem.

**Pilha de Histórico (Undo):** cada operação bem-sucedida é empilhada. A opção 3 do menu desempilha e reverte: se foi alocação → libera; se foi liberação → realoca.

**Listas de Blocos Livres:** 14 listas separadas por tamanho (4 KB a 32 MB), mantidas em ordem crescente de endereço. O split remove da lista maior e insere dois na menor; o merge faz o inverso.

### RA2 — Alocador Buddy Binário

| Classe | Descrição |
|--------|-----------|
| `NoBinario` | Nó da árvore com estado LIVRE, OCUPADO ou DIVIDIDO |
| `BuddyAlocador` | Núcleo do sistema: split recursivo, merge em cascata, fila e undo |
| `Visualizador` | Exibe a árvore hierarquizada, listas de blocos livres e fila |
| `DatasetLoader` | Lê e processa o `dataset.txt` linha a linha |
| `Main` | Menu interativo e modo de argumento pela linha de comando |

**Parâmetros:**
- Memória total: 32 MB (32.768 KB)
- Buddy mínimo: 4 KB
- Níveis: 14 (log₂(32768/4) + 1)

**Split:** desce recursivamente até encontrar um bloco livre do tamanho necessário, dividindo os blocos maiores ao longo do caminho e atualizando as listas e a árvore.

**Merge:** ao liberar um bloco, verifica se o buddy (irmão) também está livre. Se sim, funde os dois, restaura o pai e continua subindo até a raiz.

---

## Formato do dataset.txt

```
# comentários começam com #
ALOCAR <id> <tamanho_em_kb>
LIBERAR <id>
```

Exemplo:
```
ALOCAR img01 8
ALOCAR video01 16384
LIBERAR img01
```

---

## Exemplo de Saída

### Alocação de 5120 KB
```
Solicitado: 5120 KB → alocado: 8192 KB (fragmentação interna: 3072 KB)
[OK] 'dados01' alocado: 8192 KB no endereço 8192 KB
```

### Árvore após alocação
```
└── [32 MB@0KB DIVIDIDO]
    ├── [16 MB@0KB DIVIDIDO]
    │   ├── [8 MB@0KB OCUPADO:dados01]
    │   └── [8 MB@8192KB LIVRE]
    └── [16 MB@16384KB LIVRE]
```

### Fila de pendentes
```
[Linha 33] ALOCAR backup01 12288
  Solicitado: 12288 KB → alocado: 16384 KB (fragmentação interna: 4096 KB)
  [SEM MEMÓRIA] Requisição 'backup01' adicionada à fila de pendentes.

[Linha 36] LIBERAR video01
  Liberando 'video01': 16384 KB @ 16384 KB
  [OK] 'video01' liberado.
  [FILA] Pendente 'backup01' atendido: 16384 KB @ 16384 KB
```

### Merge em cascata
```
[Linha 68] LIBERAR icone03
  Merge: 16KB@512 + 16KB@528 → 32KB@512
  Merge: 32KB@512 + 32KB@544 → 64KB@512
  Merge: 64KB@512 + 64KB@576 → 128KB@512
  Merge: 128KB@512 + 128KB@640 → 256KB@512
  Merge: 256KB@512 + 256KB@768 → 512KB@512
  [OK] 'icone03' liberado.
```

---

## Análise de Fragmentação Interna

Requisições são arredondadas para a próxima potência de 2 ≥ buddy mínimo (4 KB):

| Solicitado | Alocado | Desperdício | % |
|-----------|---------|-------------|---|
| 8 KB | 8 KB | 0 KB | 0% |
| 12 KB | 16 KB | 4 KB | 25% |
| 24 KB | 32 KB | 8 KB | 25% |
| 256 KB | 256 KB | 0 KB | 0% |
| 512 KB | 512 KB | 0 KB | 0% |
| 3072 KB | 4096 KB | 1024 KB | 25% |
| 5120 KB | 8192 KB | 3072 KB | 38% |
| 12288 KB | 16384 KB | 4096 KB | 25% |

Valores retirados da execução real do `dataset.txt` incluído no repositório.
