

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Login</title>
        <%@ include file="/WEB-INF/jspf/common-head.jspf" %>
    </head>
    <body>
        <%@ include file="/WEB-INF/jspf/top.jspf" %>
        <div class="content">
            <div class="login">
                <%@ include file="/WEB-INF/jspf/title.jspf" %>              
                <applet class="applet" codebase="/applet/" code="LoginApplet.class" archive="ASW_Applet1.jar,ASW_Lib1.jar,org.json.jar">
                    <param name="sessionId" value="<%= session.getId()%>" />
                </applet>
            </div>
        </div>
    </body>
</html>
