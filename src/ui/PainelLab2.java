package ui;

import algorithms.*;
import raster.ResultadoRasterizacao;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/** Janela didática: controles, framebuffer e contas permanecem visíveis juntos. */
public final class PainelLab2 extends JPanel {
    // <-------------------ESTADO DA INTERFACE------------------->
    // Tamanho inicial, usado apenas antes de o painel receber seu layout.
    public static final int LARGURA = 640;
    public static final int ALTURA = 400;
    private static final Color COR_PIXEL = new Color(0x00FF88);
    private final JComboBox<String> algoritmo = new JComboBox<>(new String[]{
            "Reta (DDA)", "Reta (Bresenham)", "Circunferência (Polinomial)",
            "Circunferência (Trigonométrica)", "Circunferência (Ponto médio)"
    });
    private final Map<String, JTextField> campos = new LinkedHashMap<>();
    private final JCheckBox automatico = new JCheckBox("Passo angular automático (1/r rad)", true);
    private final JCheckBox mostrarEixos = new JCheckBox("Mostrar eixos", true);
    private final JButton desenhar = new JButton("Desenhar");
    private final JButton limpar = new JButton("Limpar tela");
    private final JTextArea ajuda = texto();
    private final JTextArea resumo = texto();
    private final JTextArea detalhe = texto();
    private final JTextArea incrementos = texto();
    private final JLabel estado = new JLabel("Escolha um algoritmo, informe os dados e clique em Desenhar.");
    private final ModeloPassos modelo = new ModeloPassos();
    private final JTabbedPane abasCalculos = new JTabbedPane();
    private final JTable tabela = new JTable(modelo);
    private final TelaPixels tela = new TelaPixels();
    private final CardLayout tipos = new CardLayout();
    private final JPanel entradas = new JPanel(tipos);
    private final JPanel painelTheta = new JPanel(new BorderLayout(4, 4));
    private ResultadoRasterizacao resultadoAtual;
    private PedidoDesenho ultimoPedido;
    private boolean calculando;
    // Agrupa os eventos de arraste para não recalcular a cada pixel de movimento.
    private final Timer ajusteDaArea = new Timer(120, evento -> ajustarArea());

