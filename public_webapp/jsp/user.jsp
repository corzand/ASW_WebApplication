<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>User Management</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
        <script src="/scripts/user.js"></script>
    </head>
    <body class="vertical-box">
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <div class="editUserDiv container horizontal-box fill-box-pack">       
            <form class="fixed-box-pack">
                <div>Username: <div data-bind="text: username"></div></div>
                <div class="data">
                    <div>Nome : </div><div><input name="firstName" data-bind="value : firstName" type="text" /></div>
                    <div>Cognome : </div><div><input name="lastName" data-bind="value : lastName" type="text" /></div>
                    <div>Email : </div><div><input name="email" data-bind="value : email" type="text" /></div>
                    <div class="password">
                        <div>Password: </div><div><input name="oldPassword" data-bind="value : oldPassword" type="password" /></div>
                        <div>Nuova Password : </div><div><input id="newPassword" name="newPassword" data-bind="value : newPassword" type="password" /></div>
                        <div>Conferma Password : </div><div><input id="confirmNewPassword" name="confirmNewPassword" data-bind="value : confirmNewPassword" type="password" /></div>
                        <button class="button editUser-button" data-bind="click : actions.editUser" ><span>Modifica</span></button>
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
