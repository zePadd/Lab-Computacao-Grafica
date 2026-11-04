package ui;

import model.EstadoTransformacoes;
import transformer.MatrizUtils;
import transformer.Transformacoes2D;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

/**
 * Interface do laboratório com canvas predominante, operações individuais, composição e inspeção das contas.
 *
 * @author Gabriela Almeida de Andrade
 * @date 03/11/2026
 */
public final class PainelLab3 extends JPanel {
    private final EstadoTransformacoes estado = new EstadoTransformacoes();
    private final CanvasTransformacoes canvas = new CanvasTransformacoes(estado);
    private final JComboBox<String> forma = new JComboBox<>(new String[] {"Triângulo equilátero", "Quadrado", "Seta côncava"});
    private final JComboBox<String> operacao = new JComboBox<>(new String[] {
        "Translação", "Escala na origem", "Rotação na origem", "Reflexão em X", "Reflexão em Y",
        "Reflexão na origem", "Reflexão em y = x", "Cisalhamento em X", "Cisalhamento em Y",
        "Cisalhamento simultâneo XY", "Escala com ponto fixo", "Rotação com ponto fixo", "Reflexão em y = mx + b"});
    private final JPanel entradas = new JPanel(new GridLayout(0,2,8,10));
    private final JTextField[] campos = {new JTextField(),new JTextField(),new JTextField(),new JTextField()};
    private final JLabel[] rotulos = {new JLabel(),new JLabel(),new JLabel(),new JLabel()};
    private final JTextArea contas = area();
    private final JTextArea vertices = area();
    private final JTextArea historico = area();
    private final JTextArea ajuda = area();
    private final JLabel status = new JLabel("Pronto. A = (0,0) na configuração inicial.");
    private final JButton desfazer = new JButton("Desfazer");
    private final List<String> detalhes = new ArrayList<>();
    private JSplitPane divisao;
    private JTabbedPane inspecao;

