/*
 * 数据库连接类
 * 每一个连接实体用一个 Connect，循环体务必注意
 * 修改日志：<br>
 * 2016-7-1 增加关闭自动提交和提交函数
 * 2017-2-27 修正一些规范性设置，稳定性提高
 * 2017-4-10 Connect 构造函数机上异常状态触发
 * 2018-4-20 增加存储过程获取对象的函数
 * 
 */
package com.sfang.data;

/*
 * 数据库连接类
 */

import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.sfang.config.Constant;

public class Connect
{
  private static final Logger logger = LogManager.getLogger(Connect.class);

  private Connection conn = null;
  private Statement stmt = null;
  private CallableStatement cstmt = null;
  private PreparedStatement pstmt = null;
  private ResultSet rs = null;

  private boolean connected = false;

  /**
   * 连接模式：J2EE 环境下，连接池方式
   */
  public static int JNDI_MODE = 100;
  /**
   * 控制台环境下，URL 连接方式（用于jUnit测试）
   */
  public static int URL_MODE = 200;

  /**
   * 初始化连接
   * 
   * @param mode
   *          连接模式：JNDI 和 URL
   */
  public Connect(final int mode) throws SQLException
  {
    // J2EE 环境下连接池
    if (mode == JNDI_MODE)
    {
      logger.debug("JNDI_MODE begin ...");

      // 连接数据库
      try
      {
        Context context = new InitialContext();
        logger.debug("InitialContext success");

        DataSource dataSource = (DataSource) context.lookup(Constant.JNDI_NAME);
        logger.debug(String.format("DataSource lookup \"%s\" %s", Constant.JNDI_NAME, "success"));

        conn = dataSource.getConnection();
        logger.debug(String.format("Connection dataSource %s", "success"));

        // sql server jdbc 调用 first 需要如下参数
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        logger.debug(String.format("CreateStatement %s", "success"));

        // 修改連接狀態
        connected = true;
      }
      catch (NamingException e)
      {
        e.printStackTrace();
        logger.error(e.getMessage());
        Close();
        throw new SQLException("Connect fail by JNDI");
      }
      catch (SQLException e)
      {
        e.printStackTrace();
        logger.error(e.getMessage());
        Close();
        throw new SQLException("Connect fail by JNDI");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        logger.error(e.getMessage());
        Close();
        throw new SQLException("Connect fail by JNDI");
      }
      finally
      {
      }
    }
    // 主要是用于 jUnit 测试时没有连接池的情況
    else if (mode == URL_MODE)
    {
      logger.debug("URL_MODE begin...");
      try
      {
        //
        Class.forName("com.mysql.jdbc.Driver");
        // 创建连接 URL
        final String url = String.format(
            "jdbc:mysql://127.0.0.1:3306/editor?user=%s&password=%s&useSSL=false&characterEncoding=UTF-8", "root", "123456");

        // final String url =
        // String.format("jdbc:mysql://120.76.114.155:3306/editor?user=%s&password=%s&useSSL=false&characterEncoding=UTF-8",
        // "tuwen", "tuwen123");

        // 连接 …… 并得到 connection
        conn = DriverManager.getConnection(url);
        // Statement
        stmt = conn.createStatement();
        // 修改連接狀態
        connected = true;
      }
      catch (ClassNotFoundException e)
      {
        e.printStackTrace();
        logger.error(e.getMessage());
        Close();
        throw new SQLException("Connect fail by URL");
      }
      catch (SQLException e)
      {
        e.printStackTrace();
        logger.error(e.getMessage());
        Close();
        throw new SQLException("Connect fail by URL");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        logger.error(e.getMessage());
        Close();
        throw new SQLException("Connect fail by URL");
      }
      finally
      {
      }
    }
  }

  /**
   * 返回连接状态
   * 
   * @return
   */
  public boolean isConnected()
  {
    return connected;
  }

