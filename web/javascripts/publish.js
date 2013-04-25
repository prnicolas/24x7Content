var title_style = "font-family:Tahoma;color:#444444;font-size:16px;text-align:center";
var target_names = [
    "twitter", "facebook", "email", "web", "blog", "foursquare"
];

var twitter_layout = [ "Tweet", "Link" ];
var facebook_layout = [ "Status", "Link"];
var email_layout = [ "To", "Subject", "Message"];

function target(key) {
    this.key = key;
    this.head_id = null;
    this.msg_name = null;
    this.url_link = null;
    this.body_id = null;
    this.target_id = null;
    this.display_url_link = display_url_link;
    this.display_logo = display_logo;
}

function display_url_link() {
    return "<a href=http://www.pnexpert.com/" + this.url_link + ">" + this.url_link + "</a>";
}

function display_logo() {
    return  "<img src=\"/images/logos/" + this.key + "_logo.png\" width=\"24\" height=\"24\" /> " + this.key;
}

var new_target;

function charscounter(max_chars, counter_el, textarea_el) {
    this.max_chars = max_chars;
    this.counter_el = counter_el;
    this.textarea_el = textarea_el;
    this.display_chars = display_chars;
}

function display_chars() {
    var num_chars_left = this.max_chars - this.textarea_el.value.length;
    this.counter_el.innerHTML =  num_chars_left + ' characters left';
}
var new_charscounter;


var tbl_el = null;



function publish_onload() {
    showImg();
    ticker_start('ticker_start_id');

    get_cookie_attrs('24x7c');
    get_cookie_attrs('24x7cs');
}



function publish_select_oauthtarget(form_el, authorized_targets, el) {
    var target_name = el.name;
    if( el.checked == true && (authorized_targets == null || authorized_targets.match(target_name) == null)) {
        form_el.selection.value = target_name;
        form_el.submit();
    }
}


/*
function publish_select_oauthtarget(form_el, authorized_targets, target_name, disabled) {
    var cookie_value = get_cookie_value('24x7cs',  'selection');

    if( cookie_value == null ) {
        set_cookie_value('24x7cs', 'selection', target_name);
    }
    else {
        var new_cookie_value = null;
        if( disabled == false) {
            new_cookie_value = cookie_value + "*" + target_name;
        }
        else {
            new_cookie_value = cookie_value.replace(target_name, '');
        }
        replace_cookie_value('24x7cs', cookie_value, new_cookie_value);
    }


    if( authorized_targets == null || authorized_targets.match(target_name) == null) {
        if( form_el != null) {
            form_el.selection.value = target_name;
            form_el.submit();
        }
    }
}
*/

function publish_select_pwdtarget(disabled, login_id, authorized_targets, target_name) {
    if( authorized_targets == null || authorized_targets.match(target_name) == null) {
        signup_create_auth(disabled, login_id, target_name);
    }
}


function publish_get_selection(targets_list, selection) {
    var select_el;
    var select_el_id_str;
    var select_el_img_src;

    if( selection != null ) {
        select_el_id_str = selection + '+_auth_id';
        select_el = document.getElementById(select_el_id_str);
        if( select_el != null ) {
            select_el.innerHTML += " " + "<img src=\"/images/key.png\" width=\"14\" height=\"14\" />";
        }
        select_el_id_str = 'select_' + selection + '_id';
        select_el = document.getElementById(select_el_id_str );
        if( select_el != null ) {
            select_el.setAttribute('checked', true);
        }
    }

    if( targets_list != null) {
        for( var j = 0; j < target_names.length; j++) {
            if( targets_list.match(target_names[j]) ) {
                select_el_id_str = target_names[j] + '_auth_id';

                select_el = document.getElementById(select_el_id_str);
                if( select_el != null ) {
                    select_el.innerHTML += " " + "<img src=\"/images/key.png\" width=\"14\" height=\"14\" />";
                }
            }
        }
    }
}


function publish_valid_seed(content_str, button_id) {
    var content_str_ok = (content_str.length > 32) ? false : true;
    document.getElementById(button_id).disabled = content_str_ok;
}


