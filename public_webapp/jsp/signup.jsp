<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>      
        <title>Signup</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
        <script src="/scripts/signup.js"></script>
    </head>
    <body>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <%@ include file="/WEB-INF/jspf/title.jspf" %>    
        <div class="signUpDiv">
            <div>Nome : </div><div><input data-bind="value : firstName" type="text" /></div>
            <div>Cognome : </div><div><input data-bind="value : lastName" type="text" /></div>
            <div>Email : </div><div><input data-bind="value : email" type="text" /></div>
            <div>Username : </div><div><input data-bind="value : username" type="text" /></div>
            <div>Password : </div><div><input data-bind="value : password" type="password" /></div>
            <div>Confirm Password : </div><div><input data-bind="value : confirmPassword" type="password" /></div>
            <button data-bind="click : actions.signUp" >Registrati</button>
        </div>  
    </body>
</html>
