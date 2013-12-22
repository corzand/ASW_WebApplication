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
        <title>Login</title>
    </head>
    <body>
        <applet style="width: 200px; height: 50px" codebase="http://localhost:8080/WebApplication/applet/" code="LoginApplet.class" archive="Applet1.jar,Lib1.jar,org.json.jar" width=350 height=200>
            <param name="sessionId" value="<%= session.getId()%>" />
        </applet>  
    </body>
</html>
