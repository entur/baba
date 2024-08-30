<html>
<head>
    <style>
        @import url(//fonts.googleapis.com/earlyaccess/notosanskannada.css);
        a {
            color: #2196F3;
            text-decoration: none;
        }

        a:hover, a:focus {
            color: #1976D2;
        }

        body {
            font-family: 'Noto Sans Kannada', sans-serif;
            color: #191919;
        }

        p,h1,h2,h3,h4,h5, span {
            color: #191919;
        }

        th, td {
            padding: 5px;
            text-align: left;
        }


    </style>
</head>

<body>
<h4>${message("new.user.email.header")} ${user.contactDetails.firstName} ${user.contactDetails.lastName}</h4>

<p>${message("new.user.email.introduction")}:</p>

${message("new.user.email.username")}: ${user.contactDetails.email}
<br>
<p>
${message("new.user.email.password.text")}${message("new.user.email.password.forgot.text")}
</p>


<p>
${message("new.user.email.user.guide.text")} <a href="${userGuideLink}">${message("new.user.email.user.guide.link.text")}</a>
</p>

<h4>${message("new.user.email.contact.info.text")}: <a href="${contactInfoEmail}">${contactInfoEmail}</a></h4>


</body>
</html>