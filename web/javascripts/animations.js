// ----------------------------- animations.js ----------------------------------
// Script that implements the animation in the home page. The animation consists of a array
// of images and a textual context to be updated at each step.
// Patrick Nicolas 01/12/20


var img_select = "<img src=\"/images/2_light.png\" width=\"14\" height=\"14\" alt=\"Checked Feature\" ><a href=\"javascript:void(0)\" class=\"list\" onclick=\"dashboard_anim_show(";
var img_noselect = "<img src=\"/images/1_light.png\" width=\"14\" height=\"14\" alt=\"Checked Feature\" ><a href=\"javascript:void(0)\" class=\"list\" onclick=\"dashboard_anim_show(";
var index = 0;

function anim_object(title, img, summary, description) {
    this.title = title;
    this.img = img;
    this.summary = summary;
    this.description = description;
}

var anim_object_array = [
    new anim_object("Identify Trendy Topics",
                    "H0",
                    "Am I trendy?",
                    "Our agent collects the latest trends and articles related to your field of interest and expertise. The most popular topics and major headlines are extracted from your selection of news feeds, social networks and content aggregators." + "<br>" + "Our algorithm estimates the popularity index of your site or blog before it is published."),

    new anim_object("Leverage Media Resources",
                    "H1",
                    "A picture (or video) worth a thousand words!",
                    "Our agent provides you with a list of royalty and copyright free images, audio & video relevant to your blog or web sites." + + "<br>" +  "The placement and relevancy of multi-media content provides your visitors with a visually rich layout and appealing experience."),

    new anim_object("Create Promotional Content",
                    "H2",
                    "First impressions always count!",
                    "For most of us, creating the right promotional material is time consuming, error prone and frankly dull. Our agent generates social messages such as tweets, Facebook updates or blog comments to promote your content or product." + "<br>" + "The agent analyzes the response of the promotional campaign to evaluate the optimum format of the message."),

    new anim_object("Target Social Media",
                    "H3",
                    "Let's be heard!",
                    "Finding the right medium to promote your site or blog can be a very long and cumbersome process. Our agent leverages the best practices and wisdom of our existing users to suggest the most appropriate combination of social networks, feeds, blog sites and aggregators to promote your content." + "<br>" + "The agent analyzes the impact of the promotional campaign and rank the effectiveness of each social media outlet."),

    new anim_object("Track & Manage Impact",
                    "H4",
                    "What you do not know may hurt you!",
                    "Our tracking and reporting capabilities provide you with the analytics you need to understand and target appropriately your audience. The list of elements to make a web presence successful includes:" + "<ul><li>" + "Trendiness" + "</li><li>" + "Format and multi-media support" + "</li><li>" + "Quality and relevancy of promotional material" + "</li><li>" + "Properly targeted social media outlets" + "</li></ul>")
];


var content_anim_list = "";

function dashboard_anim_step() {
    var div_el = document.getElementById("anim_list_id");
    if( div_el != null) {
        div_el.innerHTML = "<ul>";
        var img_src = (index == 0) ? img_select : img_noselect;
        content_anim_list += "<ll>" + img_src + index + ")\">" + "  " + anim_object_array[index].title + "<br></a></ll>";
        div_el.innerHTML += content_anim_list;
        for(var k = index; k < 4; k++) {
          div_el.innerHTML += "<ll><br></ll>";
        }
        div_el.innerHTML += "</ul>";
    }

    index++;
    if( index >= anim_object_array.length)       {
       dashboard_anim_show(0);
    }
}

function dashboard_ll(index) {
    var div_el = document.getElementById("anim_list_id");
    if( div_el != null) {
        var img_src = "";
        div_el.innerHTML = "<ul>";
        for( var counter = 0; counter< anim_object_array.length; counter++ ) {
            img_src = (index == counter) ? img_select : img_noselect;
            div_el.innerHTML += "<ll>" + img_src + counter + ")\">" + "  " + anim_object_array[counter].title + "<br></a></ll>";
        }
        div_el.innerHTML += "</ul>";
    }
}

function dashboard_anim_start() {
    var next_action = 3000;
    for( var n =0; n < anim_object_array.length; n++ ) {
        setTimeout("dashboard_anim_step()", next_action);
        next_action += 1500;
    }
}



function dashboard_anim_show(indx) {
        var anim_obj = anim_object_array[indx];

        var table_el = document.createElement("table");
        table_el.setAttribute('width', '380');
        var tr_el = document.createElement("tr");
        var td_el = document.createElement("td");
        td_el.innerHTML = "<img src=\"/images/home/" + anim_obj.img + ".png\" width=\"82\" height=\"82\" />";
        tr_el.appendChild(td_el);
        td_el = document.createElement("td");
        td_el.setAttribute('class', 'small_justify');

        td_el.innerHTML = "<b>" + anim_obj.summary + "</b><br>" + anim_obj.description;
        tr_el.appendChild(td_el);
        table_el.appendChild(tr_el);
        dashboard_ll(indx);

        var pane = $('.scroll-pane');
        var api = pane.data('jsp');
        var content_str = "<table width=\"380\">" + table_el.innerHTML + "</table>"
        api.getContentPane().html(content_str);
        api.reinitialise();
}
