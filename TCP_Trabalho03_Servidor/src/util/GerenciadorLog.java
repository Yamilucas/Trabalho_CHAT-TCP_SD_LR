package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class GerenciadorLog {
    private String arquivoLog;

    //arquivo txt do log
    public GerenciadorLog() {
        this.arquivoLog = "log_sistema.txt";
    }
    
    public GerenciadorLog(String arquivoLog) {
        this.arquivoLog = arquivoLog;
    }
    //registra log
    public synchronized void registrar(String operacao, String ipCliente, String mensagem) {
        try {
            FileWriter fw = new FileWriter(arquivoLog, true);
            PrintWriter pw = new PrintWriter(fw);
            
            String timestamp = new Date().toString();
            String logEntry = String.format("[%s] %-20s IP: %-15s %s", 
                timestamp, operacao, ipCliente, mensagem);
            
            pw.println(logEntry);
            System.out.println(logEntry);
            
            pw.close();
            fw.close();
            
        } catch (IOException e) {
            System.err.println("Erro ao escrever log: " + e.getMessage());
        }
    }
}