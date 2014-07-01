package de.hsbremen.mds.server.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.json.JSONObject;

public class MdsFileWriter {
	
	
	private BufferedReader reader = null;
	private PrintWriter writer = null;
	
	private String readLine() throws IOException {
		if (reader != null)
			return reader.readLine();
		else
			return "";
	}

	private void writeLine(String data) {
		if (writer != null)
			writer.println(data);
	}
	
	
	public void writeConfig(JSONObject config) {
		writeLine(config.toString());
		
	}
	
	public JSONObject loadConfig() throws IOException {
		JSONObject config = new JSONObject(readLine());
		return config;
	}
	
	public void openForReading(String filename) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(filename));
	}

	public void openForWriting(String filename) throws IOException {
		writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
	}

	public boolean close() {
		if (writer != null)
			writer.close();

		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				return false;
			}
		}

		return true;
	}
	

}
