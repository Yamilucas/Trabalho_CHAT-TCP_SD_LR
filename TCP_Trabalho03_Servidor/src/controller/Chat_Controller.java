package controller;

import util.Conexao;
import model.Arquivo;
import model.MensagemChat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Chat_Controller {
    private static final String PASTA_ARQUIVOS = "arquivos_servidor/";
    
    public Chat_Controller() {
        //Cria a pasta dos arquivos do servidor
        new File(PASTA_ARQUIVOS).mkdirs();
    }
    
    //Salva os arquivos no banco
    public boolean salvarArquivo(String nomeArquivo, long tamanho, byte[] dados) {
    Conexao conn = new Conexao();
    
    try {
        // 1. Salvar arquivo físico
        String caminho = PASTA_ARQUIVOS + nomeArquivo;
        FileOutputStream fos = new FileOutputStream(caminho);
        fos.write(dados);
        fos.close();
        
        // 2. Criar objeto model Arquivo
        Arquivo arquivo = new Arquivo();
        arquivo.setNome(nomeArquivo);
        arquivo.setTamanho(tamanho);
        arquivo.setCaminho(caminho);
        
        // 3. Verificar se o arquivo já existe no banco
        conn.conectar();
        
        // Primeiro, verificar se já existe um registro com este nome
        String sqlVerificar = "SELECT id FROM arquivos WHERE nome = ?";
        PreparedStatement stmtVerificar = conn.conector.prepareStatement(sqlVerificar);
        stmtVerificar.setString(1, nomeArquivo);
        ResultSet rs = stmtVerificar.executeQuery();
        
        int resultado;
        if (rs.next()) {
            // Se já existe, fazer UPDATE
            String sql = "UPDATE arquivos SET tamanho = ?, caminho = ?, data_upload = CURRENT_TIMESTAMP WHERE nome = ?";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            stmt.setLong(1, arquivo.getTamanho());
            stmt.setString(2, arquivo.getCaminho());
            stmt.setString(3, arquivo.getNome());
            resultado = stmt.executeUpdate();
        } else {
            // Se não existe, fazer INSERT
            String sql = "INSERT INTO arquivos (nome, tamanho, caminho) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            stmt.setString(1, arquivo.getNome());
            stmt.setLong(2, arquivo.getTamanho());
            stmt.setString(3, arquivo.getCaminho());
            resultado = stmt.executeUpdate();
        }
        
        return resultado > 0;
        
    } catch (Exception e) {
        System.out.println("Erro salvando arquivo: " + e.getMessage());
        return false;
    } finally {
        conn.desconectar();
    }
}
    //busca arquivos
    public byte[] buscarArquivo(String nomeArquivo) {
        Conexao conn = new Conexao();
        
        try {
            conn.conectar();
            String sql = "SELECT caminho FROM arquivos WHERE nome = ?";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            stmt.setString(1, nomeArquivo);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String caminho = rs.getString("caminho");
                return Files.readAllBytes(Paths.get(caminho));
            }
            return null;
            
        } catch (Exception e) {
            System.out.println("Erro buscando arquivo: " + e.getMessage());
            return null;
        } finally {
            conn.desconectar();
        }
    }
    //lista os arquivos disponiveis
    public List<String> listarArquivos() {
        List<String> nomesArquivos = new ArrayList<>();
        Conexao conn = new Conexao();
        
        try {
            conn.conectar();
            String sql = "SELECT nome FROM arquivos ORDER BY nome";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String nome = rs.getString("nome");
                nomesArquivos.add(nome);
            }
            
        } catch (Exception e) {
            System.out.println("Erro listando arquivos: " + e.getMessage());
        } finally {
            conn.desconectar();
        }
        return nomesArquivos;
    }
    //deleta os arquivos
    public boolean deletarArquivo(String nomeArquivo) {
        Conexao conn = new Conexao();
        
        try {
            conn.conectar();
            
            // 1. Buscar caminho do arquivo
            String sqlBuscar = "SELECT caminho FROM arquivos WHERE nome = ?";
            PreparedStatement stmtBuscar = conn.conector.prepareStatement(sqlBuscar);
            stmtBuscar.setString(1, nomeArquivo);
            
            ResultSet rs = stmtBuscar.executeQuery();
            if (rs.next()) {
                String caminho = rs.getString("caminho");
                Files.deleteIfExists(Paths.get(caminho));
            }
            
            // 2. Deletar do banco
            String sqlDeletar = "DELETE FROM arquivos WHERE nome = ?";
            PreparedStatement stmtDeletar = conn.conector.prepareStatement(sqlDeletar);
            stmtDeletar.setString(1, nomeArquivo);
            
            int resultado = stmtDeletar.executeUpdate();
            return resultado > 0;
            
        } catch (Exception e) {
            System.out.println("Erro deletando arquivo: " + e.getMessage());
            return false;
        } finally {
            conn.desconectar();
        }
    }
    //Mensagem dos cliente e seus nome a elas vinculados
    public boolean salvarMensagemChat(String clienteNome, String mensagem) {
        Conexao conn = new Conexao();
        
        try {
            // Criar objeto model MensagemChat
            MensagemChat msg = new MensagemChat();
            msg.setClienteNome(clienteNome);
            msg.setMensagem(mensagem);
            
            // Salvar no banco
            conn.conectar();
            String sql = "INSERT INTO mensagens_chat (cliente_nome, mensagem) VALUES (?, ?)";
            PreparedStatement stmt = conn.conector.prepareStatement(sql);
            stmt.setString(1, msg.getClienteNome());
            stmt.setString(2, msg.getMensagem());
            
            int resultado = stmt.executeUpdate();
            return resultado > 0;
            
        } catch (Exception e) {
            System.out.println("Erro salvando mensagem: " + e.getMessage());
            return false;
        } finally {
            conn.desconectar();
        }
    }
}