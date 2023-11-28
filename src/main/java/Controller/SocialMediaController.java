package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import DAO.AccountDAO;
import DAO.MessageDAO;
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

MessageDAO messageDAO=new MessageDAO();
AccountDAO accountDAO=new AccountDAO();

/**
 * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
 * suite must receive a Javalin object from this method.
 * @return a Javalin app object which defines the behavior of the Javalin controller.
 */
public Javalin startAPI() {
	Javalin app = Javalin.create();
	app.get(	"example-endpoint", 	this::exampleHandler);
	app.post(	"register",				this::endRegister);
	app.post(	"login",				this::endLogin);
	app.post(	"messages",				this::endMessagesPOST);
	app.get(	"messages",				this::endMessagesGET_ALL);
	app.get(	"messages/{message_id}",this::endMessageGET_BY_ID);
	app.delete(	"messages/{message_id}",this::endMessageDELETE_BY_ID);
	app.patch(	"messages/{message_id}",this::endMessagePATCH_BY_ID);
	app.get(	"accounts/{account_id}/messages",this::endAccounts);
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

	//Check if an account with this username already exists
	int existingAccountId=accountDAO.selectAccountIdByUsername(incoming.getUsername());
	if(existingAccountId>=0){
		ctx.status(400);/* .result("Account already exists for username "+incoming.getUsername());*/
		return;
	}

	//create new account
	int newAccountId=accountDAO.insertNew(incoming.getUsername(),incoming.getPassword());
	if(newAccountId<0)
	{
		System.err.println("newAccountId less than 0");
		ctx.status(400);/*.result("Could not create account in database");*/
		return;
	}

	//re-using existing Account object, could also re-select it back out of the db
	incoming.setAccount_id(newAccountId);
	ctx.json(incoming);
}

private void endLogin(Context ctx) throws Exception
{
	Account incoming=ctx.bodyAsClass(Account.class);

	Account existingAccount=accountDAO.selectAllByUsernameAndPassword(incoming.getUsername(),incoming.getPassword());

	if(existingAccount==null)
	{ctx.status(401);return;}

	ctx.json(existingAccount);
}

private void endMessagesPOST(Context ctx) throws Exception
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
	Account existingAccount=accountDAO.selectAllByAccountId(incoming.getPosted_by());

	if(existingAccount==null)
	{ctx.status(400);return;}//no rows given posted_by account_id


	int newMessageId=messageDAO.insertNew(incoming);

	//this will work for now
	//in the future, the db might set other columns such as time_posted_epoch
	//so we would have to re-select the new row back out
	incoming.setMessage_id(newMessageId);
	ctx.json(incoming);
}

private void endMessagesGET_ALL(Context ctx) throws Exception
{
	ctx.json(messageDAO.selectAllMessages());
}

private void endMessageGET_BY_ID(Context ctx) throws Exception
{
	if(ctx.pathParam("message_id")==null)
	{ctx.status(400);return;}
	if(ctx.pathParam("message_id").isBlank())
	{ctx.status(400);return;}
	int searchMessageId=Integer.parseInt(ctx.pathParam("message_id"));

	Message messageFound=messageDAO.selectAllByMessageId(searchMessageId);
	if(messageFound==null)
	{ctx.json("");return;}//no messages found

	ctx.json(messageFound);
}

private void endMessageDELETE_BY_ID(Context ctx) throws Exception
{
	if(ctx.pathParam("message_id")==null)
	{ctx.status(400);return;}
	if(ctx.pathParam("message_id").isBlank())
	{ctx.status(400);return;}
	int searchMessageId=Integer.parseInt(ctx.pathParam("message_id"));

	Message messageToDelete=messageDAO.selectAllByMessageId(searchMessageId);
	if(messageToDelete==null)
	{ctx.json("");return;}//making DELETE idempotent

	if(messageDAO.deleteAllByMessageId(searchMessageId)==0)
	{ctx.status(500);return;}//could not delete for some reason

	ctx.json(messageToDelete);
}

private void endMessagePATCH_BY_ID(Context ctx) throws Exception
{
	if(ctx.pathParam("message_id")==null)
	{ctx.status(400);return;}
	if(ctx.pathParam("message_id").isBlank())
	{ctx.status(400);return;}
	int searchMessageId=Integer.parseInt(ctx.pathParam("message_id"));

	Message incoming=ctx.bodyAsClass(Message.class);
	if(incoming==null)
	{ctx.status(400);return;}
	if(incoming.getMessage_text()==null)
	{ctx.status(400);return;}
	if(incoming.getMessage_text().isBlank())
	{ctx.status(400);return;}
	if(incoming.getMessage_text().length()>=255)
	{ctx.status(400);return;}

	if(messageDAO.updateMessageTextByMessageId(incoming.getMessage_text(),searchMessageId)==0)
	{ctx.status(400);return;}//no rows updated. message was not there

	Message updatedMessage=messageDAO.selectAllByMessageId(searchMessageId);
	if(updatedMessage==null)
	{ctx.status(500);return;}//couldn't find it for some reason

	ctx.json(updatedMessage);
}

public void endAccounts(Context ctx) throws Exception
{
	if(ctx.pathParam("account_id")==null)
	{ctx.status(400);return;}
	if(ctx.pathParam("account_id").isBlank())
	{ctx.status(400);return;}
	int searchAccountId=Integer.parseInt(ctx.pathParam("account_id"));

	//select messages posted by this account_id
	ctx.json(messageDAO.selectAllByPostedBy(searchAccountId));
}


}//end class