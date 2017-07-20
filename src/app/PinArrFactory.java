package app;

import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javafx.scene.control.MenuItem;
import pin.Pin;
import pin.memory._PinMemFactory;



public class PinArrFactory {
	protected static final String[] types= { "Memory", "File" };

	protected static PinArrFactory getFactory( Document doc, Element elm,
			DeskTopNoteAr deskTopNoteAr, String fACtype ) {
		switch( fACtype ){
			case "Memory" :
				return new _PinMemFactory( doc, elm, deskTopNoteAr );
			case "File" :
				return null;
			default :
				return null;
		}
	}

	protected ArrayList <Pin> getAllChildren() {
		return null;
	}

	protected void reInitCont() {}

	protected ArrayList <MenuItem> getMenus() {
		return null;
	}
}
