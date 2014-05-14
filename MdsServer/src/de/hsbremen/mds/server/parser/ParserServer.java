
package de.hsbremen.mds.server.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class ParserServer {
	
	private Whiteboard wb = new Whiteboard();

	public ParserServer(File jsonFile){
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader(jsonFile));
			 
			JSONObject jsonObject = (JSONObject) obj;
			
			JSONArray groups = (JSONArray) jsonObject.get("groups");
			for(int i = 0; i < groups.size(); i++) {
				JSONObject groupsElement = (JSONObject) groups.get(i);
				
				if(groupsElement.get("members") != null) {
					JSONArray members = (JSONArray) groupsElement.get("members");
					for(int j = 0; j < members.size(); j++) {
						JSONObject membersElement = (JSONObject) members.get(j);
						if(membersElement.get("name") != null) {
							writeValue(membersElement, membersElement.get("name").toString());
						}
						else writeValue(membersElement, "members");
					}
				}
			}
			
			
			/*
			JSONArray groups = (JSONArray) jsonObject.get("groups");
			for(int i = 0; i < groups.size(); i++) {
				JSONObject groupsElement = (JSONObject) groups.get(i);
				
				strings.add(groupsElement.get("name").toString());
				if(groupsElement.get("members") != null) {
					JSONArray members = (JSONArray) groupsElement.get("members");
					for(int j = 0; j < members.size(); j++) {
						List<String> stringsCopy = new Vector<String>();
						stringsCopy.addAll(strings);
						JSONObject membersElement = (JSONObject) members.get(j);
						
						strings.add(membersElement.get("name").toString());
						if(membersElement.get("params") != null) {
							writeValue((JSONObject) membersElement.get("params"), stringsCopy);
							
						}
					}
				}
			}
			*/
			
			
			/*
			MdsGroup[] allMdsGroups = new MdsGroup[groups.size()];
			
			for(int i = 0; i < groups.size(); i++) {
				JSONObject groupsElement = (JSONObject) groups.get(i);
				
				String name = groupsElement.get("name").toString();
				
				JSONArray members = (JSONArray) groupsElement.get("members");
				
				MdsMembers[] membersArray = new MdsMembers[members.size()];
				
				for(int j = 0; j < members.size(); j++) {
					
					JSONObject element = (JSONObject) members.get(j);
					
					HashMap<Object, Object> membersMap = new HashMap<Object, Object>();
					
					Set<Object> keySet = element.keySet();
					
					// die param werte aus dem KeySet werden dem params HashMap übergeben
					for (Object key : keySet){
						Object value = element.get(key);
						membersMap.put(key, value);
					}
					
					membersArray[j] = new MdsMembers(membersMap);
				}
				
				allMdsGroups[i] = new MdsGroup(name, membersArray);
			}
			*/
			// Ausgabe
			
			System.out.println(wb.toString());
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	/*
	public void writeValue(JSONObject jo, List<String> stringsCopy) {
		//JSONObject jo = (JSONObject) object;
		// HashMap mit parametern füllen
		HashMap<String, Object> paramsHM = new HashMap<String, Object>();
		Set<String> keySet = jo.keySet();
		for (String key : keySet){
			Object value = jo.get(key);
			stringsCopy.add(key);
			if(key.equals("params") || key.equals("useAction")) {
				writeValue((JSONObject) value, stringsCopy);
			}
			else {
				String[] keys = new String[0];
				WhiteboardEntry wbe = new WhiteboardEntry(value, "none");
				wb.setAttribute(wbe, stringsCopy.toArray(keys)); 
				stringsCopy.remove(stringsCopy.size()-1);
			}
		}
	}
	*/
	public void writeValue(JSONObject jo, String name) {
		if(jo.get("params") != null || jo.get("useAction") != null) {
			if(jo.get("params") != null)
				writeValue((JSONObject) jo.get("params"), jo.get("name").toString());
			else {
				JSONArray useActionArray = (JSONArray) jo.get("useAction");
				for(int i = 0; i < useActionArray.size(); i++) {
					JSONObject useActionElement = (JSONObject) useActionArray.get(i);
					writeValue(useActionElement, "useAction");
				}
			}
		}
		HashMap<String, Object> paramsHM = new HashMap<String, Object>();
		Set<String> keySet = jo.keySet();
		for (String key : keySet){
			Object value = jo.get(key);
			paramsHM.put(key, value);
		}
		WhiteboardEntry wbe = new WhiteboardEntry(paramsHM, "none");
		wb.setAttribute(wbe, name); 
	}
	
	public Whiteboard getWhiteboard() {
		return wb;
	}
}
