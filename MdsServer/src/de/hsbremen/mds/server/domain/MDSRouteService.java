package de.hsbremen.mds.server.domain;

import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Router;

import de.hsbremen.mds.server.valueobjects.AppInfo;
import de.hsbremen.mds.server.valueobjects.Item;
import de.hsbremen.mds.server.valueobjects.Player;

public class MDSRouteService extends Application {

	private WSServer wsserv;
	private AppInfoManager aIM;
	private GameManager gM;
	private PlayerManager pM;
	private ItemManager iM;
	
	public MDSRouteService(WSServer wsserv, AppInfoManager aIM, GameManager gM, PlayerManager pM, ItemManager iM) {

		this.wsserv = wsserv;
		this.aIM = aIM;
		this.gM = gM;
		this.pM = pM;
		this.iM = iM;

	}


	@Override
	public synchronized Restlet createInboundRoot() {

		Router router = new Router(getContext());
		
		// Referenz fuer Reslets
		final MDSRouteService mdsRS = this;
		
		Restlet appinfo = new Restlet() {  
		    @Override  
		    public void handle(Request request, Response response) { 
		    	//System.out.println(request.getMethod() +  " appinfo : " + request.getAttributes().get("appid"));
		    	// GET
		    	if(request.getMethod().equals(Method.GET)){
		    		String ai = "";
		    		
		    		// wenn keine Attribute angegeben wurden, alle ausgeben
		    		if (request.getAttributes().get("appid") == null) {
		    			ai = mdsRS.aIM.getJson();
		    		} else { // sonst Element mit der ID ausgeben
				    	int id = Integer.parseInt((String) request.getAttributes().get("appid"));
				    	//AppInfo holen
				    	try {
				    		ai = mdsRS.aIM.getJson(id);	
				    	} catch (NullPointerException e){
				    		ai = "{}";
				    	};
		    		}		    		
		    		response.setEntity(ai, MediaType.APPLICATION_JSON );
		    	}
		    	// POST
		    	if(request.getMethod().equals(Method.POST)){	
					// Parse the given representation and retrieve pairs of
					// "name=value" tokens.
			    	Representation rep = request.getEntity();
					Form form = new Form(rep);
					JSONObject json = new JSONObject(form.getFirstValue("appinfo"));
					int id = mdsRS.aIM.addObject(json);
					// Sende alle Websocket Clients info ueber neue ID 
					mdsRS.wsserv.notifyWSClients("appinfo", id, "c");
					// Set the response's status and entity
					response.setStatus(Status.SUCCESS_CREATED);
		    	}
		    }
		}; 
		
		
		Restlet player = new Restlet() {  
		    @Override  
		    public void handle(Request request, Response response) {  
		    	//System.out.println(request.getMethod() +  " player : " + request.getAttributes().get("playerid"));
		    	// GET
		    	if(request.getMethod().equals(Method.GET)){
		    		String pl = "";
		    		// wenn keine Attribute angegeben wurden, alle ausgeben
		    		if (request.getAttributes().get("playerid") == null) {
		    			pl = mdsRS.pM.getJson();	
		    		} else { // sonst Element mit der ID ausgeben
				    	int id = Integer.parseInt((String) request.getAttributes().get("playerid"));
				    	//Player holen
				    	try {
				    		pl = mdsRS.pM.getJson(id);
				    	} catch (NullPointerException e) {
				    		pl = "{}";
				    	}
		    		}	
		    		response.setEntity(pl, MediaType.APPLICATION_JSON );
		    	}
		    	// POST
		    	if(request.getMethod().equals(Method.POST)){
					// Parse the given representation and retrieve pairs of
					// "name=value" tokens.
			    	Representation rep = request.getEntity();
					Form form = new Form(rep);
					JSONObject json = new JSONObject(form.getFirstValue("player"));
					int id = mdsRS.pM.addObject(json);
					mdsRS.wsserv.notifyWSClients("player", id, "c");
					// Set the response's status and entity
					response.setStatus(Status.SUCCESS_CREATED);	
		    	}
		    }
		}; 
		
		Restlet game = new Restlet() {  
		    @Override  
		    public void handle(Request request, Response response) {  
		    	//System.out.println(request.getMethod() +  " game : " + request.getAttributes().get("gameid"));
		    	// GET
		    	if(request.getMethod().equals(Method.GET)){
		    		String ga = "";
		    		// wenn keine Attribute angegeben wurden, alle ausgeben
		    		if (request.getAttributes().get("gameid") == null) {
		    			ga = mdsRS.gM.getJson();	
		    		} else { // sonst Element mit der ID ausgeben
				    	int id = Integer.parseInt((String) request.getAttributes().get("gameid"));
				    	//Game holen
				    	try {
				    		ga = mdsRS.gM.getJson(id);
				    	} catch (NullPointerException e){
				    		ga = "{}";
				    	}
		    		}	
		    		response.setEntity(ga, MediaType.APPLICATION_JSON );
		    	}
		    	// POST
		    	if(request.getMethod().equals(Method.POST)){
					// Parse the given representation and retrieve pairs of
					// "name=value" tokens.
			    	Representation rep = request.getEntity();
					Form form = new Form(rep);
					JSONObject json = new JSONObject(form.getFirstValue("game"));
					int id = mdsRS.gM.addObject(json);
					mdsRS.wsserv.notifyWSClients("game", id, "c");
					// Set the response's status and entity
					response.setStatus(Status.SUCCESS_CREATED);	
		    	}
		    }
		};
		
		Restlet item = new Restlet() {  
		    @Override  
		    public void handle(Request request, Response response) { 
		    	//System.out.println(request.getMethod() +  " item : " + request.getAttributes().get("appid"));
		    	// GET
		    	if(request.getMethod().equals(Method.GET)){
		    		String it = "";
		    		
		    		// wenn keine Attribute angegeben wurden, alle ausgeben
		    		if (request.getAttributes().get("itemid") == null) {
		    			it = mdsRS.aIM.getJson();
		    		} else { // sonst Element mit der ID ausgeben
				    	int id = Integer.parseInt((String) request.getAttributes().get("item"));
				    	//AppInfo holen
				    	try {
				    		it = mdsRS.iM.getJson(id);	
				    	} catch (NullPointerException e){
				    		it = "{}";
				    	};
		    		}		    		
		    		response.setEntity(it, MediaType.APPLICATION_JSON );
		    	}
		    	// POST
		    	if(request.getMethod().equals(Method.POST)){	
					// Parse the given representation and retrieve pairs of
					// "name=value" tokens.
			    	Representation rep = request.getEntity();
					Form form = new Form(rep);
					JSONObject json = new JSONObject(form.getFirstValue("item"));
					int id = mdsRS.iM.addObject(json);
					mdsRS.wsserv.notifyWSClients("item", id, "c");
					// Set the response's status and entity
					response.setStatus(Status.SUCCESS_CREATED);
		    		
		    	}
		    }
		}; 
		
		Restlet playerItem = new Restlet(getContext()) {  
		    @Override  
		    public void handle(Request request, Response response) {  
		    	// GET
		    	if(request.getMethod().equals(Method.GET)){
		    		String pl = "";
		    		int playerID = Integer.parseInt((String) request.getAttributes().get("playerid"));
		    		Set<Item> backpack = mdsRS.pM.getBackpack(playerID);
	    			
		    		// wenn keine Attribute angegeben wurden, alle ausgeben
		    		if (request.getAttributes().get("itemid") == null) {
		    			try {
		    			Iterator<Item> it = backpack.iterator();
		    			while(it.hasNext()) {
		    				Item item = it.next();
		    				pl = pl + mdsRS.iM.findObjectById(item.getId()).toString();
		    			}
		    			} catch (NullPointerException e) {
				    		pl = "{}";
				    	
		    			}
		    		} else { // sonst Element mit der ID ausgeben
				    	
				    	//Item holen
				    	try {
				    		int id = Integer.parseInt((String) request.getAttributes().get("itemid"));
				    		Iterator<Item> it = backpack.iterator();
			    			while(it.hasNext()) {
			    				Item item = it.next();
			    				if(id == item.getId())
			    					pl = mdsRS.iM.getJson(item.getId());
			    			}
				    	} catch (NullPointerException e) {
				    		pl = "{}";
				    	}
		    		}	
		    		response.setEntity(pl, MediaType.APPLICATION_JSON );
		    	}
		    	// POST
		    	if(request.getMethod().equals(Method.POST)){
					// Parse the given representation and retrieve pairs of
					// "name=value" tokens.
			    	Representation rep = request.getEntity();
					Form form = new Form(rep);
					JSONObject json = new JSONObject(form.getFirstValue("item"));
					int id = mdsRS.iM.addObject(json);
					mdsRS.wsserv.notifyWSClients("item", id, "c");
					// Set the response's status and entity
					response.setStatus(Status.SUCCESS_CREATED);	
		    	}
		    }
		}; 
		

			
		// Definition der Routen
		router.attach("/appinfo", appinfo);
		
		// Noch nicht ausprogrammiert
		 router.attach("/player", player);
		 router.attach("/game", game);
		 router.attach("/item", item);
		 
		 
		 router.attach("/appinfo/", appinfo);
		 router.attach("/player/", player);
		 router.attach("/game/", game);
		 router.attach("/item/", item);

		
		router.attach("/appinfo/{appid}", appinfo); 
		router.attach("/player/{playerid}", player); 
		router.attach("/game/{gameid}", game); 
		router.attach("/item/{itemid}", game); 
		
		
		router.attach("/player/{playerid}/item/{itemid}", playerItem);  
		router.attach("/player/{playerid}/item", playerItem);  
		
		router.attach("/player/{playerid}/item/", playerItem);  
		
		return router;
		 
	}   
}
