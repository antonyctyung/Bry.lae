import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;

public class TextEditor {
	private JFrame frame;
	private JFrame findReplaceFrame;
	private JFrame onScreenKB;
	private JFrame createKBLFrame;
	private JPanel normalKB;
	private JPanel shiftKB;
	private JMenuBar menuBar;
	private JMenu menuKBL;
	private JTextArea textArea;
	private JTextField findTF;
	private JTextField replaceTF;
	private ActionListener fnLsn;
	private JButton[] btns;
	private String initContent;
	private ActionMap defaultAM;
	private File fileEditing;
	private TreeSet<KeyboardLayout> kbls;
	private KeyboardLayout currKBL;
	private JMenu subKBLMenu;
	// ROW_ELEMENTS indicate row i starts and ends with which button
	// i.e. row i starts with typed[ROW_ELEMENTS[i-1]] and ends with
	// typed[ROW_ELEMENTS[i]-1]
	// for example, row 1 on QWERTY 
	// starts with '`' (KeyboardLayout.QWERTY_KBL.typed[0])
	// ends with '=' (KeyboardLayout.QWERTY_KBL.typed[12])
	// row 1-4 is on normal Keyboard accessible when shift is not pressed
	// row 5-8 is on shift keyboard accessible when shift is pressed
	private final int[] ROW_ELEMENTS = { 0, 13, 26, 37, 47, 60, 73, 84, 94 };

	static public void main(String[] args) {
		new TextEditor();
	}

	TextEditor() {
		implementFunctionality();
		initialize();
		setIM();
		setAM();
		constructMainFrame();
		constructFileMenu();
		constructEditMenu();
		constructFindReplaceFrame();
		constructKeyboard();
		constructCreateKBLFrame();
		setKeyboardLayout(KeyboardLayout.QWERTY_KBL);
		constructKBLMenu();
		constructKBLSelection();
		visualize();
	}

	/**
	 * assign new instance to all member variable
	 */
	private void initialize() {
		frame = new JFrame("Bry.lae");
		findReplaceFrame = new JFrame("Find/Replace");
		createKBLFrame = new JFrame("Create new Keyboard Layout");
		onScreenKB = new JFrame();
		normalKB = new JPanel();
		shiftKB = new JPanel();
		menuKBL = new JMenu();
		menuBar = new JMenuBar();
		textArea = new JTextArea();
		findTF = new JTextField();
		replaceTF = new JTextField();
		kbls = new TreeSet<KeyboardLayout>();
		initContent = "";
		textArea.setText("");
		defaultAM = textArea.getActionMap();
		fileEditing = null;
		btns = new JButton[94];
		for (int i = 0; i < btns.length; i++) {
			btns[i] = new JButton();
			btns[i].addActionListener(fnLsn);
		}
		subKBLMenu = new JMenu("Select");
	}
	
	/**
	 * Replace default InputMap of textArea and keyboards
	 */
	private void setIM() {
		InputMap im = new InputMap();
		im.setParent(textArea.getInputMap());
		for (int i = 0; i < KeyboardLayout.QWERTY_TYPED.length; i++) {
			im.put(KeyStroke.getKeyStroke(KeyboardLayout.QWERTY_TYPED[i]),
					KeyStroke.getKeyStroke(KeyboardLayout.QWERTY_TYPED[i])
							.toString());
		}
		textArea.setInputMap(JComponent.WHEN_FOCUSED, im);
		normalKB.setInputMap(JComponent.WHEN_FOCUSED, im);
		shiftKB.setInputMap(JComponent.WHEN_FOCUSED, im);
	}
	
