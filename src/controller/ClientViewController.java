/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.*;

/**
 *
 * @author maxime
 */
public class ClientViewController implements Initializable  {
    
    @FXML
    TextField txt;
    @FXML
    TextArea sendMessage;
    @FXML
    ListView<User> list;
    @FXML
    Button bouton;
    
    static User utilisateur; // variable global (medine)
    
    
    private final ObjectProperty<Communaute> leModele = new SimpleObjectProperty<>(new Communaute());
        public Communaute getLeModele() {return leModele.get();};
        public void setLeModele(Communaute param) {leModele.set(param);}
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
            list.setCellFactory((param) -> {
            return new ListCell<User>(){
               @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    if (! empty) {
                        textProperty().bind(item.nameProperty());
                     
                    }else {
                        textProperty().unbind();
                        setText("");
                    }
                }  
            };
        });
    }    
    
    
    @FXML
    protected void onValidateButton (ActionEvent event) {
        String name = txt.getText();
        IMsn mn = null;
        ICallback cb = null;
	try {
            Registry rg =LocateRegistry.getRegistry("localhost",6767);
            mn = (IMsn) rg.lookup("Manager");
	} catch (NotBoundException | RemoteException e) {
            showMessage(Alert.AlertType.ERROR, null, "Erreur a l'accès du serveur, message d'erreur : \n\n" + e);
	}   
        try {
	    cb = new CallbackImpl();
	} catch (RemoteException e) {
	    System.out.println("Erreur de création, message d'erreur : \n\n" + e);
	}
	try {
            utilisateur = new User(name,mn,cb);
            utilisateur.enregistreServeur();
            launch();
	} catch (Exception e) {
            showMessage(Alert.AlertType.WARNING, null, "Erreur lors de l'authentification, message d'erreur : \n\n" + e);
	}
    }
        
    @FXML
    protected void onSendButton (ActionEvent event) {
        String message = sendMessage.getText();
        String name = list.getSelectionModel().getSelectedItem().getName();
	IMsn mn = null;
	try {
            Registry rg =LocateRegistry.getRegistry("localhost",6767);
            mn = (IMsn) rg.lookup("Manager");
	} catch (NotBoundException | RemoteException e) {
            showMessage(Alert.AlertType.ERROR, null, "Erreur a l'accès du serveur:" + e);
	}
        
	try {
            utilisateur.envoiMessage(message,name);
	} catch (Exception e) {
            showMessage(Alert.AlertType.WARNING, null, "Erreur lors de l'envoie du message, message d'erreur : \n\n" + e);
	}
        sendMessage.clear();
    }
    
    protected void launch() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/ClientView.fxml"));
        Scene scene = new Scene(root);
        Stage st = new Stage();
        st.setScene(scene);
        st.show();
        Stage stage = (Stage) bouton.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    protected void deconnexion (ActionEvent event) {
        IMsn mn = null;
	try {
            Registry rg =LocateRegistry.getRegistry("localhost",6767);
            mn = (IMsn) rg.lookup("Manager");
	} catch (NotBoundException | RemoteException e) {
            showMessage(Alert.AlertType.ERROR, null, "Erreur a l'accès du serveur, message d'erreur : \n\n" + e);
	}   
	try {
            utilisateur.seDeconnecte();
            showMessage(Alert.AlertType.INFORMATION, null, utilisateur.getName()+ " est déconnecté avec succès !\n On vous invite à quitter l'application (:");
	} catch (Exception e) {
            showMessage(Alert.AlertType.WARNING, null, "Erreur lors de la deconnexion, message d'erreur : \n\n" + e);
	}
    }
    
    @FXML
    protected void actualize (ActionEvent event) {
        try {
            Registry rg =LocateRegistry.getRegistry("localhost",6767);
            IMsn mn = (IMsn) rg.lookup("Manager"); 
            for (Map.Entry<String,ICallback> entry : mn.getMap().entrySet()) {
                User e = new User (entry.getKey(),mn,entry.getValue());
                if (!list.getItems().contains(e) && !e.getName().equals(utilisateur.getName())){
                    list.getItems().add(e);
                }
            }
        } catch (NotBoundException | RemoteException e){
            showMessage(Alert.AlertType.ERROR, null, "Erreur a l'accès du serveur, message d'erreur : \n\n" + e.getMessage());
        }
    }
    
    @FXML
    protected void exitApp(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    protected void cut(ActionEvent event) {
        this.sendMessage.cut();
    }

    @FXML
    protected void copy(ActionEvent event) {
        this.sendMessage.copy();
    }

    @FXML
    protected void paste(ActionEvent event) {
        this.sendMessage.paste();
    }

    @FXML
    protected void delete(ActionEvent event) {
        this.sendMessage.deleteNextChar();
    }

    @FXML
    protected void selectAll(ActionEvent event) {
        this.sendMessage.selectAll();
    }

    @FXML
    protected void fullscreen(ActionEvent event) {
            Stage stage = (Stage) this.sendMessage.getScene().getWindow();
            stage.setFullScreen(true);
    }
    
    protected Optional<ButtonType> showMessage(Alert.AlertType type,String header,String message,ButtonType... lesBoutonsDifferents){
        Alert laFenetre = new Alert(type);
        laFenetre.setHeaderText(header);
        laFenetre.setContentText(message);
        if (lesBoutonsDifferents.length > 0) {
            laFenetre.getButtonTypes().clear();
            laFenetre.getButtonTypes().addAll(lesBoutonsDifferents);
        }
        return laFenetre.showAndWait();
    }
    
}
