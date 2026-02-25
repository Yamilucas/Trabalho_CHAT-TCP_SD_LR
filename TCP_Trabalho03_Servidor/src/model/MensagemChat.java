package model;

import java.sql.Timestamp;

public class MensagemChat {
    private int id;
    private String clienteNome;
    private String mensagem;
    private Timestamp dataEnvio;
    
    public MensagemChat() {
    }
    
    public MensagemChat(String clienteNome, String mensagem) {
        this.clienteNome = clienteNome;
        this.mensagem = mensagem;
    }
    
    public MensagemChat(int id, String clienteNome, String mensagem, Timestamp dataEnvio) {
        this.id = id;
        this.clienteNome = clienteNome;
        this.mensagem = mensagem;
        this.dataEnvio = dataEnvio;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getClienteNome() {
        return clienteNome;
    }
    
    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public Timestamp getDataEnvio() {
        return dataEnvio;
    }
    
    public void setDataEnvio(Timestamp dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
    

}