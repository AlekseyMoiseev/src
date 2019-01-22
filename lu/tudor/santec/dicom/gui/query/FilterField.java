package lu.tudor.santec.dicom.gui.query;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderParseException;
import lu.tudor.santec.dicom.gui.header.HeaderTag;

import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterField extends JPanel implements ItemListener, ActionListener, PropertyChangeListener {

	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(FilterField.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public static final LinkedHashSet<String> OPERATORS = new LinkedHashSet<String>(); 
	static {
		OPERATORS.add("=");
//		OPERATORS.add("<");
//		OPERATORS.add(">");
	}

	public static final String DEFAULT = "DEFAULT";
	public static final String DELIM = "#";

//	private List<String> columns;
	private JComboBox columnChooser;
//	private JComboBox operator1;
	private JLabel operator1;
	private JTextField value1;
//	private JComboBox operator2;
//	private JTextField value2;
//	private boolean listenersActive = true;

	private JLabel groupOperator;

	private JButton addButton;

	private JButton deleteButton;

	private FilterPanel panel;

	private static FormLayout layout;
	private static CellConstraints cc = new CellConstraints();

	public FilterField(FilterPanel panel, boolean isFirst) {
		this.panel = panel;
		if (layout == null) {
			layout = new FormLayout(
					"25dlu, 2dlu, pref, 2dlu, pref, 2dlu, fill:pref:grow, 2dlu, pref, 2dlu", 
					"fill:pref");			
		}
		
		this.setLayout(layout);
		

		try {
			if (! isFirst) {
//				this.groupOperator = new JComboBox(new String[] {"AND" } );
				this.groupOperator = new JLabel("AND");
				this.add(this.groupOperator, cc.xy(1,1));
			} else {
				this.add(new JLabel("WHERE"), cc.xy(1,1));
			}
			this.columnChooser = new JComboBox();
			this.columnChooser.addItemListener(this);
			this.add(columnChooser, cc.xy(3,1));
//			this.operator1 = new JComboBox();
//			this.operator1.addActionListener(this);
//			this.operator1.setEnabled(false);
			this.operator1 = new JLabel("=");
			this.add(operator1, cc.xy(5,1));
			this.value1 = new JTextField();
			this.value1.addPropertyChangeListener(this);

			this.add(value1, cc.xy(7,1));
			
//			this.add(new JLabel("AND"), cc.xy(9,1));
//			this.operator2 = new JComboBox();
//			this.operator2.addActionListener(this);
//			this.add(operator2, cc.xy(11,1));
//			this.value2 = new JTextField();
//			this.value2.addPropertyChangeListener(this);
//			this.add(value2, cc.xy(13,1));
			
			if (isFirst) {
				this.addButton = new JButton(DicomIcons.getIcon16(DicomIcons.FILTER_ADD));
				this.addButton.addActionListener(this);
				this.add(addButton, cc.xy(9,1));
			} else {
				this.deleteButton = new JButton(DicomIcons.getIcon16(DicomIcons.FILTER_REMOVE));
				this.deleteButton.addActionListener(this);
				this.add(deleteButton, cc.xy(9,1));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setKeys(List<HeaderTag> keys) {
		this.columnChooser.removeAllItems();
		if (keys != null) {
			for (HeaderTag col : keys) {			
				this.columnChooser.addItem(col);
			}			
		}
	}

//	public void setOperators(LinkedHashSet<String> operators2) {
//		this.operatorHash.put(DEFAULT, operators);
//		this.operator1.removeAllItems();
//		this.operator2.removeAllItems();
//		for (String op : operators2) {			
//			this.operator1.addItem(op);
//			this.operator2.addItem(op);
//		}
//	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(this.columnChooser)) {
//			listenersActive = false;
			HeaderTag column = (HeaderTag) columnChooser.getSelectedItem();
				try {
	
					this.remove(this.value1);
//					this.remove(this.value2);

					this.value1 = new JTextField();
					this.value1.addPropertyChangeListener(this);
					this.add(value1, cc.xy(7, 1));
//					this.value2 = new JTextField();	
//					this.value2.addPropertyChangeListener(this);
//					this.add(value2, cc.xy(13, 1));
					
					this.validate();
					this.repaint();
				} catch (Exception e2) {
					logger.error("Error creating fields for " + column);
				}
			}
//			listenersActive = true;
		}
//	}
	
	public String getFilter() {
		StringBuffer sb = new StringBuffer();
		if (groupOperator != null) 
			sb.append(groupOperator.getText());
		else
			sb.append("WHERE");
		sb.append(DELIM);	
		sb.append(columnChooser.getSelectedItem());
		sb.append(DELIM);	
//		sb.append(operator1.getSelectedItem());
		sb.append(operator1.getText());
		sb.append(DELIM);	
		try {
			sb.append(value1.getText());			
		} catch (Exception e) {
			e.printStackTrace();
		}
//		sb.append(DELIM);	
//		sb.append(operator2.getSelectedItem());
//		sb.append(DELIM);	
//		try {
//			sb.append(value2.getText());			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return sb.toString();
	}
	
	public HeaderTag getFilterTag() throws DicomHeaderParseException {
		return new HeaderTag(((HeaderTag)columnChooser.getSelectedItem()).getTagInt(), value1.getText()+ "*");
	}
	
	
	public void setFilter(String filter) {
		String[] strings = filter.split(DELIM);
		if (strings.length == 6) {
			if (groupOperator != null) {
				groupOperator.setText(strings[0]);
			}
			columnChooser.setSelectedItem(strings[1]);
//			operator1.setSelectedItem(strings[2]);
			try {
				value1.setText(strings[3]);
			} catch (Exception e) {
				e.printStackTrace();
			}
//			operator2.setSelectedItem(strings[4]);
//			try {
//				value2.setText(strings[5]);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
	
	public String printFilter() {
		return printFilter(getFilter());
	}
	
	public static String printFilter(String filter) {
		StringBuffer sb = new StringBuffer();
		String[] strings = filter.split(DELIM);
		if (strings.length == 6) {
			if (strings[0] != null) 
				sb.append(strings[0]);
			else
				sb.append("WHERE");
			sb.append(" (");
			if (strings[3] != null && strings[3].length() > 0 && !"null".equals(strings[3])) {
				sb.append(strings[1]).append(strings[2]).append(strings[3]);
			}
			if (strings[5] != null && strings[5].length() > 0 && !"null".equals(strings[5])) {
				sb.append(" AND ");
				sb.append(strings[1]).append(strings[4]).append(strings[5]);				
			}
			sb.append(")");
		}
		
		return sb.toString();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.addButton) && this.panel != null) {
			panel.addItem(this);
		} else if (e.getSource().equals(this.deleteButton) && this.panel != null) {
			panel.removeItem(this);
//		} else if (e.getSource().equals(this.operator1) || e.getSource().equals(this.operator2) ) {
//			if (listenersActive) {
//				firePropertyChange("VALUE", 0, 1);
//			}
		}
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		if (FieldEditPanel.VALUE.equals(evt.getPropertyName())) {
//			firePropertyChange(FieldEditPanel.VALUE, 0, 1);			
//		}
	}


}
