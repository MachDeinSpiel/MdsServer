package de.hsbremen.mds.server.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import de.hsbremen.mds.server.valueobjects.AppInfo;

public class AppInfoManager implements ValueObjectInterface{
	
	private Set<AppInfo> appinfos;
	private int id = 0;
	private String response;
	
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
		this.appinfos.add(new AppInfo(json, id++));
		this.updateResponse();
		return id;
		
	}
	
	 public void updateResponse() {
		String response = "{";
		Iterator<AppInfo> it = appinfos.iterator();
		while(it.hasNext()) {
			AppInfo ai = it.next();
			//TODO: letztes Komma weglassen!
			response = response + ai.toString() +",";
			System.out.println(ai.toString());
		}
		response = response + "}";
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
		return this.response;
	}
	
	public String getJson(int id) {
		return this.findObjectById(id).toString();
	}


}
