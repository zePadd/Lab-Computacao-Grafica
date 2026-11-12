# Laboratórios de Computação Gráfica

## O que este projeto faz

Esse projeto visa aplicar os conceitos da disciplina Computação Gráfica estudados em sala. De tal forma, os conceitos foram divididos em laboratórios, os quais, nesse projeto, foram dividios em módulos e arquivos Main diferentes tal que cada arquivo main corresponde a uma implementação que segue à risca os requisitos do respectivo laboratório.

## ⚠️ Avisos importantes

> **Escala do Windows (DPI).** Em muitos notebooks o Windows vem com a escala padrão em **125%** (ou mais). Nesse caso, o Java (9+) passa a enxergar a resolução *lógica* da tela, e não a física: numa tela de **1920x1080** com 125%, o programa vai detectar algo como **1536x864**.
>
> Isso afeta o resultado do laboratório:
>
> - as coordenadas de dispositivo (`DC_x`, `DC_y`) passam a ser calculadas sobre a resolução errada;
>
> - cada pixel da imagem é ampliado pelo Windows para ~1,25 pixel físico, então o ponto pode não acender exatamente **um** pixel real (fere o Requisito 4).
>
> **Antes de rodar no Windows, ajuste a escala para 100%:** `Configurações → Sistema → Tela → Escala = 100%`.
>

## Integrantes da equipe

- Gabriela Almeida de Andradde
- Georis Samuel Martins Gouveia
- Horlan Silva de Lacerda
- João Victor Araújo Leite dos Santos
- José Araújo Agra Neto
- Suelle Ferreira Maciel

## Laboratório 1 - Transformações de Coordenadas

Implementa o pipeline `Mundo → NDC → Dispositivo` visto na Aula G2 e acende, na tela real do computador (notebook ou desktop do laboratório), o pixel exato correspondente a um ponto de mundo informado pelo usuário, nos dois cenários de NDC exigidos pelo enunciado: `[0,1] x [0,1]` e `[-1,1] x [-1,1]`.

O programa segue estes passos:

1. Pergunta ao usuário a janela de mundo (`xMin`, `xMax`, `yMin`, `yMax`) e um ponto `(x, y)` dentro dela.
2. Descobre a resolução real da tela do computador em uso.
3. Calcula as coordenadas NDC e, em seguida, as coordenadas de dispositivo (`DC_x`, `DC_y`), para os dois cenários de NDC.
4. Mostra um resumo dos cálculos em uma caixa de diálogo e no console.
5. Abre uma janela em tela cheia, com fundo preto, e acende **um único pixel verde (0x00FF00)** exatamente na posição calculada, usando `BufferedImage.setRGB` (por dentro de `PixelRenderer.drawPixel`), sem representar o ponto como círculo, retângulo ou qualquer outra forma com mais de um pixel.
6. Pergunta se o usuário quer visualizar também o outro cenário de NDC, para comparação.

## Como compilar e rodar o Lab1

O projeto usa pacotes (`model`, `transformer`, `ui`), então a estrutura de pastas dentro de `src` importa: `MainLab1.java` fica solto na raiz, e cada classe fica dentro da pasta que dá nome ao seu pacote. Com o JDK instalado (11 ou superior), rode a partir da pasta `src`:

```bash
javac MainLab1.java
java MainLab1
```

### Estrutura de arquivos e responsáveis pelo Lab1

|        Classe         |  Responsável     | Pacote        | O que faz                                                               |
| ----------------------| ---------------- | ------------- | ----------------------------------------------------------------------- |
|`DevicePoint`          |   **Gabriela**   | `model`       | Representa um ponto em coordenadas de dispositivo                       |
|`NdcScenario`          |   **Gabriela**   | `model`       | Representa os dois cenarios de coordenadas normalizadas de dispositivo  |
|`PointD`               |   **Gabriela**   | `model`       | Representa um ponto bidimensional com coordenadas em double             |
|`UserInputReader`      |   **Suelle**     | `model`       | Responsavel por coletar os dados do usuário                             |
|`WorldWindow`          |   **Horlan**     | `model`       | Representa a janela de mundo                                            |
|`CoordinateTransformer`|   **Horlan**     | `transformer` | Implementa os quatro procedimentos de transformacao de coordenadas      |
|`EntradaUsuario`       |   **Suelle**     | `ui`          | Agrupa os dois dados que o usuario fornece                              |
|`PixelRenderer`        |  **Georis/Neto** | `ui`          | Descobre a resolução real do dispositivo e exibe o pixel verde na tela  |
|     `MainLab1.java`   |  **João Victor** | (padrão)      | Integra tudo: entrada → transformação → exibição                        |
|     `README.md`       |  **João Victor** | —             | Este arquivo                                                            |

