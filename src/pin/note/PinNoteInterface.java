package pin.note;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import app.DeskTopNote;
import pin.Pin;



public interface PinNoteInterface {
	void setStyle( Element sty );

	Pin duplicate( Document doc, int x, int y,
			DeskTopNote bd, _PinNoteFactory fc );
}
