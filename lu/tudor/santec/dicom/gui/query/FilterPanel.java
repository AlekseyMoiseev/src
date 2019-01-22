package lu.tudor.santec.dicom.gui.query;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.header.DicomHeaderParseException;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.query.DcmQR;
import lu.tudor.santec.dicom.query.DcmQR.QueryRetrieveLevel;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FilterPanel extends JPanel implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	public static final String LINE_DELIM = "\n";
	public static final String FILTER_CHANGED = "FILTER_CHANGED";
	
	private List<HeaderTag> keys;
	private Vector<FilterField> filterFields = new Vector<FilterField>();
	private QueryPanel parentPanel;
	private JPanel filterPanel;
	private JComboBox levelBox;
	private JLabel levelLabel;
	private JButton reloadButton;
//	private JComboBox modalityBox;
//	private JLabel modalityLabel;
	private LinkedHashSet<String> operators = FilterField.OPERATORS;
	
	public FilterPanel(QueryPanel parent, String pacs) {
		setLayout(new BorderLayout());

		CellConstraints cc = new CellConstraints();
		JPanel buttonPanel = new JPanel(new FormLayout(
				"4dlu, pref, 4dlu, pref, 4dlu, pref, 2dlu, 30dlu, 2dlu, right:pref:grow, 4dlu",
				"2dlu, 20dlu, 2dlu"));

		levelLabel = new JLabel(Translatrix.getTranslationString("dicom.QueryPacs", new String[] {pacs}));

		this.levelBox = new JComboBox();
		for (DcmQR.QueryRetrieveLevel level : QuerySearchPanel.getQueryLevels()) {
			this.levelBox.addItem(level);
		}
		this.levelBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setKeys(QuerySearchPanel.getQueryFields((QueryRetrieveLevel) levelBox.getSelectedItem()));
			}
		});

//		modalityLabel = new JLabel(Translatrix.getTranslationString("dicom.Modality"));
//		
//		this.modalityBox = new JComboBox(QuerySearchPanel.MODALITIES);
//		AutoCompletion.enableWithFreeText(modalityBox);

		reloadButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_SEARCH));
		reloadButton.setToolTipText(Translatrix.getTranslationString("dicom.refreshDICOMDIR"));
		reloadButton.addActionListener(this);

		buttonPanel.add(levelLabel, cc.xy(2, 2));
		buttonPanel.add(levelBox, cc.xy(4, 2));
//		buttonPanel.add(modalityLabel, cc.xy(6, 2));
//		buttonPanel.add(modalityBox, cc.xy(8, 2));
		buttonPanel.add(reloadButton, cc.xy(10, 2));
		this.add(buttonPanel, BorderLayout.NORTH);
		
		this.filterPanel = new JPanel(new GridLayout(0, 1));
		this.add(filterPanel, BorderLayout.CENTER);
		this.setBorder(new TitledBorder(Translatrix.getTranslationString("dicom.filter")));
		this.parentPanel = parent;
		
		addFilterPanel();
		
		setKeys(QuerySearchPanel.getQueryFields((QueryRetrieveLevel) levelBox.getSelectedItem()));
		
	}

	public void setKeys(List<HeaderTag> list) {
		this.keys = list;
		filterFields.clear();
		this.filterPanel.removeAll();
		addFilterPanel();
		this.parentPanel.validate();
		this.parentPanel.repaint();
	}

	public void setOperators(LinkedHashSet<String> operators) {
		this.operators =  operators;
	}
	
	private void addFilterPanel() {
		FilterField fp = new FilterField(this, (filterFields.size() == 0));
//		fp.setOperators(operators);
		fp.setKeys(this.keys);
		fp.addPropertyChangeListener(this);
		this.filterPanel.add(fp);
		this.filterFields.add(fp);
	}

	public void addItem(FilterField filterField) {
		addFilterPanel();
		this.parentPanel.validate();
		this.parentPanel.repaint();
	}

	public void removeItem(FilterField filterField) {
		this.filterPanel.remove(filterField);
		this.filterFields.remove(filterField);
		this.parentPanel.validate();
		this.parentPanel.repaint();
	}
	
	public String getFilter() {
		StringBuffer sb = new StringBuffer();
		for (FilterField field : filterFields) {
			sb.append(field.getFilter()).append(LINE_DELIM);
		}
		return sb.toString();
	}
	
	public Vector<HeaderTag> getFilterTags() throws DicomHeaderParseException {
		Vector<HeaderTag> tags = new Vector<HeaderTag>();
		for (FilterField field : filterFields) {
			tags.add(field.getFilterTag());
		}
		return tags;
	}
	
	public void setFilter(String filter) {
		String[] strings = filter.split(LINE_DELIM);
		filterFields.clear();
		this.filterPanel.removeAll();
		for (int i = 0; i < strings.length; i++) {
			addFilterPanel();
			filterFields.get(i).setFilter(strings[i]);
		}
		this.validate();
		
	}
	
	
	public String printFilter() {
		return printFilter(getFilter());
	}
	
	public static String printFilter(String filter) {
		StringBuffer sb = new StringBuffer();
		String[] strings = filter.split(LINE_DELIM);
		for (String filterLine : strings) {
			sb.append(FilterField.printFilter(filterLine)).append(LINE_DELIM);
		}
		return sb.toString();
	}

