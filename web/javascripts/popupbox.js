// -------------------------------- popupbox.js -------------------------------------
// Implement the right click popup window
// Patrick Nicolas 01/08/2011

var title_style = "font-family:Tahoma;color:#444444;font-size:16px;text-align:center";
var title = "Trendy topics ....";
var topic_style = "color:#444444;font-size:13px;text-align:center";


function popup_select_segment(str) {
    var box_el = document.getElementById('topic_box_id');
    if( box_el != null) {
        document.body.removeChild(box_el) ;
    }
    var seed_el = document.getElementById('seed_id');
    if( seed_el != null ) {
        seed_el.value = str;
        seed_el.onkeyup();
    }
}

function popup_show_box(xAnchor, yAnchor, width, content_array) {

    if( document.getElementById('topic_box_id') == null) {
        var box_div = document.createElement('div');
        box_div.setAttribute('id', 'topic_box_id');
        box_div.style.display = 'block';
        box_div.style.position = 'absolute';
        box_div.style.border = 'solid';
        box_div.style.backgroundColor = '#EEEEEE';

        var table_el = document.createElement('table');
        table_el.setAttribute('width', '360');
        table_el.setAttribute('bgcolor', 'white');
        var tr_el = document.createElement('tr');
        var td_el = document.createElement('td');
        td_el.setAttribute('align', 'center');
        td_el.setAttribute('class', 'large');
        td_el.innerHTML = title;
        tr_el.appendChild(td_el);
        table_el.appendChild(tr_el);
        var k = 0;

        for( j = 0; j < content_array.length; j++) {
            if(content_array[j] != null && content_array[j].length > 8)   {
                var str = content_array[j].slice(0,32) + "....";
                k++;
                tr_el = document.createElement('tr');
                tr_el.setAttribute('bgcolor', '#E0E0E0');
                td_el = document.createElement('td');
                td_el.setAttribute('class', 'small');
                td_el.innerHTML = "<a href=\"javascript:void(0)\" onclick=\"popup_select_segment(\'" + str + "\')\">" + k + ". " + str + "</a><br>";
                tr_el.appendChild(td_el);
                table_el.appendChild(tr_el);
            }
        }

        tr_el = document.createElement("tr");
        td_el = document.createElement("td");
        td_el.setAttribute('class', 'std');
        td_el.setAttribute('align', 'center');
        td_el.innerHTML =  "<a href=\"javascript:void(0)\" onclick=\"popup_select_segment(\'none\')\" >Cancel</a>";
        tr_el.appendChild(td_el);

        table_el.appendChild(tr_el);
        box_div.appendChild(table_el);
        document.body.appendChild(box_div);

        box_div.style.left = xAnchor/2 + 8 + 'px';
        box_div.style.top =  yAnchor + 30 + 'px';
    }

    return false
}