(`javac MainLab1.java` já compila `model`, `transformer` e `ui` automaticamente, por causa dos imports. Se preferir compilar tudo explicitamente: `javac -d out $(find . -name "*.java")` e depois `java -cp out MainLab1`.)

O programa vai abrir caixas de diálogo pedindo os dados e, no final, ocupar a tela inteira para mostrar o pixel.

### Como cada requisito do enunciado foi atendido no Lab1

- **Requisito 1** (janela do mundo definida pelo usuário): `UserInputReader.lerJanelaDeMundo()`.
- **Requisito 2** (dois cenários de NDC): `NdcScenario` + `MainLab1` calcula os dois cenários para o mesmo ponto.
- **Requisito 3** (exibir na tela real): `PixelRenderer.obterResolucaoDaTela()` + `exibirPixelAtivo(...)`.
- **Requisito 4** (pixel de verdade, sem agrupamento): único uso de `setRGB` em `PixelRenderer.drawPixel`, cor `0x00FF00` (`MainLab1.COR_PIXEL`), conforme exigido pelo professor.

## Laboratório 2: Rasterização

O objetivo principal deste laboratório é a **rasterização de retas e circunferências**. O sistema desenha formas geométricas convertendo coordenadas matemáticas em pixels individuais em um *framebuffer* virtual, sem utilizar métodos nativos de desenho de formas do Java (como `drawLine` ou `drawOval`).

Nesse laboratório, foi adicionada uma interface gráfica interativa (Swing) que permite ao usuário escolher o algoritmo, inserir os dados de entrada (coordenadas e raio), visualizar a forma gerada sobre uma malha quadriculada com eixos cartesianos e, mais importante, acompanhar o registro passo a passo dos cálculos e decisões matemáticas feitas pelo algoritmo quadro a quadro.

### Como compilar e rodar o Lab2

Com o terminal na pasta `src` execute no seu terminal:

```bash
javac MainLab2.java
java MainLab2
```

### Funcionalidades

Através da interface gráfica, o projeto permite:

- Escolher o algoritmo desejado para rasterização.
- Inserir coordenadas customizadas para retas e circunferências.
- Visualizar o desenho em uma malha de pixels (com eixos cartesianos opcionais).
- Acompanhar uma **tabela detalhada com o passo a passo** dos cálculos matemáticos de cada algoritmo.
- Execução assíncrona (via `SwingWorker`) para não travar a interface durante o processamento.

### Algoritmos Implementados

| # | Algoritmo | Tipo de Forma | Características Principais |
| --- | ----------- | --------------- | ---------------------------- |
| 1 | **DDA (Digital Differential Analyzer)** | Reta | Usa aritmética de ponto flutuante e calcula incrementos fracionários em X e Y. |
| 2 | **Bresenham / Ponto Médio** | Reta | Rápido e otimizado. Utiliza apenas aritmética inteira e uma variável de decisão. |
| 3 | **Polinomial** | Circunferência | Usa a equação explícita do círculo ($y = \sqrt{r^2 - x^2}$). Requer cálculo de raiz quadrada. |
| 4 | **Trigonométrico** | Circunferência | Usa equações paramétricas (seno e cosseno). Calcula pontos baseados no ângulo $\theta$. |
| 5 | **Bresenham / Ponto Médio** | Circunferência | O mais eficiente. Apenas aritmética inteira, sem uso de raiz quadrada ou trigonometria. |

*Nota: Todos os algoritmos de circunferência utilizam a propriedade da **simetria de ordem 8**, calculando apenas $1/8$ do círculo e espelhando os pontos para economizar processamento.*

### Estrutura de arquivos e responsáveis pelo Lab2

Para atender aos requisitos do segundo laboratório, foram implementadas as seguintes classes:

