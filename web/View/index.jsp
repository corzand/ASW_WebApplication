<%-- 
    Document   : index
    Created on : 20-dic-2013, 17.21.07
    Author     : Andrea
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Multi-User Task List</title>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
        <script src="//ajax.googleapis.com/ajax/libs/jqueryui/1.10.3/jquery-ui.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-min.js"></script>
        <script src="/scripts/utility.js"></script>
        <script src="/scripts/application.js"></script>        
        <link rel="stylesheet" href="style/jquery-ui-1.10.3.custom.css" />
        <link href="/style/style.css" rel="stylesheet" type="text/css">
    </head>
    <body>   
        <%@ include file="/WEB-INF/jspf/auth.jspf" %>
        <%@ include file="/WEB-INF/jspf/top.jspf" %> 
        <h1>Hello World!</h1>
    </body>
</html>
