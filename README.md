# jdbc-connect
Description Description 

创建连接
Connect connect = new Connect(Connect.URL_MODE);
if(connect.executeUpdate(sql) == 1)
{
 // 执行成功
}
connect.Close();

查询 Connect connect = new Connect(Connect.URL_MODE);
connect.executeQuery(sql);
while (connect.getResultSet().next() == true)
{
String val = connect.getResultSet().getString("field1");
System.out.println(val);
}
connect.Close();

// 一个演示 Connect connect = null; try { connect = new Connect(Connect.JNDI_MODE);

connect.executeQuery(sql);
while (connect.getResultSet().next() == true)
{
 String val = connect.getResultSet().getString("field");
 }
 }
 catch (SQLException e)
  {
     e.printStackTrace();
     result = Constant.DB_ERROR;
  }
  catch (Exception e)
  {
    e.printStackTrace();
    result = Constant.DB_ERROR;
  }
  finally
  {
    if (connect != null)
    {
      connect.Close();
    }
  }
