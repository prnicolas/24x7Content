function set_cookie( name, value, expires, path, domain, secure ) {
    var today = new Date();
    today.setTime( today.getTime() );

    if ( expires != null) {
        expires = expires * 86400000;
    }
    var expires_date = new Date( today.getTime() + expires );

    document.cookie =   name +
                        "=" + encodeURIComponent( value ) +
                        ( ( expires != null ) ? ";expires=" + expires_date.toGMTString() : "" ) +
                        ( ( path != null ) ? ";path=" + path : "" ) +
                        ( ( domain != null ) ? ";domain=" + domain : "" ) +
                        ( ( secure != null ) ? ";secure" : "" );
}

function get_cookie( check_name ) {

	var a_all_cookies = document.cookie.split( ';' );
	var a_temp_cookie = [];
	var cookie_name = '';
	var cookie_value = null;

	for ( i = 0; i < a_all_cookies.length; i++ )	{
		a_temp_cookie = a_all_cookies[i].split( '=' );
		cookie_name = a_temp_cookie[0].replace(/^\s+|\s+$/g, '');

		if ( cookie_name == check_name ) {
			if ( a_temp_cookie.length > 1 ) {
				cookie_value = decodeURIComponent( a_temp_cookie[1].replace(/^\s+|\s+$/g, '') );
			}
			break;
		}
		a_temp_cookie = null;
		cookie_name = '';
	}

    /*
    if( cookie_value == null )  {
        var error_el = document.getElementById('global_error_id');
        if( error_el != null) {
            error_el.innerHTML = "Cookies need to be enabled for this browser";
            error_el.style.display = 'block';
        }
    }
    */

    return cookie_value
}

// this deletes the cookie when called
function delete_cookie( name, path, domain ) {
    if ( get_cookie( name ) != null)  {
        document.cookie = name + "=" +
                         ( ( path ) ? ";path=" + path : "") +
                         ( ( domain ) ? ";domain=" + domain : "" ) +
                            ";expires=Thu, 01-Jan-1970 00:00:01 GMT";
    }
}


function get_cookie_value(name, cookie_name) {
    var cookie_value = null;
    var cookie_val = get_cookie(name);

    if( cookie_val != null) {
        var index_cookie_name = cookie_val.indexOf(cookie_name);
        if( index_cookie_name != -1 ) {
            var cookie_substr = cookie_val.substr(index_cookie_name);
            var index_begin_cookie_value = cookie_substr.indexOf('=');

            if( index_begin_cookie_value != -1 ) {
                var index_end_cookie_value = index_begin_cookie_value + cookie_name.length+1;
                cookie_value =  cookie_substr.substr(index_begin_cookie_value, index_end_cookie_value);
            }
        }
    }

    return cookie_value;
}


function set_cookie_value(name, cookie_name, cookie_value)  {
    var cookie_val = get_cookie(name);
    var new_cookie_val = cookie_name + '=' + cookie_value;
    cookie_val = ( cookie_val != null) ? cookie_val + ':' + new_cookie_val : new_cookie_val;

    set_cookie(name, cookie_val, 180, null, null, null);
}

function replace_cookie_value(name, cookie_old_value, cookie_new_value) {
    var cookie_val = get_cookie(name);

    if( cookie_val != null) {
       var new_cookie_val = cookie_val.replace(cookie_old_value, cookie_new_value);
       set_cookie(name, new_cookie_val, 180, null, null, null);
    }
}

function get_cookie_attrs(cookie_name) {
    var cookie_val = get_cookie(cookie_name);

    if( cookie_val != null ) {
        var cookie_values= [];
        cookie_values = cookie_val.split(':');
        var cookie_next_value = [];

        if( cookie_values != null ) {
            for(var k = 0; k < cookie_values.length; k++) {

                cookie_next_value = cookie_values[k].split('=');
                switch( cookie_name ) {
                    case '24x7cs':
                        get_state_cookie( cookie_next_value[0], cookie_next_value[1]);
                        break;

                    case '24x7c':
                        get_account_cookie( cookie_next_value[0], cookie_next_value[1]);
                        break;

                    default:
                        break;
                }
            }
        }
    }
}


function get_account_cookie(name, value) {
    var form_el = null;

    switch ( name ) {
        case 'account':
            var form_ids = [  'publish_auth_id', 'publish_select_target_id', 'publish_content_id', 'publish_commit_id' ];
            for( var j = 0; j < form_ids.length; j++)  {
                form_el =  document.getElementById(form_ids[j]);
                if( form_el != null) {
                    form_el.account.value = value;
                }
            }
            break;

        case 'password':
            break;

        /*
        case 'targets':
            var form_ids = [  'publish_auth_id', 'publish_select_target_id' ];
            for( var j = 0; j < form_ids.length; j++)  {
                form_el =  document.getElementById(form_ids[j]);
                if( form_el != null ) {
                    form_el.targets.value = value;
                }
            }
            break;
         */


        default:
            break;
    }
}

function get_state_cookie(name, value) {
    switch ( name ) {
        case 'seed':
            var seed_el = document.getElementById('seed_id');
            if( seed_el != null ) {
                seed_el.value = value;
            }
            publish_valid_seed(value, 'submit_seed');
            break;

        case 'selection':
            var form_el = document.getElementById('publish_select_target_id');
            if( form_el != null ) {
                form_el.selection.value = value;
                for( var k = 0; k < form_el.elements.length; k++) {
                    if( value.match(form_el.elements[k].name))  {
                         form_el.elements[k].disabled = false;
                    }
                }

            }

        default:
            break;
    }

}


