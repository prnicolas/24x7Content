// --------------------------------------  ticker.js ----------------------------------------
// Implement ticker tape and associated management functions.
// Patrick Nicolas
var ticker_width='640px';               // width (in pixels)
var ticker_height='22px';               // height
var ticker_color='#000000';            // background color:
var ticker_stopped =true;               // pause on mouseover (true or false)
var ticker_font_family = 'tahoma';      // font for content
var ticker_speed = 3;                   // Ticker speed from 1 (slow) to 5 (fast)

var aw, mq;
var tape_segments_xloc = [];
var tape_segments = [];


            // Implement the click on the ticker tape
            // parameter: event the browser click event to be captured...

function ticker_mark(event) {
    if( event )  {
            // If the click is a left click
        if( event.which == 1) {
            var seed_el = document.getElementById('seed_id');

            if( seed_el != null ) {
                var topic_str =  ticker_select_content(event.layerX).trim();
                if( topic_str.charAt(0) == ' ')  {
                    topic_str = topic_str.substring(1, topic_str.length);
                }
                seed_el.value = topic_str;
                seed_el.onkeyup();
            }
        }
             // If the click is a right-click
        else {
            var element = document.getElementById('ticker');
            popup_show_box(event.clientX, event.clientY, '420', tape_segments);
        }
    }
}


        // Select the context or segment in the ticker tape using the relative location x
        // parameter x relative location

function ticker_select_content(x) {
   var j = 0;
   while( j < tape_segments_xloc.length) {
       if( x < tape_segments_xloc[j]) {
           return tape_segments[j];
       }
       j++;
   }
}

        // Disable the default right side click for the home page to avoid
        // conflict with the mouse action on the tape..

document.oncontextmenu = function() {
    return false;   
}

        // Break down the entire content of the ticker tape into
        // segments or stories.
        // parameter px_len the length in pixel of the tape content
        // parameter content_str entire content in the tape..

function ticker_split_content(px_len, content_str) {
    var av_char_width = px_len/content_str.length;

    var cursor = 0;
    var prevCursor = 0;
    var count = -1;

    while (true) {
        cursor = content_str.indexOf('#', prevCursor);
        if(cursor == -1) {
            break;
        }
        if( count >= 0) {
            var sub_str = content_str.substring(0, cursor);
            tape_segments_xloc[count] =  sub_str.length*av_char_width;
            tape_segments[count] = content_str.substring(prevCursor, cursor-1);
        }
        count++;
        prevCursor = cursor+1;
    }
}




function ticker_start(ticker_id)  {
    var content_str = document.getElementById(ticker_id).value;
    var tick = '<div style="position:relative;width:'+ticker_width+';height:'+ticker_height+';overflow:hidden;background-color:'+ticker_color+'"';

    if (ticker_stopped)  {
        tick += ' onmouseover="ticker_speed=0" onmouseout="ticker_speed=3"';
    }
        
    var fsz = parseInt(ticker_height) - 8;
    tick +='><div id="mq" style="position:absolute;left:0px;top:0px;font-family:'+ticker_font_family+';font-size:'+fsz+'px;color:#FFFF00;white-space:nowrap;"><\/div><\/div>';

    document.getElementById('ticker').innerHTML = tick;
    mq = document.getElementById("mq");
    mq.style.left=(parseInt(ticker_width)+10)+"px";
    mq.style.top = "2px";
    mq.innerHTML='<span id="tx">' + content_str + '<\/span>';
    aw = document.getElementById("tx").offsetWidth;

    ticker_split_content(aw, content_str);
    lefttime=setInterval("ticker_scroll()",50);
}



function ticker_scroll(){
    mq.style.left = (parseInt(mq.style.left)>(-10 - aw)) ?parseInt(mq.style.left)-ticker_speed+"px" : parseInt(ticker_width)+10+"px";
}
