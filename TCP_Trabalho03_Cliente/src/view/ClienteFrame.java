package view;

import javax.swing.*;
import java.awt.*;
import javax.swing.SwingUtilities;
import java.io.File;
import java.util.List;
import cliente.ClienteTCP;

public class ClienteFrame extends JFrame {
    private ClienteTCP cliente;
    private JTextArea areaChat;
    private JTextArea areaLog;
    private JList<String> listaArquivos;
    private JList<String> listaClientes;
    private DefaultListModel<String> modeloArquivos;
    private DefaultListModel<String> modeloClientes;
    private JTextField fieldIP, fieldPorta, fieldNome, fieldMensagem;
    private JButton btnConectar, btnEnviarMsg, btnUpload, btnDownload, btnDeletar;
    private JProgressBar progressBarDownload, progressBarUpload;
    private JLabel labelStatus;
    private JLabel labelDownload, labelUpload;

    public ClienteFrame() {
        super("Cliente de Chat e Arquivos - Lucas Eufrásio e Rodrian");
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        inicia_Interface();
        setVisible(true);
    }

    private void inicia_Interface() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel de Conexão
        JPanel painelConexao = new JPanel(new FlowLayout());
        painelConexao.setBorder(BorderFactory.createTitledBorder("🔗 Conexão"));

        fieldIP = new JTextField("127.0.0.1", 12);
        fieldPorta = new JTextField("1500", 5);
        fieldNome = new JTextField("Cliente", 10);
        btnConectar = new JButton("Conectar");
        labelStatus = new JLabel("Status: Desconectado");
        labelStatus.setForeground(Color.RED);

        painelConexao.add(new JLabel("IP:"));
        painelConexao.add(fieldIP);
        painelConexao.add(new JLabel("Porta:"));
        painelConexao.add(fieldPorta);
        painelConexao.add(new JLabel("Nome:"));
        painelConexao.add(fieldNome);
        painelConexao.add(btnConectar);
        painelConexao.add(labelStatus);

