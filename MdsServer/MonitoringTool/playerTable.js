


//var whiteboard = this.Whiteboard;

function delete_table()	{

	if (document.getElementById("players_table")) {
		$(document.getElementById("players_table")).remove();
	}
}

function generate_table(whiteboard) {
	
	if (whiteboard) {
		var wb = whiteboard.Players;
		console.log("whiteboard: " + wb);
	}
//	var wp = whiteboard.Players;
//	var wph = whiteboard.Players.health;
//	console.log("whiteboard Players: " + wp);
//	console.log("whiteboard Players health: " + wph);
	
	delete_table();
	// get the reference for the body
	var body = document.getElementsByTagName("body")[0];
 
	// creates the <table> element and creates a <tbody> element
	var tbl = document.createElement("table");
	tbl.setAttribute('id','players_table');
	tbl.setAttribute('border','2');
	console.log(tbl);
  var tblBody = document.createElement("tbody");
  
//  counter (entspricht anz Spieler) and table content
  var tblPlayers = ["player1","p2","p3","p4"];
  
  // creating all cells
  for (var i = 0; i < tblPlayers.length; i++) {
    // creates a table row
    var row = document.createElement("tr");
 
      // Create a <td> element and a text node, make the text
      // node the contents of the <td>, and put the <td> at
      // the end of the table row
      var cell = document.createElement("td");
      var cellText = document.createTextNode(tblPlayers[i]);
      cell.appendChild(cellText);
      row.appendChild(cell);
 
    // add the row to the end of the table body
    tblBody.appendChild(row);
  }
  
 
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
        		// Hier könnte der Aufruf für die Ausgabe des Inventars stehen
        		alert("id:" + id);
        	};
        };
        
        currentRow.onclick = createClickHandler(currentRow);
        
    }
}

function update_table() {
	
}