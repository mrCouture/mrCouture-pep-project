package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.Message;
import Util.ConnectionUtil;

/**
 * Attempting to write all sql database logic here for Message
 */
public class MessageDAO {

/**
 * @return all messages in the system
 */
public List<Message> selectAllMessages() throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("select*from message");
	ResultSet rs=ps.executeQuery();

	List<Message> messages=new ArrayList<>();

	while(rs.next()){
		messages.add(new Message(
			rs.getInt("message_id"),
			rs.getInt("posted_by"),
			rs.getString("message_text"),
			rs.getLong("time_posted_epoch")
		));
	}

	return messages;
}

/**
 * @return The message id of the new row or -1 when there was a failure saving
 */
public int insertNew(Message newMessage) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement(
	"insert into message (posted_by,message_text,time_posted_epoch) values (?,?,?)",Statement.RETURN_GENERATED_KEYS);
	ps.setInt(		1, newMessage.getPosted_by());
	ps.setString(	2, newMessage.getMessage_text());
	ps.setLong(	3, newMessage.getTime_posted_epoch());//in the future, the db might set this using NOW()
	if(ps.executeUpdate()==0)
	{return -1;}//could not save message for some reason

	//Get message id from the newly created row
	ResultSet rs=ps.getGeneratedKeys();
	if(!rs.next())
	{return -1;}//could not get newly created row id

	int newMessageId=(int)rs.getLong("message_id");//defined as int in the table
	System.out.println("rs="+rs);
	System.out.println("newMessageId="+newMessageId);

	return newMessageId;
}

/**
 * @return The Message found in the database or null, when a message with message_id was not found.
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

/**
 * @return The messages assoicated with the given account_id
 */
public List<Message> selectAllByPostedBy(int postedBy) throws Exception
{
	Connection conn=ConnectionUtil.getConnection();
	PreparedStatement ps=conn.prepareStatement("select*from message where posted_by=?");
	ps.setInt(1, postedBy);
	ResultSet rs=ps.executeQuery();

	List<Message> messages=new ArrayList<>();
	while(rs.next()){
		messages.add(new Message(
			rs.getInt("message_id"),
			rs.getInt("posted_by"),
			rs.getString("message_text"),
			rs.getLong("time_posted_epoch")
		));
	}
	return messages;
}

}//end class
