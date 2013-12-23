<%-- 
    Document   : page
    Created on : 29-nov-2013, 10.11.41
    Author     : Andrea
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Signup</title>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-min.js"></script>
        <script src="/scripts/signup.js"></script>
    </head>
    <body>
        <div id="signUpDiv">
            <div>Nome : </div><div><input data-bind="value : firstName" type="text" /></div>
            <div>Cognome : </div><div><input data-bind="value : lastName" type="text" /></div>
            <div>Email : </div><div><input data-bind="value : email" type="text" /></div>
            <div>Username : </div><div><input data-bind="value : username" type="text" /></div>
            <div>Password : </div><div><input data-bind="value : password" type="password" /></div>
            <div>Confirm Password : </div><div><input data-bind="value : confirmPassword" type="password" /></div>
            <button data-bind="click : signUp" >Registrati</button>
        </div>  
    </body>
</html>