|        Classe            |  Responsável     | Pacote       | O que faz                                                                                               |
| ----------------------   | ---------------- | ------------ | ------------------------------------------------------------------------------------------------------- |
| `AlgoritmoDDA`           |  **Neto**        | `algorithms` | Implementa o Analisador Diferencial Digital (DDA) para desenhar retas usando ponto flutuante            |
| `AlgoritmoBresenham`     |  **Horlan**      | `algorithms` | Implementa o algoritmo de Bresenham (Ponto Médio) para retas, cobrindo todos os oitantes                |
| `CircPolinomial`         |  **Georis**      | `algorithms` | Rasteriza circunferências utilizando a equação explícita (método polinomial)                            |
| `CircTrigonometrico`     |  **João Victor** | `algorithms` | Rasteriza circunferências utilizando equações trigonométricas (paramétricas)                            |
| `CircPontoMedio`         |  **Suelle**      | `algorithms` | Implementa o algoritmo do Ponto Médio (Bresenham) para rasterização de circunferências                  |
| `ResultadoRasterizacao`  |  **Gabriela**    | `raster`     | Responsável pelo registro detalhado, passo a passo e tabelado, dos cálculos e do desenho dos pixels     |
| `JanelaLab2`/`PainelLab2`| **Equipe**       | `ui`         | Componentes da GUI Swing (seleção de algoritmos, campos de entrada e área de renderização customizada)  |
|     `MainLab2.java`      |  **Equipe**      | `ui`         | Ponto de entrada do Lab 2: inicia a interface gráfica e integra os algoritmos ao visualizador           |

## Laboratório 3 — Transformações geométricas 2D

Entrega independente, compatível com Java 11 ou superior, sem dependências externas.
Inclui os códigos fontes originais dos Labs 1 e 2 e um módulo novo executado por `MainLab3`.

### Como compilar e rodar o Lab3

Abra o terminal **na raiz desta entrega**, onde estão `src`, `tests` e este README.

```sh
javac --release 11 -encoding UTF-8 -d out -sourcepath src src/MainLab1.java src/MainLab2.java src/MainLab3.java tests/TestesLab3.java
java -cp out MainLab3
```

O mesmo comando funciona no PowerShell e no terminal do Linux. É necessário um **JDK**, não apenas o JRE.
No Linux, também é possível usar `bash executar-lab3.sh`.
Para os testes (não precisam de monitor):

```sh
java -Djava.awt.headless=true -cp out TestesLab3
```

Para gerar capturas do painel Swing junto com os testes:

```sh
java -Djava.awt.headless=true -cp out TestesLab3 docs/capturas
```

Labs anteriores: `java -cp out MainLab1` ou `java -cp out MainLab2`.
A documentação histórica foi preservada em [README-Labs1e2](docs/README-Labs1e2.md).

### Uso da interface

1. Na aba a direita, é possível escolher o polígono que será desenhada no mundo. Estão disponíveis para a escolha: um triângulo equilátero, um quadrado e uma seta côncava. O vértice **A** começa em **(0,0)** e os demais vértices se posicionam em relação a tal.
2. Logo abaixo, é possível escolher a transformação que será aplicada e os parâmetros para aplicar na transformação. São aceitos vírgula ou ponto decimal.
3. Clique em **Aplicar transformação**. Cada aplicação age sobre o estado atual do polígono.
4. Consulte **Matrizes e etapas** para visualizar as matrizes utilizadas nos cálculos das transformações; **Vértices** para visualizar os pontos cartesianos **(X, Y)** de cada um dos vértices da forma; **Composição / histórico** para visualizar o histórico de transformações feitas na forma desde a sua ciração inicial.
5. Use **Desfazer** para retirar a última operação ou **Restaurar** para voltar à forma inicial.
6. Você pode desmarcar **Mostrar cálculos** para ampliar o canvas. A divisória da aba também pode ser arrastada para cima ou para baixo.

O polígono atual é verde petróleo e o original é cinza. A origem permanece no centro do canvas, com X para a direita e Y para cima. O enquadramento uniforme é automático para manter a forma visível.
Esse ajuste é somente de exibição, isto é, não altera coordenadas nem a composição do objeto. Uma ampliação grande pode reduzir a quantidade de pixels por unidade devido ao zoom; confira os eixos e os vértices para avaliar a escala matemática. A troca de polígono reinicia o histórico.

### Convenção matemática e fidelidade às aulas

