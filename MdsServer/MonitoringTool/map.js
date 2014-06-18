function Map(){

var map;
	var TILE_SIZE = 256;
	var bremen = new google.maps.LatLng(53.05437, 8.78788);
	var player= 1;
	// array for player marker
	var players=[];
	// array for item marker
	var items = [];

	var whiteboard;	
	
	var itemDiv = [];
    var ddDivOptions = {};
    var dropDownOptions = {};
    var dropDown1;
    var dropDown2;
    var first = true;

	// array for DropDown information
	var playersDiv = [];

	// initializing player information window
	function createInfoWindowContent(player) {	
	  if(typeof player.inventory != 'undefined'){
		  return [
	     player.marker.title,
	     'Position: ' + player.marker.position,
		 'Health: ' + player.health,
	     'Inventory: ' + player.inventory
	    
	  ].join('<br>');
	  }
	  return [
	 	     player.marker.title,
	 	     'Position: ' + player.marker.position, 	    
	 	  ].join('<br>');
	}
	
	// major update function
	Map.prototype.update = function (qwhiteboard){	
		whiteboard = qwhiteboard;
		for (var i in whiteboard){
			if(typeof whiteboard[i] === 'object'){				
				updateItems(whiteboard[i]);
				createItemDiv(whiteboard[i]);
				addInfoWindowListener(whiteboard[i]);
			}

		}

		if(typeof whiteboard.Players != 'undefined' && first === true ){
			createItemDropDown();
			first = !first;
		}

		generate_table(whiteboard);

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
	function updateItems(item) {
		
		totalItem = item;
		for (var i in totalItem) {
			if((typeof totalItem[i].latitude !='undefined') &&  (typeof totalItem[i].latitude !='undefined')){
				var myLatlng = new google.maps.LatLng(totalItem[i].latitude, totalItem[i].longitude);
			}
		    if (totalItem[i].marker == null) {// Create new marker if not already exist
		    	var image = new google.maps.MarkerImage(
		    			item[i].imagePath,
		    		    null, /* size is determined at runtime */
		    		    null, /* origin is 0,0 */
		    		    null, /* anchor is bottom center of the scaled image */
		    		    new google.maps.Size(40, 40)
		    		);  
		    	totalItem[i].marker = new google.maps.Marker({
		        	position: myLatlng,
		        	map: map,
		        	animation: google.maps.Animation.DROP,
		        	title: item[i].pathKey,
		        	icon: image
		        });
		    } else { // update the marker
		    	totalItem[i].marker.setPosition(myLatlng);
		    }
		}

	}
	
	
	function addInfoWindowListener(item){
		for(var i in item){
		    google.maps.event.addListener(item[i].marker, 'click', function() { 
		        if (!item[i].marker.infowindow) { // um bestehende infowindows wiederzuverwenden 
		            this.infowindow = new google.maps.InfoWindow({ 
		                content: createInfoWindowContent(item[i])
		            }); 
		        }; 
		        this.infowindow.open(map, this); 
		    });	
		}
	}
	
	// Shows any markers currently in the array.
	function showPlayers(id) {
		//if((typeof players !='undefined')){
			if(document.getElementById(id).style.display == 'block'){
				clearPlayers();
			} else {
				setAllPlayersMap(map, whiteboard.Players);
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
		  if(typeof whichItem[i].marker != 'undefined'){
			  whichItem[i].marker.setMap(map);
		  }
	  }
	}
	
	function clearItems(which) {
		  setAllItemsMap(null, which);
		}
	
	function showItems(id, which) {
		if(document.getElementById(id).style.display == 'block'){
			clearItems(which);
		} else {
			setAllItemsMap(map, which);
		}

	}
	
	// focus on the playermarker
	function centerPlayer(player, map){
		map.panTo(player.position);
	}
	
	function createItemDiv(whichItem){
		for(var i in whichItem){
			var labelName = whichItem[i].title;
			if(whichItem[i].pathKey == 'template'){
				labelName = 'Player'
			}
	        var hideItemOptions = {
	        		gmap: map,
	        		title: "Click to hide Item",
	        		id: 'ID' + whichItem[i].pathKey,
	        		label: "Hide " + labelName,				
	        		action: function(){
	        			showItems(hideItemOptions.id, whichItem);
	        		}        		        		
	        }
	        var check1 = new checkBox(hideItemOptions);
	
	        itemDiv.push(check1);
		break;
		}  
	}
	
    function createItemDropDown(){
    	
        //put them all together to create the drop down       
        ddDivOptions = {
        	items: itemDiv,
        	id: "myddDiv"        		
        }
        itemdropDownDiv = new dropDownOptionsDiv(ddDivOptions);               
                
        dropDownOptions = {
        		gmap: map,
        		name: 'Item Filter',
        		id: 'ddControl',
        		title: 'Click to use several Filters',
        		position: google.maps.ControlPosition.TOP_RIGHT,
        		dropDown: itemdropDownDiv 
        }   
        //Item DropDown
        dropDown2 = new dropDownControl(dropDownOptions);               

	}
	
	
	// initializing the actual map
	Map.prototype.initialize = function (){	
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
        dropDown1 = new dropDownControl(dropDownOptions);
 
        return map;
		
	}
}
//google.maps.event.addDomListener(window, 'load', initialize);