//	public void loadFilter() {
//		if (this.context != null) {
//			SearchFilterBean selectedSfb = filterSelectionDialog.selectFilter(this.context);
//			if (selectedSfb != null) {
//				this.sfb = selectedSfb;
//				
//				if (this.filterInterface != null) {
//					String columns = this.sfb.getColumns();
//					if (columns != null && columns.trim().length() > 0) {
//						this.filterInterface.setColumnFilter(this.sfb.getColumns());					
//					}					
//				}
//				
//				this.setFilter(this.sfb.getFilter());	
//			}
//		}
//	}

//	public void saveFilter() {
//		String name = (String)JOptionPane.showInputDialog(
//                this,
//                Translatrix.getTranslationString("Iveu.filterName"),
//                Translatrix.getTranslationString("Iveu.saveFilter"),
//                JOptionPane.PLAIN_MESSAGE,
//                null,
//                null,
//                (this.sfb != null ? this.sfb.getName() : "")
//                );
//		
//		if (name == null) {
//			return;
//		}
//		
//		// if we do not have a bean or the new name is different from the old one
//		// store as new bean
//		if (sfb == null|| ! sfb.getName().equals(name)) {
//			this.sfb = new SearchFilterBean();			
//		}
//		
//		String filter = getFilter();
//		this.sfb.setFilter(filter);
//		if (this.context != null) {
//			this.sfb.setContext(context);			
//		}
//		if (name != null) {
//			this.sfb.setName(name);			
//		}
//		
//		if (this.filterInterface != null) {
//			String columns = this.filterInterface.getColumnFilter();
//			if (columns != null && columns.trim().length() > 0) {
//				this.sfb.setColumns(columns);
//			}
//			
//		}
//		
//		this.sfb = Iveu.instance.getDataAccess().mergeSearchFilter(sfb);
//	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.reloadButton)) {
			try {
				Vector<HeaderTag> tags = getFilterTags();
				parentPanel.queryPanel.query(tags, (QueryRetrieveLevel) levelBox.getSelectedItem());				
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} 
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
//		if (FieldEditPanel.VALUE.equals(evt.getPropertyName())) {
//			firePropertyChange(FILTER_CHANGED, "old", "new");
//		}
	}
	
//	public static void main(String[] args) {
//	try {
//		Translatrix.addBundle("lu.tudor.santec.iveu.gui.resources.Translatrix");
//		Translatrix.addBundle("lu.tudor.santec.settings.resources.WidgetResources");
//		Translatrix.addBundle("lu.tudor.santec.dicom.gui.resources.WidgetResources");
//		Translatrix.addBundle(SwingLocalizer.getBundle());
//		Translatrix.setDefaultWhenMissing(true);
//		SwingLocalizer.localizeJFileChooser();
//		SwingLocalizer.localizeJOptionPane();
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	
//	
//	try {
//		LinkedHashMap<String, Class<?>> columns = new LinkedHashMap<String, Class<?>>();
//		columns.put("String", String.class);
//		columns.put("Integer", Integer.class);
//		columns.put("Double", Double.class);
//		columns.put("Date", Date.class);
//		
//		LinkedHashSet<String> operators = new LinkedHashSet<String>();
//		operators.add("=");
//		operators.add("<");
//		operators.add(">");
//		operators.add("!=");
//		
//		JFrame jf = new JFrame("filterpanel test");
//		jf.setLayout(new BorderLayout());
//		
//		
//		FilterPanel fp = new FilterPanel(new JPanel());
//		fp.setOperators(operators);
//		fp.setColumns(columns);
//		jf.add(fp, BorderLayout.CENTER);
//
//		jf.setSize(400,150);
//		jf.setVisible(true);
//		
//		
//		
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	
//	
//}
	
}
