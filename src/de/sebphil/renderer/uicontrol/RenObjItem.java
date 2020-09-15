package de.sebphil.renderer.uicontrol;

import de.sebphil.renderer.objects.RenObject;

/**
 * 	Dise Klasse dient der Steuerung der Benutzeroberfl�che.
 *	Sie ist f�r die Lagerung eines RenObject in der TreeView n�tig.
 */
public class RenObjItem {

	private String name;
	private RenObject renObj;

	public RenObjItem(String name, RenObject renObj) {
		this.name = name;
		this.renObj = renObj;
	}

	public RenObjItem(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RenObject getRenObj() {
		return renObj;
	}

	public void setRenObj(RenObject renObj) {
		this.renObj = renObj;
	}
}
