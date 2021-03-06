
package de.hsbremen.mds.server.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.hsbremen.mds.common.whiteboard.InvalidWhiteboardEntryException;
import de.hsbremen.mds.common.whiteboard.Whiteboard;
import de.hsbremen.mds.common.whiteboard.WhiteboardEntry;

public class ParserServerNew {
	
	private Whiteboard WB = null;

	public ParserServerNew(File jsonFile){
		JSONParser parser = new JSONParser();
		System.out.println("____________");
		try {
			
			Object obj = parser.parse(new FileReader(jsonFile));
	 
			JSONObject jsonObject = (JSONObject) obj;
			
//			Set<String> keys = ((JSONObject)((JSONObject)((JSONObject)((JSONObject)jsonObject.get("Bombs")).get("Bomb1")).get("useAction")).get("removeFromGroup")).keySet();
//			for(String s : keys){
//				System.out.println(s);
//			}
		
			WB = parse((JSONObject) obj);
			
			
			printWhiteboard("", WB);
			System.out.println("------------");
			
					
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
	
	public Whiteboard parse(JSONObject jo) {
		Whiteboard wb = new Whiteboard();
		Set<String> keySet = jo.keySet();
		for (String key : keySet){
			//System.out.print("parse Key:["+key+"]");
			if(jo.get(key) instanceof JSONObject) {
				//System.out.print(" -> ist JSONObject \n");
				try {
					wb.setAttribute(new WhiteboardEntry(parse((JSONObject) jo.get(key)), "none"), key);
				} catch (InvalidWhiteboardEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				//System.out.print(" -> ist value \n");
				try {
					wb.setAttribute(new WhiteboardEntry(jo.get(key).toString(), "none"), key);
				} catch (InvalidWhiteboardEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return wb;
	}
	
	public Whiteboard getWB() {
		return WB;
	}
	
	
	public void printWhiteboard(String keyPath, Whiteboard wb){
		for(String key : wb.keySet()){
			if(!(wb.getAttribute(key).value instanceof String)){
				if (key.equals("inventory")) 
					System.out.println(keyPath+","+key+ ":"+ wb.getAttribute(key).value.toString());
				else 
					printWhiteboard(keyPath+","+key, (Whiteboard) wb.getAttribute(key).value);
			}else{
				System.out.println(keyPath+","+key+ ":"+ wb.getAttribute(key).value.toString());
			}
		}
	}
}
