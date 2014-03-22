package de.hsbremen.mds.server.domain;

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

public class MDSRouteService extends Application {

	
	private AppInfoManager aIM;
	private GameManager gM;
	private PlayerManager pM;
	
	public MDSRouteService(AppInfoManager aIM, GameManager gM, PlayerManager pM) {
		this.aIM = aIM;
		this.gM = gM;
		this.pM = pM;

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
					mdsRS.aIM.addObject(json);

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
					mdsRS.pM.addObject(json);

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
					mdsRS.gM.addObject(json);

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

		
		router.attach("/appinfo/{appid}", appinfo); 
		router.attach("/player/{playerid}", player); 
		router.attach("/game/{gameid}", game); 
		return router;
		 
	}   
}