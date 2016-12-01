/**
 * Created by Administrator on 2016/11/24.
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
public class HownetAPI {
    //根据义原内容得到义原编号
    public int Get_Sememe_Code(String sememe_content, Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select id from sememe where sememe_content like \"" + sememe_content + "\" ;";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                int sememe_id = resultSetStatus.getInt("id");
                return sememe_id;
            }

            else{
                return -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return -2;
        }
    }

    //得到一个义原的上位义原
    public int Get_Sememe_Hyp(int sememe_id, Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select parent_id from sememe where id =" + sememe_id + ";";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                int hyp = resultSetStatus.getInt("parent_id");
                return hyp;
            }

            else{
                return -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return -2;
        }
    }

    //根据义原编号得到义原内容
    public String Get_Sememe_String(int id, Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select sememe_content from sememe where id = " + id + " ;";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                String sememe_content = resultSetStatus.getString("sememe_content");
                return sememe_content;
            }

            else{
                return "";
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "";
        }
    }

    //根据所在义原树中的编号
    public int Get_TreeID(int id, Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select tree_id from sememe where id = " + id + " ;";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                int tree_id = resultSetStatus.getInt("tree_id");
                return tree_id;
            }

            else{
                return -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return -2;
        }
    }

    //根据概念编号得到指定部分的词名
    public String Get_Unit_Item2(int id, Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select W_C from hownet where id = " + id + " ;";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                String def = resultSetStatus.getString("W_C");
                return def;
            }

            else{
                return "";
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "";
        }
    }

    //根据概念编号得到指定部分的具体内容
    public String Get_Unit_Item10(int id, Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select DEF from hownet where id = " + id + " ;";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                String def = resultSetStatus.getString("DEF");
                return def;
            }

            else{
                return "";
            }
        }catch(SQLException e){
            e.printStackTrace();
            return "";
        }
    }

    //获得条目数
    public int GetUnitNum(Connection connection) throws UnsupportedEncodingException{
        try{
            // first, construct the select syntax and ResultSet of status
            String selectStatus = "select count(*) from hownet;";
            Statement statementStatus;
            statementStatus = connection.createStatement();
            ResultSet resultSetStatus = statementStatus.executeQuery(selectStatus);

            if(resultSetStatus.next()){
                int count = resultSetStatus.getInt(1);
                return count;
            }

            else{
                return -2;
            }
        }catch(SQLException e){
            e.printStackTrace();
            return -2;
        }
    }


    private Connection connection = null;
    public Connection getConnnection(){
        return this.connection;
    }

    public HownetAPI(Connection connection){
        this.connection = connection;
    }

}
