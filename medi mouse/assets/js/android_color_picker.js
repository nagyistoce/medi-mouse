$(document).ready(function () {
	$('#color-picker').minicolors({
		defaultValue: getDefaultColor(), 
		change: function(){
			saveDefaultColor(this.val());
		},
		
		});
	
	$('body').css('background-color',getBackgroundColor());
});

function getDefaultColor(){
	if(typeof Android != "undefined"){
		return Android.getDefaultColor();
	}
}

function saveDefaultColor(value){
	if(typeof Android != "undefined"){
		return Android.saveDefaultColor(value);
	}
}
function getBackgroundColor(){
	if(typeof Android != "undefined"){
		return Android.getBackgroundColor();
	}
	
}
function redrawUI(){
	if(typeof Android != "undefined"){
		return Android.redrawUI();
	}
}
