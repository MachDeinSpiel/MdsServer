	var map;
	var TILE_SIZE = 256;
	var bremen = new google.maps.LatLng(53.05437, 8.78788);
	var player= 1;
	// array for player marker
	var players=[];
	// array for item marker
	var items = [];

	var whiteboard;
	var totalPlayerMarker;
	var totalItemMarker;
	
    var dropDown1;
	
	var itemDiv = [];
	var totalPLayer;
	var totalItem;
	
	var message = null;

	// array for DropDown information
	var playersDiv = [];
    var dropDown1;

	// initializing player information window
	function createInfoWindowContent(player) {	
	  return [
	     player.marker.title,
	    'Position: ' + player.marker.position,
	    'Health: ' + player.health,
		'Team: ' + '  '
		
	  ].join('<br>');
	}
	
	// major update function
	function update(changings, qwhiteboard, values){
		whiteboard = qwhiteboard;
		whiteboard.updateWhiteboard(changings, whiteboard, values);
		updatePlayers(whiteboard);
		updateItems(whiteboard.Bombs, 'bomb', 0);
		updateItems(whiteboard.Medipacks, 'medi', 1);
		//for(var i = 1;  i < whiteboard.length; i++){
			console.log("Ich werde ausgeführt");
			createItemDropDown(whiteboard.Bombs);
			createItemDropDown(whiteboard.Medipacks);
		//}
		
		return whiteboard;
	
	}
	
	// updating player markers
	function updatePlayers(whiteboard) {
		totalPlayer  = whiteboard.Players;
		totalPlayerMarker = 0;
		for (var i in totalPlayer) {
			var myLatlng = null;
			if((typeof totalPlayer[i].latitude !='undefined') &&  (typeof totalPlayer[i].latitude !='undefined')){
				myLatlng = new google.maps.LatLng(totalPlayer[i].latitude, totalPlayer[i].longitude);
			}
		    if (totalPlayer[i].marker == null) { // Create new marker if player does not already exists
		    	totalPlayer[i].marker = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: 'Player ' + totalPlayer[i].health.toString()
		        });
			    alert('Neuer Spieler: ' + totalPlayer[i].marker.title);
		        
		    } else { // Update the player marker if already exists
		    	totalPlayer[i].marker.setPosition(myLatlng);
		    }
		

	    google.maps.event.addListener(totalPlayer[i].marker, 'click', function() { 
			console.log(totalPlayer[i].marker);
	        if (!totalPlayer[i].marker.infowindow) { // um bestehende infowindows wiederzuverwenden 
	            this.infowindow = new google.maps.InfoWindow({ 
	                content: createInfoWindowContent(totalPlayer[i])
	            }); 
	        }; 
	        this.infowindow.open(map, this); 
	    });	
	    totalPlayerMarker = totalPlayerMarker + 1;
		}
	}
	
	// Updating the itemmarkers
	function updateItems(item, icon, count) {

		totalItem = item;
		totalItemMarker = 0;
		for (var i in totalItem) {
			if((typeof totalItem[i].latitude !='undefined') &&  (typeof totalItem[i].latitude !='undefined')){
				var myLatlng = new google.maps.LatLng(totalItem[i].latitude, totalItem[i].longitude);
			}
		    if (totalItem[i].marker == null) { // Create new marker if not already exist
		    	totalItem[i].marker = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: 'Item ' + totalItem[i].toString(),
		        	icon: 'images/'+ icon + '.png'
		        });
		    } else { // update the marker
		    	totalItem[i].marker.setPosition(myLatlng);
		    }
	    	console.log("Marker: " + totalItem[i].marker);

		    google.maps.event.addListener(totalItem[i].marker, 'click', function() { 
				console.log(totalItem[i].marker);
		        if (!totalItem[i].marker.infowindow) { // um bestehende infowindows wiederzuverwenden 
		            this.infowindow = new google.maps.InfoWindow({ 
		                content: createInfoWindowContent(totalItem[i])
		            }); 
		        }; 
		        this.infowindow.open(map, this); 
		    });	
		    totalItemMarker = totalItemMarker + 1;
		}

	}
	
	
	// Shows any markers currently in the array.
	function showPlayers(id) {
		//if((typeof players !='undefined')){
			if(document.getElementById(id).style.display == 'block'){
				clearPlayers();
				console.log("block");
			} else {
				setAllPlayersMap(map, whiteboard.Players);
				console.log("none");
			}
		//}
	}
	
	//Shows all Players with Health lower than 20
	function showLowHealthPlayers(id){
		//if((typeof players !='undefined')){
			if(document.getElementById(id).style.display == 'block'){
				playersLow = {};
				for(var i in whiteboard.Players){
					if(whiteboard.Players[i].health <= 20){
						playersLow.push(whiteboard.Players[i]);
					}
				}
				if(playersLow[0] != null){
					setAllPlayersMap(map, playersLow);
					clearPlayers();
				}
			} else {
				setAllPlayersMap(map, whiteboard.Players);
			}
		//}
	}
	
	//Sets the map on all markers in the array.
	function setAllPlayersMap(map, playersTotal) {
	  for (var i in playersTotal) {
	    playersTotal[i].marker.setMap(map);
	  }
	}

	//Removes the markers from the map, but keeps them in the array.
	function clearPlayers() {
	  setAllPlayersMap(null, whiteboard.Players);
	}
	
	
	//Sets the map on all markers in the array.
	function setAllItemsMap(map, whichItem) {
	  for (var i in whichItem) {
	    whichItem[i].marker.setMap(map);
	  }
	}
	
	function clearItems(which) {
		  setAllItemsMap(null, which);
		}
	
	function showItems(id, which) {
		if(document.getElementById(id).style.display == 'block'){
			clearItems(which);
			console.log("block");
		} else {
			setAllItemsMap(map, which);
			console.log("none");
		}

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
	
	function createItemDropDown(whichItem){
        var hideItemOptions = {
        		gmap: map,
        		title: "Click to hide Item",
        		id: "Item" + whichItem.health,
        		label: "Hide Item",				
        		action: function(){
        			showItems(hideItemOptions.id, whichItem);
        		}        		        		
        }
        var check1 = new checkBox(hideItemOptions);

        itemDiv.push(check1);
        //create the input box items
        
        //put them all together to create the drop down       
        var ddDivOptions = {
        	items: itemDiv,
        	id: "myddDiv"        		
        }
        //alert(ddDivOptions.items[1]);
        var itemdropDownDiv = null;
        itemdropDownDiv = new dropDownOptionsDiv(ddDivOptions);               
                
        var dropDownOptions = {
        		gmap: map,
        		name: 'Item Filter',
        		id: 'ddControl',
        		title: 'Click to use several Filters',
        		position: google.maps.ControlPosition.TOP_RIGHT,
        		dropDown: itemdropDownDiv 
        }   
        //Item DropDown
        dropDown1 = null;
        dropDown1 = new dropDownControl(dropDownOptions);               

	}
	
	
	// initializing the actual map
	function initialize() {
		var mapOptions = {
			zoom: 14,
			center: bremen
		};
	
		map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);

        var hidePlayersOptions = {
        		gmap: map,
        		title: "Click to hide all active Players",
        		id: "terrainCheck",
        		label: "Hide Players",				
        		action: function(){
        			showPlayers(hidePlayersOptions.id);
        		}        		        		
        }
        var check1 = new checkBox(hidePlayersOptions);
        
        var checkOptions2 = {
        		gmap: map,
        		title: "Click to show Players with low Health",
        		id: "myCheck",
        		label: "Low Health",
        		action: function(){
        			showLowHealthPlayers(checkOptions2.id);
        		}        		        		
        }
        var check2 = new checkBox(checkOptions2);
        
        //create the input box items
        
        //put them all together to create the drop down       
        var ddDivOptions = {
        	items: [check1, check2],
        	id: "myddOptsDiv"        		
        }
        //alert(ddDivOptions.items[1]);
        var dropDownDiv = new dropDownOptionsDiv(ddDivOptions);               
                
        var dropDownOptions = {
        		gmap: map,
        		name: 'Player Filter',
        		id: 'ddControl',
        		title: 'Click to use several Filters',
        		position: google.maps.ControlPosition.TOP_RIGHT,
        		dropDown: dropDownDiv 
        }   
        //Player DropDown
        var dropDown1 = new dropDownControl(dropDownOptions);
 
	}

google.maps.event.addDomListener(window, 'load', initialize);