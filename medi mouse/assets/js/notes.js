$(document).ready(function () {
	loadNotes();
	$('#save').click(function (){
		saveNotes();
	});
});
function loadNotes(){
	if(typeof Android != "undefined"){
		notes = Android.getNotes();
		$('#notes').html(notes);
	} else {
		$('#notes').html('pooop');
	}
}
function returnNotes(){
	
	if(typeof Android != "undefined"){
		Android.returnNotes($('#notes').html());
		
	} 
}
function saveNotes(){
	if(typeof Android != "undefined"){
		Android.saveNotes($('#notes').html());
	} else {
		alert($('#notes').html());
	}
}