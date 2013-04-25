

function dashboard_content(title, description) {
    this.title = title;
    this.description = description;
}

var dashboard_content_list = [
    new dashboard_content("Privacy issues raised by collecting data",
                          "When you request additional information or register on 24x7 Content's Web site, the Company will ask you to provide basic contact information. 24x7 Content uses common Internet technologies, such as cookies and beacons, to keep track of interactions with the Company's Web sites and emails." ),
    new dashboard_content("Privacy issues regarding customer data",
                          "24x7 Content may electronically submit data or information to the Company' services. 24x7 Content will not review, share, distribute, or reference any such Customer Data except as may be required by law. 24x7 Content may access Customer Data for addressing service or technical problems, only at a Customer's request."),
    new dashboard_content("Disclaimer",
                          "Disclaimer" ),
    new dashboard_content("Overview Terms of Use",
                          "By using the 24x7 Content website ('Service') and related services, you are agreeing to be bound by the following terms and conditions ('Terms of Service')." +
                          "24x7 Content reserves the right to update and change the Terms of Service without notice. Any new features that enhance the current Service, including the new release of resources, shall be subject to the Terms of Service. Continued use of the Service after any such changes shall constitute your consent to such changes." +
                          "Violation of any of the terms below will result in the immediate termination of your Account." + "<br><br>" + "You understand and agree that 24x7 Content cannot be held responsible for the content posted on the Service. You agree to use the Service at your own risk."),
     new dashboard_content("Terms and Conditions",
                            "<ul><li class=\"std\">" + "Your use of the Service is at your sole risk. The service is provided on an 'as is' and 'as available' basis." + "</li><br>"  +
                            "<li class=\"std\">" + "You must not modify, adapt or hack the Service or modify another website so as to falsely imply that it is associated with the company" + "</li><br>"  +
                            "<li class=\"std\">" + "You must not transmit any worms or viruses or any code of a destructive nature or upload unsolicited email, NMS or SMS messages." + "</li><br>"  +
                            "<li class=\"std\">" + "24x7 Content reserves the right to immediately disable your account or throttle your access to services until in the case your bandwidth consumption significantly exceeds the average bandwidth usage." +  "</li><br>"  +
                            "<li class=\"std\">" + "You expressly understand and agree that 24x7 Content shall not be in any way liable for any direct, indirect, incidental, special, consequential or exemplary damages, including but not limited to, damages for loss of profits, goodwill, use, data or other intangible losses. You expressly understand and agree that 24x7 Content shall not be in any way liable for the failure to exercise or enforce any right or provision of the 'Terms of Service'" + "</li></ul>"),
     new dashboard_content("Usage",
                            "<ul><li class=\"std\">" +  "The account owner must be 15 years or older to use this Service." + "</li><br>" +
                            "<li class=\"std\">" + "You must provide your legal full name, a valid email address, and any other information requested in order to complete the sign-up process." + "</li><br>" +
                            "<li class=\"std\">" + "Accounts registered by 'bots' or 'robots' are not permitted." + "</li><br>" +
                            "<li class=\"std\">" + "You are responsible for the content posted and activity that occurs under your account, maintaining the security of your account and password." + "</li><br>" +
                            "<li class=\"std\">" + "24x7 Content is not be liable for any loss or damage from your failure to comply with this security obligation" + "</li><br>" +
                            "<li class=\"std\">" + "24x7 Content does not authorize a single login being shared by multiple users." + "</li><br>" +
                            "<li class=\"std\">" + "You may not violate any copyright laws or use the Service for any illegal or unauthorized purpose or violate any laws in your jurisdiction." + "</li></ul>"),
    new dashboard_content("Our Mission ...",
                            "24x7 Content's mission is to enable users to create quality, relevant and ultimately profitable content. Our social agent will guide you through the process to create quality content (web sites, newsletter, blogs..), match with relevant images, videos and references, generate and publish targeted social promotional messages and measure impact on traffic and monetization." + "<br><br>" +
                            "The agent relies on advanced learning and predictive algorithms to enhance the quality and relevancy of the generated content." + "<br><br>"),


    new dashboard_content("An experienced team ...",
                            "<span style=\"font-size:16px;color:blue;\">" + "Staff" +  "</span><br><b>" +
                             "Patrick Nicolas" + "</b><br>" +
                            "Patrick has 25 years experience in software engineering, operations and technical management, holding VP engineering and CTO positions at Resonate, BiggerBoat and Zvents. Patrick launched the product development and deployment effort and built for framework for the business development and marketing operations of 24x7 Content" + "<br><br>" +
                            "<b>" + "Gabor Dobrosci" + "</b><br>" +
                             "Gabor is an experienced software engineer with a extensive background in Java development, Databases and Natural Language Processing algorithms. Gabor built and evaluated the original prototype of the content generation component of 24x7 Content." + "<br><br>" +
                             "<b>" + "Cheryl Rick" + "</b><br>" +
                             "Cheryl is an accomplished front-end engineer with 15 years experience in software development at IBM and GTE with a focus on web applications using Javascript/jQuery, CSS, HTML 5,0, PHP and Java. She guided the design and implementation of some key elements of 24x7 Content's web interface." + "<br><br><br>" +
                             "<span style=\"font-size:16px;color:blue;\">" + "Advisors" +  "</span><br><b>" +
                             "Boris Galitsky" + "</b><br>" +
                             "Boris has 20 years experience in Natural Language Processing, Data mining and AI research. He has published numerous papers and contributed to . In his advisory role, Boris has been instrumental in guiding 24x7 Content research effort." + "<br><br>" +
                             "<b>" + "Luis de La Rosa" + "</b><br>" +
                              "Luis incubated the original research project on content generation at the university of Gerona in Spain."),

  new dashboard_content("Careers opportunities ...",
                         "If you are a high-profile, talented individual looking to contribute to a high-growth, pre-IPO, social company, this opportunity is for you. We are building the technology that is revolutionizing the people create content and interact through social networks." +
                         "<br><br><b>" + "Usability/UI engineer" + "</b><i>" + "(part-time):" + "</i><br><i>" + "Responsibilities:" + "</i><br>" +
                          "The Usability/UI engineer is responsible for the design of interfaces, creation of mock-ups and visualization technologies to enable understanding of large, complex data sets. He/she is expected to contribute to the overall look & feel of rich internet and mobile applications" +
                          "<br><br><i>" + "Requirements:" + "</i><br>" + "* Proven experience creating mock-ups for user interfaces" + "<br>" +
                          "* Knowledge of HTML, CSS, Javascript programming" + "<br>" + "* Experience interacting with users and collecting real-time behavioral data" +
                          "<br>" + "* Good understanding of performance & quality trade-offs in building a professional web site" + "<br><br><i>" +
                          "Nice to have" + "</i><br>" + "* Knowledge of HTML 5.0, CSS 3.0" + "<br>" + "* Experience with Mobile web framework (Sencha, WebKit, Zepto...) " +"<br><br><br><b>" +
                          "VP Marketing" + "</b><br><i>" + "Responsibilities:" + "</i><br>" + "The executive role will oversee the overall marketing and business development strategy for 24x7 Content, including establishing partnership with content providers, defining a clear product position  and assisting in our effort to raise first round of capital."  +
                           "<br><br><i>" + "Requirements:" + "</i><br>" + "* Minimum 5 years in software marketing or product management experience" + "<br>" + "* Expertise in search, social and online consumer marketing solutions" +
                           "<br>" + "* Proven track record in creating case studies, demos and press kits" + "<br>" + "* Excellent writing & oral communication skills" +
                           "<br><br><i>" + "Nice to have" + "</i><br>" + "* Experience in driving business strategy with content providers and SEO firms" +
                           "<br>" + "* Knowledge of market surveys and analysis"),
    new dashboard_content("Contact us ...",
                           "24x7 Content" + "</b>" + " is currently located in Palo Alto in the heart of Silicon Valley" + "<br><br>" +
                           "Information: " + "<a href=\"mailto:info@24x7content.com\" class=\"small\">" + " info@24x7content.com" + "</a><br>" +
                           "Customer support: " + "<a href=\"mailto:support@24x7content.com\" class=\"small\">" + " support@24x7content.com" + "</a><br>" +
                           "Blog: " + "<a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('http://24x7content.blogspot.com/', '24x7_Content_Blog', 'width=680,height=560,location,menubar,resizable,scrollbars');\">" + "24x7Content.blogspot.com" + "</a><br>" +
                           "Twitter: " + "<a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('http://www.twitter.com/24x7Content', 'Twitter', 'width=660,height=520')\">" + "www.twitter.com/24x7Content" + "</a><br>" +
                           "Facebook: " + "<a href=\"javascript:void(0)\" class=\"small\" onclick=\"window.open('http://www.facebook.com/24x7Content', 'Facebook', 'width=660,height=520')\">" + "www.facebook.com/24x7Content" + "</a>")
];


