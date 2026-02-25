package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
//Conexão com o banco de dados mysql
public class Conexao {
    public Connection conector;
    
    public void conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/servidor_arquivos_chat";
            String usuario = "root";
            String senha = "admin";
            conector = DriverManager.getConnection(url, usuario, senha);
        } catch(ClassNotFoundException e) {
            System.out.println("Erro no Driver: " + e.getMessage());
        } catch(SQLException e) {
            System.out.println("Erro na conexao com banco: " + e.getMessage());
        }
    }
    
    public void desconectar() {
        try {
            if (conector != null && !conector.isClosed()) {
                conector.close();
            }
        } catch(SQLException e) {
            System.out.println("Erro ao fechar conexao: " + e.getMessage());
        }
    }
}