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

## Laboratório 1 - Transformações de Coordenadas

Implementa o pipeline `Mundo → NDC → Dispositivo` visto na Aula G2 e acende, na tela real do computador (notebook ou desktop do laboratório), o pixel exato correspondente a um ponto de mundo informado pelo usuário, nos dois cenários de NDC exigidos pelo enunciado: `[0,1] x [0,1]` e `[-1,1] x [-1,1]`.

O programa segue estes passos:

1. Pergunta ao usuário a janela de mundo (`xMin`, `xMax`, `yMin`, `yMax`) e um ponto `(x, y)` dentro dela.
2. Descobre a resolução real da tela do computador em uso.
3. Calcula as coordenadas NDC e, em seguida, as coordenadas de dispositivo (`DC_x`, `DC_y`), para os dois cenários de NDC.
4. Mostra um resumo dos cálculos em uma caixa de diálogo e no console.
5. Abre uma janela em tela cheia, com fundo preto, e acende **um único pixel verde (0x00FF00)** exatamente na posição calculada, usando `BufferedImage.setRGB` (por dentro de `PixelRenderer.drawPixel`), sem representar o ponto como círculo, retângulo ou qualquer outra forma com mais de um pixel.
6. Pergunta se o usuário quer visualizar também o outro cenário de NDC, para comparação.

## Estrutura de arquivos e responsáveis

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

## Como compilar e rodar

O projeto usa pacotes (`model`, `transformer`, `ui`), então a estrutura de pastas dentro de `src` importa: `MainLab1.java` fica solto na raiz, e cada classe fica dentro da pasta que dá nome ao seu pacote. Com o JDK instalado (11 ou superior), rode a partir da pasta `src`:

```bash
javac MainLab1.java
java MainLab1
```

(`javac MainLab1.java` já compila `model`, `transformer` e `ui` automaticamente, por causa dos imports. Se preferir compilar tudo explicitamente: `javac -d out $(find . -name "*.java")` e depois `java -cp out MainLab1`.)

O programa vai abrir caixas de diálogo pedindo os dados e, no final, ocupar a tela inteira para mostrar o pixel.

## Como cada requisito do enunciado foi atendido

- **Requisito 1** (janela do mundo definida pelo usuário): `UserInputReader.lerJanelaDeMundo()`.
- **Requisito 2** (dois cenários de NDC): `NdcScenario` + `MainLab1` calcula os dois cenários para o mesmo ponto.
- **Requisito 3** (exibir na tela real): `PixelRenderer.obterResolucaoDaTela()` + `exibirPixelAtivo(...)`.
- **Requisito 4** (pixel de verdade, sem agrupamento): único uso de `setRGB` em `PixelRenderer.drawPixel`, cor `0x00FF00` (`MainLab1.COR_PIXEL`), conforme exigido pelo professor.

## Laboratório 2: Rasterização

O objetivo principal deste laboratório é a **rasterização de retas e circunferências**. O sistema desenha formas geométricas convertendo coordenadas matemáticas em pixels individuais em um *framebuffer* virtual, sem utilizar métodos nativos de desenho de formas do Java (como `drawLine` ou `drawOval`).

Nesse laboratório, foi adicionada uma interface gráfica interativa (Swing) que permite ao usuário escolher o algoritmo, inserir os dados de entrada (coordenadas e raio), visualizar a forma gerada sobre uma malha quadriculada com eixos cartesianos e, mais importante, acompanhar o registro passo a passo dos cálculos e decisões matemáticas feitas pelo algoritmo quadro a quadro.

## Funcionalidades

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

## Estrutura de arquivos e responsáveis

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

## Como compilar e rodar

Com o terminal na pasta `src` execute no seu terminal:

```bash
javac MainLab2.java
java MainLab2
```

## Integrantes da equipe

- Gabriela Almeida de Andradde
- Georis Samuel Martins Gouveia
- Horlan Silva de Lacerda
- João Victor Araújo Leite dos Santos
- José Araújo Agra Neto
- Suelle Ferreira Maciel
