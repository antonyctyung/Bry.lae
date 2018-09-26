import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.ArrayList;

import javax.swing.ActionMap;
import javax.swing.JOptionPane;

public class KeyboardLayout implements Comparable<KeyboardLayout>{

	private String name; // name of this keyboard layout
	private String lang; // language of this keyboard layout
	// keys of this keyboard layout relative to 
	// QWERTY keyboard
	private char[] typed; 
	
	static char[] QWERTY_TYPED = 
	       {'`','1','2','3','4','5','6','7','8','9','0','-','=',
			'q','w','e','r','t','y','u','i','o','p','[',']','\\',
			'a','s','d','f','g','h','j','k','l',';','\'',
			'z','x','c','v','b','n','m',',','.','/',
			'~','!','@','#','$','%','^','&','*','(',')','_','+',
			'Q','W','E','R','T','Y','U','I','O','P','{','}','|',
			'A','S','D','F','G','H','J','K','L',':','\"',
			'Z','X','C','V','B','N','M','<','>','?'};
	
	static char[] DVORAK_TYPED = 
			{'`','1','2','3','4','5','6','7','8','9','0','[',']',
			'\'',',','.','p','y','f','g','c','r','l','/','=','\\',
			'a','o','e','u','i','d','h','t','n','s','-',
			';','q','j','k','x','b','m','w','v','z',
			'~','!','@','#','$','%','^','&','*','(',')','{','}',
			'\"','<','>','P','Y','F','G','C','R','L','?','+','|',
			'A','O','E','U','I','D','H','T','N','S','_',
			':','Q','J','K','X','B','M','W','V','Z'};
	
	static char[] JCUKEN_TYPED = 
	       {'\u0451','1','2','3','4','5','6','7','8','9','0','-','=',
			'\u0439','\u0446','\u0447','\u043A','\u0435','\u043D','\u0433','\u0448','\u0449','\u0437','\u0445','\u044A','\\',
			'\u0444','\u044B','\u0432','\u0430','\u043F','\u0440','\u043E','\u043B','\u0434','\u0436','\u044D',
			'\u044F','\u0447','\u0441','\u043C','\u0438','\u0442','\u044C','\u0431','\u044E','.',
			'\u0401','!','\"','\u2116',';','%',':','?','*','(',')','_','+',
			'\u0419','\u0426','\u0427','\u041A','\u0415','\u041D','\u0413','\u0428','\u0429','\u0417','\u0425','\u042A','/',
			'\u0424','\u042B','\u0412','\u0410','\u041F','\u0420','\u041E','\u041B','\u0414','\u0416','\u042D',
			'\u042F','\u0427','\u0421','\u041C','\u0418','\u0422','\u042C','\u0411','\u042E',','};
	
	static public KeyboardLayout QWERTY_KBL = new KeyboardLayout("QWERTY","English",QWERTY_TYPED);
	static public KeyboardLayout DVORAK_KBL = new KeyboardLayout("DVORAK","English",DVORAK_TYPED);
	static public KeyboardLayout JCUKEN_KBL = new KeyboardLayout("JCUKEN","Russian",JCUKEN_TYPED);
	
	KeyboardLayout (String name, String lang, char[] typed)
	{
		if (typed.length == 94)
		{
			this.typed = typed;
			this.name = name;
			this.lang = lang;
		}
		else throw new IllegalArgumentException();
	}
	
	/**
	 * @return an ArrayList of KeyboardLayout consist of 
	 * the three default keyboard layout
	 */
	static public ArrayList<KeyboardLayout> getDefault()
	{
		ArrayList<KeyboardLayout> result = new ArrayList<KeyboardLayout>();
		result.add(QWERTY_KBL);
		result.add(DVORAK_KBL);
		result.add(JCUKEN_KBL);
		return result;
	}
	
	/**
	 * import all of the keyboard layout file exist in the 
	 * root directory of the program
	 * @return a TreeSet of keyboardLayout consist of 
	 * 		   the one processed from files
	 */
	static public TreeSet<KeyboardLayout> importKBL()
	{
		System.out.println("Importing...");
		TreeSet<KeyboardLayout> result = new TreeSet<KeyboardLayout>();
		File currDir = new File((new File("")).getAbsolutePath());
		File[] kbls = currDir.listFiles();
		if (kbls != null)
		{
			for (int i = 0; i < kbls.length; i++)
			{
				if (kbls[i].getName().endsWith("kbl"))
				{ result.add(importKBL(kbls[i]));}
			}
		}
		return result;
	}
	
