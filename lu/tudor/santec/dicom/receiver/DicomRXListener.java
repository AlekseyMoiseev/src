package lu.tudor.santec.dicom.receiver;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.receiver.DICOMListener;
import lu.tudor.santec.dicom.receiver.DicomEvent;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;

/**
 * @author hermenj
 *
 * @version
 * <br>$Log: DicomRXListener.java,v $
 * <br>Revision 1.4  2013-08-23 12:58:58  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.3  2013-07-12 06:02:28  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.2  2013-06-20 14:22:59  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.1  2013-06-19 13:53:17  hermen
 * <br>*** empty log message ***
 * <br>
 */
public abstract class DicomRXListener implements DICOMListener{
	
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomRXListener.class.getName());

	private ConcurrentHashMap<String, Object[]> lastSeries = new ConcurrentHashMap<String, Object[]>();
	private ConcurrentHashMap<String, Object[]> lastStudies = new ConcurrentHashMap<String, Object[]>();
	
	public DicomRXListener(long waitSeconds) {
		final long waitmillis = waitSeconds*1000;
		new Thread() {
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
						long now = System.currentTimeMillis();
						// check for old series
						for (String key : lastSeries.keySet()) {
							Object[] val = lastSeries.get(key);
							Long time = (Long) val[0];
							if ((time + waitmillis) < now) {
								File folder = (File) val[1];
								seriesReceived(key, folder);
								lastSeries.remove(key);
							}
						}
						// check for old studies
						for (String key : lastStudies.keySet()) {
							Object[] val = lastStudies.get(key);
							Long time = (Long) val[0];
							if ((time + waitmillis * 3) < now) {
								File folder = (File) val[1];
								studyReceived(key, folder);
								lastStudies.remove(key);
							}
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						logger.log(Level.ERROR, "", e);
					}
				}
			}
		}.start();
		
	}

	@Override
	public void fireDicomEvent(DicomEvent event) {
		if (event != null && DicomEvent.ADD.equals(event.getType())) {
			File f = event.getFile();
			DicomHeader dh = new DicomHeader(f);
			Long time = System.currentTimeMillis();
			String sUID = dh.getHeaderStringValue(Tag.SeriesInstanceUID);
			Object[] val = new Object[] { time, f.getAbsoluteFile().getParentFile() };
			lastSeries.put(sUID, val);
//			System.out.println("ADD " + sUID + " " + time);
			
			String studyUID = dh.getHeaderStringValue(Tag.StudyInstanceUID);
			Object[] val2 = new Object[] { time, f.getAbsoluteFile().getParentFile().getParentFile() };
			lastStudies.put(studyUID, val2);
//			System.out.println("ADD " + sUID + " " + time);
			
			String iUID = dh.getHeaderStringValue(Tag.SOPInstanceUID);
			imageReceived(iUID, f);
		}
	}
	
	protected abstract void imageReceived(String sopUID, File image);
	
	protected abstract void seriesReceived(String seriesUID, File folder);
	
	protected abstract void studyReceived(String studyUID, File folder);

}
