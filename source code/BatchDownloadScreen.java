import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class BatchDownloadScreen extends JFrame implements ActionListener {
	
	//reference to parent screen
	private final DownloadManagerScreen referenceToScreen;
	
	
	//labels to show user example
	private JLabel label1 = new JLabel("   Url:(Example:http://www.download.com/img*.png/)");
	private JLabel label2 = new JLabel("Replace asterisk to:          ");
	
	
	//label for textFields
	private JLabel from = new JLabel("From:");
	private JLabel to = new JLabel("To:");
	private JLabel wildcard= new JLabel("Wildcard size:");
	
	//textFields
	private JTextField tot = new JTextField(5);
	private JTextField urlt = new JTextField(30);
	private JTextField fromt = new JTextField(5);
	private JTextField wildt = new JTextField(5);

	
	//radioButtons
	private JRadioButton rb1 = new JRadioButton("number");
	private JRadioButton rb2 = new JRadioButton("letter");

	//buttons
	private JButton jb = new JButton("Ok");
	private JButton resetButton=new JButton("Reset");

	//combobox to let user select download option
	private JComboBox DownloadOption;
	//this are the download option
	private final String []names={"All at once","Queue1","Queue2","Queue3","Queue4"}; 
	
	
	
	public BatchDownloadScreen(DownloadManagerScreen ref) {
		super("Batch Download");
		
		//saves parent screen reference
		referenceToScreen=ref;

		/*
		 * this part adds different component to the window
		 */
		
		ButtonGroup bg = new ButtonGroup();
		
		bg.add(rb1);
		bg.add(rb2);

		DownloadOption=new JComboBox<>(names);
		
	
		setLayout(new BorderLayout());
		
		JPanel northPanel=new JPanel(new BorderLayout());
		
		northPanel.add(label1,BorderLayout.NORTH);
		northPanel.add(urlt,BorderLayout.CENTER);
		
		JPanel tmpPanel=new JPanel(new FlowLayout());
		
		tmpPanel.add(label2);
		tmpPanel.add(rb1);
		tmpPanel.add(rb2);
		
		northPanel.add(tmpPanel,BorderLayout.SOUTH);
		
		add(northPanel,BorderLayout.NORTH);
		
		tmpPanel =new JPanel(new FlowLayout());
		
		tmpPanel.add(from);
		tmpPanel.add(fromt);
		
		tmpPanel.add(to);
		tmpPanel.add(tot);
		
		add(tmpPanel,BorderLayout.CENTER);
		
		tmpPanel=new JPanel(new FlowLayout());
		
		tmpPanel.add(wildcard);
		tmpPanel.add(wildt);
		
		
		tmpPanel.add(DownloadOption);
		
		
		tmpPanel.add(resetButton);
		tmpPanel.add(jb);
		
		add(tmpPanel,BorderLayout.SOUTH);
		
		resetButton.addActionListener(this);
		jb.addActionListener(this);
		rb1.addActionListener(this);
		rb2.addActionListener(this);
		tot.addActionListener(this);
		fromt.addActionListener(this);
		wildt.addActionListener(this);
		urlt.addActionListener(this);

		setSize(400, 250);
		setLocation(100, 100);

		/*
		 *	finished adding component 
		 */
	}
	
	//this resets all the textFields
	public void reset(){
		fromt.setText("");
		tot.setText("");
		urlt.setText("");
		wildt.setText("");
		
	}
	
	//these are the actionPerformed functions
	public void actionPerformed(ActionEvent ae) {
		//for "ok" button
		if (ae.getSource() == jb) {
			// JOptionPane.showMessageDialog(null,tf.getText()+"@"+pf.getText())
			String str = urlt.getText();

			//for numbers
			if (rb2.isSelected() == false) {
				int start=0, end=-1, width=0;
				try{
					start = Integer.parseInt(fromt.getText());
					end = Integer.parseInt(tot.getText());
					width = Integer.parseInt(wildt.getText());
				}
				catch(Exception e){
					JOptionPane.showMessageDialog( this ,"Invalid number","Error", JOptionPane.ERROR_MESSAGE);
				}
				
				System.out.println(start + " " + end + "" + width);
				for (int i = start; i <= end; i++) {
					String temp = Integer.toString(i);
					for (int j = 0, k = width - temp.length(); j < k; j++)
						temp = '0' + temp;
					
					referenceToScreen.addDownloadForBatchDownload(new StringBuffer(str).replace(
							str.indexOf('*'), str.indexOf('*') + 1, temp).toString(), DownloadOption.getSelectedIndex());
				}
			} 
			//for letters
			else {
				URL url;
				char start=0, end=(char)-1;
				try{
					start = fromt.getText().charAt(0);
					end = tot.getText().charAt(0);
				}
				catch(Exception e){
					JOptionPane.showMessageDialog( this ,"Invalid number","Error", JOptionPane.ERROR_MESSAGE);
				}
				
				System.out.println(start + " " + end);
				for (char i = start; i <= end; i++) {
					String temp = Character.toString(i);
					try {
						referenceToScreen.addDownloadForBatchDownload(new StringBuffer(str).replace( 
								str.indexOf('*'), str.indexOf('*') + 1, temp).toString(), DownloadOption.getSelectedIndex());

					} catch (Exception e) {
						System.out.println(e);
					}
				}
			}
			
		}
		//if number option is selected
		else if (ae.getSource() == rb1) {
			wildcard.setEnabled(true);
			wildt.setEnabled(true);
		}
		//if letter option is selected
		else if (ae.getSource() == rb2) {
			wildcard.setEnabled(false);
			wildt.setEnabled(false);
		}
		//for reset button
		else if (ae.getSource()==resetButton){
			reset();
		}
	}

}
