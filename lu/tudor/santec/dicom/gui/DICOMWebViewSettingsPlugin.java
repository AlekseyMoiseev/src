 package lu.tudor.santec.dicom.gui;

 /*****************************************************************************
  *                                                                           
  *  Copyright (c) 2006 by SANTEC/TUDOR www.santec.tudor.lu                   
  *                                                                           
  *                                                                           
  *  This library is free software; you can redistribute it and/or modify it  
  *  under the terms of the GNU Lesser General Public License as published    
  *  by the Free Software Foundation; either version 2 of the License, or     
  *  (at your option) any later version.                                      
  *                                                                           
  *  This software is distributed in the hope that it will be useful, but     
  *  WITHOUT ANY WARRANTY; without even the implied warranty of               
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
  *  Lesser General Public License for more details.                          
  *                                                                           
  *  You should have received a copy of the GNU Lesser General Public         
  *  License along with this library; if not, write to the Free Software      
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  
  *                                                                           
  *****************************************************************************/

import ij.plugin.BrowserLauncher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.SettingsPlugin;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 */
public class DICOMWebViewSettingsPlugin extends SettingsPlugin {

	private static final long serialVersionUID = 1L;

	public static final String WEBVIEW1_ENABLED = "WebView1Enabled";
	public static final String WEBURL1 = "WEBURL1";
	public static final String WEBNAME1 = "WEBNAME1";
	
	public static final String WEBVIEW2_ENABLED = "WebView2Enabled";
	public static final String WEBURL2 = "WEBURL2";
	public static final String WEBNAME2 = "WEBNAME2";
	
	public static final String WEBVIEW3_ENABLED = "WebView3Enabled";
	public static final String WEBURL3 = "WEBURL3";
	public static final String WEBNAME3 = "WEBNAME3";

	private JPanel dicomWebViewPanel;

	private JCheckBox url1Enabled;

	private JButton jb1;

	private JLabel url1StringLabel;

	private JTextField url1String;

	private JLabel help1String;

	private JLabel help1StringLabel;

	private JLabel name1StringLabel;

	private JTextField name1String;

	private JCheckBox url2Enabled;

	private JLabel name2StringLabel;

	private JTextField name2String;

	private JLabel url2StringLabel;

	private JTextField url2String;

	private JLabel help2StringLabel;

	private JLabel help2String;

	private JCheckBox url3Enabled;

	private JLabel name3StringLabel;

	private JTextField name3String;

	private JLabel url3StringLabel;

	private JTextField url3String;

	private JLabel help3StringLabel;

	private JLabel help3String;

	 private static Logger logger = Logger.getLogger("lu.tudor.santec.dicom.gui.DICOMWebViewSettingsPlugin");
	 
	 private final static ImageIcon FAILED = DicomIcons.getIcon(DicomIcons.STATUS_FAILED);
//	 private final static ImageIcon OK = DicomIcons.getIcon(DicomIcons.STATUS_OK);
	 private final static ImageIcon UNKNOWN = DicomIcons.getIcon(DicomIcons.STATUS_UNKNOWN);

	// ***************************************************************************
	// * Constructor *
	// ***************************************************************************

	 

		/**
		 * Creates a new instance of DICOMPlugin
		 */
		public DICOMWebViewSettingsPlugin(String name) {
			super(name);
			init();
		}

	private void init() {
		setIcon(DicomIcons.getIcon(DicomIcons.WEB));
		this.setStationaryValues();
		this.buildPanel();
				
		relocalize();
	}

	// ***************************************************************************
	// * Class Primitives *
	// ***************************************************************************

