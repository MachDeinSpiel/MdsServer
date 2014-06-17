function delete_table()	{

	if (document.getElementById("players_table")) {
		$(document.getElementById("players_table")).remove();
	}
}

function generate_table(whiteboard) {
	
	if (whiteboard) {
		var tblContentPlayers = new Array(whiteboard);
		console.log("tblContentPlayers: " + tblContentPlayers[0]);
//		var tblContentPlayers = ["das","ist","ein","test"];		
//		console.log("Players: " + tblContentPlayers);
	}
	
	delete_table();
	
	// get the reference for the body
	var body = document.getElementsByTagName("body")[0];
 
	// creates the <table> element and creates a <tbody> element
	var tbl = document.createElement("table");
	tbl.setAttribute('id','players_table');
	tbl.setAttribute('border','2');
	var tblBody = document.createElement("tbody");
  
//	in case of undefined data
	var noData = ["keine","Daten","verfuegbar"];
  
	// creating all cells
//	bedingung???
	if (whiteboard) {
		for (var i = 0; i <= tblContentPlayers.length; i++) {
			// creates a table row
			var row = document.createElement("tr");
			// Create a <td> element and a text node, make the text
			// node the contents of the <td>, and put the <td> at
			// the end of the table row
//			var cell = document.createElement("td");
			if (i = 0) {
				var cellTextPlayer = document.createTextNode('Player');
				var cellTextHealth = document.createTextNode('health');
				var cellTextInventory = document.createTextNode('inventory');
			} else {
				var cellTextPlayer = document.createTextNode('dummy_' + i);
				var cellTextHealth = document.createTextNode(tblContentPlayers[i].health);
				var cellTextInventory = document.createTextNode(tblContentPlayers[i].inventory);
			}
//			cell.appendChild(cellText);
			var cell = document.createElement("td");
			cell.appendChild(cellTextPlayer);
			row.appendChild(cell);
			cell = document.createElement("td");
			cell.appendChild(cellTextHealth);
			row.appendChild(cell);
			cell = document.createElement("td");
			cell.appendChild(cellTextInventory);
			row.appendChild(cell);
 
			// add the row to the end of the table body
			tblBody.appendChild(row);
		}
	} 
//	else {
//		tblContentPlayers = noData;
//		for (var i = 0; i < tblContentPlayers.length; i++) {
//			// creates a table row
//			var row = document.createElement("tr");
//	 
//			// Create a <td> element and a text node, make the text
//			// node the contents of the <td>, and put the <td> at
//			// the end of the table row
//			var cell = document.createElement("td");
//			var cellText = document.createTextNode(tblContentPlayers[i]);
//			cell.appendChild(cellText);
//			row.appendChild(cell);
//	 
//			// add the row to the end of the table body
//			tblBody.appendChild(row);
//		}	
//	}
 
  // put the <tbody> in the <table>
  tbl.appendChild(tblBody);
  // appends <table> into <body>
  body.appendChild(tbl);
  addRowHandlers();
}

function addRowHandlers() {
	var table = document.getElementById("players_table");
    var rows = table.getElementsByTagName("tr");
    for (i = 0; i < rows.length; i++) {
    	var currentRow = table.rows[i];
        var createClickHandler = function(row) {
        	return function() {
        		var cell = row.getElementsByTagName("td")[0];
        		var id = cell.innerHTML;
        		// Hier k�nnte der Aufruf f�r die Ausgabe des Inventars stehen
        		alert("id:" + id);
        	};
        };
        
        currentRow.onclick = createClickHandler(currentRow);
        
    }
}

function update_table() {
	
}