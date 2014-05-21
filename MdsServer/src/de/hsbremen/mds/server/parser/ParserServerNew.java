
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
	
	private Whiteboard WB = new Whiteboard();

	public ParserServerNew(File jsonFile){
		JSONParser parser = new JSONParser();
		 
		try {
			
			Object obj = parser.parse(new FileReader(jsonFile));
	 
			JSONObject jsonObject = (JSONObject) obj;
		
			WB = parse((JSONObject) obj);
					
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
}
