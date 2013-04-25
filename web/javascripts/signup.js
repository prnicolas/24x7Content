// Copyright (C) 2011 Patrick Nicolas
// Event handlers and validation routines in javascript for signup process


var email_login = [
    "Address", "Password", "Recipient"
]

var web_login = [
    "User", "Password", "URL", "Server", "File"
]
var blog_login = [
    "Account", "Password", "URL"
]

var oauth_w;

var auth_networks = [
   "facebook", "twitter"
];



function signup_oauth_auth(form, target_type) {
    form.selection.value= target_type;
    form.submit();
}


function signup_clear_authbox() {
    var form_el = document.getElementById('social_login_id');
    var table_el = document.getElementById('auth_box_tbl_id');
    if(  table_el != null ) {
        while( form_el.hasChildNodes()) {
            form_el.removeChild(form_el.lastChild);
        }
    }
}


function signup_create_auth(disabled, login_id, title) {

    var form_el = document.getElementById(login_id);
    var table_el = document.getElementById('auth_box_tbl_id');
    if( table_el != null ) {
          while( form_el.hasChildNodes()) {
            form_el.removeChild(form_el.lastChild);
        }
    }

    if( true ) {
        form_el = document.getElementById(login_id);

        var hidden_el = document.createElement("input");
        hidden_el.setAttribute('type', 'hidden');
        hidden_el.setAttribute('name', 'type');
        hidden_el.setAttribute('value', title);
        form_el.appendChild(hidden_el);

        table_el = document.createElement("table");
        table_el.setAttribute('id', 'auth_box_tbl_id');
        table_el.setAttribute('bgcolor', '#E0E0E0');
        table_el.setAttribute('width', '290');

        var tr_el = document.createElement('tr');
        tr_el.setAttribute('class', 'large');

        var td_el = document.createElement('td');
        td_el.setAttribute('width', '60');
        var image_file_name = "/images/logos" + title.toLowerCase() + "_logo.png";
        var logo_img = document.createElement("img");
        logo_img.setAttribute("src", image_file_name);
        logo_img.setAttribute("alt", "social media logo");
        logo_img.setAttribute("width", "32");
        logo_img.setAttribute("height", "32");
        logo_img.style.display ="block";
        td_el.appendChild(logo_img);
        tr_el.appendChild(td_el);

        td_el = document.createElement('td');
        td_el.setAttribute('class', 'large');
        td_el.innerHTML = title;
        tr_el.appendChild(td_el);
        table_el.appendChild(tr_el);

        switch ( title) {
            case 'email':
                signup_create_any_auth(table_el, email_login);
                signup_create_submit_auth(table_el);
                break;

            case 'blog':
                signup_create_any_auth(table_el, blog_login);
                signup_create_submit_auth(table_el);
                break;

            case 'web':
                signup_create_any_auth(table_el, web_login);
                signup_create_submit_auth(table_el);
                break;

            default:
                return;
        }

        form_el.appendChild(table_el);
    }
 }


function signup_create_any_auth(table_el, any_login) {
    var tr_el;
    var td_el;

    for(var j = 0; j < any_login.length; j++ ) {
        tr_el = document.createElement('tr');
        td_el = document.createElement('td');
        td_el.setAttribute('class', 'std');
        td_el.innerHTML = any_login[j];
        tr_el.appendChild(td_el);

        td_el = document.createElement('td');
        var input_el = document.createElement('input');
        var type_str = (any_login[j] == 'Password') ? 'password' : 'text';
        input_el.setAttribute('type', type_str);
        input_el.setAttribute('class', 'content');
        var input_name = any_login[j].toLowerCase();
        input_el.setAttribute('name', input_name);
        input_el.setAttribute('size', '20');
        input_el.setAttribute('maxlength', '32');
        td_el.appendChild(input_el);
        tr_el.appendChild(td_el);

        table_el.appendChild(tr_el);
    }
}

