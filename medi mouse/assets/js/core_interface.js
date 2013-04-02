var core = {
	getStatus: function(username,password,success){
		if(typeof Android != "undefined"){
			var data = {
					type:'get_status',
					username:username,
					password:password
			}
			var callback = Android.post(data);
			
			setTimeout(function(){
				core.checkCallback(callback,success,null,1);
				},
				500);
		}	
	},
	post: function(options){
		if(typeof Android != "undefined"){
			log('posting: '+JSON.stringify(options));
			var data = options.data;
			var callback = Android.post(JSON.stringify(data));
			setTimeout(function(){
				core.checkCallback(callback,
						options.success,
						options.failure,
						1);
				},
				500);
		} else {
			//no android interface, fail
		}
	},
	checkCallback: function(callback,success,failure,timeout){
		var ret = eval(callback);
		//log("callback: "+ret);
		var obj = JSON.parse(ret);
		log("callback: "+JSON.stringify(obj));
		if(obj.error){
			if(obj.error='result not done'){
				//exponential callbacks
				var time = 500*Math.pow(2,timeout);
				log('timeout: '+time)
				setTimeout(function(){
					core.checkCallback(callback,success,failure,timeout+1);
				},
				time);
			} else {
				if(failure){
					failure(obj);
				}
			}
		} else {
			if(success){
				success(obj);
			}
		}
	},
	
};
/*


*/