function publish_create_content(form, seed_id)  {
    var seed_el = document.getElementById(seed_id);

    if( seed_el != null ) {
        if( form != null) {
            form.seed.value = seed_el.value;
            var cookie_value = 'seed=' + seed_el.value;
            set_cookie('24x7cs', cookie_value, 180, null, null, null);
            form.submit();
        }
    }
}


function publish_details(key, msg_name, link, target_id) {
    new_target = new target(key);

    new_target.head_name = 'head_name' ;
    new_target.msg_name = msg_name;

    new_target.body_name = 'body_name';
    if( link != null ) {
        new_target.url_link = link;
    }
    new_target.target_id = target_id;

    var div_el = document.createElement("div");
    div_el.setAttribute('id', 'publish_details_id');
    div_el.style.position = 'absolute';
    div_el.style.left  = '20'
    div_el.style.top = '120'
    div_el.style.width = '350';
    div_el.style.border = 'solid';
    div_el.style.borderColor = '#555555';
    div_el.style.backgroundColor = '#E0E0E0';

    var table_el = null;

    switch ( new_target.key ) {
        case 'twitter':
            div_el.style.height = '140';
            table_el = publish_details_msg('140', twitter_layout, false);
            var submit_el = publish_details_submit();
            table_el.appendChild(submit_el);
            break;

        case 'facebook':
            div_el.style.height = '140';
            table_el = publish_details_msg('256', facebook_layout, false);
            var submit_el = publish_details_submit();
            table_el.appendChild(submit_el);
            break;

        case 'email':
            div_el.style.height = '300';
            table_el = publish_details_msg('98', email_layout, true);
            var submit_el = publish_details_submit();
            table_el.appendChild(submit_el);
            break;

        default:
            break;
    }


    if( table_el != null ) {
        div_el.appendChild(table_el);
        document.body.appendChild(div_el);
        div_el.style.display = 'block';
    }

    return false;
}



function publish_details_msg(max_chars, layout, head_defined) {
    var field_num = 0;
    var table_el = document.createElement("table");
    table_el.setAttribute('width', '340');

    var tr_el = document.createElement("tr");
    var td_el = document.createElement("td");
    tr_el.appendChild(td_el);
    td_el  = document.createElement("td");

    td_el.setAttribute('width', '32');
    td_el.setAttribute('align', 'center');
     td_el.setAttribute('class', 'std');
    td_el.innerHTML = new_target.display_logo();
    tr_el.appendChild(td_el);
    table_el.appendChild(tr_el);

    var head_el = document.publish_commit_name.elements[new_target.head_name];

    if( head_defined == true && head_el != null ) {
        tr_el = document.createElement("tr");
        td_el = document.createElement("td");
        td_el.setAttribute('class', 'std');
        td_el.innerHTML = layout[field_num++];
        tr_el.appendChild(td_el);

        td_el  = document.createElement("td");
        var textarea_el = document.createElement("textarea");
        textarea_el.setAttribute('id', 'head_edit_id');
        textarea_el.setAttribute('class', 'content');
        textarea_el.setAttribute('cols', '50');
        textarea_el.setAttribute('rows', '1');
        textarea_el.innerHTML = head_el.value;
        td_el.appendChild(textarea_el);
        tr_el.appendChild(td_el);
        table_el.appendChild(tr_el);
    }

    tr_el = document.createElement("tr");
    td_el = document.createElement("td");
    td_el.setAttribute('class', 'std');
    td_el.innerHTML = layout[field_num++];
    tr_el.appendChild(td_el);

    td_el  = document.createElement("td");
    var textarea_el = document.createElement("textarea");
    textarea_el.setAttribute('id', 'details_edit_id');
    textarea_el.setAttribute('class', 'content');
    textarea_el.setAttribute('cols', '50');
    textarea_el.setAttribute('rows', '1');
    textarea_el.setAttribute('onKeyUp', 'new_charscounter.display_chars()');

    var msg_el = document.publish_commit_name.elements[new_target.msg_name];
    textarea_el.innerHTML = msg_el.value;

    td_el.appendChild(textarea_el);
    tr_el.appendChild(td_el);
    table_el.appendChild(tr_el);

    if(  head_defined == false) {
        tr_el = document.createElement("tr");
        td_el = document.createElement("td");
        td_el.setAttribute('class', 'std');
        td_el.innerHTML = layout[field_num++];

        tr_el.appendChild(td_el);
        td_el = document.createElement("td");
        td_el.setAttribute('class', 'std');
        td_el.innerHTML = new_target.display_url_link();
        tr_el.appendChild(td_el);
        table_el.appendChild(tr_el);
    }

    tr_el = document.createElement("tr");
    td_el = document.createElement("td");
    tr_el.appendChild(td_el);
    var counter_el = document.createElement("td");
    counter_el.setAttribute('class', 'std');
    new_charscounter = new charscounter(max_chars, counter_el, textarea_el);
    tr_el.appendChild(counter_el);
    table_el.appendChild(tr_el);


    if( layout.length > 2 )  {
        tr_el = document.createElement("tr");
        td_el = document.createElement("td");
        td_el.setAttribute('class', 'std');
        td_el.innerHTML = layout[field_num++];
        tr_el.appendChild(td_el);

        td_el  = document.createElement("td");
        textarea_el = document.createElement("textarea");
        textarea_el.setAttribute('id', 'body_edit_id');
        textarea_el.setAttribute('class', 'content');
        textarea_el.setAttribute('cols', '50');
        textarea_el.setAttribute('rows', '7');
        var body_el = document.getElementById(new_target.body_id);
        if( body_el != null && body_el.value != null)  {
            textarea_el.innerHTML = body_el.value;
        }

        td_el.appendChild(textarea_el);
        tr_el.appendChild(td_el);
        table_el.appendChild(tr_el);
    }

    return table_el;
}