- Cada vértice é uma coluna `(x,y,1)`. O polígono forma uma matriz `P` de dimensão `3 x N`.
- Todas as operações usam `P' = M * P`, calculado pelo laço explícito de `MatrizUtils.multiplicar`.
- Ao aplicar M1 e depois M2, a composição é `C = M2 * M1`. Uma nova operação atualiza `C = M * C`.
- Os vértices atuais são recalculados a partir do original: `P atual = C * P original`.

| Operação                   | Matriz homogênea, escrita por linhas     |
| -------------------------- | ---------------------------------------- |
| Translação                 | `[1,0,tx]; [0,1,ty]; [0,0,1]`            |
| Escala                     | `[sx,0,0]; [0,sy,0]; [0,0,1]`            |
| Rotação                    | `[cosθ,-senθ,0]; [senθ,cosθ,0]; [0,0,1]` |
| Reflexão em X              | `[1,0,0]; [0,-1,0]; [0,0,1]`             |
| Reflexão em Y              | `[-1,0,0]; [0,1,0]; [0,0,1]`             |
| Reflexão na origem         | `[-1,0,0]; [0,-1,0]; [0,0,1]`            |
| Reflexão em y=x            | `[0,1,0]; [1,0,0]; [0,0,1]`              |
| Cisalhamento simultâneo XY | `[1,a,0]; [b,1,0]; [0,0,1]`              |

- Cisalhamento somente em X: `b=0`. Somente em Y: `a=0`.
- XY simultâneo usa as coordenadas anteriores em ambas as equações. Não equivale em geral ao produto dos dois cisalhamentos simples.
- Escala/rotação com pivô F: `T(F) * M * T(-F)`.
- Reflexão em `y=m*x+b`: `T(0,b) * R(α) * Fx * R(-α) * T(0,-b)`, com `α=atan(m)`.
  As cinco etapas e seus vértices intermediários aparecem nos cálculos. A forma `y=mx+b` não representa retas verticais.
- Rotação positiva é anti-horária. Só a reflexão **na origem** equivale à rotação de 180° no plano.
- Escalas zero e negativas são aceitas. Escala zero pode reduzir a forma a segmento ou ponto; desfazer continua funcionando.

Aula G3, slides 16–28: arestas por Ponto Médio, `d=2dy-dx`, `incE=2dy`, `incNE=2(dy-dx)`,
com a extensão aos oito oitantes já presente no Lab2. O empate segue o pseudocódigo do slide 26 (`d<=0`).
Cada aresta, inclusive a última ligada à primeira, passa por `AlgoritmoBresenham.bresenham`.
As primitivas `drawLine` do canvas servem somente para grade e eixos. As arestas são pixels de um framebuffer.

Aula G5, slides 6–28: transformações básicas, coordenadas homogêneas, reflexões,
cisalhamento e composições com ponto fixo. As anotações de sala complementam o cisalhamento XY e a
reflexão em reta arbitrária, implementada com as mesmas matrizes básicas.
Janela/viewport como assunto didático, Gato de Arnold e Cohen–Sutherland ficaram fora do escopo acordado.

### Estrutura de arquivos e responsáveis pelo Lab3

| Arquivo novo                   | Responsabilidade                                                                |
| ------------------------------ | ------------------------------------------------------------------------------- |
| `transformer/MatrizUtils`      | Validação, cópia, identidade e multiplicação explícita de matrizes retangulares |
| `model/Poligono`               | Formas iniciais imutáveis, com vértices em colunas                              |
| `transformer/Transformacoes2D` | Construção das matrizes e composição ordenada                                   |
| `model/EstadoTransformacoes`   | Histórico, limites, aplicação, desfazer e restauração                           |
| `ui/CanvasTransformacoes`      | Eixos, grade, enquadramento visual e rasterização das arestas                   |
| `ui/PainelLab3`                | Controles, parâmetros e inspeção das contas                                     |
| `MainLab3`                     | Tema Nimbus do JDK e criação da janela Swing                                    |
| `tests/TestesLab3`             | Testes matemáticos, rasterização e integração dos controles                     |

### Precisão e limites

Os cálculos usam `double` e o arredondamento para inteiros ocorre apenas ao rasterizar, após converter para pixels do canvas. As tabelas, no entanto, arredondam somente o texto de apresentação. Os parâmetros e coordenadas finais ficam entre -10.000 e 10.000, com no máximo 100 operações no histórico. Qualquer entrada não finitas será rejeitada. Uma operação inválida não altera o estado.
