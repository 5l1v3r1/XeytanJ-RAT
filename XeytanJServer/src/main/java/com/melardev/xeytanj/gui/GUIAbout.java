package com.melardev.xeytanj.gui;

public class GUIAbout extends AbstractDialogHelp {

	public GUIAbout() {
		super("About XeytaJ" , "XeytaJ" , "Java Based RAT By MelarDev<br /> melardev.com");
	}

	@Override
	public void setDialogSize() {
		setSize(640, 350);		
	}
}
