package de.sebphil.renderer.uicontrol;

import de.sebphil.renderer.objects.RenObject;

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
