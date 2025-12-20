var _____WB$wombat$assign$function_____ = function(name) {return (self._wb_wombat && self._wb_wombat.local_init && self._wb_wombat.local_init(name)) || self[name]; };
if (!self.__WB_pmw) { self.__WB_pmw = function(obj) { this.__WB_source = obj; return this; } }
{
  let window = _____WB$wombat$assign$function_____("window");
  let self = _____WB$wombat$assign$function_____("self");
  let document = _____WB$wombat$assign$function_____("document");
  let location = _____WB$wombat$assign$function_____("location");
  let top = _____WB$wombat$assign$function_____("top");
  let parent = _____WB$wombat$assign$function_____("parent");
  let frames = _____WB$wombat$assign$function_____("frames");
  let opener = _____WB$wombat$assign$function_____("opener");

window.wpcom=window.wpcom||{};window._stq=window._stq||[];function st_go(a){window._stq.push(['view',a]);};function linktracker_init(b,p){window._stq.push(['clickTrackerInit',b,p]);};window.wpcom.stats=(function(){var _clickTracker=(function(){var _blog,_post;var _addEvent=function(el,t,cb){if('function'===typeof el.addEventListener){el.addEventListener(t,cb);}else if('object'===typeof el.attachEvent){el.attachEvent('on'+t,cb);}};var _getClickTarget=function(e){if('object'===typeof e&&e.target){return e.target;}else{return window.event.srcElement;}};var _clickTrack=function(e){var d=0;if('object'===typeof InstallTrigger)d=100;if(7===_getIEVer())d=100;_processLink(_getClickTarget(e),d);};var _contextTrack=function(e){_processLink(_getClickTarget(e),0);};var _isSameHost=function(a){var l=document.location;if(l.host===a.host)return true;if(''===a.host)return true;if(l.protocol===a.protocol&&l.host===a.hostname){if('http:'===l.protocol&&l.host+':80'===a.host)return true;if('https:'===l.protocol&&l.host+':443'===a.host)return true;};return false;};var _processLink=function(a,d){try{if('object'!==typeof a)return;while('A'!==a.nodeName){if('undefined'===typeof a.nodeName)return;if('object'!==typeof a.parentNode)return;a=a.parentNode;};if(_isSameHost(a))return;if('javascript:'===a.protocol)return;window._stq.push(['click',{s:'2',u:a.href,r:('undefined'!==typeof a.rel)?a.rel:'0',b:('undefined'!==typeof _blog)?_blog:'0',p:('undefined'!==typeof _post)?_post:'0'}]);if(d){var now=new Date();var end=now.getTime()+d;while(true){now=new Date();if(now.getTime()>end){break}}}}catch(e){}};var API={init:function(b,p){_blog=b;_post=p;if(document.body){_addEvent(document.body,'click',_clickTrack);_addEvent(document.body,'contextmenu',_contextTrack);}else if(document){_addEvent(document,'click',_clickTrack);_addEvent(document,'contextmenu',_contextTrack);}}};return API;})();var _getIEVer=function(){var v=0;if('object'===typeof navigator&&navigator.appName=='Microsoft Internet Explorer'){var m=navigator.userAgent.match(/MSIE ([0-9]{1,})[\.0-9]{0,}/);if(null!==m){v=parseInt(m[1]);}};return v;};var _serialize=function(o){var p,q=[];for(p in o){if(o.hasOwnProperty(p)){q.push(encodeURIComponent(p)+'='+encodeURIComponent(o[p]));}};return q.join('&');};var _loadGif=function(t,q,id){var i=new Image();i.src=document.location.protocol+'//web.archive.org/web/20170602203248/http://pixel.wp.com/'+t+'?'+q+'&rand='+Math.random();i.alt=":)";i.width='6';i.height='5';if('string'===typeof id&&document.body){i.id=id;document.body.appendChild(i);}};var STQ=function(q){this.a=1;if(q&&q.length){for(var i=0;i<q.length;i++){this.push(q[i]);}}};STQ.prototype.push=function(args){if(args){if("object"===typeof args&&args.length){var cmd=args.splice(0,1);if(API[cmd])API[cmd].apply(null,args);}else if("function"===typeof args){args();}}};var initQueue=function(){if(!window._stq.a){window._stq=new STQ(window._stq);}};var API={view:function(o){o.host=document.location.host;o.ref=document.referrer;_loadGif('g.gif',_serialize(o),'wpstats');},click:function(o){_loadGif('c.gif',_serialize(o),false);},clickTrackerInit:function(b,p){_clickTracker.init(b,p);}};var isDocumentHidden=function(){return typeof document.hidden!=="undefined"&&document.hidden;};var onDocumentVisibilityChange=function(){if(!document.hidden){document.removeEventListener('visibilitychange',onDocumentVisibilityChange);initQueue();}};var initQueueAfterDocumentIsVisible=function(){document.addEventListener('visibilitychange',onDocumentVisibilityChange);};if(6===_getIEVer()&&'complete'!==document.readyState&&'object'===typeof document.attachEvent){document.attachEvent('onreadystatechange',function(e){if('complete'===document.readyState)window.setTimeout(initQueue,250);});}else{if(isDocumentHidden()){initQueueAfterDocumentIsVisible();}else{initQueue();}};return API;})();

}
/*
     FILE ARCHIVED ON 20:32:48 Jun 02, 2017 AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON 21:21:46 Feb 22, 2021.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
*/
/*
playback timings (ms):
  exclusion.robots.policy: 0.191
  captures_list: 185.021
  exclusion.robots: 0.206
  RedisCDXSource: 20.247
  PetaboxLoader3.resolve: 143.697
  esindex: 0.012
  CDXLines.iter: 15.946 (3)
  LoadShardBlock: 98.234 (3)
  PetaboxLoader3.datanode: 122.33 (4)
  load_resource: 205.114
*/