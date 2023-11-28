package Service;

import DAO.AccountDAO;
import DAO.MessageDAO;
import Model.Account;
import Model.Message;
import io.javalin.http.Context;

public class MessageService {

private AccountDAO accountDAO;
private MessageDAO messageDAO;

public MessageService(AccountDAO accountDAO,MessageDAO messageDAO)
{
	this.accountDAO=accountDAO;
	this.messageDAO=messageDAO;
}

public void endMessagesPOST(Context ctx) throws Exception
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

public void endMessagesGET_ALL(Context ctx) throws Exception
{
	ctx.json(messageDAO.selectAllMessages());
}

public void endMessageGET_BY_ID(Context ctx) throws Exception
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

public void endMessageDELETE_BY_ID(Context ctx) throws Exception
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

public void endMessagePATCH_BY_ID(Context ctx) throws Exception
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

}//end class
