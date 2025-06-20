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
    Kontoen din hos Entur på utviklingsmiljøet (DEV) skal snart overføres til en ny autentiseringsløsning.<br>
    <i>Your account with Entur in the development environment (DEV) will soon be migrated to a new authentication system.</i><br>
    <br>
    <p>I dag får du en e-post for å bekrefte kontoen din i den nye autentiseringsløsningen.<br>
    <i>Today, you will receive an email asking you to confirm your account in the new authentication system.</i><br>
    <p>
    Om noen dager vil autentiseringsløsningen aktiveres: Før du logger inn på våre tjenester første gang, må passordet endres ved å velge "Glemt passord".<br>
    <i>In a few days, the new system will be activated. Before logging into our services for the first time, you will need to reset your password by selecting "Forgot password."</i><br>
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