package lu.tudor.santec.dicom.gui.query;

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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import lu.tudor.santec.dicom.gui.query.QueryStudyTableModel.Comparer;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class QuerySeriesTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columns = {
			Translatrix.getTranslationString("dicom.Nr"),
			Translatrix.getTranslationString("dicom.Modality"), 
			Translatrix.getTranslationString("dicom.SeriesDesc"), 
			Translatrix.getTranslationString("dicom.SeriesUID"),
			Translatrix.getTranslationString("dicom.Images")
		};
	
	private Vector series = new Vector();
	
	public int getRowCount() {
		return series.size();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
	    DicomObject dr = ((DicomObject) series.get(rowIndex));
		switch (columnIndex) {
		case 0:
			return dr.getString(Tag.SeriesNumber);
		case 1:
			return dr.getString(Tag.Modality);
		case 2:
			return dr.getString(Tag.SeriesDescription);
		case 3:
			return dr.getString(Tag.SeriesInstanceUID);
		case 4:
			return dr.getString(Tag.NumberOfSeriesRelatedInstances);
		default:
			return null;
		}
	}
	
	public void setSeries(Vector series) {
		this.series = series;
		Collections.sort(this.series, new Comparer());
		this.fireTableDataChanged();
	}

	public DicomObject getDimse(int line) {
		try {
			return (DicomObject) series.get(line);	
		} catch (Exception e) {
			return null;
		}
		
	}
	
	
	class Comparer implements Comparator {
        public int compare(Object obj1, Object obj2)
        {
			try {
			    DicomObject ds1 = ((DicomObject) obj1);
			    DicomObject ds2 = ((DicomObject) obj2);
	        	return ds1.getInt(Tag.SeriesNumber) - ds2.getInt(Tag.SeriesNumber);
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
        }
	}
}
