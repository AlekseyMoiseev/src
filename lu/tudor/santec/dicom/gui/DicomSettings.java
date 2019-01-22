package lu.tudor.santec.dicom.gui;

import java.io.File;

import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.SettingsPanel;

public class DicomSettings {

	private static DICOMSettingsPlugin dicomPlugin;
	private static SettingsPanel settings;
	private static DICOMWebViewSettingsPlugin dicomWebViewPlugin;

	public static SettingsPanel getSettingPanel() {
		if (settings == null) {
		    try {
	        	    Translatrix.addBundle("lu.tudor.santec.settings.resources.WidgetResources");
	        	    Translatrix.addBundle("lu.tudor.santec.dicom.gui.resources.WidgetResources");
	        	    Translatrix.addBundle(SwingLocalizer.getBundle());
	        	    Translatrix.setDefaultWhenMissing(true);
	        	    SwingLocalizer.localizeJFileChooser();
	        	    SwingLocalizer.localizeJOptionPane();		    
			} catch (Exception e) {
			    e.printStackTrace();
			}
	                
		    settings = new SettingsPanel(null);

		    dicomPlugin = new DICOMSettingsPlugin("dicom");
			settings.addPlugin(dicomPlugin);
			
//			loggingPlugin = new LoggingPlugin("logging");
//			settings.addPlugin(loggingPlugin);
			
//			dicomWebViewPlugin = new DICOMWebViewSettingsPlugin("dicomwebview");
//			settings.addPlugin(dicomWebViewPlugin);
			
			settings.setSettingsFile(new File("settings.xml"));
			settings.loadSettings();
		} 
		return settings;
	}
	
	public static DICOMSettingsPlugin getDicomSettingsPlugin() {
		if (dicomPlugin == null) {
			getSettingPanel();
		}
		return dicomPlugin;
	}
}
