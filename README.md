# Laboratório 1 de Transformações de Coordenadas — Computação Gráfica (Aula G2)

## O que este projeto faz

Implementa o pipeline `Mundo → NDC → Dispositivo` visto na Aula G2 e acende, na tela real do computador (notebook ou desktop do laboratório), o pixel exato correspondente a um ponto de mundo informado pelo usuário, nos dois cenários de NDC exigidos pelo enunciado: `[0,1] x [0,1]` e `[-1,1] x [-1,1]`.

O programa segue estes passos:

1. Pergunta ao usuário a janela de mundo (`xMin`, `xMax`, `yMin`, `yMax`) e um ponto `(x, y)` dentro dela.
2. Descobre a resolução real da tela do computador em uso.
3. Calcula as coordenadas NDC e, em seguida, as coordenadas de dispositivo (`DC_x`, `DC_y`), para os dois cenários de NDC.
4. Mostra um resumo dos cálculos em uma caixa de diálogo e no console.
5. Abre uma janela em tela cheia, com fundo preto, e acende **um único pixel verde** exatamente na posição calculada, usando `BufferedImage.setRGB`, sem representar o ponto como círculo, retângulo ou qualquer outra forma com mais de um pixel.
6. Abre também uma pequena janela de "lupa" (zoom), só para facilitar a visualização humana durante a defesa; ela não faz parte do pixel real, é apenas uma ampliação visual da região ao redor dele.
7. Pergunta se o usuário quer visualizar também o outro cenário de NDC, para comparação.

## Estrutura de arquivos e responsáveis

|        Classe         |  Responsável     | O que faz                                                               |
| --------------------- | ---------------- | ----------------------------------------------------------------------- |
|`DevicePoint`          |   **Grabiela**   | Representa um ponto em coordenadas de dispositivo                       |
|`NdcScenario`          |   **Grabiela**   | Representa os dois cenarios de coordenadas normalizadas de dispositivo  |
|`PointD`               |   **Grabiela**   | Representa um ponto bidimensional com coordenadas em double             |
|`UserInputReader`      |   **Suelle**     | Responsavel por coletar os dados do usuário                             |
|`WorldWindow`          |   **Horlan**     | Representa a janela de mundo                                            |
|`CoordinateTransformer`|   **Horlan**     | Implementa os quatro procedimentos de transformacao de coordenadas      |
|`EntradaUsuario`       |   **Suelle**     | Agrupa os dois dados que o usuario fornece                              |
|`PixelRenderer`        |  **Georis/Neto** | Descobre a resolução real do dispositivo e exibe o pixel verde na tela  |
|     `Main.java`       |  **João Victor** | Integra tudo: entrada → transformação → exibição                        |
|     `README.md`       |  **João Victor** | Este arquivo                                                            |

## Como compilar e rodar

Todos os arquivos estão no pacote padrão (sem `package`), então não é preciso configurar Maven/Gradle nem estrutura de pastas especial. Com o JDK instalado (11 ou superior):

```bash
javac *.java
java Main
```

O programa vai abrir caixas de diálogo pedindo os dados e, no final, ocupar a tela inteira para mostrar o pixel. Para fechar a janela de tela cheia, use `Alt+F4` (Windows/Linux) ou feche pela janela de lupa.

> [⚠️!WARNING]
> **Escala do Windows (DPI).** Em muitos notebooks o Windows vem com a escala padrão em **125%** (ou mais). Nesse caso, o Java (9+) passa a enxergar a resolução *lógica* da tela, e não a física: numa tela de **1920x1080** com 125%, o programa vai detectar algo como **1536x864**.
>
> Isso afeta o resultado do laboratório:
> - as coordenadas de dispositivo (`DC_x`, `DC_y`) passam a ser calculadas sobre a resolução errada;
> - cada pixel da imagem é ampliado pelo Windows para ~1,25 pixel físico, então o ponto pode não acender exatamente **um** pixel real (fere o Requisito 4).
>
> **Antes de rodar no Windows, ajuste a escala para 100%:** `Configurações → Sistema → Tela → Escala = 100%`.
>

## Como cada requisito do enunciado foi atendido

- **Requisito 1** (janela do mundo definida pelo usuário): `UserInputReader.lerJanelaDeMundo()`.
- **Requisito 2** (dois cenários de NDC): `NdcScenario` + `Main` calcula os dois cenários para o mesmo ponto.
- **Requisito 3** (exibir na tela real): `PixelRenderer.obterResolucaoDaTela()` + `exibirPixelAtivo(...)`.
- **Requisito 4** (pixel de verdade, sem agrupamento): único uso de `setRGB` em `PixelRenderer`, cor verde por padrão (`Color.GREEN`).

## Integrantes da equipe

- Gabriela Almeida de Andradde
- Georis Samuel Martins Gouveia
- Horlan Silva de Lacerda
- João Victor Araújo Leite dos Santos
- José Araújo Agra Neto
- Suelle Ferreira Maciel
