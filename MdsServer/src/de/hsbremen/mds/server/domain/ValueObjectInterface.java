package de.hsbremen.mds.server.domain;

import de.hsbremen.mds.server.valueobjects.Player;

interface ValueObjectInterface {

	
	
	public <MDSObject> MDSObject findObjectById(int id);
	
	public void updateResponse();
	public String getJson();
	public String getJson(int id);


}