    public PainelLab2() {
        super(new BorderLayout(12, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel titulo = new JLabel("LAB2 | Rasterização e operações");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);
        add(criarControles(), BorderLayout.WEST);
        add(criarResultados(), BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        add(estado, BorderLayout.SOUTH);
        algoritmo.setName("algoritmo");
        desenhar.setName("desenhar");
        limpar.setName("limpar");
        automatico.setName("automatico");
        mostrarEixos.setName("mostrarEixos");
        mostrarEixos.addActionListener(evento -> {
            tela.eixosVisiveis = mostrarEixos.isSelected();
            tela.repaint();
        });
        algoritmo.addActionListener(evento -> atualizarSelecao());
        automatico.addActionListener(evento -> campos.get("theta").setEnabled(!automatico.isSelected()));
        desenhar.addActionListener(evento -> executar());
        limpar.addActionListener(evento -> limparResultado());
        ajusteDaArea.setRepeats(false);
        tela.setName("telaPixels");
        tela.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent evento) {
                atualizarAjuda();
                ajusteDaArea.restart();
            }
        });
        atualizarSelecao();
        atualizarAjuda();
    }

    // <-------------------CONTROLES E CAMPOS------------------->
    private JPanel criarControles() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setPreferredSize(new Dimension(290, 650));
        lateral.add(new JLabel("Algoritmo"));
        algoritmo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        lateral.add(algoritmo);
        lateral.add(Box.createVerticalStrut(12));
        JPanel retas = new JPanel(new GridLayout(0, 2, 5, 8));
        campo(retas, "X inicial", "x1", "0");
        campo(retas, "Y inicial", "y1", "0");
        campo(retas, "X final", "x2", "150");
        campo(retas, "Y final", "y2", "80");
        JPanel circulos = new JPanel(new GridLayout(0, 2, 5, 8));
        campo(circulos, "Centro X", "cx", "0");
        campo(circulos, "Centro Y", "cy", "0");
        campo(circulos, "Raio", "raio", "120");
        entradas.add(retas, "retas");
        entradas.add(circulos, "circulos");
        entradas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        lateral.add(entradas);
        JPanel angulo = new JPanel(new GridLayout(1, 2, 5, 5));
        campo(angulo, "Passo θ (graus)", "theta", "15");
        painelTheta.add(automatico, BorderLayout.NORTH);
        painelTheta.add(angulo, BorderLayout.CENTER);
        painelTheta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        lateral.add(Box.createVerticalStrut(12));
        lateral.add(painelTheta);
        JPanel botoes = new JPanel(new GridLayout(1, 2, 5, 5));
        botoes.add(desenhar);
        botoes.add(limpar);
        botoes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        lateral.add(Box.createVerticalStrut(12));
        lateral.add(botoes);
        lateral.add(Box.createVerticalStrut(8));
        lateral.add(mostrarEixos);
        ajuda.setBackground(getBackground());
        ajuda.setPreferredSize(new Dimension(290, 175));
        ajuda.setMinimumSize(new Dimension(290, 175));
        ajuda.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));
        lateral.add(Box.createVerticalStrut(12));
        lateral.add(ajuda);
        incrementos.setBackground(new Color(235, 243, 241));
        incrementos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Incrementos calculados"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        incrementos.setText("Os valores aparecem após Desenhar.");
        lateral.add(incrementos);
        return lateral;
    }

    private void campo(JPanel painel, String rotulo, String nome, String inicial) {
        JTextField entrada = new JTextField(inicial);
        entrada.setName(nome);
        JLabel label = new JLabel(rotulo);
        label.setLabelFor(entrada);
        painel.add(label);
        painel.add(entrada);
        campos.put(nome, entrada);
    }

    // <-------------------DESENHO, RESUMO E TABELA------------------->
    private JSplitPane criarResultados() {
        // O BorderLayout entrega à tela toda a área interna disponível.
        JPanel visualizacao = new JPanel(new BorderLayout());
        visualizacao.add(tela, BorderLayout.CENTER);
        visualizacao.setBorder(BorderFactory.createTitledBorder("Pixels calculados - imagem sem escala"));
        JScrollPane contasIniciais = new JScrollPane(resumo);
        contasIniciais.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(25);
        tabela.setShowGrid(false);
        tabela.setIntercellSpacing(new Dimension(8, 0));
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            private final java.text.DecimalFormat formato = new java.text.DecimalFormat(
                    "0.####", java.text.DecimalFormatSymbols.getInstance(new java.util.Locale("pt", "BR")));
            @Override protected void setValue(Object valor) {
                // Arredonda apenas a exibição; as contas completas mantêm a precisão.
                setText(valor instanceof Number ? formato.format(valor) : String.valueOf(valor));
            }
        });
        tabela.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabela.getSelectedRow() >= 0 && resultadoAtual != null) {
                ResultadoRasterizacao.Passo passo = resultadoAtual.getPassos().get(tabela.getSelectedRow());
                detalhe.setText("Etapa " + passo.getEtapa() + "\n\n"
                        + passo.getOperacao().replace("; ", "\n")
                        + "\n\nPixels gerados — coordenadas de entrada → imagem\n"
                        + passo.getDescricaoPixels().replace("; ", "\n"));
                detalhe.setCaretPosition(0);
            }
        });
        JScrollPane linhas = new JScrollPane(tabela);
        linhas.setColumnHeaderView(tabela.getTableHeader());
        linhas.setBorder(BorderFactory.createTitledBorder("Selecione uma etapa para conferir as contas"));
        JScrollPane leitura = new JScrollPane(detalhe);
        leitura.setBorder(BorderFactory.createTitledBorder("Detalhe completo da etapa selecionada"));
        linhas.setMinimumSize(new Dimension(200, 80));
        leitura.setMinimumSize(new Dimension(200, 80));
        JSplitPane passos = new JSplitPane(JSplitPane.VERTICAL_SPLIT, linhas, leitura);
        passos.setResizeWeight(0.55);
        passos.setDividerLocation(150);
        passos.setContinuousLayout(true);
        abasCalculos.addTab("Passos", passos);
        abasCalculos.addTab("Fórmulas e dados iniciais", contasIniciais);
        JComponent calculos = abasCalculos;
        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visualizacao, calculos);
        divisao.setResizeWeight(0.52);
        divisao.setDividerLocation(420);
        visualizacao.setMinimumSize(new Dimension(200, 150));
        calculos.setMinimumSize(new Dimension(200, 200));
        return divisao;
    }

    private static JTextArea texto() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(5, 5, 5, 5));
        return area;
    }

    // <-------------------VALIDAÇÃO E EXECUÇÃO SEM BLOQUEAR A JANELA------------------->
    private int inteiro(String nome, int minimo, int maximo) {
        try {
            int valor = Integer.parseInt(campos.get(nome).getText().trim());
            if (valor >= minimo && valor <= maximo) return valor;
        } catch (NumberFormatException erro) {
            // Mensagem única para texto inválido ou inteiro fora dos limites.
        }
        throw new IllegalArgumentException(nome + ": informe um inteiro entre " + minimo + " e " + maximo + ".");
    }

    private void executar() {
        final int escolhido = algoritmo.getSelectedIndex();
        Dimension area = tamanhoDaArea();
        final int[] valores;
        final double passoGraus;
        try {
            if (escolhido < 2) {
                valores = new int[]{inteiro("x1", -area.width / 2, area.width - 1 - area.width / 2), inteiro("y1", area.height / 2 - (area.height - 1), area.height / 2),
                        inteiro("x2", -area.width / 2, area.width - 1 - area.width / 2), inteiro("y2", area.height / 2 - (area.height - 1), area.height / 2)};
                passoGraus = 0;
            } else {
                valores = new int[]{inteiro("cx", -area.width / 2, area.width - 1 - area.width / 2), inteiro("cy", area.height / 2 - (area.height - 1), area.height / 2),
                        inteiro("raio", 1, limiteRaio(area))};
                passoGraus = escolhido == 3 && !automatico.isSelected()
                        ? lerPasso() : Math.toDegrees(1.0 / valores[2]);
            }
        } catch (IllegalArgumentException erro) {
            estado.setText("Entrada inválida: " + erro.getMessage());
            return;
        }
        iniciarCalculo(new PedidoDesenho(escolhido, valores, passoGraus));
    }

    // <-------------------REDIMENSIONAMENTO SEM ESCALAR OS PIXELS------------------->
    private Dimension tamanhoDaArea() {
        return new Dimension(tela.getWidth() > 0 ? tela.getWidth() : LARGURA,
                tela.getHeight() > 0 ? tela.getHeight() : ALTURA);
    }

    private int limiteRaio(Dimension area) {
        // O algoritmo limita o raio a 2.000 para não produzir tabelas gigantes.
        return Math.min(2000, Math.max(area.width, area.height));
    }

    private void atualizarAjuda() {
        Dimension area = tamanhoDaArea();
        ajuda.setText("Área: " + area.width + " × " + area.height + " pixels.\n"
                + "Origem (0,0) no centro.\nX: " + (-area.width / 2) + " a " + (area.width - 1 - area.width / 2)
                + " | Y: " + (area.height / 2 - (area.height - 1)) + " a " + (area.height / 2) + ".\nY cresce para cima. Raio: 1 a "
                + limiteRaio(area) + ".\n\nCada execução substitui o desenho anterior.\n"
                + "Limpar preserva os campos digitados.");
    }

    private void ajustarArea() {
        atualizarAjuda();
        Dimension area = tamanhoDaArea();
        if (calculando || (tela.imagem.getWidth() == area.width
                && tela.imagem.getHeight() == area.height)) return;
        if (ultimoPedido != null) {
            // Reutiliza os parâmetros do desenho, não os campos talvez já editados.
            // Se a janela encolher, os pixels externos são recortados pelo registro.
            iniciarCalculo(ultimoPedido);
        } else {
            tela.imagem = PixelRenderer.criarImagem(area.width, area.height);
            tela.repaint();
        }
    }

    /** Guarda o desenho solicitado para recuperar partes recortadas ao ampliar. */
    private static final class PedidoDesenho {
        private final int escolhido;
        private final int[] valores;
        private final double passoGraus;
        private PedidoDesenho(int escolhido, int[] valores, double passoGraus) {
            this.escolhido = escolhido;
            this.valores = valores.clone();
            this.passoGraus = passoGraus;
        }
    }

    private void iniciarCalculo(PedidoDesenho pedido) {
        Dimension area = tamanhoDaArea();
        final int escolhido = pedido.escolhido;
        final int[] valores = pedido.valores;
        final double passoGraus = pedido.passoGraus;
        calculando = true;
        habilitar(false);
        estado.setText("Calculando pixels e operações...");
        new SwingWorker<ResultadoRasterizacao, Void>() {
            @Override
            protected ResultadoRasterizacao doInBackground() {
                BufferedImage imagem = PixelRenderer.criarImagem(area.width, area.height);
                switch (escolhido) {
                    case 0: return AlgoritmoDDA.dda(imagem, valores[0], valores[1], valores[2], valores[3], COR_PIXEL, true);
                    case 1: return AlgoritmoBresenham.bresenham(imagem, valores[0], valores[1], valores[2], valores[3], COR_PIXEL, true);
                    case 2: return CircPolinomial.polinomial(imagem, valores[0], valores[1], valores[2], COR_PIXEL, true);
                    case 3: return CircTrigonometrico.trigonometrico(imagem, valores[0], valores[1], valores[2], COR_PIXEL, passoGraus, true);
                    case 4: return CircPontoMedio.pontoMedio(imagem, valores[0], valores[1], valores[2], COR_PIXEL, true);
                    default: throw new IllegalArgumentException("Algoritmo desconhecido.");
                }
            }
            @Override
            protected void done() {
                boolean concluido = false;
                try {
                    apresentar(get());
                    ultimoPedido = pedido;
                    concluido = true;
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    estado.setText("Execução interrompida.");
                } catch (ExecutionException erro) {
                    estado.setText("Falha: " + erro.getCause().getMessage());
                } finally {
                    calculando = false;
                    habilitar(true);
                    // A janela pode ter mudado novamente durante o cálculo.
                    if (concluido) ajustarArea();
                }
            }
        }.execute();
    }

    private double lerPasso() {
        double valor;
        try {
            valor = Double.parseDouble(campos.get("theta").getText().trim().replace(',', '.'));
        } catch (NumberFormatException erro) {
            throw new IllegalArgumentException("θ: informe um passo numérico em graus.");
        }
        if (!Double.isFinite(valor) || valor < 0.018 || valor > 360) {
            throw new IllegalArgumentException("θ: passo entre 0,018° e 360° (até 20.000 amostras).");
        }
        return valor;
    }

    private void apresentar(ResultadoRasterizacao resultado) {
        resultadoAtual = resultado;
        tela.imagem = resultado.getImagem();
        tela.repaint();
        resumo.setText(resultado.getResumo());
        resumo.setCaretPosition(0);
        StringBuilder painel = new StringBuilder();
        for (String linha : resultado.getResumo().split("\n")) {
            if (linha.contains("Inc") || linha.contains("inc =") || linha.contains("Δθ =")) {
                painel.append(linha).append('\n');
            }
        }
        incrementos.setText(painel.length() == 0
                ? "Polinomial: avançar somente X de 1 em 1.\nNão utiliza decisão E/NE ou E/SE."
                : painel.toString());
        modelo.fireTableStructureChanged();
        detalhe.setText("");
        abasCalculos.setSelectedIndex(0);
        if (!resultado.getPassos().isEmpty()) tabela.setRowSelectionInterval(0, 0);
        estado.setText(resultado.getPassos().size() + " etapas registradas. Selecione uma linha para conferir as contas.");
    }

    private void limparResultado() {
        resultadoAtual = null;
        ultimoPedido = null;
        Dimension area = tamanhoDaArea();
        tela.imagem = PixelRenderer.criarImagem(area.width, area.height);
        tela.repaint();
        resumo.setText("");
        detalhe.setText("");
        incrementos.setText("Os valores aparecem após Desenhar.");
        modelo.fireTableStructureChanged();
        estado.setText("Tela e cálculos limpos; os campos foram preservados.");
    }

    private void atualizarSelecao() {
        tipos.show(entradas, algoritmo.getSelectedIndex() < 2 ? "retas" : "circulos");
        painelTheta.setVisible(algoritmo.getSelectedIndex() == 3);
        campos.get("theta").setEnabled(!automatico.isSelected());
        limparResultado();
        revalidate();
    }

    private void habilitar(boolean ativo) {
        algoritmo.setEnabled(ativo);
        desenhar.setEnabled(ativo);
        limpar.setEnabled(ativo);
        automatico.setEnabled(ativo);
        for (JTextField campo : campos.values()) campo.setEnabled(ativo);
        campos.get("theta").setEnabled(ativo && !automatico.isSelected());
    }

    /** Acesso de leitura útil para verificar que a tabela pertence à imagem exibida. */
    public ResultadoRasterizacao getResultadoAtual() { return resultadoAtual; }

    // <-------------------MODELO DA TABELA E ÁREA DE PIXELS------------------->
    private final class ModeloPassos extends AbstractTableModel {
        @Override public int getRowCount() { return resultadoAtual == null ? 0 : resultadoAtual.getPassos().size(); }
        @Override public int getColumnCount() { return resultadoAtual == null ? 1 : resultadoAtual.getColunas().size() + 1; }
        @Override public String getColumnName(int coluna) {
            return coluna == 0 ? "Etapa" : resultadoAtual.getColunas().get(coluna - 1);
        }
        @Override public Object getValueAt(int linha, int coluna) {
            ResultadoRasterizacao.Passo passo = resultadoAtual.getPassos().get(linha);
            return coluna == 0 ? passo.getEtapa() : passo.getValores().get(coluna - 1);
        }
    }

    private static final class TelaPixels extends JPanel {
        private BufferedImage imagem = PixelRenderer.criarImagem(LARGURA, ALTURA);
        private boolean eixosVisiveis = true;
        private BufferedImage origemDaCamada;
        private BufferedImage camadaPixels;

        private TelaPixels() {
            setPreferredSize(new Dimension(LARGURA, ALTURA));
            setBackground(Color.BLACK);
        }
        @Override protected void paintComponent(Graphics desenho) {
            super.paintComponent(desenho);
            if (!eixosVisiveis) {
                desenho.drawImage(imagem, 0, 0, null);
                return;
            }
            Graphics2D guia = (Graphics2D) desenho.create();
            desenharEixos(guia);
            guia.dispose();
            // Os pixels calculados ficam acima dos eixos, sem alterar o framebuffer.
            atualizarCamadaPixels();
            desenho.drawImage(camadaPixels, 0, 0, null);
        }

        // <-------------------EIXOS CARTESIANOS SEM GRADE------------------->
        private void desenharEixos(Graphics2D guia) {
            int largura = getWidth();
            int altura = getHeight();
            int centroX = largura / 2;
            int centroY = altura / 2;
            guia.setColor(new Color(90, 90, 90));
            guia.drawLine(0, centroY, largura - 1, centroY);
            guia.drawLine(centroX, 0, centroX, altura - 1);
            guia.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            FontMetrics fonte = guia.getFontMetrics();
            guia.setColor(new Color(145, 145, 145));
            guia.drawString("(0,0)", centroX + 7, centroY + 15);
            guia.drawString("X", largura - 13, centroY - 8);
            guia.drawString("Y", centroX + 8, 14);
            // Um passo de 50 unidades corresponde a 50 pixels, sem escala.
            for (int coluna = centroX % 50; coluna < largura; coluna += 50) {
                if (coluna == centroX) continue;
                guia.drawLine(coluna, centroY - 3, coluna, centroY + 3);
                String valor = String.valueOf(coluna - centroX);
                int inicio = coluna - fonte.stringWidth(valor) / 2;
                if (inicio >= 2 && inicio + fonte.stringWidth(valor) < largura - 2) {
                    guia.drawString(valor, inicio, centroY + 15);
                }
            }
            for (int linha = centroY % 50; linha < altura; linha += 50) {
                if (linha == centroY) continue;
                guia.drawLine(centroX - 3, linha, centroX + 3, linha);
                // Na convenção cartesiana, Y positivo aponta para cima.
                if (linha > 24 && linha < altura - 8) {
                    guia.drawString(String.valueOf(centroY - linha), centroX + 7, linha + 4);
                }
            }
        }

        // <-------------------CAMADA VISUAL DOS PIXELS CALCULADOS------------------->
        private void atualizarCamadaPixels() {
            if (origemDaCamada == imagem) return;
            // A execução entrega uma nova imagem. A cópia transparente é feita
            // uma única vez por imagem; ocultar os eixos não recalcula o algoritmo.
            camadaPixels = new BufferedImage(imagem.getWidth(), imagem.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < imagem.getHeight(); y++) {
                for (int x = 0; x < imagem.getWidth(); x++) {
                    int cor = imagem.getRGB(x, y);
                    if ((cor & 0x00FFFFFF) != 0) camadaPixels.setRGB(x, y, cor);
                }
            }
            origemDaCamada = imagem;
        }

    }
}
