package asw1009;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.w3c.dom.*;

@WebServlet(urlPatterns = {"/chat"}, asyncSupported = true)
public class CometQueuedService extends HttpServlet {

    private HashMap<String, Object> contexts = new HashMap<String, Object>();

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        InputStream is = request.getInputStream();
        response.setContentType("text/xml;charset=UTF-8");

        try {
            ManageXML mngXML = new ManageXML();
            Document data = mngXML.parse(is);
            is.close();

            operations(data, request, response, mngXML);

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private void operations(Document data, HttpServletRequest request, HttpServletResponse response, ManageXML mngXML) throws Exception {

        HttpSession session = request.getSession();
        //name of operation is message root
        Element root = data.getDocumentElement();
        String operation = root.getTagName();
        String user;
        Document answer = null;
        OutputStream os;
        if (operation.equals("login")) {
            user = ((Text) root.getChildNodes().item(0)).getData();
            System.out.println("login received: " + user);
            synchronized (this) {
                contexts.put(user, new LinkedList<Document>());
            }
            session.setAttribute("user", user);
            answer = mngXML.newDocument();
            answer.appendChild(answer.createElement("logged"));
            os = response.getOutputStream();
            mngXML.transform(os, answer);
            os.close();
        } else if (operation.equals("push")) {
            System.out.println("push received");
            synchronized (this) {
                for (String destUser : contexts.keySet()) {
                    Object value = contexts.get(destUser);
                    if (value instanceof AsyncContext) {
                        OutputStream aos = ((AsyncContext) value).getResponse().getOutputStream();
                        mngXML.transform(aos, data);
                        aos.close();
                        ((AsyncContext) value).complete();
                        contexts.put(destUser, new LinkedList<Document>());
                    } else {
                        ((LinkedList<Document>) value).addLast(data);
                    }
                }
            }
            answer = mngXML.newDocument();
            answer.appendChild(answer.createElement("ok"));
            os = response.getOutputStream();
            mngXML.transform(os, answer);
            os.close();
        } else if (operation.equals("pop")) {
            user = (String) session.getAttribute("user");
            System.out.println("pop received from: " + user);

            boolean async;
            synchronized (this) {
                LinkedList<Document> list = (LinkedList<Document>) contexts.get(user);
                if (async = list.isEmpty()) {
                    AsyncContext asyncContext = request.startAsync();
                    asyncContext.setTimeout(10 * 1000);
                    asyncContext.addListener(new AsyncAdapter() {
                        @Override
                        public void onTimeout(AsyncEvent e) {
                            try {
                                AsyncContext asyncContext = e.getAsyncContext();
                                String user = (String) ((HttpServletRequest) asyncContext.getRequest()).getSession().getAttribute("user");
                                System.out.println("timeout event launched for: " + user);
                                ManageXML mngXML = new ManageXML();
                                Document answer = mngXML.newDocument();
                                answer.appendChild(answer.createElement("timeout"));
                                boolean confirm;
                                synchronized (CometQueuedService.this) {
                                    if (confirm = (contexts.get(user) instanceof AsyncContext)) {
                                        contexts.put(user, new LinkedList<Document>());
                                    }
                                }
                                if (confirm) {
                                    OutputStream tos = asyncContext.getResponse().getOutputStream();
                                    mngXML.transform(tos, answer);
                                    tos.close();
                                    asyncContext.complete();
                                }
                            } catch (Exception ex) {
                                System.out.println(ex);
                            }
                        }
                    });
                    contexts.put(user, asyncContext);
                } else {
                    answer = list.removeFirst();
                }
            }
            if (!async) {
                os = response.getOutputStream();
                mngXML.transform(os, answer);
                os.close();
            }
        }
    }
}
