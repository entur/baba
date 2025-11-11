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
<h4>Hei, ${user.contactDetails.firstName} ${user.contactDetails.lastName}</h4>

<p>
    Kontoen din hos Entur på produksjonsmiljøet (PROD) skal snart overføres til en ny autentiseringsløsning.<br>
    <i>Your account with Entur in the production environment (PROD) will soon be migrated to a new authentication system.</i>
    <br>
    <p>
    Torsdag 13/11/2025 vil autentiseringsløsningen aktiveres: Logg inn med e-postadressen din (${user.contactDetails.email}).
    Du blir deretter omdirigert til din egen bedrifts autentiseringsportal.
    Du trenger ikke å opprette et nytt passord.<br>
    <i>On Thursday 13/11/2025, the new system will be activated: Please log in with your email address (${user.contactDetails.email}). You will then be redirected to your company's authentication portal.
        There is no need to create a new password.</i><br>
    <p>
    Dette gjelder din brukerkonto for:<br>
    <i>This applies to your user account for:</i><br>
<ul>
    <li><a href="https://operator.dev.entur.org/">https://operator.dev.entur.org/</a></li>
    <li><a href="https://stoppested.dev.entur.org/">https://stoppested.dev.entur.org/</a></li>
    <li><a href="http://avvik.dev.entur.org/">http://avvik.dev.entur.org/</a></li>
    <li><a href="https://nplan.dev.entur.org/">https://nplan.dev.entur.org/</a></li>
</ul>

     Dersom du ikke ønsker beholde denne kontoen, trenger du ikke å foreta deg noe.<br>
    <i>If you do not wish to keep this account, you can simply ignore this email.</i><br>

</p>

Ditt brukernavn er / <i>Your user name is</i>: ${user.contactDetails.email}
<br>

<p>
<a href="${userGuideLink}">Brukerveiledninger / Our user guides</a>
</p>

<h4>Synspunkter eller spørsmål kan sendes til: kollektivdata@entur.org<br>
    <i>Any feedback or questions can be sent to:</i> kollektivdata@entur.org </h4>


</body>
</html>