package de.hsbremen.mds.server.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import de.hsbremen.mds.server.valueobjects.AppInfo;

public class AppInfoManager implements ValueObjectInterface{
	
	private Set<AppInfo> appinfos;
	private int id = 0;
	private JSONObject response;
	
	public AppInfoManager() {
		this.appinfos = new HashSet<AppInfo>();
		
		// Zum Testen: HashSet befuellen
		/*
		JSONObject json = new JSONObject("{json:true;}");
		this.addObject(json);
		this.addObject(json);
		*/
		this.updateResponse();
	}
	
	public int addObject(JSONObject json) {
		int oldID = id; 
		this.appinfos.add(new AppInfo(json, id++));
		this.updateResponse();
		return oldID;
		
	}
	
	 public void updateResponse() {
		JSONObject response = new JSONObject();
		Iterator<AppInfo> it = appinfos.iterator();
		while(it.hasNext()) {
			AppInfo ai = it.next();
			response.put(Integer.toString(ai.getId()), ai.getJSON());
			System.out.println(ai.toString());
		}
		this.response = response;
	 }
	
	@SuppressWarnings("unchecked")
	@Override
	public AppInfo findObjectById(int id) {

		Iterator<AppInfo> it = appinfos.iterator();
		int i = 0;
		while(it.hasNext()) {
			AppInfo ai = it.next();
			System.out.println("IT " + i++);
			if ( ai.getId() == id) {
				return ai;
			}
		}
		return null;
		
	}
	
	public String getJson() {
		return this.response.toString();
	}
	
	public String getJson(int id) {
		return this.findObjectById(id).toString();
	}


}