var social_login_title ="";


$(function() {
     $('#description_id').jScrollPane({showArrows:true, scrollbarWidth: 12});
});


 function menu_set_bottom(top) {
     document.getElementById("bottom_menu_id").style.top = top;
 }



 function menu_load_content(selection) {

    var content_str = "<span style=\"font-size:16px;color:#660099;font-weight:bold\">" + dashboard_content_list[selection].title +
            "</span><p style=\"font-size:12px;font-family:Tahoma;color:#333333;text-align:justify;\">" + dashboard_content_list[selection].description   + "</p><br>";
    var pane = $('.scroll-pane')
    var api = pane.data('jsp');
    api.getContentPane().html(content_str);
    api.reinitialise();
 }



function menu_reset_seed(input_id, submit_id) {
    document.getElementById(input_id).value = ""
    document.getElementById(submit_id).disabled = true;
}




function menu_generate_content(ref, hidden_account, form_id) {

    if (document.getElementById(hidden_account).value != "demo") {
        var index = ref.indexOf("=");
        s = ref.substring(index+1);
        document.getElementById(hidden_account).value = s;
    }
    document.getElementById(form_id).submit();
}    


function online_help() {
  var w = window.open("help.html", "Online_Help", 'width=640, height=420,scrollbars=yes,resizable=yes');
}

