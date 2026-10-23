package org.example.dao;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDAO {

    // Metodo de conexão com o banco de dados
    public Connection conectaBD(){
        Connection conn = null;

        try {
            String url = "jdbc:mysql://localhost:3306/megabrain_db?user=root&password=1234" ; // Url completa de conexão
            conn = DriverManager.getConnection(url);
        }
        catch (SQLException erro){
            JOptionPane.showMessageDialog(null, "ConexaoDAO" + erro.getMessage());
        }
        return conn;
    }
}
