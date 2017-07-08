package pin.note;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import app.DeskTopNote;
import pin.Pin;



public interface PinNoteInterface {
	public void setStyle( Element sty );

	public String getTypeName();

	void deleteThis();
}
