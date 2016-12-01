import java.sql.*;

/**
 * Created by Administrator on 2016/11/22.
 */
public class test {
    public static void main(String[] args){
        try {
            Connection con = getConn();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet set = null;
        try {
            PreparedStatement prepar = getConn().prepareStatement("select * from sememe");
            set = prepar.executeQuery();
            int i = 0;
            while(set.next()){
                String name = set.getString("sememe_content");
                int parent = Integer.parseInt(set.getString("parent_id"));
                System.out.println("name: " + name);
                System.out.println("parent_id: " + parent);
                i ++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConn() throws SQLException {
        Connection conn = null;
        try{
            Class.forName("com.mysql.jdbc.Driver");
            //加载驱动类
            try {
                conn = DriverManager.getConnection("jdbc:mysql://localhost/sys","root","qazwsxedcrf123");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return conn;
    }
}
