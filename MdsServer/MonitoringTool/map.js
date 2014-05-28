	var map;
	var TILE_SIZE = 256;
	var bremen = new google.maps.LatLng(53.05437, 8.78788);
	var player= 1;
	// array for player marker
	var players=[];
	// array for item marker
	var items = [];
	// array for DropDown information
	var playersDiv = [];
    var dropDown1;
 // this.whiteboard = new Whiteboard();

	// initializing player information window
	function createInfoWindowContent(marker) {	
	  return [
	     marker.title,
	    'Position: ' + marker.position,
	    'Backpack: ' + ' ',
		'Team: ' + '  '
		
	  ].join('<br>');
	}
	
	// major update function
	function update(data, qwhiteboard){
		var whiteboard = qwhiteboard;
		updateWhiteboard(data, whiteboard);
		updateMarker(whiteboard);
		updateDropDown(whiteboard);
		
		return whiteboard;
	
	}
	
	//updating Whiteboard
	function updateWhiteboard(changings, whiteboard){
		var value = whiteboard.getValue(changings);
		var keys = changings.split(',');
	
		whiteboard.setAttribute(whiteboard, keys, value);
		console.log(this.whiteboard);
	}
	
	// updating player markers
	function updateMarker(whiteboard) {
		var totalmarkers  = whiteboard.Players;
		for (i = 0; i < totalmarkers; i++) {
			var myLatlng = new google.maps.LatLng(totalmarkers[i].latitude, totalmarkers[i].longitude);
			
		    if (players[i] == null) { // Create new marker if player does not already exists
		        players[i] = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: 'Player ' + totalmarkers[i].id.toString()   
		        });
			    alert('Neuer Spieler: ' + players[i].title);
		        
		    } else { // Update the player marker if already exists
				players[i].setPosition(myLatlng);
		    }
		}
		
		
		playersDiv = null;
		// initializing clicklistener for player markers
		for(var i = 0; i < players.length; i++){
			google.maps.event.addListener(marker, 'click', function() {
			    
				var coordInfoWindow = new google.maps.InfoWindow();
				coordInfoWindow.setContent(createInfoWindowContent(players[i]));
				coordInfoWindow.open(map, players[i]);
			});

			var divOptions = {
					 gmap: map,
					 name: players[i].title,
					 title: "Pan to Player",
					 id: "mapOpt",
					 action: function(){
					     centerPlayer(players[i], map);
					 }
				}
			playersDiv.push(divOptions);

		}
	}
	
	// Updating the itemmarkers
	function updateItems(whiteboard) {
		var totalItems  = whiteboard.Items;
		for (i = 0; i < totalItems; i++) {
			var myLatlng = new google.maps.LatLng(totalItems[i].latitude, totalItems[i].longitude);
			
		    if (items[i] == null) { // Create new marker if not already exist
		        items[i] = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: 'Item ' + totalItems[i].name.toString(),
		        	icon: 'images/bomb.png'
		        });
		        
		    } else { // update the marker
				items[i].setPosition(myLatlng);
		    }
		}

		
		for(var i = 0; i < items.length; i++){
			google.maps.event.addListener(marker, 'click', function() {
			    
				var coordInfoWindow = new google.maps.InfoWindow();
				coordInfoWindow.setContent(createInfoWindowContent(items[i]));
				coordInfoWindow.open(map, items[i]);
			});	
	
		}
	}
	
	// update the dropDown menu on the map
	function updateDropDown(whiteboard){
		dropDown1 = null;
	    var sep = new separator();
	      if(!(playersDiv == null)){
		      //put them all together to create the drop down       
		      var ddDivOptions = {
		      	items: playersDiv,
		      	id: "myddOptsDiv"        		
		      }
	
		      var dropDownDiv = new dropDownOptionsDiv(ddDivOptions);               
		              
		      var dropDownOptions = {
			      gmap: map,
			      name: 'Center Player',
			      id: 'ddControl',
			      title: 'A custom drop down select with mixed elements',
			      position: google.maps.ControlPosition.TOP_RIGHT,
			      dropDown: dropDownDiv 
		      }
		      
		      dropDown1 = new dropDownControl(dropDownOptions);     
	
	      }
	}
	
	//Sets the map on all markers in the array.
	function setAllMap(map) {
	  for (var i = 0; i < players.length; i++) {
	    players[i].setMap(map);
	  }
	}
	//Removes the markers from the map, but keeps them in the array.
	function clearPlayers() {
	  setAllMap(null);
	}
	
	// Shows any markers currently in the array.
	function showPlayers() {
	  setAllMap(map);
	}
	
	// Deletes all markers in the array by removing references to them.
	function deletePlayers() {
	  clearMarkers();
	  players = [];
	}
	
	// focus on the playermarker
	function centerPlayer(player, map){
		map.panTo(player.position);
	}
	
	
	// initializing the actual map
	function initialize() {
		var mapOptions = {
			zoom: 12,
			center: bremen
		};
	
		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
		
		// Hide player button
		var buttonOptions = {
      		gmap: map,
      		name: 'Hide all Players',
      		position: google.maps.ControlPosition.TOP_RIGHT,
      		action: function(){
      			clearPlayers();
      		}
		}
		var button1 = new buttonControl(buttonOptions);

		// show player button
	    var buttonOptions = {
	    	gmap: map,
	      	name: 'Show all Players',
	      	position: google.maps.ControlPosition.TOP_RIGHT,
	      	action: function(){
	      		showPlayers();    			
	      	}
	   }
	   var button1 = new buttonControl(buttonOptions);
	            
	}

google.maps.event.addDomListener(window, 'load', initialize);