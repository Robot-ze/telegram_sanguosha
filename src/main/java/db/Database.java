package db;
import java.sql.*;

 
/**
 * 存储一些key，v，因为懒得改名了，所以表名是胡乱来的
 */
public class Database {

private static Connection CONN;

static{   try {
    Class.forName("org.sqlite.JDBC");
    CONN = DriverManager.getConnection("jdbc:sqlite:sanguosha.db");
    //CONN.setAutoCommit(false); // 取消自动commit
    checkAndcreateDB(CONN);
    } catch ( Exception e ) {
        e.printStackTrace();
        try {
            CONN.rollback(); // 如果发生异常，回滚事务
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }
}
    public static void init(){}

  
    public static void checkAndcreateDB(Connection c){
 
        try (Statement stmt = c.createStatement();){
            System.out.println("Opened database successfully");
       
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='FILEID'";
            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                sql = "CREATE TABLE FILEID (" +
                      "NAME          CHAR(50) PRIMARY KEY," +
                      "FILEID        CHAR(100)) ";
                stmt.executeUpdate(sql);
                sql = "CREATE INDEX IDX_FILEID_NAME ON FILEID (NAME)";
                stmt.executeUpdate(sql);
                //CONN.commit();
                System.out.println("Table created successfully");
            } else {
                System.out.println("Table already exists");
            }
            stmt.close();
            //c.close();
        } catch (Exception e) {
            e.printStackTrace();
             
        }
    }

    /**
     * 这set是个同步方法，get是异步方法
     * @param name
     * @param fileid
     */
    public static synchronized void put(String name, String fileid) {
        try(PreparedStatement pstmt = CONN.prepareStatement("INSERT OR REPLACE INTO FILEID (NAME, FILEID) VALUES (?, ?)");){
            
            pstmt.setString(1, name);
            pstmt.setString(2, fileid);
            pstmt.executeUpdate();
            //CONN.commit();
            System.out.println("Inserted or updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CONN.rollback(); // 如果发生异常，回滚事务
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static String get(String name) {
        String fileid = null;
        try (PreparedStatement pstmt = CONN.prepareStatement("SELECT FILEID FROM FILEID WHERE NAME = ?");){
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                fileid = rs.getString("FILEID");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileid;
    }

    /**
     * 从FILEID表中删除指定NAME的行
     * @param name
     */
    public static synchronized void del(String name) {
        try (PreparedStatement pstmt = CONN.prepareStatement("DELETE FROM FILEID WHERE NAME = ?");){
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CONN.rollback(); // 如果发生异常，回滚事务
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
}
