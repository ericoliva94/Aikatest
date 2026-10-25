/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author zValk
 */
public class ConnectionManager {

    private static Connection con;
    private static String dbName = "cadastro";
    private static String username = "root";
    private static String pw = "root";
    private static String conURL = "jdbc:mysql://localhost:3306/";// + dbName;

    public static void tryConnect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            if (con == null) {
                try {
                    con = DriverManager.getConnection(conURL, username, pw);

                    JOptionPane.showMessageDialog(
                            null,
                            "Conexão estabelecida com sucesso!",
                            "Bem-vindo(a)!",
                            JOptionPane.INFORMATION_MESSAGE);

                    if (findDatabase()) {
                        JOptionPane.showMessageDialog(
                                null,
                                "O banco de dados " + dbName + " foi encontrado com sucesso!", "Atenção!",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        int question = JOptionPane.showConfirmDialog(
                                null,
                                "O banco de dados " + dbName + " não foi encontrado!"
                                + "\nVocê deseja criar um agora?", "Atenção!",
                                JOptionPane.YES_NO_OPTION);

                        switch (question) {
                            case 0:
                                JOptionPane.showMessageDialog(null, "pressionou yes");
                                break;
                            case 1:
                                JOptionPane.showMessageDialog(null, "pressionou no");
                                break;
                        }
                    }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Não foi possível conectar-se ao banco de dados.", "Erro!", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "ClassNotFoundException lançado.", "Erro!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static boolean findDatabase() {
        ResultSet rs;
        
        try {
            rs = con.getMetaData().getCatalogs();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao tentar recuperar os catalogos em ResultSet.", "Erro!", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            while (rs.next()) {
                String rsDbName = rs.getString(1);
                if (rsDbName.equals(dbName)) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao tentar recuperar os bancos de dados.", "Erro!", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
