<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>      
        <title>Signup</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
        <script src="/scripts/signup.js"></script>
    </head>
    <body class="vertical-box">
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <div class="signUpDiv container horizontal-box fill-box-pack">
            <div class="content fill-box-pack vertical-box">
                <%@ include file="/WEB-INF/jspf/title.jspf" %>   
                <form class="signUpForm fixed-box-pack">
                    <div class="table-row form-row">
                        <div class="cell right-label">Nome :</div>
                        <div class="cell"><input name="firstName" data-bind="value : firstName" type="text"/></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Cognome:</div>
                        <div class="cell"> <input name="lastName" data-bind="value : lastName" type="text"/></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Email:</div>
                        <div class="cell"><input name="email" data-bind="value : email" type="text"/></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Username:</div>
                        <div class="cell"><input name="username" data-bind="value : username" type="text"/></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Password: </div>
                        <div class="cell"><input id="password" name="password" data-bind="value : password" type="password"/></div>
                    </div>
                    <div class="table-row form-row">
                        <div class="cell right-label">Conferma Password: </div>
                        <div class="cell"><input name="confirmPassword" data-bind="value : confirmPassword" type="password"/></div> 
                    </div>
                    <a class="button signUpButton" data-bind="click : actions.signUp" >Registrati</a>
                </form>  
            </div>
        </div>
    </body>
</html>
