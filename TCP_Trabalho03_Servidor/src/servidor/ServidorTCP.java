package servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import util.GerenciadorLog;
import controller.Chat_Controller;

public class ServidorTCP {
    private ServerSocket serverSocket;
    private GerenciadorLog logger;
    private Chat_Controller arquivosController;
    private static final int PORTA = 1500;
    private boolean executando;
    private ArrayList<Socket> socketsClientes;
    private ArrayList<String> nomesClientes;
    private ArrayList<DataOutputStream> saidasClientes;
    
    public ServidorTCP() throws IOException {
        this.serverSocket = new ServerSocket(PORTA);
        this.logger = new GerenciadorLog("log_servidor.txt");
        this.arquivosController = new Chat_Controller();
        this.executando = true;
        this.socketsClientes = new ArrayList<>();
        this.nomesClientes = new ArrayList<>();
        this.saidasClientes = new ArrayList<>();
    }
    
    public void iniciar() throws UnknownHostException {
        logger.registrar("SERVIDOR_INICIADO", "SISTEMA", 
            "Servidor iniciado - IP: " + InetAddress.getLocalHost().getHostAddress() + " Porta: " + PORTA);
        
        System.out.println("SERVIDOR TCP DE MENSAGENS E ARQUIVOS");
        System.out.println("IP: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("Porta: " + PORTA);
        while (executando) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("✨ Nova conexão: " + socket.getInetAddress().getHostAddress());
                
                Thread threadCliente = new Thread(new Runnable() {
                    public void run() {
                        tratarCliente(socket);
                    }
                });
                threadCliente.start();
                
            } catch (IOException e) {
                if (executando) {
                    System.out.println("Erro aceitando conexão: " + e.getMessage());
                }
            }
        }
    }
    
    private void tratarCliente(Socket socket) {
        String ipCliente = socket.getInetAddress().getHostAddress();
        String nomeCliente = null;
        
        try {
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            
            // Receber nome do cliente
            String dadosConexao = entrada.readUTF();
            if (dadosConexao.startsWith("CONECTAR|")) {
                nomeCliente = dadosConexao.substring(9);
                
                // Verificar se nome já está em uso
                synchronized (nomesClientes) {
                    if (nomesClientes.contains(nomeCliente)) {
                        saida.writeUTF("ERRO_CONEXAO|Nome já está em uso");
                        socket.close();
                        return;
                    }
                    
                    // Registrar cliente
                    socketsClientes.add(socket);
                    nomesClientes.add(nomeCliente);
                    saidasClientes.add(saida);
                }
                
                saida.writeUTF("CONEXAO_OK|" + nomeCliente);
                logger.registrar("CLIENTE_CONECTADO", nomeCliente, "IP: " + ipCliente);
                
                // Notificar todos os clientes
                enviarMensagemBroadcast("SERVIDOR: " + nomeCliente + " entrou no chat!");
                
                // Enviar listas iniciais
                enviarListaClientes();
                enviarListaArquivos();
                
                System.out.println("Cliente registrado: " + nomeCliente);
                
                // Processar comandos do cliente
                while (executando && !socket.isClosed()) {
                    String comando = entrada.readUTF();
                    processarComando(comando, nomeCliente, saida);
                }
            }
            
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + nomeCliente);
        } finally {
            // Remover cliente
            if (nomeCliente != null) {
                synchronized (nomesClientes) {
                    int index = nomesClientes.indexOf(nomeCliente);
                    if (index != -1) {
                        socketsClientes.remove(index);
                        nomesClientes.remove(index);
                        saidasClientes.remove(index);
                    }
                }
                
                // Notificar desconexão
                enviarMensagemBroadcast("SERVIDOR: " + nomeCliente + " saiu do chat");
                enviarListaClientes();
                
                logger.registrar("CLIENTE_DESCONECTADO", nomeCliente, "Desconectado");
                System.out.println("👋 Cliente desconectado: " + nomeCliente);
            }
            
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Erro fechando socket: " + e.getMessage());
            }
        }
    }
    
    private void processarComando(String comando, String nomeCliente, DataOutputStream saida) throws IOException {
        System.out.println("📨 Comando de " + nomeCliente + ": " + comando);
        
        if (comando.startsWith("MENSAGEM|")) {
            String mensagem = comando.substring(9);
            enviarMensagemBroadcast(nomeCliente + ": " + mensagem);
            arquivosController.salvarMensagemChat(nomeCliente, mensagem);
            
        } else if (comando.equals("LISTAR_ARQUIVOS")) {
            enviarListaArquivos();
            
        } else if (comando.startsWith("UPLOAD|")) {
            processarUpload(comando, nomeCliente, saida);
            
        } else if (comando.startsWith("DOWNLOAD|")) {
            processarDownload(comando.substring(9), saida);
            
        } else if (comando.startsWith("DELETE|")) {
            processarDelete(comando.substring(7), nomeCliente, saida);
            
        } else if (comando.equals("ATUALIZAR_LISTAS")) {
            enviarListaClientes();
            enviarListaArquivos();
        }
    }
    
    private void processarUpload(String comando, String nomeCliente, DataOutputStream saida) throws IOException {
        try {
            String[] partes = comando.split("\\|", 4);
            String nomeArquivo = partes[1];
            long tamanho = Long.parseLong(partes[2]);
            String dadosBase64 = partes[3];
            
            byte[] dados = Base64.getDecoder().decode(dadosBase64);
            boolean sucesso = arquivosController.salvarArquivo(nomeArquivo, tamanho, dados);
            
            if (sucesso) {
                saida.writeUTF("UPLOAD_OK|" + nomeArquivo);
                enviarMensagemBroadcast("SERVIDOR: " + nomeCliente + " enviou o arquivo '" + nomeArquivo + "'");
                enviarListaArquivos();
            } else {
                saida.writeUTF("ERRO|Falha no upload");
            }
            
        } catch (Exception e) {
            saida.writeUTF("ERRO|" + e.getMessage());
        }
    }
    
    private void processarDownload(String nomeArquivo, DataOutputStream saida) throws IOException {
        try {
            byte[] dados = arquivosController.buscarArquivo(nomeArquivo);
            if (dados != null) {
                String dadosBase64 = Base64.getEncoder().encodeToString(dados);
                saida.writeUTF("DOWNLOAD|" + nomeArquivo + "|" + dadosBase64);
            } else {
                saida.writeUTF("ERRO|Arquivo não encontrado");
            }
        } catch (Exception e) {
            saida.writeUTF("ERRO|" + e.getMessage());
        }
    }
    
    private void processarDelete(String nomeArquivo, String nomeCliente, DataOutputStream saida) throws IOException {
        boolean sucesso = arquivosController.deletarArquivo(nomeArquivo);
        if (sucesso) {
            saida.writeUTF("DELETE_OK|" + nomeArquivo);
            enviarMensagemBroadcast("SERVIDOR: " + nomeCliente + " deletou o arquivo '" + nomeArquivo + "'");
            enviarListaArquivos();
        } else {
            saida.writeUTF("ERRO|Falha ao deletar");
        }
    }
    
    private void enviarMensagemBroadcast(String mensagem) {
        synchronized (saidasClientes) {
            for (int i = 0; i < saidasClientes.size(); i++) {
                try {
                    saidasClientes.get(i).writeUTF("CHAT|" + mensagem);
                } catch (IOException e) {
                    System.out.println("Erro enviando mensagem para cliente: " + e.getMessage());
                }
            }
        }
    }
    
    private void enviarListaClientes() {
        StringBuilder lista = new StringBuilder("CLIENTES|");
        synchronized (nomesClientes) {
            for (String nome : nomesClientes) {
                lista.append(nome).append("|");
            }
        }
        
        String mensagemLista = lista.toString();
        synchronized (saidasClientes) {
            for (int i = 0; i < saidasClientes.size(); i++) {
                try {
                    saidasClientes.get(i).writeUTF(mensagemLista);
                } catch (IOException e) {
                    System.out.println("Erro enviando lista de clientes: " + e.getMessage());
                }
            }
        }
    }
    
    private void enviarListaArquivos() {
        List<String> arquivos = arquivosController.listarArquivos();
        StringBuilder lista = new StringBuilder("ARQUIVOS|");
        
        if (arquivos.isEmpty()) {
            lista.append("VAZIA");
        } else {
            for (String arquivo : arquivos) {
                lista.append(arquivo).append("|");
            }
        }
        
        String mensagemLista = lista.toString();
        synchronized (saidasClientes) {
            for (int i = 0; i < saidasClientes.size(); i++) {
                try {
                    saidasClientes.get(i).writeUTF(mensagemLista);
                } catch (IOException e) {
                    System.out.println("Erro enviando lista de arquivos: " + e.getMessage());
                }
            }
        }
    }
    
    public void parar() {
        executando = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            
            // Fechar todas as conexões
            synchronized (socketsClientes) {
                for (Socket socket : socketsClientes) {
                    try {
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Erro fechando socket: " + e.getMessage());
                    }
                }
            }
            
            logger.registrar("SERVIDOR_PARADO", "SISTEMA", "Servidor finalizado");
            System.out.println("Servidor parado");
        } catch (IOException e) {
            System.out.println("Erro parando servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ServidorTCP servidor = new ServidorTCP();
            servidor.iniciar();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar servidor: " + e.getMessage());
        }
    }
}