package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Model.Account;
import Model.Message;
import Util.ConnectionUtil;

public class AccountDAO {

/**
 * @return The Account found in the database or null, when an Account with accountId was not found.
 */
public Account selectAllByAccountId(int accountId) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("select*from account where account_id=?");
	ps.setInt(1,accountId);
	ResultSet rs=ps.executeQuery();
	if(!rs.next())return null;//no account found

	return new Account(
		rs.getInt("account_id"),
		rs.getString("username"),
		rs.getString("password")
	);
}

/**
 * @return The account with the given username and password or null when it's not there
 */
public Account selectAllByUsernameAndPassword(String username,String password) throws Exception
{
	//Find an existing account given username and password
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("select*from account where username=? and password=?");
	ps.setString(1, username);
	ps.setString(2, password);
	ResultSet rs=ps.executeQuery();
	if(!rs.next())return null;

	return new Account(
		rs.getInt("account_id"),
		rs.getString("username"),
		rs.getString("password"));
}

/**
 * @return The account_id given uername or -1 when it wasn't found
 */
public int selectAccountIdByUsername(String username) throws SQLException 
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("select account_id from account where username=?");
	ps.setString(1, username);
	ResultSet rs=ps.executeQuery();
	if(!rs.next()){
		System.err.println("rs.next was false");
		return -1;
	}

	return rs.getInt("account_id");
}

/**
 * @return The newly created account_id or -1 on failure
 */
public int insertNew(String username,String password) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement(
	"insert into account (username,password) values (?,?)",Statement.RETURN_GENERATED_KEYS);
	ps.setString(1, username);
	ps.setString(2, password);
	int updatedRows=ps.executeUpdate();
	System.out.println("updatedRows="+updatedRows);
	if(updatedRows==0)return -1;

	//Get account id from the newly created row
	ResultSet rs=ps.getGeneratedKeys();
	if(!rs.next()){
		System.out.println("rs.next was false");
		return -1;//could not get newly created row id
	}

	int accountId=(int)rs.getLong("account_id");//defined as int in the table
	System.out.println("accountId="+accountId);
	return accountId;
}

}//end class