function publish_details_submit() {
    var tr_el = document.createElement("tr");
    var td_el = document.createElement("td");
    td_el.innerHTML ="<br>";
    tr_el.appendChild(td_el);

    td_el = document.createElement("td");
    td_el.setAttribute('align', 'center');

    var input_el = document.createElement("input");
    input_el.setAttribute('type', 'button');
    input_el.setAttribute('class', 'btn');
    input_el.setAttribute('name', 'submit');
    input_el.setAttribute('value', "Update");
    input_el.setAttribute('onClick', 'publish_details_update()');
    td_el.appendChild(input_el);

    input_el = document.createElement("input");
    input_el.setAttribute('type', 'button');
    input_el.setAttribute('class', 'btn');
    input_el.setAttribute('name', 'clear');
    input_el.setAttribute('value', "Clear");
    input_el.setAttribute('onClick', 'publish_details_clear()');
    td_el.appendChild(input_el);

    input_el = document.createElement("input");
    input_el.setAttribute('type', 'button');
    input_el.setAttribute('class', 'btn');
    input_el.setAttribute('name', 'close');
    input_el.setAttribute('value', "Close");
    input_el.setAttribute('onClick', 'publish_details_close()');
    td_el.appendChild(input_el);
    tr_el.appendChild(td_el);
    return tr_el;
}


function publish_details_update() {
    var textarea_el = document.getElementById('details_edit_id');

    if( textarea_el != null) {
        var dest_el = document.getElementById(new_target.target_id);
        if( dest_el != null ){
            dest_el.style.display = 'none';
            dest_el.innerHTML = textarea_el.value + " ..." + new_target.display_url_link();
            dest_el.style.display = 'block';
            var el_name = "msg_" + new_target.key;
            var h_el = document.getElementsByName(el_name);

            if( h_el != null && h_el[0] != null) {
                h_el[0].setAttribute('value', dest_el.innerHTML);
            }
            publish_details_close()
        }
    }
}


function publish_details_close() {
    var div_el = document.getElementById('publish_details_id');
    if( div_el != null) {
        while( div_el.hasChildNodes()) {
            div_el.removeChild(div_el.lastChild);
        }
        document.body.removeChild(div_el);
        new_charscounter = null;
    }
}



function publish_details_clear() {
    var textarea_el = document.getElementById('details_edit_id');

    if( textarea_el != null ) {
          textarea_el.value = "";
    }
}

function publish_open_details(url) {
    var url_str = 'http://www.pnexpert.com/' + url;
    window.open(url_str, "Generated Content", "width=500,height=340,scrollbars");
}

