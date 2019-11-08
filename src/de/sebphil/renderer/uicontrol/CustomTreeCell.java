package de.sebphil.renderer.uicontrol;

import javafx.scene.control.TreeCell;

public class CustomTreeCell extends TreeCell<RenObjItem> {

	@Override
	protected void updateItem(RenObjItem objItem, boolean empty) {
		// TODO Auto-generated method stub
		super.updateItem(objItem, empty);
		prefWidthProperty().bind(widthProperty());
		if (isEmpty()) {
			setText(null);
			setGraphic(null);
		} else {
			setText(objItem.getName());
			setGraphic(getGraphic());
		}
	}

}
