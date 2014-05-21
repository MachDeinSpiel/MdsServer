
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
			
			System.out.println("____________");
			printWhiteboard("", WB);
			
					
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
			if(jo.get(key) instanceof JSONObject) {
				try {
					wb.setAttribute(new WhiteboardEntry(parse((JSONObject) jo.get(key)), "none"), key);
				} catch (InvalidWhiteboardEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
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
			if(wb.getAttribute(key).value instanceof Whiteboard){
				printWhiteboard(keyPath+","+key, (Whiteboard) wb.getAttribute(key).value);
			}else{
				System.out.println(keyPath+ ":"+ wb.getAttribute(key).value.toString());
			}
		}
	}
}
