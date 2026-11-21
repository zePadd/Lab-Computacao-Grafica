package ui;

import model.EstadoTransformacoes;
import model.Poligono;
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
    private static final int PERSONALIZADO = 3;
    private final JComboBox<String> forma = new JComboBox<>(new String[] {
            "Triângulo equilátero", "Quadrado", "Seta côncava", "Personalizado (definir vértices)"});
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
    private final JPanel personalizado = new JPanel();
    private final JTextField verticeX = new JTextField(), verticeY = new JTextField();
    private final DefaultListModel<String> modeloVertices = new DefaultListModel<>();
    private final JList<String> listaVertices = new JList<>(modeloVertices);
    private final JButton criarPoligono = new JButton("Criar polígono");
    private final JButton removerVertice = new JButton("Remover último");
    private final JButton limparVertices = new JButton("Limpar");
    private final JLabel avisoVertices = new JLabel();
    private final List<double[]> verticesPersonalizados = new ArrayList<>();
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
        forma.addActionListener(evento -> trocarForma());
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
        lateral.add(new JLabel("POLÍGONO")); lateral.add(Box.createVerticalStrut(6)); lateral.add(forma);
        montarPersonalizado();
        lateral.add(personalizado);
        lateral.add(Box.createVerticalStrut(22)); lateral.add(new JLabel("TRANSFORMAÇÃO"));
        lateral.add(Box.createVerticalStrut(6)); lateral.add(operacao);
        for (int i = 0; i < 4; i++) {
            entradas.add(rotulos[i]); entradas.add(campos[i]); campos[i].setName("parametro"+i);
            rotulos[i].setLabelFor(campos[i]); campos[i].addActionListener(evento -> aplicar());
            campos[i].getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { atualizarReta(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { atualizarReta(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { atualizarReta(); }
            });
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

    // <-------------------POLÍGONO DEFINIDO PELO USUÁRIO------------------->
    /**
     * Monta o bloco de entrada manual de vértices, visível apenas quando a forma "Personalizado" está selecionada.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     */
    private void montarPersonalizado() {
        personalizado.setLayout(new BoxLayout(personalizado,BoxLayout.Y_AXIS));
        personalizado.setName("personalizado"); personalizado.setOpaque(false); personalizado.setVisible(false);
        // A margem superior fica no proprio componente: escondido, ele nao desloca os controles seguintes.
        personalizado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10,0,0,0),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1,1,1,1,new Color(0xC6D2E0)),
                        BorderFactory.createEmptyBorder(10,10,10,10))));
        JLabel titulo = new JLabel("VÉRTICES (x ; y)");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD,12f)); titulo.setForeground(new Color(0x46566D));
        JPanel coordenadas = new JPanel(new GridLayout(0,2,8,8)); coordenadas.setOpaque(false);
        JLabel rotuloX = new JLabel("X"), rotuloY = new JLabel("Y");
        rotuloX.setLabelFor(verticeX); rotuloY.setLabelFor(verticeY);
        verticeX.setName("verticeX"); verticeY.setName("verticeY");
        verticeX.addActionListener(evento -> verticeY.requestFocusInWindow());
        verticeY.addActionListener(evento -> adicionarVertice());
        coordenadas.add(rotuloX); coordenadas.add(verticeX); coordenadas.add(rotuloY); coordenadas.add(verticeY);
        coordenadas.setMaximumSize(new Dimension(Integer.MAX_VALUE,76));
        JButton adicionar = new JButton("Adicionar vértice"); adicionar.setName("adicionarVertice");
        adicionar.addActionListener(evento -> adicionarVertice());
        listaVertices.setName("listaVertices"); listaVertices.setVisibleRowCount(4);
        listaVertices.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
        listaVertices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane rolagemVertices = new JScrollPane(listaVertices);
        rolagemVertices.setPreferredSize(new Dimension(220,92));
        rolagemVertices.setMaximumSize(new Dimension(Integer.MAX_VALUE,92));
        JPanel edicao = new JPanel(new GridLayout(1,2,6,0)); edicao.setOpaque(false);
        removerVertice.setName("removerVertice"); limparVertices.setName("limparVertices");
        removerVertice.addActionListener(evento -> {
            if (!verticesPersonalizados.isEmpty()) verticesPersonalizados.remove(verticesPersonalizados.size()-1);
            recarregarVertices();
        });
        limparVertices.addActionListener(evento -> { verticesPersonalizados.clear(); recarregarVertices(); });
        edicao.add(removerVertice); edicao.add(limparVertices);
        edicao.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
        criarPoligono.setName("criarPoligono");
        criarPoligono.setBackground(new Color(0x007F87)); criarPoligono.setForeground(Color.WHITE);
        criarPoligono.addActionListener(evento -> criarPersonalizado());
        avisoVertices.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,11)); avisoVertices.setForeground(new Color(0x637187));
        for (JComponent c : new JComponent[] {adicionar,criarPoligono}) c.setMaximumSize(new Dimension(Integer.MAX_VALUE,34));
        personalizado.add(titulo); personalizado.add(Box.createVerticalStrut(8)); personalizado.add(coordenadas);
        personalizado.add(Box.createVerticalStrut(8)); personalizado.add(adicionar);
        personalizado.add(Box.createVerticalStrut(8)); personalizado.add(rolagemVertices);
        personalizado.add(Box.createVerticalStrut(6)); personalizado.add(edicao);
        personalizado.add(Box.createVerticalStrut(10)); personalizado.add(criarPoligono);
        personalizado.add(Box.createVerticalStrut(6)); personalizado.add(avisoVertices);
        for (Component filho : personalizado.getComponents())
            if (filho instanceof JComponent) ((JComponent)filho).setAlignmentX(Component.LEFT_ALIGNMENT);
        recarregarVertices();
    }

    /**
     * Mostra ou esconde a entrada manual e troca a forma em uso conforme a opção escolhida.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     */
    private void trocarForma() {
        boolean manual = forma.getSelectedIndex() == PERSONALIZADO;
        personalizado.setVisible(manual);
        revalidate(); repaint();
        if (!manual) { estado.selecionar(forma.getSelectedIndex()); detalhes.clear(); atualizar(); return; }
        if (verticesPersonalizados.size() >= 3) criarPersonalizado();
        else { verticeX.requestFocusInWindow(); status.setText("Informe ao menos 3 vértices e clique em Criar polígono."); }
    }

    /**
     * Lê o par digitado, acrescenta um vértice à lista e devolve o foco ao campo X.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     */
    private void adicionarVertice() {
        try {
            double x = coordenada(verticeX,"X"), y = coordenada(verticeY,"Y");
            verticesPersonalizados.add(new double[] {x,y});
            verticeX.setText(""); verticeY.setText(""); verticeX.requestFocusInWindow();
            recarregarVertices();
            status.setText(verticesPersonalizados.size()+" vértice(s) informado(s). Mínimo de 3 para criar o polígono.");
        } catch (IllegalArgumentException erro) { avisar(erro.getMessage(),"Confira o vértice"); }
    }

    /**
     * Reconstrói a lista exibida, renomeia os vértices em ordem e libera os botões conforme a quantidade.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     */
    private void recarregarVertices() {
        modeloVertices.clear();
        for (int i = 0; i < verticesPersonalizados.size(); i++) {
            double[] v = verticesPersonalizados.get(i);
            modeloVertices.addElement(String.format(Locale.ROOT,"%c   (%s ; %s)",(char)('A'+i),decimal(v[0]),decimal(v[1])));
        }
        int total = verticesPersonalizados.size();
        criarPoligono.setEnabled(total >= 3);
        removerVertice.setEnabled(total > 0); limparVertices.setEnabled(total > 0);
        avisoVertices.setText(total >= 3 ? "<html>" + total + " vértices informados. Arestas na ordem A, B, C...</html>"
                : "<html>Faltam " + (3-total) + " vértice(s) para o mínimo de 3.</html>");
    }

    /**
     * Monta a matriz homogênea 3 x N com os vértices digitados e a entrega ao estado como forma original.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     */
    private void criarPersonalizado() {
        int total = verticesPersonalizados.size();
        double[][] pontos = new double[3][total];
        for (int i = 0; i < total; i++) {
            pontos[0][i] = verticesPersonalizados.get(i)[0];
            pontos[1][i] = verticesPersonalizados.get(i)[1];
            pontos[2][i] = 1;
        }
        try {
            estado.selecionar(new Poligono("Personalizado",pontos));
            detalhes.clear(); atualizar();
            status.setText("Polígono personalizado com " + total + " vértices. Histórico reiniciado.");
        } catch (IllegalArgumentException erro) { avisar(erro.getMessage(),"Confira os vértices"); }
    }

    /**
     * Lê uma coordenada decimal finita dentro do limite didático, aceitando vírgula como separador.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     * @param campo campo de texto com o valor digitado
     * @param rotulo nome do eixo exibido em caso de erro
     * @return coordenada convertida para double
     * @throws IllegalArgumentException se os parâmetros violarem as condições descritas
     */
    private static double coordenada(JTextField campo, String rotulo) {
        double n;
        try { n = Double.parseDouble(campo.getText().trim().replace(',','.')); }
        catch (NumberFormatException erro) { throw new IllegalArgumentException("Informe um número em " + rotulo + "."); }
        if (!Double.isFinite(n) || Math.abs(n) > 10000) throw new IllegalArgumentException("Coordenadas devem estar entre -10.000 e 10.000.");
        return n;
    }

    /**
     * Escreve um valor sem zeros decimais supérfluos, apenas para leitura na lista de vértices.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     * @param v valor a formatar
     * @return texto compacto do valor
     */
    private static String decimal(double v) {
        String t = String.format(Locale.ROOT,"%.4f",v);
        return t.contains(".") ? t.replaceAll("0+$","").replaceAll("\\.$","") : t;
    }

    /**
     * Informa um problema de entrada na barra de status e em uma caixa de diálogo.
     *
     * @author Suelle Maciel
     * @date 20/11/2026
     * @param mensagem explicação do problema encontrado
     * @param titulo título da caixa de diálogo
     */
    private void avisar(String mensagem, String titulo) {
        status.setText("Operação não aplicada: " + mensagem);
        JOptionPane.showMessageDialog(this,mensagem,titulo,JOptionPane.WARNING_MESSAGE);
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
        atualizarReta();
    }

    /**
     * Mostra no canvas a reta de reflexão quando a operação é y = x ou y = mx + b; nas demais, oculta.
     * Para y = mx + b a reta acompanha os valores digitados em m e b, antes mesmo de aplicar.
     */
    private void atualizarReta() {
        int tipo = operacao.getSelectedIndex();
        if (tipo == 6) { canvas.mostrarReta(1,0); return; }
        if (tipo == 12) {
            try { canvas.mostrarReta(numero(0),numero(1)); }
            catch (IllegalArgumentException erro) { canvas.ocultarReta(); }
            return;
        }
        canvas.ocultarReta();
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
        } catch (IllegalArgumentException erro) { avisar(erro.getMessage(),"Confira os parâmetros"); }
    }

    /**
     * Atualiza a inspeção e o canvas a partir do mesmo estado matemático.
     */
    private void atualizar() {
        List<String> nomes = estado.getHistorico();
        double[][] antes = estado.getOriginal();
        String inicial = "A = ("+decimal(antes[0][0])+" ; "+decimal(antes[1][0])+")";
        String prefixo = detalhes.isEmpty()?"Estado inicial: matriz identidade. "+inicial+".\n":detalhes.get(detalhes.size()-1);
        contas.setText(prefixo+"\nComposição acumulada C (nova operação multiplica à esquerda):\n"+formatar(estado.getMatriz())+"\nP atual = C · P original\n"+formatar(estado.getPontos()));
        double[][] depois = estado.getPontos();
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
        status.setText(nomes.size()+" operação(ões) • "+antes[0].length+" vértices • Arestas: Ponto Médio • Coordenadas homogêneas • "+inicial+" na origem do histórico");
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
