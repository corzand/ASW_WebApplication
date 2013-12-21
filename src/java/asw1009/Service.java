package asw1009;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import org.w3c.dom.*;

@WebServlet(urlPatterns = {"/service"})
public class Service extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        InputStream is = request.getInputStream();
        HttpSession session = request.getSession();
        response.setContentType("text/xml;charset=UTF-8");
        OutputStream os = response.getOutputStream();
        try {
        ManageXML mngXML = new ManageXML();
        Document data = mngXML.parse(is);
        is.close();
        
        Document answer= operations(data,session,mngXML);
        mngXML.transform(os, answer);
        os.close();
        }
        catch (Exception e){ 
            System.out.println(e);
        }
    }
    
    private Document operations(Document data, HttpSession session, ManageXML mngXML) throws TransformerConfigurationException {
    
        //name of operation is message root
        Element root = data.getDocumentElement();
        String operation = root.getTagName();
        Document answer = null;
        if (operation.equals("prova")) {
            
            System.out.println("Prova prova 1 2 3");
            answer = mngXML.newDocument();             
            answer.appendChild(answer.createElement("ok"));
            
            
        }else if(operation.equals("first")){
            session.setAttribute("counter", 0);
            answer = mngXML.newDocument();             
            answer.appendChild(answer.createElement("hello"));
            System.out.println("first.");
            // case ....
        }else if (operation.equals("next")){
            Integer counter = (Integer)session.getAttribute("counter");
            counter = counter+1;
            session.setAttribute("counter", counter);
            answer = mngXML.newDocument();             
            answer.appendChild(answer.createElement("ok"+counter));
            System.out.println("next. Counter val: " + counter);
        }else if (operation.equals("last")){
            answer = mngXML.newDocument();             
            answer.appendChild(answer.createElement("bye"));
            System.out.println("last.");            
            session.invalidate();
        }
        return answer;
    }

}
