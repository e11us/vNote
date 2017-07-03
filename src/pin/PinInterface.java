package pin;

import org.w3c.dom.*;



public interface PinInterface {
	/*-----------------------------------------------------------------------------------------
	 * to call after construction.
	 */
	public void init();

	/*-----------------------------------------------------------------------------------------
	 * create XML blank elm function for the data.
	 */
	public void createXMLdataElm( Document doc );

	/*-----------------------------------------------------------------------------------------
	 * return the XML elm.
	 */
	public Node getXMLdataElm();

	public String getID();
}
