package lu.tudor.santec.dicom.gui.utils;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import lu.tudor.santec.dicom.gui.viewer.OSDText;

public class RoiCoder {

	public final static int TAG_ROIS = 0x00131111;
	public final static int TAG_TEXTS = 0x00131112;
	
	public static byte[] encodeRois(Vector<Roi> rois) throws IOException {
		if (rois == null | rois.size() == 0)
			return null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(bout);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
		RoiEncoder re = new RoiEncoder(out);
		String name = System.currentTimeMillis()+ "_";
		int i = 1;
		for (Roi roi : rois) {
			String label = roi.getName();
			if (label == null) {
				label = name + i++;
				roi.setName(label);
			}
			if (!label.endsWith(".roi")) label += ".roi";
			zos.putNextEntry(new ZipEntry(label));
			re.write(roi);
			out.flush();
		}
		out.close();
		return bout.toByteArray();
	}
	
	
	public static Vector<Roi> decodeRois(byte[] b) throws Exception {
		ByteArrayInputStream bin = new ByteArrayInputStream(b);
		ZipInputStream in = new ZipInputStream(bin); 
		ByteArrayOutputStream out;
		byte[] buf = new byte[1024]; 
		int len; 
		Vector<Roi> rois = new Vector<Roi>();
		ZipEntry entry = in.getNextEntry(); 
		while (entry!=null) { 
			String name = entry.getName(); 
			if (name.endsWith(".roi")) { 
				out = new ByteArrayOutputStream(); 
				while ((len = in.read(buf)) > 0) 
					out.write(buf, 0, len); 
				out.close(); 
				byte[] bytes = out.toByteArray(); 
				RoiDecoder rd = new RoiDecoder(bytes, name); 
				Roi roi = rd.getRoi(); 
				if (roi!=null) { 
					rois.add(roi);
				} 
			} 
			entry = in.getNextEntry(); 
		};
		in.close();
		return rois;
	}
	
	
	public static byte[] encodeTexts(Vector<OSDText> osdTexts) throws IOException {
		if (osdTexts == null | osdTexts.size() == 0)
			return null;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
//		for (OSDText txt : osdTexts) {
			out.writeObject(osdTexts);
			out.flush();
//		}
		out.close();
		return bout.toByteArray();
	}
	
	public static Vector<OSDText> decodeTexts(byte[] b) throws Exception {
		ByteArrayInputStream bin = new ByteArrayInputStream(b);
		ObjectInputStream oin = new ObjectInputStream(bin);
		@SuppressWarnings("unchecked")
		Vector<OSDText> texts = (Vector<OSDText>) oin.readObject(); 
		bin.close();
		return texts;
	}
	
}
