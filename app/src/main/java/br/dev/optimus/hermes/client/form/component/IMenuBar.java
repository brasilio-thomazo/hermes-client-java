package br.dev.optimus.hermes.client.form.component;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class IMenuBar extends MenuBar {
    public IMenuBar(EventHandler<ActionEvent> actionEventEventHandler) {
        MenuItem itemGenerate = new MenuItem("Gerar arquivo");
        itemGenerate.setId("menu-generate");
        itemGenerate.setOnAction(actionEventEventHandler);
        MenuItem itemTest = new MenuItem("Connection test");
        itemGenerate.setId("menu-test");
        itemGenerate.setOnAction(actionEventEventHandler);
        MenuItem itemClose = new MenuItem("Fechar");
        itemClose.setId("menu-close");
        itemClose.setOnAction(actionEventEventHandler);
        Menu menuFile = new Menu("Arquivo", null, itemClose);
        Menu menuTest = new Menu("Testes", null, itemTest, itemGenerate);
        getMenus().addAll(menuFile, menuTest);
    }
}
