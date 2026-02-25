package cliente;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Base64;
import javax.swing.*;
import view.ClienteFrame;

public class ClienteTCP {
    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream saida;
    private String nomeCliente;
    private String serverIP;
    private int serverPort;
    private boolean conectado;
    private ClienteFrame frame;
    private Thread threadRecebimento;
    
    public ClienteTCP(String serverIP, int serverPort, String nomeCliente, ClienteFrame frame) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.nomeCliente = nomeCliente;
        this.frame = frame;
        this.conectado = false;
    }
    
    public boolean conectar() {
        try {
            // Configurar timeout de conexão
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIP, serverPort), 5000); // 5 segundos timeout
            
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());
            
            // Enviar nome para conexão
            saida.writeUTF("CONECTAR|" + nomeCliente);
            
            // Aguardar resposta com timeout
            socket.setSoTimeout(5000); // Timeout de 5 segundos para leitura
            String resposta = entrada.readUTF();
            
            // Remover timeout após conexão estabelecida
            socket.setSoTimeout(0);
            
            if (resposta.equals("CONEXAO_OK") || resposta.startsWith("CONEXAO_OK")) {
                conectado = true;
                
                // Iniciar thread de recebimento
                threadRecebimento = new Thread(new Runnable() {
                    public void run() {
                        receberMensagens();
                    }
                });
                threadRecebimento.setName("Thread-Recebimento-Cliente");
                threadRecebimento.start();
                
                return true;
            } else {
                String erroMsg = resposta.length() > 13 ? resposta.substring(13) : resposta;
                SwingUtilities.invokeLater(() -> {
                    frame.log("Conexão recusada: " + erroMsg);
                });
                desconectar();
                return false;
            }
            
        } catch (SocketTimeoutException e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Timeout: Servidor não respondeu");
            });
            desconectar();
            return false;
        } catch (ConnectException e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Não foi possível conectar ao servidor: " + e.getMessage());
            });
            return false;
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro conectando: " + e.getMessage());
            });
            desconectar();
            return false;
        }
    }
    
    private void receberMensagens() {
        try {
            while (conectado) {
                String mensagem = entrada.readUTF();
                processarMensagem(mensagem);
            }
        } catch (SocketTimeoutException e) {
            // Timeout é normal, continuar ouvindo
        } catch (EOFException e) {
            // Conexão foi fechada normalmente
        } catch (SocketException e) {
            if (conectado) {
                SwingUtilities.invokeLater(() -> {
                    frame.log("Conexão perdida: " + e.getMessage());
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro recebendo mensagem: " + e.getMessage());
            });
        } finally {
            if (conectado) {
                SwingUtilities.invokeLater(() -> {
                    frame.forcarDesconexao();
                });
            }
        }
    }
    
    private void processarMensagem(String mensagem) {
        try {
            if (mensagem.startsWith("CHAT|")) {
                String msgChat = mensagem.substring(5);
                SwingUtilities.invokeLater(() -> {
                    frame.adicionarMensagemChat(msgChat);
                });
                
            } else if (mensagem.startsWith("CLIENTES|")) {
                String[] partes = mensagem.split("\\|");
                java.util.List<String> clientes = new java.util.ArrayList<>();
                for (int i = 1; i < partes.length; i++) {
                    if (!partes[i].isEmpty()) {
                        clientes.add(partes[i]);
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    frame.atualizarListaClientes(clientes);
                });
                
            } else if (mensagem.startsWith("ARQUIVOS|")) {
                String[] partes = mensagem.split("\\|");
                java.util.List<String> arquivos = new java.util.ArrayList<>();
                for (int i = 1; i < partes.length; i++) {
                    if (!partes[i].isEmpty() && !partes[i].equals("VAZIA")) {
                        arquivos.add(partes[i]);
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    frame.atualizarListaArquivos(arquivos);
                });
                
            } else if (mensagem.startsWith("DOWNLOAD|")) {
                processarDownload(mensagem);
                
            } else if (mensagem.startsWith("UPLOAD_OK|")) {
                String nomeArquivo = mensagem.substring(10);
                SwingUtilities.invokeLater(() -> {
                    frame.atualizarProgressoUpload(100, "Upload concluído");
                    frame.log("Upload realizado: " + nomeArquivo);
                });
                
            } else if (mensagem.startsWith("DELETE_OK|")) {
                String nomeArquivo = mensagem.substring(10);
                SwingUtilities.invokeLater(() -> {
                    frame.log("Arquivo deletado: " + nomeArquivo);
                });
                atualizarListas();
                
            } else if (mensagem.startsWith("ERRO|")) {
                String erro = mensagem.substring(5);
                SwingUtilities.invokeLater(() -> {
                    frame.log("Erro do servidor: " + erro);
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro processando mensagem: " + e.getMessage());
            });
        }
    }
    
    private void processarDownload(String mensagem) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    String[] partes = mensagem.split("\\|", 3);
                    if (partes.length < 3) {
                        throw new Exception("Formato de download inválido");
                    }
                    
                    String nomeArquivo = partes[1];
                    String dadosBase64 = partes[2];
                    
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoDownload(50, "Baixando...");
                    });
                    
                    byte[] dados = Base64.getDecoder().decode(dadosBase64);
                    
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoDownload(75, "Salvando...");
                        
                        File pastaDownloads = new File("download_cliente");
                        if (!pastaDownloads.exists()) {
                            pastaDownloads.mkdirs();
                        }
                        
                        File arquivo = new File(pastaDownloads, nomeArquivo);
                        
                        if (arquivo.exists()) {
                            String nomeBase = nomeArquivo.substring(0, nomeArquivo.lastIndexOf('.'));
                            String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf('.'));
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            arquivo = new File(pastaDownloads, nomeBase + "_" + timestamp + extensao);
                        }
                        
                        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
                            fos.write(dados);
                            frame.atualizarProgressoDownload(100, "Download concluído");
                            frame.log("Download salvo: " + arquivo.getName() + " em " + pastaDownloads.getName() + "/");
                        } catch (Exception e) {
                            frame.atualizarProgressoDownload(0, "Erro ao salvar");
                            frame.log("Erro salvando arquivo: " + e.getMessage());
                        }
                    });
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoDownload(0, "Erro no download");
                        frame.log("Erro processando download: " + e.getMessage());
                    });
                }
            }
        }).start();
    }
    
    public void enviarMensagem(String mensagem) {
        if (!conectado || saida == null) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Não conectado ao servidor");
            });
            return;
        }
        
        try {
            saida.writeUTF("MENSAGEM|" + mensagem);
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro enviando mensagem: " + e.getMessage());
            });
        }
    }
    
    public void enviarArquivo(File arquivo) {
        if (!conectado || saida == null) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Não conectado ao servidor");
            });
            return;
        }
        
        new Thread(new Runnable() {
            public void run() {
                try {
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoUpload(0, "Preparando envio...");
                    });
                    
                    byte[] dados = Files.readAllBytes(arquivo.toPath());
                    String dadosBase64 = Base64.getEncoder().encodeToString(dados);
                    
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoUpload(50, "Enviando...");
                    });
                    
                    String comando = "UPLOAD|" + arquivo.getName() + "|" + arquivo.length() + "|" + dadosBase64;
                    saida.writeUTF(comando);
                    
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoUpload(75, "Aguardando confirmação...");
                    });
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        frame.atualizarProgressoUpload(0, "Erro no upload");
                        frame.log("Erro enviando arquivo: " + e.getMessage());
                    });
                }
            }
        }).start();
    }
    
    public void baixarArquivo(String nomeArquivo) {
        if (!conectado || saida == null) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Não conectado ao servidor");
            });
            return;
        }
        
        try {
            SwingUtilities.invokeLater(() -> {
                frame.atualizarProgressoDownload(0, "Solicitando download...");
            });
            
            saida.writeUTF("DOWNLOAD|" + nomeArquivo);
            
            SwingUtilities.invokeLater(() -> {
                frame.atualizarProgressoDownload(25, "Aguardando servidor...");
            });
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro solicitando download: " + e.getMessage());
            });
        }
    }
    
    public void deletarArquivo(String nomeArquivo) {
        if (!conectado || saida == null) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Não conectado ao servidor");
            });
            return;
        }
        
        try {
            saida.writeUTF("DELETE|" + nomeArquivo);
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro solicitando exclusão: " + e.getMessage());
            });
        }
    }
    
    public void atualizarListas() {
        if (!conectado || saida == null) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Não conectado ao servidor");
            });
            return;
        }
        
        try {
            saida.writeUTF("LISTAR_ARQUIVOS");
            saida.writeUTF("LISTAR_CLIENTES");
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                frame.log("Erro atualizando listas: " + e.getMessage());
            });
        }
    }
    
    public void desconectar() {
        conectado = false;
        
        try {
            if (saida != null) {
                saida.writeUTF("DESCONECTAR");
            }
        } catch (Exception e) {
        }
        
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (Exception e) {}
        }
        
        if (threadRecebimento != null && threadRecebimento.isAlive()) {
            threadRecebimento.interrupt();
        }
        
        entrada = null;
        saida = null;
        socket = null;
    }
    
    public boolean isConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
}
