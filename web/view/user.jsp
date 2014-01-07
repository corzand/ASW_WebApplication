<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>User Management</title>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-min.js"></script>
        <script src="/scripts/utility.js"></script>
        <script src="/scripts/application.js"></script>
        <script src="/scripts/user.js"></script>
        <script src="/scripts/jquery.validate.min.js"></script>
        <link rel="stylesheet" href="style/jquery-ui-1.10.3.custom.css" />
        <link href="/style/style.css" rel="stylesheet" type="text/css">
    </head>
    <body>
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <div class="editUserDiv">   
            <form class="editUserForm">
                <div>Username: <div data-bind="text: username"></div></div>
                <div class="data">
                    <div>Nome : </div><div><input name="firstName" data-bind="value : firstName" type="text" /></div>
                    <div>Cognome : </div><div><input name="lastName" data-bind="value : lastName" type="text" /></div>
                    <div>Email : </div><div><input name="email" data-bind="value : email" type="text" /></div>
                    <div class="password">
                        <div>Password: </div><div><input name="oldPassword" data-bind="value : oldPassword" type="password" /></div>
                        <div>Nuova Password : </div><div><input id="newPassword" name="newPassword" data-bind="value : newPassword" type="password" /></div>
                        <div>Conferma Password : </div><div><input id="confirmNewPassword" name="confirmNewPassword" data-bind="value : confirmNewPassword" type="password" /></div>
                        <button data-bind="click : actions.editUser" >Modifica</button>
                    </div>
                </div>
                <div class="image">
                    <div>Nuova immagine: </div> 
                    <div>
                        <img data-bind="attr : {src: picture}"/>
                    </div>
                    <div>
                        <input type="file" id="pictureButton" accept="image/x-png, image/gif, image/jpeg" />
                    </div>
                </div>
            </form>
        </div>

    </body>
</html>
