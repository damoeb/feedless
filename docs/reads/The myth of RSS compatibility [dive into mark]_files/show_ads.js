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

(function(){
function m(b){return b!=null?'"'+b+'"':'""'}function B(b){if(typeof encodeURIComponent=="function"){return encodeURIComponent(b)}else{return escape(b)}}function c(b,a){if(a){window.google_ad_url+="&"+b+"="+a}}function f(b,a){if(a){c(b,B(a))}}function l(b,a,d){if(a&&typeof a=="object"){a=a[d%a.length]}c("color_"+b,a)}function D(b,a){var d=b.screen,g=navigator.javaEnabled(),e=-a.getTimezoneOffset();if(d){c("u_h",d.height);c("u_w",d.width);c("u_ah",d.availHeight);c("u_aw",d.availWidth);c("u_cd",d.colorDepth)}c("u_tz",
e);c("u_his",history.length);c("u_java",g);if(navigator.plugins){c("u_nplug",navigator.plugins.length)}if(navigator.mimeTypes){c("u_nmime",navigator.mimeTypes.length)}}function y(b){b=b.toLowerCase();if(b.substring(0,3)!="ca-"){b="ca-"+b}return b}function G(b,a,d){d=d.substring(0,1000);d=d.replace(/%\w?$/,"");if(b.google_ad_output=="js"&&(b.google_ad_request_done||b.google_radlink_request_done)){a.write('<script language="JavaScript1.1" src='+m(d)+"><\/script>")}else if(b.google_ad_output=="html"){if(b.name!=
"google_ads_frame"){a.write('<iframe name="google_ads_frame" width='+m(b.google_ad_width)+" height="+m(b.google_ad_height)+" frameborder="+m(b.google_ad_frameborder)+" src="+m(d)+' marginwidth="0" marginheight="0" vspace="0" hspace="0" allowtransparency="true" scrolling="no">');a.write("</iframe>")}}else if(b.google_ad_output=="textlink"){a.write('<script language="JavaScript1.1" src='+m(d)+"><\/script>")}}function F(b){var a=null;b.google_ad_frameborder=a;b.google_ad_format=a;b.google_page_url=a;
b.google_language=a;b.google_gl=a;b.google_country=a;b.google_region=a;b.google_city=a;b.google_hints=a;b.google_safe=a;b.google_encoding=a;b.google_ad_output=a;b.google_max_num_ads=a;b.google_ad_channel=a;b.google_contents=a;b.google_alternate_ad_url=a;b.google_alternate_color=a;b.google_color_bg=a;b.google_color_text=a;b.google_color_link=a;b.google_color_url=a;b.google_color_border=a;b.google_color_line=a;b.google_adtest=a;b.google_kw_type=a;b.google_kw=a;b.google_num_radlinks=a;b.google_max_radlink_len=
a;b.google_rl_filtering=a;b.google_rl_mode=a;b.google_rt=a;b.google_ad_type=a;b.google_image_size=a;b.google_feedback=a;b.google_skip=a;b.google_page_location=a;b.google_referrer_url=a;b.google_ad_region=a;b.google_ad_section=a;b.google_bid=a;b.google_cpa_choice=a;b.google_cust_age=a;b.google_cust_gender=a;b.google_cust_interests=a;b.google_cust_id=a;b.google_cust_job=a;b.google_cust_u_url=a;b.google_cust_ch=a;b.google_ed=a;b.google_targeting=a;b.google_ad_host=a}function A(){var b=null,a=window,
d=document,g=new Date,e=g.getTime(),j=a.google_ad_format;if(a.google_cpa_choice){a.google_ad_url="https://web.archive.org/web/20070223120000/http://pagead2.googlesyndication.com/cpa/ads?";a.google_ad_url+="client="+escape(y(a.google_ad_client));a.google_ad_region="_google_cpa_region_";c("cpa_choice",a.google_cpa_choice);if(typeof d.characterSet!="undefined"){f("oe",d.characterSet)}else if(typeof d.charset!="undefined"){f("oe",d.charset)}}else{a.google_ad_url="https://web.archive.org/web/20070223120000/http://pagead2.googlesyndication.com/pagead/ads?";a.google_ad_url+="client="+escape(y(a.google_ad_client))}c("host",
a.google_ad_host);var k=a.google_num_slots_by_client,w=a.google_num_slots_by_channel,i=a.google_prev_ad_formats_by_region;a.onerror=a.google_org_error_handler;if(a.google_ad_region==b&&a.google_ad_section!=b){a.google_ad_region=a.google_ad_section}var h=a.google_ad_region==b?"":a.google_ad_region,q=false;if(j){q=j.indexOf("_0ads")>0}if(q){if(a.google_num_0ad_slots){a.google_num_0ad_slots=a.google_num_0ad_slots+1}else{a.google_num_0ad_slots=1}if(a.google_num_0ad_slots>1){return}}else if(!a.google_cpa_choice){if(a.google_num_ad_slots){a.google_num_ad_slots=
a.google_num_ad_slots+1}else{a.google_num_ad_slots=1}if(a.google_num_slots_to_rotate){i[h]=b;if(a.google_num_slot_to_show==b){a.google_num_slot_to_show=e%a.google_num_slots_to_rotate+1}if(a.google_num_slot_to_show!=a.google_num_ad_slots){return}}else if(a.google_num_ad_slots>3&&h==""){return}}c("dt",g.getTime());c("hl",a.google_language);if(a.google_country){c("gl",a.google_country)}else{c("gl",a.google_gl)}c("gr",a.google_region);f("gcs",a.google_city);f("hints",a.google_hints);c("adsafe",a.google_safe);
c("oe",a.google_encoding);c("lmt",a.google_last_modified_time);f("alternate_ad_url",a.google_alternate_ad_url);c("alt_color",a.google_alternate_color);c("skip",a.google_skip);c("targeting",a.google_targeting);var n=a.google_ad_client;if(!k[n]){k[n]=1;k.length+=1}else{k[n]+=1}if(i[h]){f("prev_fmts",i[h].toLowerCase());if(k.length>1){c("slot",k[n])}}if(j){f("format",j.toLowerCase());if(i[h]){i[h]=i[h]+","+j}else{i[h]=j}}c("num_ads",a.google_max_num_ads);c("output",a.google_ad_output);c("adtest",a.google_adtest);
if(a.google_ad_channel){var r=a.google_ad_channel;f("channel",r);var s="",t=r.split("+");for(var o=0;o<t.length;o++){var p=t[o];if(!w[p]){w[p]=1}else{s+=p+"+"}}f("pv_ch",s)}f("url",a.google_page_url);l("bg",a.google_color_bg,e);l("text",a.google_color_text,e);l("link",a.google_color_link,e);l("url",a.google_color_url,e);l("border",a.google_color_border,e);l("line",a.google_color_line,e);c("kw_type",a.google_kw_type);f("kw",a.google_kw);f("contents",a.google_contents);c("num_radlinks",a.google_num_radlinks);
c("max_radlink_len",a.google_max_radlink_len);c("rl_filtering",a.google_rl_filtering);c("rl_mode",a.google_rl_mode);c("rt",a.google_rt);c("ad_type",a.google_ad_type);c("image_size",a.google_image_size);c("region",a.google_ad_region);c("feedback_link",a.google_feedback);f("ref",a.google_referrer_url);f("loc",a.google_page_location);c("bid",a.google_bid);c("cust_age",a.google_cust_age);c("cust_gender",a.google_cust_gender);c("cust_interests",a.google_cust_interests);c("cust_id",a.google_cust_id);c("cust_job",
a.google_cust_job);c("cust_u_url",a.google_cust_u_url);c("cust_ch",a.google_cust_ch);c("ed",a.google_ed);if(z(a,d)&&d.body){var u=d.body.scrollHeight,v=d.body.clientHeight;if(v&&u){f("cc",Math.round(v*100/u))}}D(a,g);G(a,d,a.google_ad_url);F(a)}function C(b,a,d){A();return true}function z(b,a){return b.top.location==a.location}function x(b,a){var d=a.documentElement;if(z(b,a))return false;if(b.google_ad_width&&b.google_ad_height){var g=1,e=1;if(b.innerHeight){g=b.innerWidth;e=b.innerHeight}else if(d&&
d.clientHeight){g=d.clientWidth;e=d.clientHeight}else if(a.body){g=a.body.clientWidth;e=a.body.clientHeight}if(e>2*b.google_ad_height||g>2*b.google_ad_width){return false}}return true}function E(){var b=window,a=document,d=a.location,g=a.referrer,e=null;b.google_org_error_handler=b.onerror;b.onerror=C;if(b.google_ad_frameborder==e){b.google_ad_frameborder=0}if(b.google_ad_output==e){b.google_ad_output="html"}if(b.google_ad_format==e&&b.google_ad_output=="html"){b.google_ad_format=b.google_ad_width+
"x"+b.google_ad_height}if(b.google_page_url==e){b.google_page_url=g;if(!x(b,a)){b.google_page_url=d;b.google_last_modified_time=Date.parse(a.lastModified)/1000;b.google_referrer_url=g}}else{b.google_page_location=g;if(!x(b,a)){b.google_page_location=d}}if(b.google_num_slots_by_channel==e){b.google_num_slots_by_channel=[]}if(b.google_num_slots_by_client==e){b.google_num_slots_by_client=[]}if(b.google_prev_ad_formats_by_region==e){b.google_prev_ad_formats_by_region=[]}}E();A();
})()

}
/*
     FILE ARCHIVED ON 12:00:00 Feb 23, 2007 AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON 20:30:18 Dec 05, 2020.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
*/
/*
playback timings (ms):
  exclusion.robots: 0.157
  exclusion.robots.policy: 0.144
  RedisCDXSource: 2.154
  esindex: 0.011
  LoadShardBlock: 884.89 (6)
  PetaboxLoader3.datanode: 155.438 (7)
  CDXLines.iter: 191.594 (3)
  load_resource: 228.998
  PetaboxLoader3.resolve: 213.786
*/