	/**
	 * adds the components to the panel
	 */
	private void buildPanel() {
		initComponents();
		CellConstraints cc = new CellConstraints();
		FormLayout dicomQueryLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 20dlu, 10dlu",
				"pref, 2dlu, pref, 2dlu, pref, 2dlu, top:pref, 2dlu");
	
//		 build the Layout for DICOM webview 1
		dicomWebViewPanel = createSubPanel(Translatrix.getTranslationString("dicom.DicomWebView") + " 1");
		dicomWebViewPanel.setLayout(dicomQueryLayout);
		dicomWebViewPanel.add(this.url1Enabled, cc.xyw(1, 1, 3));
		jb1 = createTestButton(url1String);
		dicomWebViewPanel.add(jb1, cc.xy(10, 1));
		dicomWebViewPanel.add(this.name1StringLabel, cc.xy(1, 3));
		dicomWebViewPanel.add(this.name1String, cc.xyw(3, 3, 8));
		dicomWebViewPanel.add(this.url1StringLabel, cc.xy(1, 5));
		dicomWebViewPanel.add(this.url1String, cc.xyw(3, 5, 8));
		dicomWebViewPanel.add(this.help1StringLabel, cc.xy(1, 7));
		dicomWebViewPanel.add(this.help1String, cc.xyw(3, 7, 8));
		addSubPanel(dicomWebViewPanel);
		
		dicomWebViewPanel = createSubPanel(Translatrix.getTranslationString("dicom.DicomWebView") + " 2");
		dicomWebViewPanel.setLayout(dicomQueryLayout);
		dicomWebViewPanel.add(this.url2Enabled, cc.xyw(1, 1, 3));
		JButton jb2 = createTestButton(url2String);
		dicomWebViewPanel.add(jb2, cc.xy(10, 1));
		dicomWebViewPanel.add(this.name2StringLabel, cc.xy(1, 3));
		dicomWebViewPanel.add(this.name2String, cc.xyw(3, 3, 8));
		dicomWebViewPanel.add(this.url2StringLabel, cc.xy(1, 5));
		dicomWebViewPanel.add(this.url2String, cc.xyw(3, 5, 8));
		dicomWebViewPanel.add(this.help2StringLabel, cc.xy(1, 7));
		dicomWebViewPanel.add(this.help2String, cc.xyw(3, 7, 8));
		addSubPanel(dicomWebViewPanel);
		
