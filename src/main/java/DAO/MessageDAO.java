package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Model.Message;
import Util.ConnectionUtil;

public class MessageDAO {

/**
 * @return
 * 1) The Message found in the database. 2) null, when a message with message_id was not found.
 */
public Message selectAllByMessageId(int message_id) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("select*from message where message_id=?");
	ps.setInt(1,message_id);
	ResultSet rs=ps.executeQuery();
	if(!rs.next())return null;//no message found

	return new Message(
		rs.getInt("message_id"),
		rs.getInt("posted_by"),
		rs.getString("message_text"),
		rs.getLong("time_posted_epoch")
	);
}

/**
 * @return the number of rows deleted
 */
public int deleteAllByMessageId(int message_id) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("delete from message where message_id=?");
	ps.setInt(1,message_id);
	return ps.executeUpdate();
}

/**
 * @return the number of rows updated
 */
public int updateMessageTextByMessageId(String messageText,int messageId) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("update message set message_text=? where message_id=?");
	ps.setString(1,messageText);
	ps.setInt(2,messageId);
	return ps.executeUpdate();
}

}//end class
