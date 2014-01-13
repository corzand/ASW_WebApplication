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
        <%@ include file="/WEB-INF/jspf/validation-dialog.jspf" %> 
        <div class="container fill-box-pack"> 
            <form class="edit-user">
                <div class="left">
                    <div class="table-row form-row">
                        <div class="cell right-label">Username: </div>
                        <div class="cell left-label">
                            <span data-bind="text: username"></span>
                        </div>
                    </div>

                    <div class="table-row form-row">
                        <div class="cell right-label">Nome : </div>
                        <div class="cell"><input name="firstName" data-bind="value : firstName" type="text" /></div>
                    </div>

                    <div class="table-row form-row">
                        <div class="cell right-label">Cognome : </div>
                        <div class="cell"><input name="lastName" data-bind="value : lastName" type="text" /></div>
                    </div>

                    <div class="table-row form-row">
                        <div class="cell right-label">Email : </div>
                        <div class="cell"><input name="email" data-bind="value : email" type="text" /></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Password: </div>
                        <div class="cell"><input name="oldPassword" data-bind="value : oldPassword" type="password" /></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Nuova Password : </div>
                        <div class="cell"><input id="newPassword" name="newPassword" data-bind="value : newPassword" type="password" /></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Conferma Password : </div>
                        <div class="cell"><input id="confirmNewPassword" name="confirmNewPassword" data-bind="value : confirmNewPassword" type="password" /></div>
                    </div>
                </div>
                <div class="right">
                    <div>Immagine: </div> 
                    <div>
                        <img data-bind="attr : {src: picture}"/>
                    </div>
                    <div>
                        <button class="button" data-bind="click : actions.readImage"><span>Seleziona Immagine</span></button>
                        <input type="file" id="pictureButton" accept="image/x-png, image/gif, image/jpeg" />
                    </div>
                </div>    
                <div class="clear"> 
                    <button class="button" data-bind="click : actions.editUser" ><span>Modifica</span></button>
                </div>
            </form>
        </div>

    </body>
</html>