        // Painel Principal
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));

        // Painel do Chat
        JPanel painelChat = new JPanel(new BorderLayout());
        painelChat.setBorder(BorderFactory.createTitledBorder("💬 Chat em Grupo"));

        areaChat = new JTextArea(20, 40);
        areaChat.setEditable(false);
        areaChat.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane scrollChat = new JScrollPane(areaChat);

        JPanel painelEnvioMsg = new JPanel(new BorderLayout());
        fieldMensagem = new JTextField();
        btnEnviarMsg = new JButton("Enviar");
        btnEnviarMsg.setEnabled(false);

        painelEnvioMsg.add(fieldMensagem, BorderLayout.CENTER);
        painelEnvioMsg.add(btnEnviarMsg, BorderLayout.EAST);

        painelChat.add(scrollChat, BorderLayout.CENTER);
        painelChat.add(painelEnvioMsg, BorderLayout.SOUTH);

        // Painel de Informações
        JPanel painelInformacoes = new JPanel(new GridLayout(3, 1, 10, 10));

        // Painel de Clientes
        JPanel painelClientes = new JPanel(new BorderLayout());
        painelClientes.setBorder(BorderFactory.createTitledBorder("👥 Clientes Conectados"));
        modeloClientes = new DefaultListModel<>();
        listaClientes = new JList<>(modeloClientes);
        painelClientes.add(new JScrollPane(listaClientes), BorderLayout.CENTER);

        // Painel de Arquivos
        JPanel painelArquivos = new JPanel(new BorderLayout());
        painelArquivos.setBorder(BorderFactory.createTitledBorder("📁 Arquivos Compartilhados"));
        modeloArquivos = new DefaultListModel<>();
        listaArquivos = new JList<>(modeloArquivos);
        JScrollPane scrollArquivos = new JScrollPane(listaArquivos);

        JPanel painelBotoesArquivos = new JPanel(new FlowLayout());
        btnUpload = new JButton("📤 Upload");
        btnDownload = new JButton("📥 Download");
        btnDeletar = new JButton("🗑 Deletar");
        btnUpload.setEnabled(false);
        btnDownload.setEnabled(false);
        btnDeletar.setEnabled(false);

        painelBotoesArquivos.add(btnUpload);
        painelBotoesArquivos.add(btnDownload);
        painelBotoesArquivos.add(btnDeletar);

        painelArquivos.add(scrollArquivos, BorderLayout.CENTER);
        painelArquivos.add(painelBotoesArquivos, BorderLayout.SOUTH);

        // Painel de Progresso
        JPanel painelProgresso = new JPanel(new GridLayout(2, 1, 10, 10));
        painelProgresso.setBorder(BorderFactory.createTitledBorder("📊 Progresso"));

        // Progresso Download
        JPanel painelProgressoDownload = new JPanel(new BorderLayout(5, 5));
        labelDownload = new JLabel("Download: Aguardando...");
        progressBarDownload = new JProgressBar(0, 100);
        progressBarDownload.setStringPainted(true);
        progressBarDownload.setString("0%");
        progressBarDownload.setPreferredSize(new Dimension(300, 20));
        progressBarDownload.setForeground(new Color(0, 100, 200));
        
        painelProgressoDownload.add(labelDownload, BorderLayout.NORTH);
        painelProgressoDownload.add(progressBarDownload, BorderLayout.CENTER);

        // Progresso Upload
        JPanel painelProgressoUpload = new JPanel(new BorderLayout(5, 5));
        labelUpload = new JLabel("Upload: Aguardando...");
        progressBarUpload = new JProgressBar(0, 100);
        progressBarUpload.setStringPainted(true);
        progressBarUpload.setString("0%");
        progressBarUpload.setPreferredSize(new Dimension(300, 20));
        progressBarUpload.setForeground(new Color(50, 150, 50));
        
        painelProgressoUpload.add(labelUpload, BorderLayout.NORTH);
        painelProgressoUpload.add(progressBarUpload, BorderLayout.CENTER);

        painelProgresso.add(painelProgressoDownload);
        painelProgresso.add(painelProgressoUpload);

        painelInformacoes.add(painelClientes);
        painelInformacoes.add(painelArquivos);
        painelInformacoes.add(painelProgresso);

        // Painel de Log
        JPanel painelLog = new JPanel(new BorderLayout());
        painelLog.setBorder(BorderFactory.createTitledBorder("📋 Log do Sistema"));
        areaLog = new JTextArea(6, 60);
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(areaLog);
        painelLog.add(scrollLog, BorderLayout.CENTER);

        // Layout Principal
        painelPrincipal.add(painelChat, BorderLayout.CENTER);
        painelPrincipal.add(painelInformacoes, BorderLayout.EAST);

        setLayout(new BorderLayout(10, 10));
        add(painelConexao, BorderLayout.NORTH);
        add(painelPrincipal, BorderLayout.CENTER);
        add(painelLog, BorderLayout.SOUTH);

        // Action Listeners
        btnConectar.addActionListener(e -> conectarDesconectar());
        btnEnviarMsg.addActionListener(e -> enviarMensagem());
        fieldMensagem.addActionListener(e -> enviarMensagem());
        btnUpload.addActionListener(e -> fazerUpload());
        btnDownload.addActionListener(e -> fazerDownload());
        btnDeletar.addActionListener(e -> deletarArquivo());

        // Listener para seleção de arquivos
        listaArquivos.addListSelectionListener(e -> {
            boolean arquivoSelecionado = listaArquivos.getSelectedValue() != null;
            btnDownload.setEnabled(arquivoSelecionado);
            btnDeletar.setEnabled(arquivoSelecionado);
        });

        log("Sistema inicializado. Conecte-se ao servidor.");
        limparBarrasProgresso();
    }

    private void conectarDesconectar() {
        if (cliente != null && cliente.isConectado()) {
            desconectarServidor();
        } else {
            conectarServidor();
        }
    }

    private void conectarServidor() {
        String ip = fieldIP.getText();
        int porta;
        
        try {
            porta = Integer.parseInt(fieldPorta.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Porta inválida", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nome = fieldNome.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite um nome para conectar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                atualizarProgressoUpload(0, "Conectando ao servidor...");
                
                cliente = new ClienteTCP(ip, porta, nome, this);
                if (cliente.conectar()) {
                    SwingUtilities.invokeLater(() -> {
                        btnConectar.setText("Desconectar");
                        btnEnviarMsg.setEnabled(true);
                        btnUpload.setEnabled(true);
                        fieldIP.setEnabled(false);
                        fieldPorta.setEnabled(false);
                        fieldNome.setEnabled(false);
                        atualizarStatus("Conectado como: " + nome, Color.GREEN);
                    });
                    
                    // Solicita atualização das listas
                    cliente.atualizarListas();
                    atualizarProgressoUpload(100, "Conectado com sucesso");
                } else {
                    atualizarProgressoUpload(0, "Falha na conexão");
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    log("Erro na conexão: " + e.getMessage());
                    atualizarStatus("Erro na conexão", Color.RED);
                    atualizarProgressoUpload(0, "Erro na conexão");
                });
            }
        }).start();
    }

    private void desconectarServidor() {
        if (cliente != null) {
            cliente.desconectar();
        }
        SwingUtilities.invokeLater(() -> {
            btnConectar.setText("Conectar");
            btnEnviarMsg.setEnabled(false);
            btnUpload.setEnabled(false);
            btnDownload.setEnabled(false);
            btnDeletar.setEnabled(false);
            fieldIP.setEnabled(true);
            fieldPorta.setEnabled(true);
            fieldNome.setEnabled(true);
            modeloClientes.clear();
            modeloArquivos.clear();
            areaChat.setText("");
            atualizarStatus("Desconectado", Color.RED);
            limparBarrasProgresso();
            log("Desconectado do servidor");
        });
    }

    private void enviarMensagem() {
        String mensagem = fieldMensagem.getText().trim();
        if (!mensagem.isEmpty() && cliente != null && cliente.isConectado()) {
            cliente.enviarMensagem(mensagem);
            fieldMensagem.setText("");
        }
    }

    private void fazerUpload() {
        if (cliente == null || !cliente.isConectado()) {
            log("Não conectado ao servidor");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecionar arquivo para upload");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = chooser.getSelectedFile();
            new Thread(() -> {
                try {
                    cliente.enviarArquivo(arquivo);
                } catch (Exception e) {
                    log("Erro no upload: " + e.getMessage());
                    atualizarProgressoUpload(0, "Erro no upload");
                }
            }).start();
        }
    }

    private void fazerDownload() {
        if (cliente == null || !cliente.isConectado()) {
            log("Não conectado ao servidor");
            return;
        }

        String nomeArquivo = listaArquivos.getSelectedValue();
        if (nomeArquivo == null) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um arquivo para download", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        log("Download: " + nomeArquivo);
        cliente.baixarArquivo(nomeArquivo);
    }

    private void deletarArquivo() {
        if (cliente == null || !cliente.isConectado()) {
            log("Não conectado ao servidor");
            return;
        }

        String nomeArquivo = listaArquivos.getSelectedValue();
        if (nomeArquivo == null) {
            JOptionPane.showMessageDialog(this, 
                "Selecione um arquivo para deletar", 
                "Aviso", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacao = JOptionPane.showConfirmDialog(this,
            " CONFIRMAR EXCLUSÃO\n\n" +
            " Arquivo: " + nomeArquivo + "\n\n" ,
            "Confirmar Exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacao == JOptionPane.YES_OPTION) {
            log("🗑️ Solicitando exclusão: " + nomeArquivo);
            cliente.deletarArquivo(nomeArquivo);
        } else {
            log("Exclusão cancelada pelo usuário");
        }
    }

    private String extrairNomeArquivo(String nomeCompleto) {
        if (nomeCompleto == null) return "";
        
        String[] partes = nomeCompleto.split("\\(");
        if (partes.length > 0) {
            return partes[0].trim();
        }
        return nomeCompleto;
    }

    public void adicionarMensagemChat(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            areaChat.append(mensagem + "\n");
            areaChat.setCaretPosition(areaChat.getDocument().getLength());
        });
    }

    public void atualizarListaClientes(List<String> clientes) {
        SwingUtilities.invokeLater(() -> {
            modeloClientes.clear();
            for (String cliente : clientes) {
                modeloClientes.addElement(cliente);
            }
        });
    }

    public void atualizarListaArquivos(List<String> arquivos) {
        SwingUtilities.invokeLater(() -> {
            modeloArquivos.clear();
            for (String arquivo : arquivos) {
                modeloArquivos.addElement(arquivo);
            }
            
            // Atualizar estado dos botões baseado na seleção
            boolean arquivoSelecionado = listaArquivos.getSelectedValue() != null;
            btnDeletar.setEnabled(arquivoSelecionado);
            btnDownload.setEnabled(arquivoSelecionado);
        });
    }

    // Métodos de Progresso
    public void atualizarProgressoUpload(int progresso, String texto) {
        SwingUtilities.invokeLater(() -> {
            progressBarUpload.setValue(progresso);
            progressBarUpload.setString(progresso + "%");
            labelUpload.setText("Upload: " + texto);
            
            // Mudar cor baseada no progresso
            if (progresso < 30) {
                progressBarUpload.setForeground(new Color(200, 50, 50)); // Vermelho
            } else if (progresso < 70) {
                progressBarUpload.setForeground(new Color(200, 150, 0)); // Laranja
            } else {
                progressBarUpload.setForeground(new Color(50, 150, 50)); // Verde
            }
        });
    }

    public void atualizarProgressoDownload(int progresso, String texto) {
        SwingUtilities.invokeLater(() -> {
            progressBarDownload.setValue(progresso);
            progressBarDownload.setString(progresso + "%");
            labelDownload.setText("Download: " + texto);
            
            // Mudar cor baseada no progresso
            if (progresso < 30) {
                progressBarDownload.setForeground(new Color(200, 50, 50)); // Vermelho
            } else if (progresso < 70) {
                progressBarDownload.setForeground(new Color(200, 150, 0)); // Laranja
            } else {
                progressBarDownload.setForeground(new Color(0, 100, 200)); // Azul
            }
        });
    }

    private void limparBarrasProgresso() {
        SwingUtilities.invokeLater(() -> {
            progressBarUpload.setValue(0);
            progressBarUpload.setString("0%");
            progressBarUpload.setForeground(new Color(50, 150, 50));
            labelUpload.setText("Upload: Aguardando...");
            
            progressBarDownload.setValue(0);
            progressBarDownload.setString("0%");
            progressBarDownload.setForeground(new Color(0, 100, 200));
            labelDownload.setText("Download: Aguardando...");
        });
    }

    public void log(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            areaLog.append("[" + timestamp + "] " + mensagem + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    public void atualizarStatus(String status, Color cor) {
        SwingUtilities.invokeLater(() -> {
            labelStatus.setText(status);
            labelStatus.setForeground(cor);
        });
    }

    public void forcarDesconexao() {
        desconectarServidor();
        log("Conexão com o servidor foi perdida");
        JOptionPane.showMessageDialog(this, 
            "Conexão com o servidor foi perdida.", 
            "Conexão Perdida", 
            JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClienteFrame();
        });
    }
}