	/**
	 * @param file to be processed as a keyboard layout file
	 * @return a KeyboardLayout processed from the file
	 */
	static public KeyboardLayout importKBL (File file)
	{
		try (Scanner scn = new Scanner(file,"UTF-8")) 
		{
			// throw IndexOutOfBoundsException if format does not match
			String name;
			String lang;
			String temp;
			if (scn.hasNextLine())
			{
				name = scn.nextLine().trim();
			}
			else throw new IndexOutOfBoundsException();
			if (scn.hasNextLine())
			{
				lang = scn.nextLine().trim();
			}
			else throw new IndexOutOfBoundsException();
			if (scn.hasNextLine())
			{
				temp = scn.nextLine().trim();
				if (!temp.equals("startchar"))
					throw new IndexOutOfBoundsException();
			}
			else throw new IndexOutOfBoundsException();
			char[] typed = new char[94];
			int count = 0;
			while (scn.hasNextLine())
			{ // \n?
				temp = scn.nextLine().trim();
				if (temp.length()==1)
				{
					typed[count]=temp.charAt(0);
					count++;
				}
				if (temp.length()==0)
				{
					typed[count]=0;
					count++;
				}
			}
			if (count == 94)
			{
				System.out.println(name+" ("+lang+") imported");
				return new KeyboardLayout(name, lang, typed);
			}
			throw new IndexOutOfBoundsException();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog
			(null, "File selected does not exist or deleted.", "File Not Found", JOptionPane.ERROR_MESSAGE);
		} catch (IndexOutOfBoundsException e){
			JOptionPane.showMessageDialog
			(null, "File selected is either corrupted or not a keyboard layout file.", "Failed to Read Keyboard Layout", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	/**
	 * Takes in a TreeSet of KeyoardLayout and export the content
	 * to the root directory of the program 
	 * @param kbls a TreeSet of KeyboardLayout to be exported
	 */
	static public void exportKBL(TreeSet<KeyboardLayout> kbls)
	{
		System.out.println("Exporting...");
		KeyboardLayout[] kblArr = kbls.toArray(new KeyboardLayout[0]);
		for (int i = 0; i < kbls.size(); i++)
		{
			exportKBL(kblArr[i]);
		}
	}
	
	/**
	 * @param kbl the KeyboardLayout to be exported
	 * @param file the file the keyboard layout is exporting to
	 */
	static public void exportKBL(KeyboardLayout kbl, File file)
	{
		try (PrintWriter pw = new PrintWriter(file,"UTF-8"))
		{
			pw.println(kbl.name);
			pw.println(kbl.lang);
			pw.println("startchar");
			for (int i = 0; i < (kbl.typed.length-1); i++)
			{
				pw.println(kbl.typed[i]);
			}
			pw.print(kbl.typed[kbl.typed.length-1]);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Export the keyboard layout to the root directory
	 * with file name [kbl.name]_([kbl.lang]).kbl
	 * @param kbl
	 */
	static public void exportKBL(KeyboardLayout kbl)
	{
		System.out.println("Exporting " + kbl.toString());
		File fold = new File(kbl.getName()+"_"+kbl.getLang()+".kbl");
		fold.delete();
		File fnew = new File(kbl.getName()+"_"+kbl.getLang()+".kbl");
		exportKBL(kbl,fnew);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getLang()
	{
		return lang;
	}
	
	public char[] getTyped()
	{
		return typed;
	}
	
	@Override
	public String toString()
	{
		return (name+" ("+lang+")");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Overrided equals method
	 * determine equality by the name and language
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof KeyboardLayout)
		{
			KeyboardLayout other = (KeyboardLayout) o;
			return this.toString().equals(other.toString());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Overrided compareTo method
	 * Compare keyboard layouts by the alphabetical order of their name
	 * if same name, compare by language
	 * if same name and language, they are equal to each other
	 */
	@Override
	public int compareTo(KeyboardLayout other) {
		if (other instanceof KeyboardLayout)
		{
			return this.toString().compareTo(other.toString());
		}
		return 0;
	}
}