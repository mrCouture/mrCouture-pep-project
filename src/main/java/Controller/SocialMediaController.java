package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Model.Account;
import Model.Message;
import Util.ConnectionUtil;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);
		app.post("register",		this::endRegister);
		app.post("login",			this::endLogin);
		app.post("messages",		this::endMessages);
        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }

	private void endRegister(Context ctx) throws Exception
	{
		Account incoming=ctx.bodyAsClass(Account.class);

		if(incoming.getUsername()==null)
		{ctx.status(400);/*..result("username was null")*/;return;}
		if(incoming.getUsername().isBlank())
		{ctx.status(400);/*.result("username was blank");*/return;}
		if(incoming.getPassword()==null)
		{ctx.status(400);/*.result("password was null");*/return;}
		if(incoming.getPassword().length()<4)
		{ctx.status(400);/*.result("password length was less than 4 characters")*/;return;}

		//Check if an account with this username alreday exists
		Connection conn1=ConnectionUtil.getConnection();
		PreparedStatement ps1=conn1.prepareStatement("select account_id from account where username=?");
		ps1.setString(1, incoming.getUsername());
		ResultSet rs1=ps1.executeQuery();

		if(rs1.next()){
			System.err.println("rs1.getInt(\"account_id\")="+rs1.getInt("account_id"));
			ctx.status(400);/* .result("Account already exists for username "+incoming.getUsername());*/
			return;
		}

		//create new account
		Connection conn2=ConnectionUtil.getConnection();
		PreparedStatement ps2=conn2.prepareStatement("insert into account (username,password) values (?,?)");
		ps2.setString(1, incoming.getUsername());
		ps2.setString(2, incoming.getPassword());
		if(ps2.executeUpdate()==0)
		{ctx.status(400).result("Could not create account in database");return;}


		//select the newly created row to know the account_id
		Connection conn3=ConnectionUtil.getConnection();
		PreparedStatement ps3=conn3.prepareStatement("select * from account where username=?");
		ps3.setString(1, incoming.getUsername());
		ResultSet rs3=ps3.executeQuery();

		if(!rs3.next()){
			System.err.println("rs3.getInt(\"account_id\")="+rs3.getInt("account_id"));
			ctx.status(500).result("Could not select newly created account from database");return;
		}

		Account newAccount=new Account(
			rs3.getInt("account_id"),
			rs3.getString("username"),
			rs3.getString("password"));

		System.err.println("newAccount="+newAccount);

		ctx.json(newAccount);
	}

	private void endLogin(Context ctx) throws Exception
	{
		Account incoming=ctx.bodyAsClass(Account.class);

		//Find an existing account given username and password
		Connection conn1=ConnectionUtil.getConnection();
		PreparedStatement ps1=conn1.prepareStatement("select*from account where username=? and password=?");
		ps1.setString(1, incoming.getUsername());
		ps1.setString(2, incoming.getPassword());
		ResultSet rs1=ps1.executeQuery();
		if(!rs1.next())
		{ctx.status(401);return;}

		Account existingAccount=new Account();
		existingAccount.setAccount_id(rs1.getInt("account_id"));
		existingAccount.setUsername(rs1.getString("username"));
		existingAccount.setPassword(rs1.getString("password"));
		ctx.json(existingAccount);
	}

	private void endMessages(Context ctx) throws Exception
	{
		Message incoming=ctx.bodyAsClass(Message.class);

		if(incoming==null)
		{ctx.status(400);return;}
		if(incoming.getMessage_text()==null)
		{ctx.status(400);return;}
		if(incoming.getMessage_text().isBlank())
		{ctx.status(400);return;}
		if(incoming.getMessage_text().length()>=255)
		{ctx.status(400);return;}

		//Check that posted_by matches an existing user
		Connection conn1=ConnectionUtil.getConnection();
		PreparedStatement ps1=conn1.prepareStatement("select*from account where account_id=?");
		ps1.setInt(1, incoming.getPosted_by());
		ResultSet rs1=ps1.executeQuery();
		if(!rs1.next())
		{ctx.status(400);return;}//no rows given posted_by account_id

		//Save the message to the database
		Connection conn2=ConnectionUtil.getConnection();
		PreparedStatement ps2=conn2.prepareStatement(
		"insert into message (posted_by,message_text,time_posted_epoch) values (?,?,?)",Statement.RETURN_GENERATED_KEYS);
		ps2.setInt(		1, incoming.getPosted_by());
		ps2.setString(	2, incoming.getMessage_text());
		ps2.setLong(	3, incoming.getTime_posted_epoch());
		if(ps2.executeUpdate()==0)
		{ctx.status(500);return;}//could not save message for some reason

		//Get message id from the newly created row
		ResultSet rs2=ps2.getGeneratedKeys();
		if(!rs2.next())
		{ctx.status(500);return;}//could not get newly created row id

		int newMessageId=(int)rs2.getLong("message_id");//defined as int in the table
		System.out.println("rs2="+rs2);
		System.out.println("newMessageId="+newMessageId);

		incoming.setMessage_id(newMessageId);
		ctx.json(incoming);
	}

}