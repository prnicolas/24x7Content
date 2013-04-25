


function valid_content_length(input_title_id, input_description_id, button_id) {
    if( document.getElementById(input_title_id).value.length > 8 &&
        document.getElementById(input_description_id).value.length > 32) {
        document.getElementById(button_id).disabled = false
    }
}