

var ltChars = 0;
var dgChars = 0;
var pwd_status = [
 "Strong password", "Not enough characters", "Blank character", "Weak Password"
]
var acc_status = [
  "Ok", "Not enough characters", "Blank character"
]


function login_onload() {
    showImg();
    var cookie_val = get_cookie('24x7c');

    if( cookie_val != null ) {
        var cookie_values= [];
        cookie_values = cookie_val.split(':');

         if( cookie_values != null && cookie_values.length > 1) {
             var cookie_next_values = [];
             cookie_next_values = cookie_values[0].split('=');
             var form_el = document.getElementById('login_id');

             if( form_el != null ) {
                form_el.account.value = cookie_next_values[1];
                cookie_next_values = cookie_values[1].split('=');
                form_el.password.value = cookie_next_values[1];
             }
         }
    }
}


        // Validate the account id as no blank and at least 4 characters
 function login_check_acc(val, feedback_id) {
    var err_status =  login_field_blank(val, 4);

    var el = document.getElementById(feedback_id);
    if( el ) {
        el.innerHTML = acc_status[err_status];
        el.style.color = ( err_status > 0) ? "red" : "blue";
    }
 }

 // validate password as bad, weak and strong
 function login_check_pwd(val, feedback_id, submit_ctrl) {
    var pwd_strength = login_pwd_strong(val, 6);
    if( submit_ctrl != null ) {
        submit_ctrl.disabled = ( pwd_strength > 0);
    }

    var el = document.getElementById(feedback_id);
    if( el )  {
        el.innerHTML = pwd_status[pwd_strength];
        el.style.color = ( pwd_strength > 0) ? "red" : "blue";
    }
 }


 function login_check_rpwd(val2, pwd, feedback_id, submit_ctrl) {
    var val1 = pwd.value;

    if( submit_ctrl != null) {
        submit_ctrl.disabled = (val1 != val2);
    }

    var el = document.getElementById(feedback_id);
    if( el ) {
        if( val1 != val2)  {
            el.innerHTML = "Passwords do not match";
            el.style.color = "red";
        }
        else {

            el.innerHTML = "Ok";
            el.style.color = "blue";
        }
    }
 }



function login_clear_pwd(element, control1, control2) {
    var type_str = (element.checked == true) ? 'text' : 'password';
    control1.type = type_str;

    if( control2 != null ) {
        control2.type = type_str;
    }
}



 function login_field_blank(val, num_chars) {
    for(var j = 0; j < val.length; j++) {
        if ( val.charAt(j) == ' ' )  {
            return 2;
        }
    }
    if( val.length < num_chars)  {
        return 1;
    }
    return 0;
 }

 function login_pwd_strong(val, num_chars) {
    var chCode;
    if( val.length == 1) {
        dgChars = 0;
        ltChars = 0;
    }
    if( val.length < num_chars)  {
        return 1;
    }
    for(var j = 0; j < val.length; j++) {
        chCode = val.charCodeAt(j);
        if ( chCode == 0x20 )  {
            return 2;
        }
        else if( chCode < 0x41) {
            dgChars++;
        }
        else  {
            ltChars++;
        }
    }

    return (dgChars > 0 && ltChars > 0) ? 0 : 3;
 }


function login_submit(form) {

    if( form != null ) {
        var cookie_value = 'account='+ form.account.value;
        if( form.remember.disabled == false) {
            cookie_value = cookie_value + ':password=' + form.password.value;
        }
        set_cookie('24x7c', cookie_value, 180, null, null, null);
        form.submit();
    }
}


function login_submit_demo(form) {
    window.location = "alpha_test.html";
}
