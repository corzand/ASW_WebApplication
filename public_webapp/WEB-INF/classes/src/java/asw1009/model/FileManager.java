package asw1009.model;

import asw1009.ManageXML;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Classe base per le classi manager che implementa la scrittura e la lettura di entità java in file XML
 * In entrambi i metodi si fa utilizzo di reflection Java per poter inferire il tipo degli oggetti 
 * @author ASW1009
 */
public class FileManager {

    protected String servletPath;
    private String fileName;
    protected File xml;
    protected ManageXML xmlManager;
    private int progId;
    private DateFormat dateFormat;
    
    /**
     * Metodo utilizzato per inizializzare il manager, in particolare per quanto riguarda
     * la gestione del file e relativi percorsi.
     * @param servletPath percorso della servlet in cui inserire il file
     * @param fileName nome del file 
     */
    protected void init(String servletPath, String fileName) {
        this.servletPath = servletPath;
        this.fileName = fileName;
        this.xml = new File(this.servletPath + System.getProperty("file.separator") + "WEB-INF" + System.getProperty("file.separator") + "xml" + System.getProperty("file.separator") + this.fileName + ".xml");
        try {
            this.xmlManager = new ManageXML();
        } catch (TransformerConfigurationException | ParserConfigurationException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        this.progId = 0;
    }
    
    /**
     * Metodo utilizzato per ottenere il nuovo identificatore progressivo.
     * 
     * @return 
     */
    protected int getNextId() {
        progId = progId + 1;
        return progId;
    }

    /**
     * Metodo utilizzato per scrivere l'istanza della lista passata come parametro
     * nel relativo file xml. Viene richiamato dopo ogni modifica alla lista di oggetti in memoria.
     * @param list istanza della lista tipizzata
     * @param type tipo di oggetti contenuti nella lista
     */
    protected void writeXML(List list, Class type) {
        String itemName = type.getSimpleName();      
        
        List<Field> allFields = new LinkedList<>(Arrays.asList(type.getDeclaredFields()));
        Class superclass = type.getSuperclass();
        
        if(superclass != null){
            allFields.addAll(Arrays.asList(superclass.getDeclaredFields()));
        }
        
        Document document = xmlManager.newDocument();
        Element root = document.createElement(this.fileName);
        document.appendChild(root);
        
        for (Object instance : list) {
            Element[] itemFields = new Element[allFields.size()];
            
            //Viene preparato il nodo con tutti i campi
            for (int i = 0; i < allFields.size(); i++) {
                
                try {
                    Field field = allFields.get(i);
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    itemFields[i] = document.createElement(fieldName);
                    itemFields[i].setTextContent(field.get(instance).toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Element item = document.createElement(itemName);
            for (Element itemField : itemFields) {
                item.appendChild(itemField);
            }
            //Viene appeso il nodo entità alla root
            root.appendChild(item);
        }
        
        //Creato il nodo con il progressivo
        Element listId = document.createElement("progId");
        listId.setTextContent(this.progId + "");
        root.appendChild(listId);

        //Completata la scrittura
        try (OutputStream out = new FileOutputStream(xml)) {
            xmlManager.transform(out, document);
            out.flush();
        } catch (TransformerException | IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metodo utilizzato per creare la lista di entità in memoria partendo dalla lettura 
     * dell'xml
     * @param type Il tipo di entità che la lista contiene
     * @return Lista tipizzata
     */
    protected List readXML(Class type) {
        try {
            List itemList = new ArrayList();
            //parse xml document
            InputStream in = new FileInputStream(xml);
            Document document = xmlManager.parse(in);
            NodeList nodeItems = document.getElementsByTagName(type.getSimpleName());
            for (int i = 0; i < nodeItems.getLength(); i++) {
                Node childNode = nodeItems.item(i);

                //Viene istanziato l'oggetto richiamando via reflection il costruttore
                Constructor<?> ctor = type.getConstructor();
                Object object = ctor.newInstance();

                //Viene preparato l'oggetto impostando tutti i campi, leggendo e parsando
                //il nodo relativo all'entità all'interno del documento xml.
                NodeList properties = childNode.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    if (property.getNodeType() == Node.ELEMENT_NODE) {
                        Field field;
                        try{
                            field = type.getDeclaredField(property.getNodeName());
                        }catch(NoSuchFieldException ex){
                            field = type.getSuperclass().getDeclaredField(property.getNodeName());
                        }
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();

                        if (int.class.isAssignableFrom(fieldType)) {
                            field.set(object, Integer.parseInt(property.getTextContent()));
                        } else if (double.class.isAssignableFrom(fieldType)) {
                            field.set(object, Double.parseDouble(property.getTextContent()));
                        } else if (long.class.isAssignableFrom(fieldType)) {
                            field.set(object, Long.parseLong(property.getTextContent()));
                        } else if (boolean.class.isAssignableFrom(fieldType)) {
                            field.set(object, Boolean.parseBoolean(property.getTextContent()));
                        } else if (Date.class.isAssignableFrom(fieldType)) {
                            field.set(object, this.dateFormat.parse(property.getTextContent()));
                        } else {
                            field.set(object, property.getTextContent());
                        }
                    }
                }
                itemList.add(object);
            }

            //Lettura del progressivo da file
            this.progId = Integer.parseInt(document.getElementsByTagName("progId").item(0).getTextContent());

            return itemList;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException | IOException | SAXException | ParseException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