		dicomWebViewPanel = createSubPanel(Translatrix.getTranslationString("dicom.DicomWebView") + " 3");
		dicomWebViewPanel.setLayout(dicomQueryLayout);
		dicomWebViewPanel.add(this.url3Enabled, cc.xyw(1, 1, 3));
		JButton jb3 = createTestButton(url3String);
		dicomWebViewPanel.add(jb3, cc.xy(10, 1));
		dicomWebViewPanel.add(this.name3StringLabel, cc.xy(1, 3));
		dicomWebViewPanel.add(this.name3String, cc.xyw(3, 3, 8));
		dicomWebViewPanel.add(this.url3StringLabel, cc.xy(1, 5));
		dicomWebViewPanel.add(this.url3String, cc.xyw(3, 5, 8));
		dicomWebViewPanel.add(this.help3StringLabel, cc.xy(1, 7));
		dicomWebViewPanel.add(this.help3String, cc.xyw(3, 7, 8));
		addSubPanel(dicomWebViewPanel);
		

	}

	/**
	 * initialises the Components
	 */
	private void initComponents() {

		String help = "<html>" +  
				"$PATID<br>" + 
		    	"$ACCNR<br>" + 
		    	"$STUDYUID<br>" + 
		    	"$SERIESUID<br>" + 
		    	"$SOPUID";
		
		//	 elements for Webview 
		this.url1Enabled = new JCheckBox();
		this.name1StringLabel = new JLabel();
		this.name1String = new JTextField();
		this.url1StringLabel = new JLabel();
		this.url1String = new JTextField();
		this.help1StringLabel = new JLabel();
		this.help1String = new JLabel();
		this.help1String.setText(help);
		
		this.url2Enabled = new JCheckBox();
		this.name2StringLabel = new JLabel();
		this.name2String = new JTextField();
		this.url2StringLabel = new JLabel();
		this.url2String = new JTextField();
		this.help2StringLabel = new JLabel();
		this.help2String = new JLabel();
		this.help2String.setText(help);
		
		this.url3Enabled = new JCheckBox();
		this.name3StringLabel = new JLabel();
		this.name3String = new JTextField();
		this.url3StringLabel = new JLabel();
		this.url3String = new JTextField();
		this.help3StringLabel = new JLabel();
		this.help3String = new JLabel();
		this.help3String.setText(help);
		
	}

	// ***************************************************************************
	// * Class Body *
	// ***************************************************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingsPlugin#revertToDefaults()
	 */
	public void revertToDefaults() {

		// Webview
		this.url1Enabled.setSelected(((Boolean) getDefault(WEBVIEW1_ENABLED)).booleanValue());
		this.url1String.setText((String) getDefault(WEBURL1));
		
		this.url2Enabled.setSelected(((Boolean) getDefault(WEBVIEW2_ENABLED)).booleanValue());
		this.url2String.setText((String) getDefault(WEBURL2));
		
		this.url3Enabled.setSelected(((Boolean) getDefault(WEBVIEW3_ENABLED)).booleanValue());
		this.url3String.setText((String) getDefault(WEBURL3));
		
		reflectSettings();
		super.revertToDefaults();
	}

	public void setStationaryValues() {

		//	dicom query
		setStationary(WEBVIEW1_ENABLED, new Boolean(false));
		setStationary(WEBNAME1, "View Patient");
		setStationary(WEBURL1, "http://pacs_ip:8080/webviewer?patID=$PATID");
		
		setStationary(WEBVIEW2_ENABLED, new Boolean(false));
		setStationary(WEBNAME2, "View Series");
		setStationary(WEBURL2, "http://pacs_ip:8080/webviewer?seriesID=$SERIESUID");
		
		setStationary(WEBVIEW3_ENABLED, new Boolean(false));
		setStationary(WEBNAME3, "View Study");
		setStationary(WEBURL3, "http://pacs_ip:8080/webviewer?studyID=$STUDYUID");
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingsPlugin#updateSettings()
	 */
	public void updateSettings() {
		// Webview
		setValue(WEBVIEW1_ENABLED, new Boolean(this.url1Enabled.isSelected()));
		setValue(WEBNAME1, this.name1String.getText());
		setValue(WEBURL1, this.url1String.getText());
		
		setValue(WEBVIEW2_ENABLED, new Boolean(this.url2Enabled.isSelected()));
		setValue(WEBNAME2, this.name2String.getText());
		setValue(WEBURL2, this.url2String.getText());
		
		setValue(WEBVIEW3_ENABLED, new Boolean(this.url3Enabled.isSelected()));
		setValue(WEBNAME3, this.name3String.getText());
		setValue(WEBURL3, this.url3String.getText());
	
		super.updateSettings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingsPlugin#reflectSettings()
	 */
	public void reflectSettings() {
		super.reflectSettings();
		try {

			//	Webview
			this.url1Enabled.setSelected(((Boolean) getValue(WEBVIEW1_ENABLED)).booleanValue());
			this.url1String.setText((String) getValue(WEBURL1));
			this.name1String.setText((String) getValue(WEBNAME1));
			
			this.url2Enabled.setSelected(((Boolean) getValue(WEBVIEW2_ENABLED)).booleanValue());
			this.url2String.setText((String) getValue(WEBURL2));
			this.name2String.setText((String) getValue(WEBNAME2));
			
			this.url3Enabled.setSelected(((Boolean) getValue(WEBVIEW3_ENABLED)).booleanValue());
			this.url3String.setText((String) getValue(WEBURL3));
			this.name3String.setText((String) getValue(WEBNAME3));
			
		} catch (Exception e) {
			logger.warn("Could not load Settings: "+e.getLocalizedMessage()); 
		}
	}

	/**
	 * Method is part of the Relocalizable interface. The method does everything
	 * required to reflect changes of active Locale
	 */
	public void relocalize() {
		
		setLabel(Translatrix.getTranslationString("dicom.DicomWebView"));
				
		setSubPanelTitle(dicomWebViewPanel,Translatrix.getTranslationString("dicom.DicomWebView"));
		
		this.url1Enabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.name1StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewName"));
		this.url1StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewURL"));
		this.help1StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewHelp"));
		
		this.url2Enabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.name2StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewName"));
		this.url2StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewURL"));
		this.help2StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewHelp"));
		
		this.url3Enabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.name3StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewName"));
		this.url3StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewURL"));
		this.help3StringLabel.setText(Translatrix.getTranslationString("dicom.WebViewHelp"));
	}

    public JButton createTestButton(final JTextField urlString) {
    	final JButton jb = new JButton(UNKNOWN);
    	jb.setToolTipText("Test connection");
    	jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					BrowserLauncher.openURL(getURL(urlString.getText(), "aaaa", "bbbb", "cccc", "dddd", "eeee"));		
				} catch (Exception ee) {
					ee.printStackTrace();
					jb.setIcon(FAILED);
				}
			}
    	});
    	return jb;
    }
    
    public static String getURL(String url, String patID, String accNr, String studyUID, String seriesUID, String sopUID) {
    	url = url.replaceAll("\\$PATID", patID);
    	url = url.replaceAll("\\$ACCNR", accNr);
    	url = url.replaceAll("\\$STUDYUID", studyUID);
    	url = url.replaceAll("\\$SERIESUID", seriesUID);
    	url = url.replaceAll("\\$SOPUID", sopUID);
    	return url;
    }
    
    public static void openURL(String url, String patID, String accNr, String studyUID, String seriesUID, String sopUID) throws IOException {
    	BrowserLauncher.openURL(getURL(url, patID, accNr, studyUID, seriesUID, sopUID));
    }
       
    public Vector<JButton> getButtons() {
    	Vector<JButton> buttons = new Vector<JButton>();
    	if (url1Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon(DicomIcons.WEB1));
    		b1.setToolTipText(name1String.getText());
    		b1.setActionCommand(url1String.getText());
    		buttons.add(b1);
    	}
    	if (url2Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon(DicomIcons.WEB2));
    		b1.setToolTipText(name2String.getText());
    		b1.setActionCommand(url2String.getText());
    		buttons.add(b1);
    	}
    	if (url3Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon(DicomIcons.WEB3));
    		b1.setToolTipText(name3String.getText());
    		b1.setActionCommand(url3String.getText());
    		buttons.add(b1);
    	}
    	return buttons;
    }
    
    public Vector<JButton> getButtons22() {
    	Vector<JButton> buttons = new Vector<JButton>();
    	if (url1Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon22(DicomIcons.WEB1));
    		b1.setToolTipText(name1String.getText());
    		b1.setActionCommand(url1String.getText());
    		buttons.add(b1);
    	}
    	if (url2Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon22(DicomIcons.WEB2));
    		b1.setToolTipText(name2String.getText());
    		b1.setActionCommand(url2String.getText());
    		buttons.add(b1);
    	}
    	if (url3Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon22(DicomIcons.WEB3));
    		b1.setToolTipText(name3String.getText());
    		b1.setActionCommand(url3String.getText());
    		buttons.add(b1);
    	}
    	return buttons;
    }
    
    public Vector<JButton> getButtons16() {
    	Vector<JButton> buttons = new Vector<JButton>();
    	if (url1Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon16(DicomIcons.WEB1));
    		b1.setToolTipText(name1String.getText());
    		b1.setActionCommand(url1String.getText());
    		buttons.add(b1);
    	}
    	if (url2Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon16(DicomIcons.WEB2));
    		b1.setToolTipText(name2String.getText());
    		b1.setActionCommand(url2String.getText());
    		buttons.add(b1);
    	}
    	if (url3Enabled.isSelected()) {
    		JButton b1 = new JButton(DicomIcons.getIcon16(DicomIcons.WEB3));
    		b1.setToolTipText(name3String.getText());
    		b1.setActionCommand(url3String.getText());
    		buttons.add(b1);
    	}
    	return buttons;
    }

}
