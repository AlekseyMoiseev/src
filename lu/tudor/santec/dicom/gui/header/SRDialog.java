package lu.tudor.santec.dicom.gui.header;

import ij.plugin.BrowserLauncher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lu.tudor.santec.i18n.Translatrix;

import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;
import org.w3c.dom.Document;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.StructuredReport;
import com.pixelmed.dicom.StructuredReportTreeBrowser;
import com.pixelmed.dicom.XMLRepresentationOfStructuredReportObjectFactory;
import com.pixelmed.dose.CTDose;


/**
 * @author hermenj
 *
 * @version
 * <br>$Log: SRDialog.java,v $
 * <br>Revision 1.3  2014-10-30 11:14:33  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.2  2013-07-03 14:23:06  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.1  2013-06-25 10:04:39  hermen
 * <br>open on receive
 * <br>Sr Viewer
 * <br>
 */
public class SRDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(SRDialog.class.getName());
	
	private JTabbedPane tabbedPane;
	private JButton closeButton;
	private Vector<AttributeList> lists = new Vector<AttributeList>();

	private JButton viewDoseHTMLButton;

	private Component owner;

	private JButton viewXMLButton;

	public SRDialog() {
		super();
		buildDialog();
	}
	
	public SRDialog(JFrame owner) {
		super(owner);
		this.owner = owner;
		buildDialog();
	}
	
	public SRDialog(JDialog owner) {
		super(owner);
		this.owner = owner;
		buildDialog();
	}
	
	private void buildDialog() {
		setTitle("DICOM SR Viewer");
		
		this.setLayout(new BorderLayout());
		
		this.tabbedPane = new JTabbedPane();
		this.add(tabbedPane, BorderLayout.CENTER);
		this.tabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					int index = tabbedPane.getSelectedIndex();
					tabSelected(index);
				}
			}
		});
		
		ButtonBarBuilder bb = new ButtonBarBuilder();
		
		this.viewXMLButton = new JButton("View as XML");
		this.viewXMLButton.addActionListener(this);
		bb.addGridded(viewXMLButton);
		
		bb.addRelatedGap();
		
		this.viewDoseHTMLButton = new JButton("DoseSR as HTML");
		this.viewDoseHTMLButton.addActionListener(this);
		bb.addGridded(viewDoseHTMLButton);
		
		bb.addGlue();
		
		this.closeButton = new JButton(Translatrix.getTranslationString("dicom.close"));
		this.closeButton.addActionListener(this);
		bb.addGridded(closeButton);
		
		this.add(bb.getPanel(), BorderLayout.SOUTH);
		
		this.setSize(800, 700);
		
		if (owner != null)
			this.setLocationRelativeTo(owner);
	}
	
	protected void tabSelected(int index) {
		try {
			AttributeList list = lists.get(index);
			CTDose ctDose = new CTDose(list);
			viewDoseHTMLButton.setEnabled(true);
		} catch (Exception e2) {
//			 e2.printStackTrace();
			viewDoseHTMLButton.setEnabled(false);
		}
	}

	public void showReports(File[] sRs) {
		this.tabbedPane.removeAll();
		this.lists.clear();
		
		if (sRs == null || sRs.length == 0)
			return;
		
		for (File file : sRs) {
			AttributeList list = readSR(file);
			if (list != null ) {
				try {
					DicomHeader dh = new DicomHeader(file);
					String title = "<html>" + dh.getHeaderStringValue(Tag.PatientName) + "<br>" + 
							dh.getHeaderStringValue(Tag.ImageType)  + "<br>" + 
							dh.getHeaderDateValue(Tag.SeriesDate) + " /" + dh.getHeaderStringValue(Tag.InstanceNumber);
					JScrollPane scrollPane = new JScrollPane();
					new StructuredReportTreeBrowser(list,scrollPane);
					this.tabbedPane.add(title, scrollPane);
					lists.add(list);
				} catch (Exception e) {
					logger.error("Error showing SR", e);
				}
			}
		}
		this.tabbedPane.setSelectedIndex(0);
		this.tabSelected(0);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.closeButton)) {
			this.tabbedPane.removeAll();
			this.lists.clear();
			this.setVisible(false);
		} else if (e.getSource().equals(this.viewDoseHTMLButton)) {
			try {
				int index = this.tabbedPane.getSelectedIndex();
				if (index >= 0) {
					AttributeList list = this.lists.get(index);
					
					CTDose ctDose = new CTDose(list);
					String doseHTML = "<html>" + ctDose.getHTMLTableRow(true);
					JDialog dialog = new JDialog(this, "HTML Dose-SR View");
					JEditorPane editorPane = new JEditorPane();
					editorPane.setEditable(false);
					editorPane.setContentType("text/html");
					editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,Boolean.TRUE);
					editorPane.setText(doseHTML);
					JScrollPane editorScrollPane = new JScrollPane(editorPane);
					dialog.getContentPane().add(editorScrollPane);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					double width = 800;
					try {
						width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
					} catch (Exception e2) {
						// TODO: handle exception
					}
					dialog.setSize((int) width, 400);
					dialog.setLocationRelativeTo(this);
					dialog.setVisible(true);
				}				
			} catch (Exception e2) {
				e2.printStackTrace();
				logger.error("Error showing SR as HTML", e2);
			}
		} else if (e.getSource().equals(this.viewXMLButton)) {
			try {
				int index = this.tabbedPane.getSelectedIndex();
				if (index >= 0) {
					AttributeList list = this.lists.get(index);
					StructuredReport sr = new StructuredReport(list);
					Document document = new XMLRepresentationOfStructuredReportObjectFactory().getDocument(sr,list);

					File f = File.createTempFile("dicom_sr_", ".xml");
					f.deleteOnExit();
					FileOutputStream fout = new FileOutputStream(f);
					write(fout, document);
					
					// TODO open file
					BrowserLauncher.openURL(f.getAbsolutePath());
				}				
			} catch (Exception e2) {
				e2.printStackTrace();
				logger.error("Error showing SR as HTML", e2);
			}
		} 
		
	}
	
	
	private AttributeList readSR(File file) {
		try {
			AttributeList list = new AttributeList();
			list.read(file);
			return list;
		} catch (Exception e) {
			logger.error("Error parsing SR", e);
		}
		return null;
	}
	
	
	/**
	 * <p>Serialize an XML document (DOM tree).</p>
	 *
	 * @param	out		the output stream to write to
	 * @param	document	the XML document
	 * @exception	IOException
	 */
	public static void write(OutputStream out,Document document) throws IOException, TransformerConfigurationException, TransformerException {
		
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(out);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Properties outputProperties = new Properties();
		outputProperties.setProperty(OutputKeys.METHOD,"html");
		outputProperties.setProperty(OutputKeys.INDENT,"yes");
		outputProperties.setProperty(OutputKeys.ENCODING,"UTF-8");	// the default anyway
		transformer.setOutputProperties(outputProperties);
		transformer.transform(source, result);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File[] sRs = new File[] {
				new File("/media/daten/DICOM/DICOM_Images/Zitha DoseSR/dicom/13022015/08290000/03867043"),
				new File("/media/daten/DICOM/DICOM_Images/Zitha DoseSR/dicom/13022015/08290000/03864904"),
				new File("/media/daten/DICOM/DICOM_Images/SR/srdoc103/report03.dcm")
		};
		new SRDialog(new JFrame()).showReports(sRs);
		
	}

}