  /**
   * 检查数据库連接是否有效
   * 
   * @return
   */
  public boolean Test()
  {
    // 返回值
    boolean result = false;
    ResultSet resultSet = null;
    // 检查
    try
    {
      resultSet = conn.createStatement().executeQuery("select 1");

      if (resultSet.first() == true)
      {
        result = true;
        logger.debug("Test connect success");
      }

      if (resultSet != null)
      {
        try
        {
          resultSet.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      result = false;
      logger.debug("Test connect fail");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {

    }

    // 返回
    return result;
  }

  /**
   * 傳入一個預備格式化的 SQL<br>
   * insert into table (`field1`, `field2`) values (?, ?);
   * 
   * @param sql
   * @throws SQLException
   */
  public void setPreparedStatementSQL(final String sql) throws SQLException
  {
    pstmt = conn.prepareStatement(sql);
  }

  /**
   * 傳入格式化的<br>
   * SQL connect.setPreparedStatementSQL(sql);<br>
   * 
   * 設置各個參數的類型和值<br>
   * connect.getPreparedStatement().setLong(1, 1);<br>
   * connect.getPreparedStatement().setString(2, "name");<br>
   * connect.getPreparedStatement().setString(3, null);<br>
   * 
   * 執行<br>
   * connect.executeUpdate(null);<br>
   * 
   * @return
   */
  public PreparedStatement getPreparedStatement()
  {
    return pstmt;
  }

  /**
   * 返回 Connection
   * 
   * @return
   */
  public Connection getConnection()
  {
    return conn;
  }

  /**
   * 返回 ResultSet
   * 
   * @return
   */
  public ResultSet getResultSet()
  {
    return rs;
  }

  /**
   * 获得 Statement 执行 SQL 命令
   * 
   * @return Statement
   * @throws SQLException
   */
  public Statement getStatement() throws SQLException
  {
    // 创建 statement

    // mysql use
    stmt = conn.createStatement();
    // mssql use
    // stmt =
    // conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);

    return stmt;
  }

  /**
   * 获取 CallableStatement 并创建存储过程
   * 
   * @param strCallableStatement
   * @return CallableStatement
   * @throws SQLException
   */
  public CallableStatement setCallableStatement(String sql) throws SQLException
  {
    // storedProc
    cstmt = conn.prepareCall(sql);
    return cstmt;
  }

  /**
   * 获取存储过程对象
   * @return
   */
  public CallableStatement getCallableStatement()
  {
    return cstmt;
  }

  public ResultSet executeQuery() throws SQLException
  {
    return executeQuery(null);
  }

  /**
   * 查询 sql
   * 
   * @param sql
   * @return
   * @throws SQLException
   */
  public ResultSet executeQuery(final String sql) throws SQLException
  {
    // init ResultSet
    if (rs != null)
    {
      rs.close();
      rs = null;
    }

    // 正常的 SQL 传入调用
    if (sql != null)
    {
      logger.debug("connect: stmt.executeQuery(sql)");
      if (stmt != null)
      {
        rs = stmt.executeQuery(sql);
      }
    }
    else
    {
      logger.debug("connect: pstmt.executeQuery()");
      if (pstmt != null)
      {
        rs = pstmt.executeQuery();
      }
    }

    return rs;
  }

  /**
   * 执行 sql
   * 
   * @param sql
   * @return
   * @throws SQLException
   */
  public int executeUpdate(final String sql) throws SQLException
  {
    int result = 0;

    if (sql == null && pstmt != null)
    {
      result = pstmt.executeUpdate();
    }
    else
    {
      result = stmt.executeUpdate(sql);
    }
    return result;
  }

  /**
   * pstmt 无参数时调用
   * 
   * @return
   * @throws SQLException
   */
  public int executeUpdate() throws SQLException
  {
    return executeUpdate(null);
  }

  /**
   * 取得最后编号
   * 
   * @return
   */
  public Long getLastInsertId()
  {
    Long result = null;
    ResultSet resultSet = null;

    try
    {
      // 查询最后插入的记录的编号
      String sql = "select last_insert_id() as `id`";
      // 调用查询
      resultSet = stmt.executeQuery(sql);
      if (resultSet.first() == true)
      {
        result = resultSet.getLong("id");
        logger.debug("getLastInsertId is success");
      }
      else
      {
        logger.debug("getLastInsertId is null");
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      logger.error("getLastInsertId is fail");
    }
    finally
    {
      if (resultSet != null)
      {
        try
        {
          resultSet.close();
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }

    return result;
  }

  /**
   * 设置是否自动提交<br>
   * 默認爲 true，執行后即刻對數據庫改寫<br>
   * 当为 false 时，所有修改需调用 Commit() 方可实际完成。
   * 
   * @param model
   * @throws SQLException
   */
  public void setAutoCommit(boolean model) throws SQLException
  {
    logger.debug("setAutoCommit", model);
    conn.setAutoCommit(model);
  }

  /**
   * 若处于手动提交, 需要提交
   * 
   * @throws SQLException
   */
  public void Commit() throws SQLException
  {
    conn.commit();
  }

  /**
   * 事物回退
   * 
   * @throws SQLException
   */
  public void Rollback()
  {
    try
    {
      conn.rollback();
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * 關閉所有連接
   */
  public void Close()
  {
    logger.debug("Connect close all");
    connected = false;

    // Close ResultSet
    if (rs != null)
    {
      try
      {
        rs.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      rs = null;
    }

    // Close Statement
    if (stmt != null)
    {
      try
      {
        stmt.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      stmt = null;
    }

    // storedProc
    if (cstmt != null)
    {
      try
      {
        cstmt.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      cstmt = null;
    }

    if (conn != null)
    {
      try
      {
        conn.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      conn = null;
    }
  }
}