	/**
	 * construct the main frame that consist of textArea and menuBar
	 */
	private void constructMainFrame() {
		JPanel container = new JPanel();
		JScrollPane scrollPane = new JScrollPane(textArea);
		frame.add(container);
		container.setLayout(new BorderLayout());
		container.add(menuBar, BorderLayout.PAGE_START);
		container.add(scrollPane, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (!discardUnsave())
					return;
				System.exit(0);
			}
		});
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * construct the file menu
	 */
	private void constructFileMenu() {
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenuItem item = new JMenuItem("New");
		item.setMnemonic(KeyEvent.VK_N);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.CTRL_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);
		item = new JMenuItem("Open...");
		item.setMnemonic(KeyEvent.VK_O);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Save");
		item.setMnemonic(KeyEvent.VK_S);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);
		item = new JMenuItem("Save As...");
		item.setMnemonic(KeyEvent.VK_A);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Exit");
		item.setMnemonic(KeyEvent.VK_E);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				ActionEvent.ALT_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);
	}

	/**
	 * construct the edit menu
	 */
	private void constructEditMenu() {
		JMenu menu = new JMenu("Edit");
		menuBar.add(menu);
		JMenuItem item = new JMenuItem(new DefaultEditorKit.CutAction());
		item.setText("Cut");
		item.setMnemonic(KeyEvent.VK_X);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		menu.add(item);
		item = new JMenuItem(new DefaultEditorKit.CopyAction());
		item.setText("Copy");
		item.setMnemonic(KeyEvent.VK_C);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		menu.add(item);
		item = new JMenuItem(new DefaultEditorKit.PasteAction());
		item.setText("Paste");
		item.setMnemonic(KeyEvent.VK_P);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				ActionEvent.CTRL_MASK));
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Find...");
		item.setMnemonic(KeyEvent.VK_F);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				ActionEvent.CTRL_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);
		item = new JMenuItem("Replace...");
		item.setMnemonic(KeyEvent.VK_R);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				ActionEvent.CTRL_MASK));
		item.addActionListener(fnLsn);
		menu.add(item);

	}
	
	/**
	 * construct the keyboard layout menu
	 */
	private void constructKBLMenu() {
		menuKBL = new JMenu("KBLs");
		menuBar.add(menuKBL);
		JMenuItem item = new JMenuItem("New...");
		item.addActionListener(fnLsn);
		menuKBL.add(item);
		menuKBL.addSeparator();
		item = new JMenuItem("Import");
		item.addActionListener(fnLsn);
		menuKBL.add(item);
		item = new JMenuItem("Import from...");
		item.addActionListener(fnLsn);
		menuKBL.add(item);
		item = new JMenuItem("Export");
		item.addActionListener(fnLsn);
		menuKBL.add(item);
		item = new JMenuItem("Export selected to...");
		item.addActionListener(fnLsn);
		menuKBL.add(item);
		menuKBL.addSeparator();
		menuKBL.add(subKBLMenu);
		menuKBL.addSeparator();
		item = new JMenuItem("Show/Hide Keyboard");
		item.setMnemonic(KeyEvent.VK_K);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				ActionEvent.CTRL_MASK));
		item.addActionListener(fnLsn);
		menuKBL.add(item);
	}
	
	/**
	 * construct the menu for keyboard layout selection
	 */
	private void constructKBLSelection() {
		subKBLMenu.removeAll();
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem rbItem = new JRadioButtonMenuItem(
				KeyboardLayout.QWERTY_KBL.toString());
		rbItem.putClientProperty("KBL", KeyboardLayout.QWERTY_KBL);
		rbItem.addActionListener(fnLsn);
		if (currKBL.equals(KeyboardLayout.QWERTY_KBL))
			rbItem.setSelected(true);
		group.add(rbItem);
		subKBLMenu.add(rbItem);
		rbItem = new JRadioButtonMenuItem(KeyboardLayout.DVORAK_KBL.toString());
		rbItem.putClientProperty("KBL", KeyboardLayout.DVORAK_KBL);
		rbItem.addActionListener(fnLsn);
		if (currKBL.equals(KeyboardLayout.DVORAK_KBL))
			rbItem.setSelected(true);
		group.add(rbItem);
		subKBLMenu.add(rbItem);
		rbItem = new JRadioButtonMenuItem(KeyboardLayout.JCUKEN_KBL.toString());
		rbItem.putClientProperty("KBL", KeyboardLayout.JCUKEN_KBL);
		rbItem.addActionListener(fnLsn);
		if (currKBL.equals(KeyboardLayout.JCUKEN_KBL))
			rbItem.setSelected(true);
		group.add(rbItem);
		subKBLMenu.add(rbItem);
		subKBLMenu.addSeparator();
		KeyboardLayout[] kblArr = kbls.toArray(new KeyboardLayout[0]);
		for (int i = 0; i < kblArr.length; i++) {
			rbItem = new JRadioButtonMenuItem(kblArr[i].toString());
			rbItem.putClientProperty("KBL", kblArr[i]);
			rbItem.addActionListener(fnLsn);
			if (currKBL.equals(kblArr[i]))
				rbItem.setSelected(true);
			group.add(rbItem);
			subKBLMenu.add(rbItem);
		}
		menuKBL.revalidate();
		menuKBL.repaint();
	}
	
	/**
	 * construct on screen keyboard
	 */
	private void constructKeyboard() {
		JPanel panel;
		normalKB.setLayout(new GridLayout(4, 1));
		for (int i = 1; i < 5; i++) {
			panel = new JPanel();
			for (int j = ROW_ELEMENTS[i - 1]; j < ROW_ELEMENTS[i]; j++) {
				panel.add(btns[j]);
			}
			normalKB.add(panel);
		}
		shiftKB.setLayout(new GridLayout(4, 0));
		for (int i = 5; i < 9; i++) {
			panel = new JPanel();
			for (int j = ROW_ELEMENTS[i - 1]; j < ROW_ELEMENTS[i]; j++) {
				panel.add(btns[j]);
			}
			shiftKB.add(panel);
		}
		onScreenKB.add(normalKB);
		onScreenKB.pack();
		onScreenKB.setVisible(true);
		// Switching between "shifted" and "normal" keyboard
		KeyListener klsn = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					onScreenKB.getContentPane().removeAll();
					onScreenKB.add(shiftKB);
					onScreenKB.revalidate();
					onScreenKB.pack();
					onScreenKB.repaint();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					onScreenKB.getContentPane().removeAll();
					onScreenKB.add(normalKB);
					onScreenKB.revalidate();
					onScreenKB.pack();
					onScreenKB.repaint();
				}
			}
		};
		textArea.addKeyListener(klsn);
		normalKB.addKeyListener(klsn);
		shiftKB.addKeyListener(klsn);
		onScreenKB.setAlwaysOnTop(true);
		onScreenKB.setResizable(false);
		onScreenKB.setLocationRelativeTo(frame);
		onScreenKB.setLocation(onScreenKB.getX(),
				onScreenKB.getY() + frame.getHeight());
	}

	/**
	 * construct the find & replace frame
	 */
	private void constructFindReplaceFrame() {
		JButton btn;
		JPanel container = new JPanel();
		container.setLayout(new GridLayout(3, 1));
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(findTF, BorderLayout.CENTER);
		btn = new JButton("Find");
		btn.addActionListener(fnLsn);
		panel.add(btn, BorderLayout.LINE_END);
		container.add(panel);
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(replaceTF, BorderLayout.CENTER);
		btn = new JButton("Replace");
		btn.addActionListener(fnLsn);
		panel.add(btn, BorderLayout.LINE_END);
		container.add(panel);
		panel = new JPanel();
		btn = new JButton("Replace & Find");
		btn.addActionListener(fnLsn);
		panel.add(btn);
		btn = new JButton("Replace All");
		btn.addActionListener(fnLsn);
		panel.add(btn);
		container.add(panel);
		findReplaceFrame.add(container);
		findReplaceFrame.setResizable(false);
		findReplaceFrame.setAlwaysOnTop(true);
	}
	
	/**
	 * construct create new keyboard layout frame
	 */
	private void constructCreateKBLFrame() {
		JTextField[] newTyped = new JTextField[94];
		JPanel container = new JPanel();
		JPanel panel;
		JTextField nameTF = new JTextField();
		nameTF.setColumns(20);
		JTextField langTF = new JTextField();
		langTF.setColumns(20);
		container.setLayout(new GridLayout(12, 1));
		createKBLFrame.add(container);
		JPanel nameLangPanel = new JPanel();
		nameLangPanel.setLayout(new GridLayout(2, 1));
		JPanel panel1 = new JPanel();
		JPanel panel2 = new JPanel();
		panel1.add(new JLabel("Layout Name: "));
		panel2.add(new JLabel("Language: "));
		panel1.add(nameTF);
		panel2.add(langTF);
		nameLangPanel.add(panel1);
		nameLangPanel.add(panel2);
		container.add(nameLangPanel);
		container.add(new JLabel("Normal Keyboard: when SHIFT is not pressed"));
		for (int i = 1; i < 5; i++) {
			panel = new JPanel();
			panel.setLayout(new GridLayout(2, ROW_ELEMENTS[i]
					- ROW_ELEMENTS[i - 1]));
			for (int j = ROW_ELEMENTS[i - 1]; j < ROW_ELEMENTS[i]; j++) {
				panel.add(new JLabel("" + KeyboardLayout.QWERTY_TYPED[j]));
			}
			for (int j = ROW_ELEMENTS[i - 1]; j < ROW_ELEMENTS[i]; j++) {
				newTyped[j] = new JTextField();
				newTyped[j].setColumns(1);
				newTyped[j].setText("" + KeyboardLayout.QWERTY_TYPED[j]);
				panel.add(newTyped[j]);
			}
			container.add(panel);
		}
		container.add(new JLabel("Shift Keyboard: when SHIFT is pressed"));
		for (int i = 5; i < 9; i++) {
			panel = new JPanel();
			panel.setLayout(new GridLayout(2, ROW_ELEMENTS[i]
					- ROW_ELEMENTS[i - 1]));
			for (int j = ROW_ELEMENTS[i - 1]; j < ROW_ELEMENTS[i]; j++) {
				panel.add(new JLabel("" + KeyboardLayout.QWERTY_TYPED[j]));
			}
			for (int j = ROW_ELEMENTS[i - 1]; j < ROW_ELEMENTS[i]; j++) {
				newTyped[j] = new JTextField();
				newTyped[j].setColumns(1);
				newTyped[j].setText("" + KeyboardLayout.QWERTY_TYPED[j]);
				panel.add(newTyped[j]);
			}
			container.add(panel);
		}
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		// By using the client property map of buttons,
		// the references of JTextFields were "passed" to
		// ActionListener fnLsn. fnLsn can then process
		// user's input to generate a KeyboardLayout and
		// add it to Set kbls or export to a file
		JButton addBtn = new JButton("Add");
		addBtn.putClientProperty("kblNameTF", nameTF);
		addBtn.putClientProperty("kblLangTF", langTF);
		addBtn.putClientProperty("kblTypedTFArr", newTyped);
		addBtn.addActionListener(fnLsn);
		JButton saveBtn = new JButton("Save To...");
		saveBtn.putClientProperty("kblNameTF", nameTF);
		saveBtn.putClientProperty("kblLangTF", langTF);
		saveBtn.putClientProperty("kblTypedTFArr", newTyped);
		saveBtn.addActionListener(fnLsn);
		bottomPanel.add(addBtn);
		bottomPanel.add(saveBtn);
		container.add(bottomPanel);
		createKBLFrame.setResizable(false);
	}
	
	/**
	 * Implement the functionality of most JMenuItem and JButton
	 */
	private void implementFunctionality() {
		fnLsn = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e.getSource().toString());

				if (e.getSource() instanceof JMenuItem) {
					if (e.getSource() instanceof JRadioButtonMenuItem) {
						JRadioButtonMenuItem item = (JRadioButtonMenuItem) e
								.getSource();
						KeyboardLayout kbl = (KeyboardLayout) item
								.getClientProperty("KBL");
						setKeyboardLayout(kbl);
						item.setSelected(true);
						return;
					}
					JMenuItem item = (JMenuItem) e.getSource();
					switch (item.getText()) {
					case "New":
						reset();
						break;
					case "Open...":
						openFile();
						break;
					case "Save":
						saveFile(fileEditing);
						break;
					case "Save As...":
						saveFile();
						break;
					case "Find...":
						findReplaceFrame.revalidate();
						findReplaceFrame.pack();
						findReplaceFrame.repaint();
						findReplaceFrame.setVisible(true);
						findTF.setText(textArea.getSelectedText());
						findTF.requestFocusInWindow();
						break;
					case "Replace...":
						findReplaceFrame.revalidate();
						findReplaceFrame.pack();
						findReplaceFrame.repaint();
						findReplaceFrame.setVisible(true);
						findTF.setText(textArea.getSelectedText());
						replaceTF.requestFocusInWindow();
						break;
					case "New...":
						constructCreateKBLFrame();
						createKBLFrame.revalidate();
						createKBLFrame.pack();
						createKBLFrame.repaint();
						createKBLFrame.setVisible(true);
						break;
					case "Import":
						kbls.addAll(KeyboardLayout.importKBL());
						constructKBLSelection();
						break;
					case "Import from...":
						importKBL();
						break;
					case "Export":
						KeyboardLayout.exportKBL(kbls);
						break;
					case "Export selected to...":
						exportKBL(currKBL);
						break;
					case "Show/Hide Keyboard":
						toggleKB();
						break;
					case "Exit":
						System.exit(0);
						break;
					}
				} else if (e.getSource() instanceof JButton) {
					JButton btn = (JButton) e.getSource();
					switch (btn.getText()) {
					case "Find":
						find(findTF.getText());
						break;
					case "Replace":
						textArea.replaceSelection(replaceTF.getText());
						break;
					case "Replace & Find":
						textArea.replaceSelection(replaceTF.getText());
						find(findTF.getText());
						break;
					case "Replace All":
						replaceAll(findTF.getText(), replaceTF.getText());
						break;
					case "Save To...":
						String newName = ((JTextField) btn
								.getClientProperty("kblNameTF")).getText();
						String newLang = ((JTextField) btn
								.getClientProperty("kblLangTF")).getText();
						char[] newTyped = arrJTextFieldToArrChar((JTextField[]) btn
								.getClientProperty("kblTypedTFArr"));
						exportKBL(new KeyboardLayout(newName, newLang, newTyped));
						createKBLFrame.dispose();
						break;
					case "Add":
						String newName2 = ((JTextField) btn
								.getClientProperty("kblNameTF")).getText();
						String newLang2 = ((JTextField) btn
								.getClientProperty("kblLangTF")).getText();
						char[] newTyped2 = arrJTextFieldToArrChar((JTextField[]) btn
								.getClientProperty("kblTypedTFArr"));
						kbls.add(new KeyboardLayout(newName2, newLang2,
								newTyped2));
						constructKBLSelection();
						createKBLFrame.dispose();
						break;
					default:
						textArea.insert(btn.getText(),
								textArea.getCaretPosition());
						break;
					}
				}
			}
		};
	}

	/**
	 * Make main frame visible
	 */
	private void visualize() {
		frame.setMinimumSize(new Dimension(750, 550));
		frame.pack();
		frame.setVisible(true);
	};

	/**
	 * Open file for view or edit
	 */
	private void openFile() {
		if (discardUnsave()) {
			JFileChooser fc = new JFileChooser();
			FileFilter textFilter = new FileNameExtensionFilter("Text file",
					"txt");
			fc.setFileFilter(textFilter);
			int returnVal = fc.showOpenDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// if user did selected a file
				File file = fc.getSelectedFile();
				String fileContent = "";
				try (Scanner in = new Scanner(file)) {
					textArea.setText("");
					initContent = "";
					if (in.hasNextLine()) {
						fileContent += in.nextLine();
						while (in.hasNextLine()) {
							fileContent += ('\n' + in.nextLine());
						}
					}
					initContent = fileContent;
					textArea.setText(fileContent);
					fileEditing = file;
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(null,
							"File selected does not exist or deleted.",
							"File Not Found", JOptionPane.ERROR_MESSAGE);

				}
			}
		}
	}
	
	/**
	 * save the current content of textArea to a file specified
	 * @param file 
	 * the file to be written on
	 */
	private void saveFile(File file) {
		if (file == null) {
			saveFile();
			return;
		}
		try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
			pw.print(textArea.getText());
			pw.close();
			initContent = textArea.getText();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,
					"File selected does not exist or deleted.",
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Prompt for a file selection and save content on 
	 * textArea to the file user selected
	 */
	private void saveFile() {
		JFileChooser fc = new JFileChooser();
		FileFilter textFilter = new FileNameExtensionFilter("Text file", "txt");
		fc.setFileFilter(textFilter);
		fc.setSelectedFile(new File("Untitled.txt"));
		int returnVal = fc.showSaveDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if user did selected a file
			File file = fc.getSelectedFile();
			if (fc.getFileFilter() == textFilter) {
				if (!file.exists() && !file.getName().endsWith(".txt")) {
					file = new File(file.getPath() + ".txt");
				}
			}
			saveFile(file);
		}

	}

	/**
	 * Prompt for a file selection and add the selected
	 * keyboard layout (.kbl) file
	 */
	private void importKBL() {
		JFileChooser fc = new JFileChooser();
		FileFilter textFilter = new FileNameExtensionFilter(
				"Keyboard layout file", "kbl");
		fc.setFileFilter(textFilter);
		int returnVal = fc.showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if user did selected a file
			File file = fc.getSelectedFile();
			kbls.add(KeyboardLayout.importKBL(file));
		}
	}

	/**
	 * Prompt for a file selection and export the selected
	 * keyboard layout to a file
	 */
	private void exportKBL(KeyboardLayout kbl) {
		JFileChooser fc = new JFileChooser();
		FileFilter textFilter = new FileNameExtensionFilter(
				"Keyboard layout file", "kbl");
		fc.setFileFilter(textFilter);
		fc.setSelectedFile(new File(kbl.getName() + "_" + kbl.getLang()
				+ ".kbl"));
		int returnVal = fc.showSaveDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// if user did selected a file
			File file = fc.getSelectedFile();
			if (fc.getFileFilter() == textFilter) {
				if (!file.exists() && !file.getName().endsWith(".kbl")) {
					file = new File(file.getPath() + ".kbl");
				}
			}
			KeyboardLayout.exportKBL(kbl, file);
		}
	}

	/**
	 * Select phrase provided
	 * 
	 * @param phrase
	 */
	private void find(String phrase) {
		if (phrase == "")
			return;
		if (textArea.getText().indexOf(phrase) == -1) { // Phrase not found in
														// the whole document
			JOptionPane.showMessageDialog(null, phrase
					+ " was not found in the document.", "Text Not Found",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (textArea.getSelectedText() != null
				&& textArea.getSelectedText().equals(phrase)) { 
			// Phrase are currently selected
			// i.e. Are previously searched
			if (textArea.getText().indexOf(phrase, textArea.getCaretPosition()) != -1) {
				// If phrase found after current caret position
				textArea.requestFocus();
				textArea.setCaretPosition(textArea.getText().indexOf(phrase,
						textArea.getCaretPosition()));
				textArea.moveCaretPosition(textArea.getCaretPosition()
						+ phrase.length());
				return;
			}
		}
		// not searched
		System.out.println(textArea.getText().indexOf(phrase));
		textArea.requestFocus();
		textArea.setCaretPosition(textArea.getText().indexOf(phrase));
		textArea.moveCaretPosition(textArea.getCaretPosition()
				+ phrase.length());
	}
	
	/**
	 * replace all [finding] matching string in textArea to [replacing]
	 * @param finding
	 * @param replacing
	 */
	private void replaceAll(String finding, String replacing) {
		if (finding == "")
			return;
		textArea.setText(textArea.getText().replaceAll(finding, replacing));
	}

	/**
	 * Clear textArea
	 */
	private void reset() {
		if (discardUnsave()) {
			initContent = "";
			textArea.setText("");
		}
	}

	/**
	 * Prompt for a confirmation to discard, if any, unsaved changes 
	 * @return true if no unsaved changes or user chose to discard
	 * 		   false if user chose not to discard unsaved changes
	 */
	private boolean discardUnsave() {
		if (!textArea.getText().equals(initContent)) {
			int userInput = JOptionPane.showConfirmDialog(frame,
					"Do you want to discard unsaved changes?",
					"Unsaved changes", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (userInput != JOptionPane.OK_OPTION) {
				// User chose not to discard unsaved changes
				return false;
			}
		}
		// No unsaved changes or user chose to discard
		return true;
	}

	/**
	 * replace textArea's ActionMap 
	 */
	private void setAM() {
		ActionMap result = new ActionMap();
		result.setParent(defaultAM);
		char[] qtyped = KeyboardLayout.QWERTY_TYPED;
		for (int i = 0; i < qtyped.length; i++) {
			final int I = i;
			result.put(KeyStroke.getKeyStroke(qtyped[i]).toString(),
					new AbstractAction() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							btns[I].doClick();
							textArea.requestFocusInWindow();
						}
					});
		}
		textArea.setActionMap(result);
	}
	
	/**
	 * determine if the on screen keyboard is shown
	 * if true, dispose it
	 * if not, show it
	 */
	private void toggleKB() {
		if (onScreenKB.isShowing()) {
			System.out.println(onScreenKB.isShowing());
			onScreenKB.dispose();
			textArea.requestFocusInWindow();
		} else {
			System.out.println(onScreenKB.isShowing());
			onScreenKB.pack();
			onScreenKB.setVisible(true);
			textArea.requestFocusInWindow();
		}
	}

	/**
	 * Replace current keyboard layout in use to [kbl]
	 * @param kbl KeyboardLayout to replace the one in use
	 */
	private void setKeyboardLayout(KeyboardLayout kbl) {
		for (int i = 0; i < btns.length; i++) {
			btns[i].setText("" + kbl.getTyped()[i]);
		}
		onScreenKB.pack();
		currKBL = kbl;
	}

	/**
	 * This method take in an array of JTextField
	 * get the first character of text of each text field
	 * and return them in an array of character with same order
	 * @param tf text field to be process
	 * @return a char[] consist of the first letter of text
	 * of each text field in tf
	 */
	private char[] arrJTextFieldToArrChar(JTextField[] tf) {
		char[] result = new char[tf.length];
		for (int i = 0; i < tf.length; i++) {
			if ((tf[i] != null) && (tf[i].getText().length() > 0))
				result[i] = tf[i].getText().charAt(0);
			else
				result[i] = ' ';
		}
		return result;
	}
}