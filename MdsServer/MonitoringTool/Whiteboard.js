function Whiteboard() {
	
	//updating Whiteboard
	Whiteboard.prototype.updateWhiteboard = function (changings, whiteboard, values){			
		for (var i = 0; i < changings.length; i++){
			 var keys = changings[i].split(',');
			 var value = values[i];
			 if(keys != null){
				 whiteboard.setAttribute(whiteboard, keys, value);

			 }			 
		}
	}

		Whiteboard.prototype.setAttribute = function(base, keys, value){
			// Function: createNestedObject( base, names[, value] )
		//   base: the object on which to create the hierarchy
		//   names: an array of strings contaning the names of the objects
		//   value (optional): if given, will be the last object in the hierarchy
		// Returns: the last object in the hierarchy
		    // If a value is given, remove the last name and keep it for later:
		    var lastName = arguments.length === 3 ? keys.pop() : false;
		    // Walk the hierarchy, creating new objects where needed.
		    // If the lastName was removed, then the last object is not set yet:
		    for( var i = 0; i < keys.length; i++ ) {
		        base = base[ keys[i] ] = base[ keys[i] ] || {};
		    }

		    // If a value was given, set it to the last name:
		    if( lastName ) base = base[ lastName ] = value;

		    // Return the last object in the hierarchy:
		    return base;
		};
	
}


