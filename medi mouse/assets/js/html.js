var init = {
		settings: {
			setup:function() {
				loadPrefs();
				//$('.colorpicker').minicolors('create');
				$('#text-color').minicolors({
					defaultValue: user_info['text-color'], 
					change: function(){
						$('.box').css('color',this.val());
						saveInfo({'text-color':this.val()});
					}});
				$('#bkg-color').minicolors({
					defaultValue: user_info['background-color'],
					change: function(){
						$('body').css('background-color',this.val());
						saveInfo({'background-color':this.val()});
					}});
				$('#box-color').minicolors({
					defaultValue: user_info['box-color'],
					change: function(){
						$('.box').css('background-color',this.val());
						saveInfo({'box-color':this.val()});
					}});
				$('#username').val(user_info['username']);
				$('#username').change(function(){
					saveInfo({username:$('#username').val()});
				});
				$('#password').val(user_info['password']);
				$('#password').change(function(){
					saveInfo({password:$('#password').val()});
				});
			},
			html:
"<div class='box'>" +
"<table><tr><td>username</td>" +
"<td><input id='username'></td></tr>" +
"<tr><td>password</td>" +
"<td><input type='password' id='password'></td></tr></table></div>" +
"<div class='box'>" +
"<table><tr>" +
"<td>text color</td>" +
"<td><input id='text-color' class='colorpicker'></td>" +
"</tr><tr>" +
"<td>background color</td>" +
"<td><input id='bkg-color'  class='colorpicker'/></td>" +
"</tr><tr><td>box color</td>" +
"<td><input id='box-color' class='colorpicker'/></td>" +
"</tr></table></div>",
		},
		status: {
			setup:function(){
				loadPrefs();
				
				core.post({
					data : {
						type:'get_status',
						username:user_info.username,
						password:user_info.password,
					},
					success: function(data){
						log('setting up stuff');
						$('#full_name').html(data.full_name);
						$('#status').html(data.status);
					}
				});
				core.post({
					data : {
						type:'get_notes',
						username:user_info.username,
						password:user_info.password,
					},
					success: function(data){
						log('setting up stuff');
						$('#notes').html(data.notes);
					}
				});
				
			},
			html:"<div class='box'><div id='full_name'>your name here</div>" +
					"<div id='status'>a status</div></div>" +
					"<div class='box'><div id='notes'>maybe even some notes</div></div>",

		},
		inout: {
			html:"<div class='canvas' id='canvas'></div>" +
					"<div class='extra_input' id='extra'></div>",
			setup:function(){
				canvas.setup('canvas');
			}
		}

}
var user_info = {
		username:'',
		password:'',
		'text-color':'',
		'box-color':'',
		'background-color':'',
};
function loadPrefs(){
	log('loading info: '+JSON.stringify(user_info));
	if(typeof Android != "undefined"){
		var color = Android.getSetting("text-color");
		$('.box').css('color',color);
		var box_color = Android.getSetting("box-color");
		$('.box').css("background-color",box_color);
		var background_color = Android.getSetting("background-color");
		$('body').css("background-color",background_color);
		
		var username = Android.getSetting("username");
		var password = Android.getSetting("password");
		
		user_info={
				username:username,
				password:password,
				'text-color':color,
				'box-color':box_color,
				'background-color':background_color,
				};
		log('loading info: '+JSON.stringify(user_info));
		return user_info;
		
	} 
}
function saveInfo(data){
	log('saving stuff'+JSON.stringify(data));
	if(typeof Android != "undefined"){
		Android.saveSettings(JSON.stringify(data));
	} 
}