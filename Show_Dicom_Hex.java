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

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.File;

import lu.tudor.santec.dicom.gui.header.DicomHexInfoDialog;

/**
 * ImageJ Plugin that shows dialog with the dicom header of the current image
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Show_Dicom_Hex implements PlugInFilter {

	private ImagePlus ip;

	public void run(ImageProcessor arg0) {
		System.out.println("run: " + "Show_Dicom_Hex");
		DicomHexInfoDialog dhd = new DicomHexInfoDialog();
		File f = new File(ip.getOriginalFileInfo().directory + ip.getOriginalFileInfo().fileName);
		dhd.setInfo(f);
		dhd.setVisible(true);
	}

	public int setup(String arg0, ImagePlus ip) {
		   if (ip == null ) {
			      return DONE;
			}
			this.ip = ip;
			return DOES_ALL;
	}

}
