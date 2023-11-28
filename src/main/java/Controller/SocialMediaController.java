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
import Service.AccountService;
import Service.MessageService;
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
	AccountDAO accountDAO=new AccountDAO();
	MessageDAO messageDAO=new MessageDAO();
	MessageService messageService=new MessageService(accountDAO,messageDAO);
	AccountService accountService=new AccountService(accountDAO,messageDAO);

	
	app.get(	"example-endpoint", 	this::exampleHandler);
	app.post(	"register",				accountService::endRegister);
	app.post(	"login",				accountService::endLogin);
	app.get(	"accounts/{account_id}/messages",accountService::endAccounts);

	app.post(	"messages",				messageService::endMessagesPOST);
	app.get(	"messages",				messageService::endMessagesGET_ALL);
	app.get(	"messages/{message_id}",messageService::endMessageGET_BY_ID);
	app.delete(	"messages/{message_id}",messageService::endMessageDELETE_BY_ID);
	app.patch(	"messages/{message_id}",messageService::endMessagePATCH_BY_ID);
	return app;
}

/**
 * This is an example handler for an example endpoint.
 * @param context The Javalin Context object manages information about both the HTTP request and response.
 */
private void exampleHandler(Context context) {
	context.json("sample text");
}

}//end class