
$(document).ready(function () {
	log('ready to go');
	var jQueryDocument = $(document);
   if ("ontouchstart" in window) {
	   touch = true;
        jQueryDocument.on("touchstart", start);
        jQueryDocument.on("touchmove", move);
        jQueryDocument.on("touchend", end);
    } else {
        jQueryDocument.on("mousedown", start);
        jQueryDocument.on("mouseup", end);
        jQueryDocument.on("mousemove", move);
    }
   jQueryDocument.resize(function(){
	   screen = {
			   height:$(document).height(),
			   width:$(document).width(),
		};
   });
   log('size {'+screen.width+','+screen.height+'}');
   buildFrame(page,$('#current'));
   
});
function buildFrame(page,dom){
	framedom = $(pages[page].init.html);
	pages[page].dom = framedom;
	dom.append(framedom);
	pages[page].init.setup();
	dom.css({
		width:'95%',
		heigt:screen.height,
	});

}
var touch = false;
var pages = [
             {
            	 id:'settings',
            	 init:init['settings'],
            	 dom:null,
             },
             {
            	 id:'status',
            	 init:init['status'],
            	 dom:null,
             },
             {
            	 id:'inout',
            	 init:init['inout'],
            	 dom:null,
             }
             ];
var page = 1;

var pointer = {
		down:false,
		start:{
			pos:{
				x:0,
				y:0
			}
		},
		current:{
			pos:{
				x:0,
				y:0
			}
		},
		last:{
			pos:{
				x:0,
				y:0
			}
		}
			
}
var screen = {
		height:$(document).height(),
		width:$(document).width(),
		noBounce:true,
}
function getPos(evt){
	if(touch){
		var t = evt.originalEvent.touches[0];
		var ret = {
				x:t.pageX,
				y:t.pageY,
		};
		return ret;
	} else {
		return {
			x:evt.pageX,
			y:evt.pageY
		};
	}
	
}
function start(evt){
	
	pointer.down = true;
	
	pointer.start.pos = getPos(evt);
	pointer.last.pos=pointer.start.pos;
	pointer.current.pos=pointer.start.pos;

}
function move(evt){
	
	if(pointer.down){
		//log('move: '+evt.type+"{"+pointer.current.pos.x+","+pointer.current.pos.y+"}");
		//move
		pointer.current.pos=getPos(evt);
		var delta ={
				x:pointer.current.pos.x-pointer.last.pos.x,
				y:pointer.current.pos.y-pointer.last.pos.y
		}
		var deltas ={
				x:pointer.current.pos.x-pointer.start.pos.x,
				y:pointer.current.pos.y-pointer.start.pos.y
		}
		//log(pointer.delta.pos);
		if(evt.target.parentElement &&
				evt.target.parentElement.className.search('minicolor')!=-1){
			end();
			return;
		}
		$('.content-iframe').animate({left:'+='+delta.x},0);


		var threshold = screen.width*.15
		//log('deltas.x: '+deltas.x+'>'+threshold);
		if(deltas.x>(threshold)){
			//swipe right
			log('swipe right');
			end(evt,{
				direction:'right'
			});
		}else if(deltas.x<(-threshold)){
			//swipe left

			log('swipe left');
			end(evt,{
				direction:'left'
			});
		}
		
	
			
	}
	pointer.last.pos=pointer.current.pos;
	1;
}
function end(evt,opts){
	if(opts){
		swipe(opts);
	} else {
		swipe({direction:''});
	}
	pointer.down = false;
	log('end'+pointer);
	1;
}

function swipe(opts){
	var duration = 500;
	switch(opts.direction){
	case 'left':
		if(pages[page+1]){
			page++;
			var next  = $('#next');
			var frame = buildFrame(page,next);
			next.css({
				left:screen.width,
				
			});
			screen.noBounce=true;
			$('.content-iframe').animate(
					{left:'-='+screen.width},
					{
						duration:duration,
						complete:function(){
							if(screen.noBounce){
								screen.noBounce=false;
								var next = $('#next');
								var current = $('#current');
								current.children().remove();
								current.attr('id','next');
								next.attr('id','current');
							}
						}
					});
		} else {
			//move back to origin
			$('.content-iframe').animate({left:'0px'},duration);
		}
		break;
	case 'right':
		if(pages[page-1]){
			page--;
			var next  = $('#next');
			var frame = buildFrame(page,next);
			
			next.css({
				left:-screen.width,

			});
			next.append(frame);
			screen.noBounce = true;
			$('.content-iframe').animate(
				{left:'+='+screen.width},
				{
					duration:duration,
					complete:function(){
						if(screen.noBounce){
							screen.noBounce=false;
							var next = $('#next');
							var current = $('#current');
							current.children().remove();
							current.attr('id','next');
							next.attr('id','current');
						}
					}
				});
		} else {
			//move back to origin
			$('.content-iframe').animate({left:'0px'},duration);
		}
		break;
	default:
		$('.content-iframe').animate({left:'0px'},duration);
	}
}
function log(msg){
	if(typeof Android != "undefined"){
		Android.log(String(msg));
	} else {
		console.log(msg);
	}
}
function whatsNew(){
	if(typeof Android != "undefined"){
		var lastSeen = Android.getWhatsNew();
	}	
}
