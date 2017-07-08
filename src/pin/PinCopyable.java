package pin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import app.DeskTopNote;
import javafx.geometry.Point2D;
import pin.note._PinNoteFactory;



public interface PinCopyable {
	public String getFactyName();

	public void selectHL();

	public void selectDeHL();

	public Element getXMLDatForDup();

	public boolean setXMLDatForDup( Element inp );

	public void deleteAfterCut();

	public Point2D getGridloc();
}
