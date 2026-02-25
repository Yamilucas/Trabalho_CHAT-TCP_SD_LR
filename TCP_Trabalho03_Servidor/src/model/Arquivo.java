package model;

import java.sql.Timestamp;

public class Arquivo {
    private int id;
    private String nome;
    private long tamanho;
    private String caminho;
    private Timestamp dataUpload;
    
    public Arquivo() {
    }
    
    public Arquivo(String nome, long tamanho, String caminho) {
        this.nome = nome;
        this.tamanho = tamanho;
        this.caminho = caminho;
    }
    
    public Arquivo(int id, String nome, long tamanho, String caminho, Timestamp dataUpload) {
        this.id = id;
        this.nome = nome;
        this.tamanho = tamanho;
        this.caminho = caminho;
        this.dataUpload = dataUpload;
    }
    
    // Getters e Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public long getTamanho() {
        return tamanho;
    }
    
    public void setTamanho(long tamanho) {
        this.tamanho = tamanho;
    }
    
    public String getCaminho() {
        return caminho;
    }
    
    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }
    
    public Timestamp getDataUpload() {
        return dataUpload;
    }
    
    public void setDataUpload(Timestamp dataUpload) {
        this.dataUpload = dataUpload;
    }
    
   
}