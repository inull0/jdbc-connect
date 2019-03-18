# jdbc-connect
Description Description 

// new connect
Connect connect = new Connect(Connect.URL_MODE);
if(connect.executeUpdate(sql) == 1)
{
 // 执行成功
}

connect.Close();

// Query
Connect connect = new Connect(Connect.URL_MODE);
connect.executeQuery(sql);
while (connect.getResultSet().next() == true)
{
String val = connect.getResultSet().getString("field1");
System.out.println(val);
}
connect.Close();

// Demo
Connect connect = null; try { connect = new Connect(Connect.JNDI_MODE);

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
