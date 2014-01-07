<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>User Management</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
        <script src="/scripts/user.js"></script>
    </head>
    <body>
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <div class="editUserDiv">       
            <div>Username: <div data-bind="text: username"></div></div>
            <div class="data">
                <div>Nome : </div><div><input data-bind="value : firstName" type="text" /></div>
                <div>Cognome : </div><div><input data-bind="value : lastName" type="text" /></div>
                <div>Email : </div><div><input data-bind="value : email" type="text" /></div>
                <div class="password">
                    <div>Password: </div><div><input data-bind="value : oldPassword" type="password" /></div>
                    <div>Nuova Password : </div><div><input data-bind="value : newPassword" type="password" /></div>
                    <div>Conferma Password : </div><div><input data-bind="value : confirmNewPassword" type="password" /></div>
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
        </div>

    </body>
</html>
