package Service;

import DAO.AccountDAO;
import DAO.MessageDAO;
import Model.Account;
import io.javalin.http.Context;

public class AccountService {

private AccountDAO accountDAO;
private MessageDAO messageDAO;

public AccountService(AccountDAO accountDAO,MessageDAO messageDAO)
{
	this.accountDAO=accountDAO;
	this.messageDAO=messageDAO;
}

public void endRegister(Context ctx) throws Exception
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

public void endLogin(Context ctx) throws Exception
{
	Account incoming=ctx.bodyAsClass(Account.class);

	Account existingAccount=accountDAO.selectAllByUsernameAndPassword(incoming.getUsername(),incoming.getPassword());

	if(existingAccount==null)
	{ctx.status(401);return;}

	ctx.json(existingAccount);
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
