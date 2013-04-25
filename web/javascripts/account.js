/**
 * Created by .
 * User: Patrick
 * Date: Feb 25, 2011
 * Time: 3:55:05 PM
 * To change this template use File | Settings | File Templates.
 */


var interests_selection = [];
var sources_selection = []
var delete_image = "<img src=\"/images/delete.png\" width=\"10\" height=\"10\" />";
var selections;
var max_selection_size = 0;
var bg_colors = [
    "", "#FFFFFF","#FFF68F", "#9BC4E2", "#66FF66", "#FFE1FF"
];


function account_delete(table_id, item_index)  {
 return "<a href=\"javascript:void(0)\" onclick=\"account_delete_item(\'" +
         table_id + "\',\'" + item_index +  "\')\" >" + delete_image + "</a>";
}

function account_delete_item(table_id, item_index) {
  var index = 0;
  var selected = selections[item_index];
  for( var j = 0; j < selections.length; j++) {
      if( selections[j] == selected) {
          index = j;
          break;
      }
  }
  if( index == 0) {
      selections.pop();
  }
  else {
      selections.splice(index, index);
  }


  var table_el = document.getElementById(table_id);
  while( table_el.hasChildNodes()) {
    table_el.removeChild(table_el.lastChild);
  }
  selections = (table_id == 'interests_view_id') ? interests_selection : sources_selection;

  account_load(table_id);
}


function account_load(td_id) {
    var table_el = document.getElementById(td_id);

    if( table_el != null )  {
        for( var j = 0; j < selections.length; j++) {
            var tr_el = document.createElement("tr");
            tr_el.setAttribute('bgcolor', 'white');
            var td_el = document.createElement("td");
            td_el.setAttribute('width', '120');
            td_el.setAttribute('class', 'small');
            td_el.setAttribute('align', 'left');
            td_el.innerHTML =  selections[j];
            tr_el.appendChild(td_el);
            td_el = document.createElement("td");
            td_el.setAttribute('width', '14');
            td_el.innerHTML = account_delete(td_id, j);
            tr_el.appendChild(td_el);
            table_el.appendChild(tr_el);
        }
    }
}

        // Account selection (interests or sources) updates
function account_change_interests(sel) {
    selections = interests_selection;
    account_change(sel, 'interests_view_id');
}

function account_change_sources(sel) {
    selections = sources_selection;
    account_change(sel, 'sources_view_id');
}

function account_change(sel, view_id) {
    if( sel != null ) {
          var my_index = sel.selectedIndex;
          var new_value = sel.options[my_index].value;

          if(new_value != null) {
              var already_exists = false;
              for( var j = 0; j < selections.length; j++) {
                  if(selections[j] == new_value) {
                      already_exists = true;
                      break;
                  }
              }

              if( already_exists == false) {
                    selections.push(new_value);

                  var table_el = document.getElementById( view_id);
                  if( table_el != null )  {
                    var tr_el = document.createElement("tr");
                    var td_el = document.createElement("td");
                    td_el.setAttribute('width', '120');
                    td_el.setAttribute('class', 'small');
                    td_el.innerHTML =  new_value;
                    tr_el.appendChild(td_el);
                    td_el = document.createElement("td");
                    td_el.setAttribute('width', '10');
                    td_el.innerHTML = account_delete(view_id, j);
                    tr_el.appendChild(td_el);
                    table_el.appendChild(tr_el);
                     //  account_set_submit_top();
                  }
              }
              return true;
          }
      }

      return false;
  }




function theme_selection(theme, color) {
    this.theme = theme;
    this.color = color;
}

var cur_theme = new theme_selection(0, '#FFFFFF');


function account_change_theme(index) {

    var display_bg_el = document.getElementById('main-content');

    if( display_bg_el != null) {
        cur_theme.theme = index;

        if( cur_theme.theme == 0 ) {
            display_bg_el.style.background = 'none';
            showImg();
        }
        else if (cur_theme.theme == 1 )  {
            display_bg_el.style.background =  cur_theme.color;
            showImg();
        }

        else {
            display_bg_el.style.background =  cur_theme.color;
            document.body.style.backgroundColor = cur_theme.color;
            document.body.background = 'none';
        }
    }
}


function account_change_color(new_color) {
    if(  cur_theme.theme  > 0) {
        cur_theme.color = new_color;
        var display_bg_el = document.getElementById('main-content');
        if( display_bg_el != null) {
            display_bg_el.style.background = cur_theme.color;
        }
    }
}



function account_change_background(select_el) {

    var my_index = select_el.selectedIndex;
    var display_bg_el = document.getElementById('main-content');
    if( display_bg_el != null) {
        if( my_index == 0) {
            display_bg_el.style.background = "none";
        }
        else {
            display_bg_el.style.background = bg_colors[my_index];
        }
    }
}


var font_size_list = [ '9', '10', '11', '12', '13', '14'];
var font_family_list = [ 'Arial', 'Courier', 'Tahoma', 'Lucinda'];


function font_selection(family, size) {
    this.family = family;
    this.size = size;
}

var cur_font = new font_selection('Tahoma', '11');


function account_display_sample()  {
    return "<span style=\"font-size:" + cur_font.size + "px;font-family:" + cur_font.family + ";color:" + "black" + ";\">Sample<br><i>Edited</i><br><b>Content</b></span>";
}


function account_font_size(select_el) {
    var my_index = select_el.selectedIndex;
    var font_sample_el = document.getElementById('font_sample_id');
    if( font_sample_el != null) {
        cur_font.size = font_size_list[my_index];
        font_sample_el.innerHTML = account_display_sample();
    }
}

function account_font_family(select_el) {
    var my_index = select_el.selectedIndex;
    var font_sample_el = document.getElementById('font_sample_id');
    if( font_sample_el != null) {
        cur_font.family = font_family_list[my_index];
        font_sample_el.innerHTML = account_display_sample();
    }
}