function signup_add_check_box(table_el, label) {
    var tr_el = document.createElement('tr');
    var td_el = document.createElement('td');
    td_el.setAttribute('class', 'std');
    td_el.innerHTML = label;
    tr_el.appendChild(td_el);

    td_el = document.createElement('td');
    var input_el = document.createElement('input');
    input_el.setAttribute('type', 'checkbox');
    input_el.setAttribute('name', 'ssl');
    td_el.appendChild(input_el);
    tr_el.appendChild(td_el);
    table_el.appendChild(tr_el);
}

  
function signup_create_submit_auth(table_el) {
    var tr_el = document.createElement('tr');
    var td_el = document.createElement('td');
    tr_el.appendChild(td_el);
    td_el = document.createElement('td');
    var input_el = document.createElement('input');
    input_el.setAttribute('class', 'btn');
    input_el.setAttribute('type', 'submit');
    input_el.setAttribute('value', 'Set..');
    td_el.appendChild(input_el);

    input_el = document.createElement('input');
    input_el.setAttribute('class', 'btn');
    input_el.setAttribute('type', 'reset');
    input_el.setAttribute('value', 'Clear');
    td_el.appendChild(input_el);

    input_el = document.createElement('input');
    input_el.setAttribute('class', 'btn');
    input_el.setAttribute('type', 'button');
    input_el.setAttribute('value', 'Close');
    input_el.setAttribute('onClick', 'signup_remove_authbox()');
    td_el.appendChild(input_el);

    tr_el.appendChild(td_el);

    table_el.appendChild(tr_el);
}

function signup_remove_authbox() {
    var div_el = document.getElementById('publish_targets_add_id');
    if( div_el != null) {
        signup_clear_authbox();
        div_el.style.display = 'none';
    }
}



function signup_clear_sel_fb() {
    signup_clear_authbox();
    var targets_list = document.getElementById('targets_id');
    if(targets_list != null) {
        targets_list.innerHTML += "Facebook";  
    }
}

function signup_clear_sel_tw() {
   var form_el = document.getElementById('social_login_id');
   form_el.submit();
    if( oauth_w != null ) {
        oauth_w.close();
    }
}


function signup_open_oauth_w(type) {
    oauth_w = window.open(type, "scollbars=0,width=440, height=380");
    signup_get_type(type);
}


function signup_get_type(url_str) {
    var index_url = url_str.indexOf('twitter');
    var social_str = ( index_url != -1) ? "Twitter" : "Facebook";
    signup_create_auth(social_str);
}



var interest_selection = [];

function signup_change_interests(sel, view) {
    if( sel != null ) {
        var my_index = sel.selectedIndex;
        var new_value = sel.options[my_index].value;

        if(new_value != null) {
            var already_exists = false;
            for( var j = 0; j < interest_selection.length; j++) {
                if(interest_selection[j] == new_value) {
                    already_exists = true;
                    break;
                }
            }

            if( already_exists == false) {
                interest_selection.push(new_value);
                view.value += new_value + "/";
            }
            return true;
        }
    }

    return false;
}


function signup_submit_sources(form, element_name) {
   var els = document.getElementsByName(element_name);
   var all_sources ="";

   for( var j = 0; j < els.length; j++) {
        if(els[j].checked == true) {
            all_sources +=  els[j].value + "/";
        }
   }
   form.sources.value = all_sources;
   form.submit();
}


function signup_submit_interests(form, view) {
    form.interests.value = view.value;
    form.submit();
}


function signup_get_auth_token() {
    var url_str = window.location.toString();
    var auth_token_key = "auth_token=";
    var index_auth_token = url_str.indexOf(auth_token_key, 0);

    return ( index_auth_token != -1) ? url_str.substring(index_auth_token + auth_token_key.length) : null
}


function signup_set_auth_token() {
    var auth_token_str = signup_get_auth_token();

    if( auth_token_str ==  null ) {
        self.close();
    }
    else {
        var form_obj = document.createElement("form");
        form_obj.setAttribute('action', 'signup_auth_social');
        form_obj.setAttribute('method', 'post');

        var hidden_obj = document.createElement("input");
        hidden_obj.setAttribute('type', 'hidden');
        hidden_obj.setAttribute('name', 'auth_token');
        hidden_obj.setAttribute('value', auth_token_str);
        form_obj.appendChild(hidden_obj);
        document.body.appendChild(form_obj);

        form_obj.submit();
    }
}