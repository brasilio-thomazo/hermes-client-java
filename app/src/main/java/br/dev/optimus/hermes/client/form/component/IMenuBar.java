package br.dev.optimus.hermes.client.form.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class IMenuBar extends MenuBar {
    public IMenuBar(EventHandler<ActionEvent> actionEventEventHandler) {
        MenuItem itemGenerate = new MenuItem("Gerar arquivo");
        itemGenerate.setId("menu-generate");
        itemGenerate.setOnAction(actionEventEventHandler);
        MenuItem itemClose = new MenuItem("Fechar");
        itemClose.setId("menu-close");
        itemClose.setOnAction(actionEventEventHandler);
        Menu menuFile = new Menu("Arquivo", null, itemClose);
        Menu menuTest = new Menu("Testes", null, itemGenerate);
        getMenus().addAll(menuFile, menuTest);
    }
}
