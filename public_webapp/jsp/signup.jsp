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
        <div class="container horizontal-box fill-box-pack">
            <div class="content fill-box-pack vertical-box">
                <%@ include file="/WEB-INF/jspf/title.jspf" %>    
                <div class="fixed-box-pack signUpDiv">
                    <div>Nome : </div><div><input data-bind="value : firstName" type="text" /></div>
                    <div>Cognome : </div><div><input data-bind="value : lastName" type="text" /></div>
                    <div>Email : </div><div><input data-bind="value : email" type="text" /></div>
                    <div>Username : </div><div><input data-bind="value : username" type="text" /></div>
                    <div>Password : </div><div><input data-bind="value : password" type="password" /></div>
                    <div>Confirm Password : </div><div><input data-bind="value : confirmPassword" type="password" /></div>
                    <button data-bind="click : actions.signUp" >Registrati</button>
                </div>  
            </div>
        </div>
    </body>
</html>
