package lu.tudor.santec.dicom.receiver;

/*****************************************************************************
 *                                                                           
 *  Copyright (c) 2008 by SANTEC/TUDOR www.santec.tudor.lu                   
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.transform.TransformerConfigurationException;

import lu.tudor.santec.dicom.gui.header.DicomHeader;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.media.ApplicationProfile;
import org.dcm4che2.media.DicomDirWriter;
import org.dcm4che2.media.DirectoryRecordType;
import org.dcm4che2.media.FileSetInformation;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomDirReader {

	static final Logger log = Logger.getLogger("DicomDirReader");

	private File STORE_DIR = null;
	
	public static final String LEVEL_PATIENT = "PATIENT";
	public static final String LEVEL_STUDY = "STUDY";
	public static final String LEVEL_SERIES = "SERIES";
	public static final String LEVEL_IMAGE = "IMAGE";
	
	private final static String[] QRLEVEL = { LEVEL_PATIENT, LEVEL_STUDY, LEVEL_SERIES, LEVEL_IMAGE};
	
	private static final int[] DICOM_DIR_NAMING = {
	    Tag.PatientName,
	    Tag.Modality,
	    Tag.StudyDate,
	    Tag.StudyTime,
	    Tag.StudyID,
	    Tag.SeriesNumber,
	    Tag.SOPInstanceUID
	};

	private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");

	private org.dcm4che2.media.DicomDirReader dicomDir;

	private int qrLevel;

	private Vector<DicomObject> foundRecords = new Vector<DicomObject>();

	private FileSetInformation fsinfo;
	
	private ApplicationProfile ap = new MyExtendedApplicationProfile();

	private boolean checkDuplicate = true;
	
	
	public boolean loadDicomDirFile(File dirFile, boolean create, boolean writeable) {
		log.info("loading DICOMDIR: \"" + dirFile.getAbsolutePath()+ "\"");
		STORE_DIR = dirFile;
		try {
			
			// create dicomdir
			if (create) {
				// open dicom writer
			        File rootDir = STORE_DIR.getParentFile();
			        if (rootDir != null && !rootDir.exists()) {
			            rootDir.mkdirs();
			        }
				try {
				    if (STORE_DIR.exists()) {
					// open dicomdir
				        dicomDir = new DicomDirWriter(STORE_DIR); 
				        log.info("Loading DICOMDIR at: " + STORE_DIR.getParentFile() +  "  ----------------------------");					
				    } else {
						// create dicomdir
						dicomDir = new DicomDirWriter(STORE_DIR, fsinfo());
						log.info("Creating DICOMDIR at: " + STORE_DIR.getParentFile() +  "  ----------------------------");					
				    }
				    
				    
				} catch (Exception e) {
					log.info("Error starting DicomdirWriter: \"" + dirFile + "\"  "+ e.getMessage());
				}
			} else if (writeable && STORE_DIR.exists()) {
				// open dicomdir
		        dicomDir = new DicomDirWriter(STORE_DIR); 
		        log.info("Loading DICOMDIR at: " + STORE_DIR.getParentFile() +  "  ----------------------------");					
			} else {
			    dicomDir = new org.dcm4che2.media.DicomDirReader(dirFile);
			    
			}
			dicomDir.setShowInactiveRecords(false);
		} catch (Exception e) {
			log.info("Error loading Dicomdir: The File: \"" +dirFile + "\"" + " does not exits, or is no dicomdir");
			return false;
		}
		return true;
	}

	public boolean isOK() {
		if (dicomDir != null) 
			return true;
		else
			return false;
	}
	
	public Vector<DicomObject> getPatients() {
		Vector<DicomObject> patientVector = new Vector<DicomObject>();
		log.info("reading patient records:");
		try {
			DicomObject next = dicomDir.findFirstRootRecord();
			while (next != null) {
				if (DirectoryRecordType.PATIENT.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						patientVector.add(next);
					} catch (Exception e) {
						log.warn("Error getting Patients: "+e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("Error getting Patients: "+e.getLocalizedMessage());
		}
		return patientVector;
	}

	public Vector<DicomObject> getStudiesFromPatients(DicomObject patientRecord) {
		Vector<DicomObject> studyVector = new Vector<DicomObject>();
		log.info("reading study records:");
		try {
			DicomObject next = dicomDir.findFirstChildRecord(patientRecord);
			while (next != null) {
			    if (DirectoryRecordType.STUDY.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						studyVector.add(next);
					} catch (Exception e) {
						log.warn("Error getting Studies: "+e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("Error getting Studies: "+e.getLocalizedMessage());
		}
		return studyVector;
	}

	public Vector<StudyObject> getAllStudys() {
		String patientName = null;
		String patientID = null;
		Vector<StudyObject> studyVector = new Vector<StudyObject>();
		log.info("reading patient records:");
		try {
			DicomObject next = dicomDir.findFirstRootRecord();
			while (next != null) {
			    if (DirectoryRecordType.PATIENT.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						patientName = next.getString(Tag.PatientName);
						patientID = next.getString(Tag.PatientID);
						DicomObject study = dicomDir.findFirstChildRecord(next);
						while (study != null) {
						    if (DirectoryRecordType.STUDY.equals(next.getString(Tag.DirectoryRecordType))) {
								try {
									studyVector.add(new StudyObject(study,
											patientName, patientID));
								} catch (Exception e) {
									log.warn("Error getting AllStudies: "+e.getLocalizedMessage());
								}
							}
							study = dicomDir.findNextSiblingRecord(study);
						}
					} catch (Exception e) {
						log.warn("Error getting AllStudies: "+e.getLocalizedMessage());
					}
				} 
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("Error getting AllStudies: "+e.getLocalizedMessage());
		}
		Collections.sort(studyVector, new StudyComparer());
		return studyVector;
	}

	public Vector<DicomObject> getSeriesFromStudy(DicomObject studyRecord) {
		Vector<DicomObject> seriesVector = new Vector<DicomObject>();
		log.info("reading series records:");
		try {
			DicomObject next = dicomDir.findFirstChildRecord(studyRecord);
			while (next != null) {
			    	if (DirectoryRecordType.SERIES.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						seriesVector.add(next);
					} catch (Exception e) {
						log.warn("Error getting Series from Studies: "+e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("Error getting Series from Studies: "+e.getLocalizedMessage());
		}
		return seriesVector;
	}

	public Vector<DicomObject> getSeriesFromPatient(DicomObject patientRecord) {
		Vector<DicomObject> seriesVector = new Vector<DicomObject>();
		log.info("reading series records:");
		try {
			DicomObject study = dicomDir.findFirstChildRecord(patientRecord);
			// list all studies
			while (study != null) {
			    if (DirectoryRecordType.STUDY.equals(study.getString(Tag.DirectoryRecordType))) {
					try {
						DicomObject series = dicomDir.findFirstChildRecord(study);
						// list all series
						while (series != null) {
						    if (DirectoryRecordType.SERIES.equals(series.getString(Tag.DirectoryRecordType))) {
								try {
									seriesVector.add(series);
									System.out.println("found series: " + series);
								} catch (Exception e) {
									log.warn("Error getting Series from Patients: "+e.getLocalizedMessage());
								}
							}
							series = dicomDir.findNextSiblingRecord(series);
						}
					} catch (Exception e) {
						log.warn("Error getting Series from Patients: "+e.getLocalizedMessage());
					}
				}
			    study = dicomDir.findNextSiblingRecord(study);
			}
		} catch (IOException e) {
			log.warn("Error getting Series from Patients: "+e.getLocalizedMessage());
		}
		return seriesVector;
	}

	public Vector<DicomObject> getImagesFromSeries(DicomObject seriesRecord) {

		Vector<DicomObject> imageVector = new Vector<DicomObject>();
		log.info("reading image records:");
		try {
			DicomObject next = dicomDir.findFirstChildRecord(seriesRecord);
			while (next != null) {
//			    if (DirectoryRecordType.IMAGE.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						imageVector.add(next);
					} catch (Exception e) {
						log.warn("Error getting Images from Series: "+e.getLocalizedMessage());
					}
//				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("Error getting Images from Series: "+e.getLocalizedMessage());
		}
		Collections.sort(imageVector, new ImageComparer());
		return imageVector;
	}

	public File[] getImagePathsFromSeries(DicomObject seriesRecord) {
	
		Vector<DicomObject> imageVector = getImagesFromSeries(seriesRecord);
		File[] files = new File[imageVector.size()];
		int i = 0;
		for (Iterator<DicomObject> iter = imageVector.iterator(); iter.hasNext();) {
			DicomObject element = (DicomObject) iter.next();
			try {					
				File f = dicomDir.toReferencedFile(element);
				
				files[i] = checkFileLowerCase(f.getAbsoluteFile());
				i++;
			} catch (Exception e) {
				log.warn("Error getting Imagepaths from Series: "+e.getLocalizedMessage());			
			}
		}
		return files;
	}
	
	public File getImageFromSeriesUID(String seriesInstanceUID, int instanceNumber) {
		try {
			Vector<DicomObject> v = getImageRecordFromSeriesUID(seriesInstanceUID, instanceNumber);
			DicomObject dr = (DicomObject) v.get(0);
			return getImagePathFromImage(dr);			
		} catch (Exception e) {
		}		
		return null;
	}
	
	public File getImageFromSOPInstanceUID(String SOPInstanceUID) {
		try {
			DicomObject dr = getImageRecordFromSOPInstanceUID(SOPInstanceUID);
			return getImagePathFromImage(dr);			
		} catch (Exception e) {
		}		
		return null;
	}
	
	public File[] getSeriesFromSeriesUID(String seriesInstanceUID) {
		ArrayList<File> al = new ArrayList<File>();
		try {
			Vector<DicomObject> v = getImageRecordFromSeriesUID(seriesInstanceUID, -1);
			for (Iterator<DicomObject> iter = v.iterator(); iter.hasNext();) {
				DicomObject dr = (DicomObject) iter.next();
				al.add(getImagePathFromImage(dr));		
			}
		} catch (Exception e) {
		}		
		return (File[]) al.toArray(new File[0]);
	}
	
	
	public DicomObject getRecordFromSeriesUID(String seriesInstanceUID) {
		Vector<DicomObject> v = new Vector<DicomObject>();

		DicomObject seriesKeys = new BasicDicomObject();
		seriesKeys.putString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUID);
		
		try {
			Vector<DicomObject> results = this.query(LEVEL_SERIES, seriesKeys);
			if (results.size() > 1) {
				System.out.println("SeriesInstanceUID not unique");
			}
			DicomObject seriesRecord = (DicomObject) results.get(0);
			return seriesRecord;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public Vector<DicomObject> getImageRecordFromSeriesUID(String seriesInstanceUID, int instanceNumber) {
		Vector<DicomObject> v = new Vector<DicomObject>();

		DicomObject seriesKeys = new BasicDicomObject();
		seriesKeys.putString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUID);
		
		try {
			Vector<DicomObject> results = this.query(LEVEL_SERIES, seriesKeys);
			if (results.size() > 1) {
				System.out.println("SeriesInstanceUID not unique");
			}
			DicomObject seriesRecord = (DicomObject) results.get(0);
			DicomObject next = dicomDir.findFirstChildRecord(seriesRecord);
			while (next != null) {
			    if (DirectoryRecordType.IMAGE.equals(next.getString(Tag.DirectoryRecordType))) {
//					System.out.println("(0002,0003) " + next.getDataset().getInteger());
					if (instanceNumber == -1) {
						v.add(next);
					} else if (next.getInt(Tag.InstanceNumber) == instanceNumber) {
						v.add(next);
						return v;
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (Exception e) {
		}		
		return v;
	}
	
	public DicomObject getImageRecordFromSOPInstanceUID(String SOPInstanceUID) {
	    
	    DicomObject imageKeys = new BasicDicomObject();
		imageKeys.putString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI, SOPInstanceUID);

		try {
			Vector<DicomObject> results = this.query(LEVEL_IMAGE, imageKeys);
			for (Iterator<DicomObject> iter = results.iterator(); iter.hasNext();) {
				DicomObject element = (DicomObject) iter.next();
//				System.out.println("-----------------------------------------------------------------------------");
//				System.out.println(element.getDataset().getString(Tags.SOPInstanceUID).trim() + "\r\n" +(SOPInstanceUID.trim()));
//				System.out.println("-----------------------------------------------------------------------------");
//				String s = element.getString(Tag.SOPInstanceUID);
				if (element.getString(Tag.ReferencedSOPInstanceUIDInFile) != null && element.getString(Tag.ReferencedSOPInstanceUIDInFile).trim().equals(SOPInstanceUID.trim()))
					return element;
			}
			return null;
	
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public File[] getNextImage(boolean series) {
	    
	    DicomObject seriesKeys = new BasicDicomObject();
		
		ArrayList<File> al = new ArrayList<File>();
		try {
			Vector<DicomObject> results = this.query(LEVEL_SERIES, seriesKeys);
			DicomObject seriesRecord = (DicomObject) results.get(0);
			DicomObject next = dicomDir.findFirstChildRecord(seriesRecord);
			while (next != null) {
			    if (DirectoryRecordType.IMAGE.equals(next.getString(Tag.DirectoryRecordType))) {
					if (series) {
						al.add(getImagePathFromImage(next));
					} else {
						File[] files = new File[1];
						files[0] =getImagePathFromImage(next); 
						return files;
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (Exception e) {
		}		
		return (File[]) al.toArray(new File[1]);
	}
		
	public File getImagePathFromImage(DicomObject imageRecord) throws Exception{
		return checkFileLowerCase(dicomDir.toReferencedFile(imageRecord));
	}
	
	public void deleteRecord(DicomObject dr) {	
		if (! (dicomDir instanceof DicomDirWriter)) {
			log.info("unable to delete Record, DICOMDIR is READ-ONLY");
			return;
		}
		if (DirectoryRecordType.PATIENT.equals(dr.getString(Tag.DirectoryRecordType))) {
			try {
				log.info("deleting PATIENT: "+ dr.getItemOffset());
				DicomObject studies = dicomDir.findFirstChildRecord(dr);
				//	list all series
				while (studies != null) {
					deleteRecord(studies);
					studies = dicomDir.findNextSiblingRecord(studies);
				}
				((DicomDirWriter) dicomDir).deleteRecord(dr);
				log.info("deleted PATIENT");
			} catch (Exception e) {
				log.warn("error deleting PATIENT from DICOMDIR: \""+ STORE_DIR+ "\"");
			} 
		}else if (DirectoryRecordType.STUDY.equals(dr.getString(Tag.DirectoryRecordType))) {
				try {
					log.info("deleting STUDY: "+ dr.getItemOffset());
					DicomObject series = dicomDir.findFirstChildRecord(dr);
					//	list all series
					while (series != null) {
						deleteRecord(series);
						series = dicomDir.findNextSiblingRecord(series);
					}
					((DicomDirWriter) dicomDir).deleteRecord(dr);
					log.info("deleted STUDY");
				} catch (Exception e) {
					log.warn("error deleting STUDY from DICOMDIR: \""+ STORE_DIR+ "\"");
				} 
			}else if (DirectoryRecordType.SERIES.equals(dr.getString(Tag.DirectoryRecordType))) {
					try {
						log.info("deleting SERIES: "+ dr.getItemOffset());
						DicomObject pictures = dicomDir.findFirstChildRecord(dr);
						// list all pictures
						while (pictures != null) {
							deleteRecord(pictures);
							pictures = dicomDir.findNextSiblingRecord(pictures);
						}
						((DicomDirWriter) dicomDir).deleteRecord(dr);
						log.info("deleted SERIES");
						
					} catch (Exception e) {
						log.warn("error deleting SERIES from DICOMDIR: \""+ STORE_DIR+ "\"");
					} 
					
				} else if (DirectoryRecordType.IMAGE.equals(dr.getString(Tag.DirectoryRecordType))) {
					try {
						log.info("deleting IMAGE: "+ dr.getItemOffset());
						((DicomDirWriter) dicomDir).deleteRecord(dr);
						log.info("deleted IMAGE");
						File img = getImagePathFromImage(dr);
						img.delete();
						
						
						
					} catch (Exception e) {
						log.warn("error deleting IMAGE from DICOMDIR: \""+ STORE_DIR+ "\"");
					} 
				}
			try {
			    ((DicomDirWriter) dicomDir).commit();
			} catch (IOException e) {
				log.warn(e.getLocalizedMessage());
			}
	}

	public static void deleteEmptyDirs(File dir) {
		if (!dir.exists()) {
			return;
		}
		if (!dir.isDirectory()) {
			dir = dir.getParentFile();
		}
		if (dir.listFiles() == null || dir.listFiles().length == 0) {
			dir.delete();
			log.info("deleted dir: \"" + dir.getAbsolutePath()+ "\"");
			deleteEmptyDirs(dir.getParentFile());
		} else {
			log.info("not deleting dir: \"" + dir.getAbsolutePath() + "\"" + " (not empty)");
		}
	}
	
//	public void deleteEmptyEntries(File dir) {
//		if (!dir.exists()) {
//			return;
//		}
//		if (!dir.isDirectory()) {
//			dir = dir.getParentFile();
//		}
//		if (dir.listFiles() == null || dir.listFiles().length == 0) {
//			dir.delete();
//			log.info("deleted dir: \"" + dir.getAbsolutePath()+ "\"");
//			deleteEmptyDirs(dir.getParentFile());
//		} else {
//			log.info("not deleting dir: \"" + dir.getAbsolutePath() + "\"" + " (not empty)");
//		}
//	}

	class ImageComparer implements Comparator<DicomObject> {
        public int compare(DicomObject obj1, DicomObject obj2)
        {
            // TODO
//        	String[] fileIDs = ((DicomObject) obj1).get
//        	if (fileIDs[fileIDs.length-1].toLowerCase().startsWith("x")) {
//        		return 1;
//        	}
        	int nr1 =Integer.parseInt(((DicomObject) obj1).getString(Tag.InstanceNumber));
        	int nr2 =Integer.parseInt(((DicomObject) obj2).getString(Tag.InstanceNumber));
        	return nr1 - nr2;
        }
}
	
	class StudyComparer implements Comparator<StudyObject> {
        public int compare(StudyObject obj1, StudyObject obj2)
        {
        	String str1 = ((StudyObject) obj1).getDirRecord().getString(Tag.StudyDate);
        	String str2 = ((StudyObject) obj2).getDirRecord().getString(Tag.StudyDate);
        	int dateCompare = str2.compareTo(str1);
        	if (dateCompare != 0 ) {
        		return dateCompare;
        	}
        	// compare time
        	String str1b = ((StudyObject) obj1).getDirRecord().getString(Tag.StudyTime);
        	String str2b = ((StudyObject) obj2).getDirRecord().getString(Tag.StudyTime);
        	return str2b.compareTo(str1b);
        	
        }
}
	
 
	/**
	 * Description of the Method
	 * 
	 * @param file
	 *            Description of the Parameter
	 * @exception IOException
	 *                Description of the Exception
	 */
	public String append(File f) throws IOException {
		return append(f, false);
	}
    
	/**
	 * Description of the Method
	 * 
	 * @param file
	 *            Description of the Parameter
	 * @exception IOException
	 *                Description of the Exception
	 */
	public String append(File f, boolean moveOnly) throws IOException {

		if (f.getName().toUpperCase().endsWith("DICOMDIR"))
			return "";

		int n = 0;
		if (f.isDirectory()) {
			StringBuffer sb = new StringBuffer();
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; ++i) {
				sb.append(append(files[i], moveOnly));
			}
			return sb.toString();
		} else {

			// read orig file
			DicomInputStream in = new DicomInputStream(f);
			in.setHandler(new StopTagInputHandler(Tag.PixelData));
			DicomObject dcmobj = in.readDicomObject();

			in.close();

			String fileName = createFileName(dcmobj);
			File outFile = new File(STORE_DIR.getParentFile(), fileName);
			outFile.getParentFile().mkdirs();

			// if old file not already has the right file-path,
			if (!f.getAbsolutePath().equals(outFile.getAbsolutePath())) {
				log.info("moving " + f.getAbsolutePath() + " to " + outFile.getAbsolutePath());
				f.renameTo(outFile);
			}

			if (moveOnly) {
				return outFile.getAbsolutePath();
			}
			
			in = new DicomInputStream(outFile);
			in.setHandler(new StopTagInputHandler(Tag.PixelData));
			dcmobj = in.readDicomObject();

			DicomObject patrec = ap.makePatientDirectoryRecord(dcmobj);
			DicomObject styrec = ap.makeStudyDirectoryRecord(dcmobj);
			DicomObject serrec = ap.makeSeriesDirectoryRecord(dcmobj);
			DicomObject instrec = ap.makeInstanceDirectoryRecord(dcmobj, dicomDir.toFileID(outFile));

			DicomObject rec = ((DicomDirWriter) dicomDir).addPatientRecord(patrec);
			if (rec == patrec) {
				++n;
			}
			rec = ((DicomDirWriter) dicomDir).addStudyRecord(rec, styrec);
			if (rec == styrec) {
				++n;
			}
			rec = ((DicomDirWriter) dicomDir).addSeriesRecord(rec, serrec);
			if (rec == serrec) {
				++n;
			}
			if (n == 0 && checkDuplicate) {
				String iuid = dcmobj.getString(Tag.MediaStorageSOPInstanceUID);
				if (dicomDir.findInstanceRecord(rec, iuid) != null) {
					System.out.print('D');
					((DicomDirWriter) dicomDir).commit();
					dicomDir.clearCache();
					return outFile.getAbsolutePath();
				}
			}
			((DicomDirWriter) dicomDir).addChildRecord(rec, instrec);
			System.out.print('.');
			((DicomDirWriter) dicomDir).commit();
			dicomDir.clearCache();
			in.close();
			
			
			return outFile.getAbsolutePath();
		}
	}
	

	public static String createFileName(DicomObject dcmobj) {
		return createFileName(dcmobj, File.separator);
	}
        
    public static String createFileName(DicomObject dcmobj, String separator) {
	StringBuffer fileName = new StringBuffer();
	
	String field = (dcmobj.getString(DICOM_DIR_NAMING[0])+"").replaceAll("\\W", "_");
	fileName.append(field);
	for (int i = 1; i < DICOM_DIR_NAMING.length; i++) {
	    fileName.append(separator);
	    fileName.append((dcmobj.getString(DICOM_DIR_NAMING[i])+"").replaceAll("\\W", "_"));
	}
	// delete NUL bytes
	String filePath =  fileName.toString().replaceAll("\\00", "");
	return filePath;
    }

    public Vector<DicomObject> query(String queryLevel, DicomObject queryKeys ) throws IOException, TransformerConfigurationException {
        this.qrLevel = Arrays.asList(QRLEVEL).indexOf(queryLevel);
    	this.foundRecords.removeAllElements();
    	query("",
                    1,
                    0,
                    dicomDir.findFirstRootRecord(), 
                    queryKeys);
    	return this.foundRecords;
    }
    
    private int query(String prefix, int no, int level, DicomObject dr, DicomObject queryKeys) throws IOException,
            TransformerConfigurationException {
        int count = 1;
        for (; dr != null; dr = dicomDir.findNextMatchingSiblingRecord(dr, queryKeys, true)) {
            if (level >= this.qrLevel) {
                System.out.println("found "+ dr);
                this.foundRecords.add(dr);
                ++no;
            } else {
                no = query(prefix + count + '.', no, level + 1, dicomDir.findFirstChildRecord(dr), queryKeys);
            }
            ++count;
        }
        return no;
    }


	/**
	 * deletes a file or series (if instanceNumber == -1) from the DicomDir
	 * @param seriesInstanceUID
	 * @param instanceNumber
	 */
	public void deleteImageFromSeriesUID(String seriesInstanceUID, int instanceNumber) {
		if (instanceNumber == -1) {
		    DicomObject seriesKeys = new BasicDicomObject();
			seriesKeys.putString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUID);
			Vector<DicomObject> results;
			try {
				results = this.query("SERIES", seriesKeys);
				if (results.size() > 1) {
					System.out.println("SeriesInstanceUID not unique");
				}
				DicomObject seriesRecord = (DicomObject) results.get(0);
				deleteRecord(seriesRecord);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Vector<DicomObject> v = getImageRecordFromSeriesUID(seriesInstanceUID, instanceNumber);
			for (Iterator<DicomObject> iter = v.iterator(); iter.hasNext();) {
				DicomObject dr = (DicomObject) iter.next();
				deleteRecord(dr);
			}
		}
	}
	
	public void deleteRecord(String level, DicomHeader dh) {
		DicomObject rec = getRecord(level, dh);
		deleteRecord(rec);
		
		if (LEVEL_SERIES.equals(level)) { // check if study is empty
			DicomObject studRec = getRecord(LEVEL_STUDY, dh);
			int ref = studRec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity);
			if (ref == 0) {
				deleteRecord(studRec);
				level = LEVEL_STUDY;
			}
		}
		
		if (LEVEL_STUDY.equals(level)) { // check if patient is empty
			DicomObject patRec = getRecord(LEVEL_PATIENT, dh);
			int ref = patRec.getInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity);
			if (ref == 0) {
				deleteRecord(patRec);
			}
		}
		
	}
	
	public DicomObject getRecord(String level, DicomHeader dh) {
		Vector<DicomObject> v = new Vector<DicomObject>();

		DicomObject keys = new BasicDicomObject();
		
		if (LEVEL_PATIENT.equals(level))
			keys.putString(Tag.PatientID, VR.UI, dh.getHeaderStringValue(Tag.PatientID));
		else if (LEVEL_STUDY.equals(level))
			keys.putString(Tag.StudyInstanceUID, VR.UI, dh.getHeaderStringValue(Tag.StudyInstanceUID));
		else if (LEVEL_SERIES.equals(level))
			keys.putString(Tag.SeriesInstanceUID, VR.UI, dh.getHeaderStringValue(Tag.SeriesInstanceUID));
		else if (LEVEL_IMAGE.equals(level))
			keys.putString(Tag.SOPInstanceUID, VR.UI, dh.getHeaderStringValue(Tag.SOPInstanceUID));
		
		try {
			Vector<DicomObject> results = this.query(level, keys);
			if (results.size() > 1) {
				System.out.println("UID not unique");
			}
			if (results.size() == 0) 
				return null;
			DicomObject rec = (DicomObject) results.get(0);
			return rec;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	
	/**
	 * deletes a file from the DicomDir
	 * @param sopInstanceUID
	 */
	public void deleteImage(String sopInstanceUID) throws Exception {
		DicomObject rec = getImageRecordFromSOPInstanceUID(sopInstanceUID);
		deleteRecord(rec);
	}
	
	
	    public FileSetInformation fsinfo() {
	        if (fsinfo == null) {
	            fsinfo = new FileSetInformation();
	            fsinfo.init();
	        }
	        return fsinfo;
	    }
	    
	    
	    private File checkFileLowerCase(File f) {
			if (IS_WINDOWS)
				return f;
			else {
				// there might be problems on linux, as file/foldernames in the dicomdir db 
				// are different (upper/lowercase) from the ones on the filesystem
				// check for upper/lowercase pathnames in DICOMDIR-FILESET
				if (! f.exists()) { 
					// folder where the DICOMDIR file is in 
					String basePath = STORE_DIR.getParentFile().getAbsolutePath();
					// path to image
					String imagePath = f.getAbsolutePath();
					// relative path to image from the dicomdir.
					String relPath = imagePath.substring(basePath.length());
					File newFile = new File(basePath, relPath.toLowerCase());
					if (newFile.exists()) 
						return newFile;
				}
				return f;			
			}
	    }

	    public void close() {
	    	if (dicomDir != null)
				try {
					dicomDir.clearCache();
					dicomDir.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    }
	    
	    
	    public void clearCache() {
	    	dicomDir.clearCache();
	    }
	    
	    
	    public boolean isWriteable() {
	    	return (dicomDir instanceof DicomDirWriter);
	    }
	    
	    /**
		 * Description of the Method
		 * 
		 * @param args
		 *            Description of the Parameter
		 * @exception Exception
		 *                Description of the Exception
		 */
		public static void main(String args[]) throws Exception {
			DicomDirReader ddr = new DicomDirReader();
			ddr.loadDicomDirFile(new File("DICOMSTORE/DICOMDIR"), false, false);

			ddr.checkFileLowerCase(new File("DICOMSTORE/hello/World/WHERE/are/you"));
			
//			System.out.println(ddr.getPatients());
//			System.out.println("fertig");
		}
}