function showImg(){
  	var imgArray = {};
	var now = new Date;
    var day_hours =  now.getHours();

	var color = "#fff";
	if (day_hours > 4 && day_hours < 9 ) {
		imgArray = ["images/sunrise/sunrise01.jpg",
					"images/sunrise/sunrise02.jpg",
					"images/sunrise/sunrise03.jpg",
					"images/sunrise/sunrise04.jpg",
					"images/sunrise/sunrise05.jpg",
					"images/sunrise/sunrise06.jpg"];
		color = "#ff9900";
	}
	else if (day_hours > 9 && day_hours < 15) {
		imgArray = ["images/day/daytime01.jpg",
		"images/day/daytime02.jpg",
		"images/day/daytime03.jpg",
		"images/day/daytime04.jpg",
		"images/day/daytime05.jpg",
		"images/day/daytime06.jpg",
		"images/day/daytime07.jpg",
		"images/day/daytime08.jpg",
		"images/day/daytime09.jpg",
		"images/day/daytime10.jpg",
		"images/day/daytime11.jpg",
		"images/day/daytime12.jpg",
		"images/day/daytime13.jpg",
		"images/day/daytime14.jpg",
		"images/day/daytime15.jpg",
		"images/day/daytime16.jpg",
		"images/day/daytime17.jpg",
		"images/day/daytime18.jpg",
		"images/day/daytime19.jpg",
		"images/day/daytime20.jpg",
		"images/day/daytime21.jpg",
		"images/day/daytime22.jpg",
		"images/day/daytime23.jpg",
		"images/day/daytime24.jpg",
        "images/day/daytime25.jpg",
		"images/day/daytime26.jpg",
		"images/day/daytime27.jpg",
		"images/day/daytime28.jpg",
		"images/day/daytime29.jpg",
        "images/day/daytime30.jpg"
        ];
		color = "beige";
	}
	else if (day_hours > 16 && day_hours < 19) {
		imgArray = [
		"images/sunset/sunset01.jpg",
		"images/sunset/sunset02.jpg",
		"images/sunset/sunset03.jpg",
		"images/sunset/sunset04.jpg",
		"images/sunset/sunset05.jpg",
		"images/sunset/sunset06.jpg",
		"images/sunset/sunset07.jpg",
		"images/sunset/sunset08.jpg"
        ];
		color = "#ffcc99";
	}
	else {
		imgArray = [
        "images/evening/evening01.jpg",
		"images/evening/evening02.jpg",
		"images/evening/evening03.jpg",
		"images/evening/evening04.jpg",
		"images/evening/evening05.jpg",
		"images/evening/evening06.jpg",
		"images/evening/evening07.jpg",
		"images/evening/evening08.jpg",
		"images/evening/evening09.jpg",
		"images/evening/evening10.jpg",
		"images/evening/evening11.jpg",
		"images/evening/evening12.jpg",
		"images/evening/evening13.jpg",
        "images/evening/evening14.jpg",
        "images/evening/evening15.jpg",
        "images/evening/evening16.jpg",
        "images/evening/evening17.jpg",
        "images/evening/evening18.jpg",
        "images/evening/evening19.jpg",
        "images/evening/evening20.jpg",
		"images/evening/evening21.jpg",
        "images/evening/evening22.jpg",
        "images/evening/evening23.jpg",
        "images/evening/evening24.jpg",
        "images/evening/evening25.jpg",
        "images/evening/evening26.jpg"
         ];
		color = "#000011";
	}

	var num = Math.floor(Math.random() * imgArray.length);
	document.body.style.backgroundColor = "#E0E0E0";
    document.body.background = "/"+imgArray[num];
}

