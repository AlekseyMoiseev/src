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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;

import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.dicomdir.DICOMDIRVIEW;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.receiver.DicomEvent;
import tudor_mod.org.dcm4che.util.DcmURL;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/** 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 * 
 */
public class QueryPanel extends JPanel implements ActionListener, DICOMDIRVIEW , ComponentListener {

	private static final long serialVersionUID = 1L;

	private CardLayout cardLayout;

	private JPanel contentPanel;

	public QuerySearchPanel queryPanel;

	public DicomFileDialog parent;

	private DcmURL dicomUrl;

	private static final String DICOMDIR_VIEW = "dicomdir";

	/**
	 * @param file
	 *            default file
	 * @param parent
	 *            the Parent DicomFileDialog
	 * @param send
	 *            show send option
	 */
	public QueryPanel(DcmURL dicomUrl,DicomFileDialog parent, boolean openOnReceive) {

		this.parent = parent;
		this.dicomUrl = dicomUrl;
		
		this.addComponentListener(this);
		
		cardLayout = new CardLayout();
		this.setLayout(cardLayout);

		CellConstraints cc = new CellConstraints();
		FormLayout fl = new FormLayout("6dlu, 200dlu:grow, 4dlu",
				"6dlu, pref, 4dlu, fill:100dlu:grow");
		contentPanel = new JPanel(fl);

//		JPanel buttonPanel = new JPanel(new FormLayout(
//				"4dlu, pref, 4dlu, pref, 4dlu, 180dlu:grow, 4dlu, pref, 2dlu, 30dlu, 2dlu, pref, 4dlu",
//				"2dlu, 20dlu, 2dlu"));
//		buttonPanel
//				.setBorder(new LineBorder(new JTabbedPane().getBackground()));
//
//		pNameFieldLabel = new JLabel(Translatrix
//				.getTranslationString("dicom.QueryFilterName"));
//		
//		this.searchTypeBox = new JComboBox(FILTERTAGS);
//		this.searchTypeBox.setRenderer(new HeaderTagComboboxRenderer());
//		
//		searchField = new JTextField();
//		searchField.addActionListener(this);
//		
//		this.modalityBox = new JComboBox(QuerySearchPanel.MODALITIES);
//		this.modalityBox.addActionListener(this);
//		AutoCompletion.enableWithFreeText(modalityBox);
//		
//		reloadButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_SEARCH));
//		reloadButton.setToolTipText(Translatrix.getTranslationString("dicom.refreshDICOMDIR"));
//		reloadButton.addActionListener(this);
//
//		
//		
//		buttonPanel.add(pNameFieldLabel, cc.xy(2, 2));
//		buttonPanel.add(searchTypeBox, cc.xy(4, 2));
//		buttonPanel.add(searchField, cc.xyw(6, 2, 3));
//		buttonPanel.add(modalityBox, cc.xy(10, 2));
//		buttonPanel.add(reloadButton, cc.xy(12, 2));
//
//		contentPanel.add(buttonPanel, cc.xy(2, 2));
		
		FilterPanel fp = new FilterPanel(this, dicomUrl.toString());
		contentPanel.add(fp, cc.xy(2, 2));

		this.queryPanel = new QuerySearchPanel(this, this.dicomUrl, openOnReceive);
		
		contentPanel.add(queryPanel, cc.xyw(1, 4, 3));

		this.add(contentPanel, DICOMDIR_VIEW);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
//		if (e.getSource().equals(this.reloadButton) || e.getSource().equals(this.searchField)) {
//			this.queryPanel.query(
//					(Integer) searchTypeBox.getSelectedItem(),
//					this.searchField.getText() + "*",
//					(String) this.modalityBox.getSelectedItem()
//			);
//		} 
	}

	public DicomFileDialog getParentDialog() {
		return this.parent;
	}

	public void setPath(File f) {
	}

	public boolean isOpenOnReceive() {
		return queryPanel.isOpenOnReceive();
	}
	
	public void setDicomSenders(DcmURL[] senders) {
		this.queryPanel.setDicomSenders(senders);
	}
	
	public void setLocalDest(DcmURL localDest) {
		this.queryPanel.setLocalDest(localDest);
	}
	
	public void setSearchString(String searchString){
//		this.searchField.setText(searchString);
	}

	public void dicomdirChanged(DicomEvent d_Event) {
	}

	public void componentHidden(ComponentEvent e) {
	}
	public void componentMoved(ComponentEvent e) {
	}
	public void componentResized(ComponentEvent e) {
	}
	public void componentShown(ComponentEvent e) {
//		this.queryPanel.reload("*" + this.pNameField.getText() + "*");
	}
	
	class HeaderTagComboboxRenderer extends DefaultListCellRenderer {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public Component getListCellRendererComponent(JList list,
		    Object value, int index, boolean isSelected,
		    boolean cellHasFocus) {
		try {
		    value = DicomHeader.toTagString((Integer)value);
		    value = DicomHeader.getHeaderName((String)value);
		} catch (Exception e) {
		}
		
		return super.getListCellRendererComponent(list, value, index, isSelected,
			cellHasFocus);
	    }
	    
	}
	
	
}