    /**
     * Monta os controles e associa eventos às operações matemáticas.
     */
    public PainelLab3() {
        super(new BorderLayout(16,12));
        setBorder(BorderFactory.createEmptyBorder(18,18,12,18));
        setBackground(new Color(0xEDF2F8));
        JLabel titulo = new JLabel("LAB 03   /   Transformações geométricas 2D");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,23f));
        add(titulo,BorderLayout.NORTH);
        add(controles(),BorderLayout.WEST);
        inspecao = new JTabbedPane();
        inspecao.addTab("Matrizes e etapas",new JScrollPane(contas));
        inspecao.addTab("Vértices",new JScrollPane(vertices));
        inspecao.addTab("Composição / histórico",new JScrollPane(historico));
        inspecao.setMinimumSize(new Dimension(250,100));
        divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT,canvas,inspecao);
        divisao.setResizeWeight(0.78); divisao.setDividerLocation(490); divisao.setContinuousLayout(true);
        add(divisao,BorderLayout.CENTER); add(status,BorderLayout.SOUTH);
        forma.setSelectedIndex(1);
        forma.addActionListener(evento -> { estado.selecionar(forma.getSelectedIndex()); detalhes.clear(); atualizar(); });
        operacao.addActionListener(evento -> configurarCampos());
        configurarCampos(); atualizar();
    }

    // <-------------------CONTROLES E PARÂMETROS------------------->
    /**
     * Cria a coluna de operações com controles compactos e ajuda contextual.
     * @return painel lateral com rolagem
     */
    private JComponent controles() {
        JPanel lateral = new JPanel(); lateral.setLayout(new BoxLayout(lateral,BoxLayout.Y_AXIS));
        lateral.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        lateral.setPreferredSize(new Dimension(280,600));
        lateral.add(new JLabel("POLÍGONO")); lateral.add(Box.createVerticalStrut(6)); lateral.add(forma);
        lateral.add(Box.createVerticalStrut(22)); lateral.add(new JLabel("TRANSFORMAÇÃO"));
        lateral.add(Box.createVerticalStrut(6)); lateral.add(operacao);
        for (int i = 0; i < 4; i++) {
            entradas.add(rotulos[i]); entradas.add(campos[i]); campos[i].setName("parametro"+i);
            rotulos[i].setLabelFor(campos[i]); campos[i].addActionListener(evento -> aplicar());
        }
        entradas.setMaximumSize(new Dimension(Integer.MAX_VALUE,160));
        lateral.add(Box.createVerticalStrut(18)); lateral.add(entradas);
        JButton aplicar = new JButton("Aplicar transformação"); aplicar.setName("aplicar");
        aplicar.setBackground(new Color(0x007F87)); aplicar.setForeground(Color.WHITE);
        aplicar.addActionListener(evento -> aplicar());
        lateral.add(Box.createVerticalStrut(15)); lateral.add(aplicar);
        JPanel acoes = new JPanel(new GridLayout(1,2,6,0));
        JButton restaurar = new JButton("Restaurar"); restaurar.setName("restaurar");
        desfazer.setName("desfazer");
        desfazer.addActionListener(evento -> {
            estado.desfazer(); if (!detalhes.isEmpty()) detalhes.remove(detalhes.size()-1); atualizar();
        });
        restaurar.addActionListener(evento -> { estado.reiniciar(); detalhes.clear(); atualizar(); });
        acoes.add(desfazer); acoes.add(restaurar); acoes.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));
        lateral.add(Box.createVerticalStrut(8)); lateral.add(acoes);
        JCheckBox referencia = new JCheckBox("Exibir original",true);
        referencia.addActionListener(evento -> canvas.mostrarOriginal(referencia.isSelected()));
        JCheckBox detalhesVisiveis = new JCheckBox("Mostrar cálculos",true);
        detalhesVisiveis.addActionListener(evento -> {
            inspecao.setVisible(detalhesVisiveis.isSelected());
            divisao.setDividerSize(detalhesVisiveis.isSelected()?8:0);
            divisao.setDividerLocation(detalhesVisiveis.isSelected()?0.72:1.0);
        });
        lateral.add(Box.createVerticalStrut(14)); lateral.add(referencia); lateral.add(detalhesVisiveis);
        ajuda.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12)); ajuda.setLineWrap(true); ajuda.setWrapStyleWord(true);
        ajuda.setBackground(lateral.getBackground()); ajuda.setOpaque(false); ajuda.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        lateral.add(ajuda); lateral.add(Box.createVerticalGlue());
        forma.setName("forma"); operacao.setName("operacao");
        for (JComponent c : new JComponent[] {forma,operacao,aplicar}) c.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));
        for (Component filho : lateral.getComponents()) if (filho instanceof JComponent) ((JComponent)filho).setAlignmentX(Component.LEFT_ALIGNMENT);
        JScrollPane rolagem = new JScrollPane(lateral);
        rolagem.setBorder(null); rolagem.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rolagem.setPreferredSize(new Dimension(290,600));
        return rolagem;
    }

    /**
     * Ajusta rótulos, valores iniciais e explicação à transformação selecionada.
     */
    private void configurarCampos() {
        String[][] nomes = {{"Tx","Ty"},{"Sx","Sy"},{"Ângulo (graus)"},{},{},{},{},
            {"a (X)"},{"b (Y)"},{"a (X)","b (Y)"},{"Sx","Sy","Pivô X","Pivô Y"},
            {"Ângulo (graus)","Pivô X","Pivô Y"},{"m (inclinação)","b (intercepto)"}};
        String[][] valores = {{"1","1"},{"2","2"},{"90"},{},{},{},{},{"1"},{"1"},{"0.5","1"},
            {"2","2","2","1"},{"90","2","1"},{"1","1"}};
        int tipo = operacao.getSelectedIndex();
        entradas.removeAll();
        for (int i = 0; i < 4; i++) {
            boolean ativo = i < nomes[tipo].length;
            rotulos[i].setVisible(ativo); campos[i].setVisible(ativo);
            if (ativo) { entradas.add(rotulos[i]); entradas.add(campos[i]); }
            rotulos[i].setText(ativo?nomes[tipo][i]:""); campos[i].setText(ativo?valores[tipo][i]:"0");
        }
        entradas.setMaximumSize(new Dimension(Integer.MAX_VALUE,nomes[tipo].length*40));
        entradas.revalidate(); entradas.repaint();
        String texto = "Cada clique atua sobre o resultado atual. Restaure para comparar operações isoladas.\n\nÂngulos positivos: anti-horário. Aceita ponto ou vírgula decimal.";
        if (tipo == 9) texto += "\n\nXY simultâneo usa [[1,a],[b,1]]. Difere de aplicar X e depois Y.";
        if (tipo >= 10) texto += "\n\nAs etapas e matrizes da composição aparecem na aba de cálculos.";
        ajuda.setText(texto);
    }

    /**
     * Lê um parâmetro decimal finito, aceitando vírgula como separador.
     * @param indice índice do campo de entrada
     * @return parâmetro convertido para double
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    private double numero(int indice) {
        double n;
        try { n = Double.parseDouble(campos[indice].getText().trim().replace(',','.')); }
        catch (NumberFormatException erro) { throw new IllegalArgumentException("Informe um número em " + rotulos[indice].getText()+"."); }
        if (!Double.isFinite(n) || Math.abs(n)>10000) throw new IllegalArgumentException("Parâmetros devem estar entre -10.000 e 10.000.");
        return n;
    }

    // <-------------------EXECUÇÃO E INSPEÇÃO MATRICIAL------------------->
    /**
     * Constrói as etapas na ordem de aplicação, preservando a formulação homogênea dos slides.
     * @return matrizes da operação selecionada na ordem de aplicação
     */
    private double[][][] etapas() {
        int tipo = operacao.getSelectedIndex();
        if (tipo >= 3 && tipo <= 6) return new double[][][] {Transformacoes2D.reflexao(tipo-3)};
        double a = numero(0);
        switch (tipo) {
            case 0: return new double[][][] {Transformacoes2D.translacao(a,numero(1))};
            case 1: return new double[][][] {Transformacoes2D.escala(a,numero(1))};
            case 2: return new double[][][] {Transformacoes2D.rotacao(a)};
            case 7: return new double[][][] {Transformacoes2D.cisalhamento(a,0)};
            case 8: return new double[][][] {Transformacoes2D.cisalhamento(0,a)};
            case 9: return new double[][][] {Transformacoes2D.cisalhamento(a,numero(1))};
            case 10: {
                double sy = numero(1), x = numero(2), y = numero(3);
                return new double[][][] {Transformacoes2D.translacao(-x,-y),Transformacoes2D.escala(a,sy),Transformacoes2D.translacao(x,y)};
            }
            case 11: {
                double x = numero(1), y = numero(2);
                return new double[][][] {Transformacoes2D.translacao(-x,-y),Transformacoes2D.rotacao(a),Transformacoes2D.translacao(x,y)};
            }
            case 12: return Transformacoes2D.etapasReflexaoReta(a,numero(1));
            default: throw new IllegalArgumentException("Operação desconhecida.");
        }
    }

    /**
     * Aplica uma operação validada e registra matrizes, pontos intermediários e ordem da composição.
     */
    private void aplicar() {
        try {
            double[][][] etapas = etapas();
            double[][] m = Transformacoes2D.compor(etapas);
            StringBuilder nome = new StringBuilder(String.valueOf(operacao.getSelectedItem()));
            for (int i = 0; i < campos.length; i++) if (campos[i].isVisible())
                nome.append(" | ").append(rotulos[i].getText()).append("=").append(campos[i].getText());
            StringBuilder texto = new StringBuilder(nome+"\n\nEtapas em ordem de aplicação (vetores-coluna):\n");
            if (operacao.getSelectedIndex() == 10 || operacao.getSelectedIndex() == 11) texto.append("1. Levar pivô à origem; 2. transformar; 3. retornar ao pivô.\n");
            if (operacao.getSelectedIndex() == 12) texto.append("1. T(0,-b); 2. R(-atan(m)); 3. refletir em X; 4. R(atan(m)); 5. T(0,b).\n");
            double[][] pontos = estado.getPontos();
            texto.append("P antes:\n").append(formatar(pontos));
            for (int i = 0; i < etapas.length; i++) {
                pontos = MatrizUtils.multiplicar(etapas[i],pontos);
                texto.append("\nE").append(i+1).append(" =\n").append(formatar(etapas[i]));
                texto.append("P após E").append(i+1).append(" =\n").append(formatar(pontos));
            }
            texto.append("\nM desta operação = E").append(etapas.length);
            for (int i = etapas.length-1; i >= 1; i--) texto.append(" · E").append(i);
            texto.append("\n").append(formatar(m));
            estado.aplicar(m,nome.toString());
            detalhes.add(texto.toString()); atualizar();
        } catch (IllegalArgumentException erro) {
            status.setText("Operação não aplicada: " + erro.getMessage());
            JOptionPane.showMessageDialog(this,erro.getMessage(),"Confira os parâmetros",JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Atualiza a inspeção e o canvas a partir do mesmo estado matemático.
     */
    private void atualizar() {
        List<String> nomes = estado.getHistorico();
        String prefixo = detalhes.isEmpty()?"Estado inicial: matriz identidade. A = (0,0).\n":detalhes.get(detalhes.size()-1);
        contas.setText(prefixo+"\nComposição acumulada C (nova operação multiplica à esquerda):\n"+formatar(estado.getMatriz())+"\nP atual = C · P original\n"+formatar(estado.getPontos()));
        double[][] antes = estado.getOriginal(), depois = estado.getPontos();
        StringBuilder tabela = new StringBuilder("Vértice       Original (x ; y)             Atual (x ; y)\n\n");
        for (int i = 0; i < antes[0].length; i++) tabela.append(String.format(Locale.ROOT,
            "%c        (%9.4f ; %9.4f)     (%9.4f ; %9.4f)%n",'A'+i,antes[0][i],antes[1][i],depois[0][i],depois[1][i]));
        vertices.setText(tabela+"\nValores exibidos arredondados; cálculos preservam double.\nCada coluna de P representa um vértice (x,y,1).");
        StringBuilder lista = new StringBuilder("ORDEM DE APLICAÇÃO\n\n");
        for (int i = 0; i < nomes.size(); i++) lista.append("M").append(i+1).append(": ").append(nomes.get(i)).append('\n');
        lista.append("\nC = ");
        if (nomes.isEmpty()) lista.append("I");
        for (int i = nomes.size(); i > 0; i--) lista.append("M").append(i).append(i>1?" · ":"");
        lista.append("\nP atual = C · P original\n\nA ordem altera o resultado em geral.\nDesfazer restaura a composição anterior, sem inversão.");
        historico.setText(lista.toString());
        contas.setCaretPosition(0); vertices.setCaretPosition(0); historico.setCaretPosition(0);
        desfazer.setEnabled(!nomes.isEmpty());
        status.setText(nomes.size()+" operação(ões) • Arestas: Ponto Médio • Coordenadas homogêneas • A inicial = (0,0)");
        canvas.repaint();
    }

    /**
     * Formata matrizes somente para leitura, sem alterar os valores usados nas contas.
     * @param a matriz à esquerda
     * @return representação textual da matriz
     */
    private static String formatar(double[][] a) {
        StringBuilder texto = new StringBuilder();
        for (double[] linha : a) {
            texto.append("[ ");
            for (double valor : linha) texto.append(String.format(Locale.ROOT,"%11.5f ",Math.abs(valor)<0.0000001?0:valor));
            texto.append("]\n");
        }
        return texto.toString();
    }

    /**
     * Cria uma área de inspeção monoespaçada e não editável.
     * @return área de texto configurada
     */
    private static JTextArea area() {
        JTextArea area = new JTextArea(); area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
        area.setMargin(new Insets(12,12,12,12));
        return area;
    }
}
