/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import RMS.DataRMS;
import agenda.Contact;
import java.io.IOException;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Vector;
import javax.microedition.rms.RecordEnumeration;

/**
 * @author Swoko
 */
public class Phonebook extends MIDlet implements CommandListener{

    //Atributos de Clase
    private Display display;
    private Form formulario, searchForm, showForm;
    private List contactsList, contactsFound;
    private Alert welcome, noContacts, deleteAlert;
    private TextField nombre, teléfono, mail, searchField;
    private ChoiceGroup tipoTel, tipoCont;
    private DateField cumpleaños;

    //Images
    private Image images[];
    //Commands
    private Command save, backContactsList;
    //Commands Main Screen
    private Command newContact, edit, deleteContact, delete;
    private Command searchScreen, search, exit;
    //RMS
    private DataRMS rms;
    static final String ID_RMS = "PhoneBook";
    private Vector vIDs = new Vector(); //records' ID

    public Phonebook(){

        //Images
        try {
            images = new Image[6];
            images[0] = Image.createImage("/pictures/patito_friend.gif");
            images[1] = Image.createImage("/pictures/patito_family.gif");
            images[2] = Image.createImage("/pictures/business.gif");
            images[3] = Image.createImage("/pictures/patito_welcome.gif");
            images[4] = Image.createImage("/pictures/patito_error.jpg");
            images[5] = Image.createImage("/pictures/patito_erase.gif");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        // Initializing Alerts
        welcome = new Alert("", "Agenda marca Patito\n Versión 1.0", images[3], AlertType.INFO);
        welcome.setTimeout(Alert.FOREVER);
        noContacts = new Alert("", "Contactos no encontrados", images[4], AlertType.INFO);
        noContacts.setTimeout(Alert.FOREVER);
        //initializing form
        formulario = new Form("Registro de Usuario");
        nombre = new TextField("Nombre", null, 50, TextField.ANY);
        teléfono = new TextField("Teléfono", null, 10, TextField.PHONENUMBER);
        String[] opcionesTel = {"Celular", "Casa", "Oficina"};
        tipoTel = new ChoiceGroup("Tipo", ChoiceGroup.EXCLUSIVE, opcionesTel, null);
        mail = new TextField("Mail", null, 20, TextField.EMAILADDR);
        //cumpleaños = new DateField("Cumpleaños", DateField.DATE);
        String[] opcionesCont = {"Amigo", "Familia", "Negocios"};
        tipoCont = new ChoiceGroup("Tipo Contacto", ChoiceGroup.EXCLUSIVE, opcionesCont, null);
        //prioridad = new Gauge("Prioridad", true, 5, 1);
        
        // Form to search contacts
        searchForm = new Form("Digite búsqueda");
        searchField = new TextField("Nombre a buscar", null, 50, TextField.ANY);

        //Comandos de control
        save = new Command("Guardar", Command.OK, 1);
        backContactsList = new Command("Volver", Command.BACK, 0);
        searchScreen = new Command("Buscar", Command.OK, 0);
        search = new Command("Buscar", Command.OK, 0);
        newContact = new Command("Nuevo contacto", Command.OK, 0);
        delete = new Command("Borrar", Command.OK, 1);
        deleteContact = new Command("Borrar", Command.OK, 1);
        exit = new Command("Salir", Command.EXIT, 2);

        //initializing list
        contactsList = new List("Contactos", List.IMPLICIT);
        contactsFound = new List("Contactos encontrados", List.IMPLICIT);

        //Create RMS zone
        try
        {
            rms = new DataRMS(ID_RMS);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        RecordEnumeration records = null;
        //fill the ID vector with RMS records' ID
        try{
            records = rms.getNumRecords();
            while(records.hasNextElement()){
                vIDs.addElement(new Integer(records.nextRecordId()));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //fill the contact list
        records.reset();
        try{
            //records = rms.

            while(records.hasNextElement()){
                Contact cont = rms.getRecord(records.nextRecordId());
                contactsList.append(cont.getName(), images[cont.getContactType()]);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        rms.closeRecordStore();
    }

    public void startApp() {
        display = Display.getDisplay(this);

        contactsList.addCommand(searchScreen);
        contactsList.addCommand(newContact);
        if(contactsList.size() > 0)  contactsList.addCommand(deleteContact);
        contactsList.addCommand(exit);
        contactsList.setCommandListener(this);
        display.setCurrent(welcome,contactsList);

        formulario.addCommand(save);
        formulario.addCommand(backContactsList);
        formulario.append(nombre);
        formulario.append(teléfono);
        formulario.append(tipoTel);
        formulario.append(mail);
        //formulario.append(cumpleaños);
        formulario.append(tipoCont);
        formulario.setCommandListener(this);

        searchForm.addCommand(search);
        searchForm.addCommand(backContactsList);
        searchForm.append(searchField);
        searchForm.setCommandListener(this);

        contactsFound.addCommand(backContactsList);
        contactsFound.addCommand(deleteContact);
        contactsFound.setCommandListener(this);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        rms.closeRecordStore();
    }

    public void commandAction(Command c, Displayable d) {

        if( c == exit ){
            destroyApp(true);
            notifyDestroyed();
        }

        if( c == newContact ){
            //Clear form
            nombre.setString("");
            teléfono.setString("");
            tipoTel.setSelectedIndex(0, true);
            mail.setString("");
            tipoCont.setSelectedIndex(0, true);
            //Add commands
            display.setCurrent(formulario);
        }

        if( c == backContactsList ){
            rms.openRecordStore(ID_RMS);
            Contact cont;
            contactsList.deleteAll();
            contactsList.removeCommand(backContactsList);
            contactsList.addCommand(searchScreen);
            contactsList.addCommand(newContact);
            contactsList.addCommand(deleteContact);
            contactsList.addCommand(exit);
            try{
                RecordEnumeration records = rms.getNumRecords();
                while(records.hasNextElement()){
                    cont = rms.getRecord(records.nextRecordId());
                    contactsList.append(cont.getName(),
                            images[cont.getContactType()]);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            display.setCurrent(contactsList);
            rms.closeRecordStore();
        }

        if( c == save ){
            rms.openRecordStore(ID_RMS);
            Contact cont = new Contact(nombre.getString(),
                     teléfono.getString(), tipoTel.getSelectedIndex(),
                     mail.getString(), tipoCont.getSelectedIndex());
            rms.newRecord(cont.getName(), cont.getPhoneNumber(),
                    cont.getPhoneType(), cont.getMail(), cont.getContactType());
            
            //Reordena la lista antes de presentarla
            contactsList.deleteAll();
            vIDs.removeAllElements();
            try{
                RecordEnumeration records = rms.getNumRecords();
                while(records.hasNextElement()){
                    int recordID = records.nextRecordId();
                    cont = rms.getRecord(recordID);
                    vIDs.addElement(new Integer(recordID));
                    contactsList.append(cont.getName(), images[cont.getContactType()]);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            contactsList.addCommand(deleteContact);
            display.setCurrent(contactsList);
            rms.closeRecordStore();
        }

        if( c == deleteContact ){
            deleteAlert = new Alert("Eliminar Contacto", "" +
                    contactsList.getString(contactsList.getSelectedIndex()) + 
                    "\n¿Eliminar contacto?", images[5], AlertType.CONFIRMATION);
            deleteAlert.addCommand(delete);
            deleteAlert.addCommand(backContactsList);
            deleteAlert.setCommandListener(this);
            display.setCurrent(deleteAlert);

        }

        if( c == delete ){
            rms.openRecordStore(ID_RMS);
            int index = contactsList.getSelectedIndex();
            int id = ((Integer)vIDs.elementAt(index)).intValue();

            //Limpieza RMS
            rms.deleteRecord(id);

            //Limpieza del vector
            vIDs.removeAllElements();
            //fill the ID vector with RMS records' ID
            RecordEnumeration records = null;
            try{
                records = rms.getNumRecords();
                while(records.hasNextElement()){
                    vIDs.addElement(new Integer(records.nextRecordId()));
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }

            // Clean the contact list
            contactsList.delete(index);
            if(contactsList.size() == 0)
                contactsList.removeCommand(deleteContact);
            display.setCurrent(contactsList);
            rms.closeRecordStore();
        }

        if( c == searchScreen ){
            searchField.setString("");
            display.setCurrent(searchForm);
        }

        if( c == search ){
            rms.openRecordStore(ID_RMS);
            String searchString = searchField.getString();
            Contact cont;
            try{
                RecordEnumeration records = rms.searchRecords(searchString);
                if(records.numRecords() > 0){
                    contactsList.deleteAll();
                    vIDs.removeAllElements();
                    while(records.hasNextElement()){
                    int recordID = records.nextRecordId();
                    cont = rms.getRecord(recordID);
                    vIDs.addElement(new Integer(recordID));
                    contactsList.append(cont.getName(),
                            images[cont.getContactType()]);
                    }
                    // Remove commands to set the new ones
                    contactsList.removeCommand(searchScreen);
                    contactsList.removeCommand(newContact);
                    contactsList.removeCommand(deleteContact);
                    contactsList.removeCommand(exit);
                    // Set new commands
                    contactsList.addCommand(backContactsList);
                    contactsList.addCommand(deleteContact);
                    display.setCurrent(contactsList);
                } else{
                    searchField.setString("");
                    display.setCurrent(noContacts);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
            //display.setCurrent(contactsList);
            rms.closeRecordStore();
        }

        if( c == List.SELECT_COMMAND ){
            rms.openRecordStore(ID_RMS);
            int index = contactsList.getSelectedIndex();
            int id = ((Integer)vIDs.elementAt(index)).intValue();
            // Selected contact is recovered from RMS
            Contact cont = rms.getRecord(id);
            //Form to visualize selected contact
            showForm = new Form("" + cont.getName());
            showForm.append("Nombre: " + cont.getName() + "\n\n");
            showForm.append("Teléfono: " + cont.getPhoneNumber() + "\n\n");
            showForm.append("mail: " + cont.getMail() + "\n\n");
            showForm.addCommand(backContactsList);
            showForm.setCommandListener(this);
            display.setCurrent(showForm);
            rms.closeRecordStore();
        }

    }
}
