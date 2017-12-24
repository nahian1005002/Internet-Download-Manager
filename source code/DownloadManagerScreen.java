import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class DownloadManagerScreen extends JFrame implements Observer{
	
	//refernce of this screen is saved because it needs to be passed to other classes
	private DownloadManagerScreen referneceToTheScreen=this;
	
	//this are the looks
	private final UIManager.LookAndFeelInfo []installedLooks=UIManager.getInstalledLookAndFeels();
	
	//this are the buttons
	private final JButton addDownloadButton=new JButton("Add Download");
	private final JButton pauseButton=new JButton("Pause");
	private final JButton resumeButton=new JButton("Resume");
	private final JButton cancelButton=new JButton("Cancel");
	private final JButton clearButton=new JButton("Clear");
	private final JButton clearCompletedButton=new JButton("Clear Completed");
	
	//this are the buttons for showing queues
	private final JButton queue1Button=new JButton("Queue1");
	private final JButton queue2Button=new JButton("Queue2");
	private final JButton queue3Button=new JButton("Queue3");
	private final JButton queue4Button=new JButton("Queue4");
	
	//this is the screen for batch download
	private final BatchDownloadScreen batchDownloadScreen=new BatchDownloadScreen(referneceToTheScreen);
	
	
	
	//this are the download queues
	private final QueueScreen  queue1=new QueueScreen("Queue1",referneceToTheScreen);
	private final QueueScreen  queue2=new QueueScreen("Queue2",referneceToTheScreen);
	private final QueueScreen  queue3=new QueueScreen("Queue3",referneceToTheScreen);
	private final QueueScreen  queue4=new QueueScreen("Queue4",referneceToTheScreen);
	
	//this is the the label that saves the destination path
	public static JLabel downloadPathLabel=new JLabel("E:"+File.separator);
	
	//download Menu
	private JTable centerTable;
	
	//this is the tableModel for the downloadMenu
	public TableModel tableModel=new TableModel();
	
	//the selected download of the menu
	private IndividualDownload selectedDownload=null;
	
	
	public DownloadManagerScreen(){
		super("Internet Download Manager");
		
		
		
		setSize(900,500);
		setLocation(100,100);
		
		/*
		 * start of work for menuBar
		 */
		
		
		//this is the top menu bar
		JMenuBar topMenuBar=new JMenuBar();
		
		//this is for top menu for changing look
		JMenu looksMenu=new JMenu("Looks");
		looksMenu.setToolTipText("Changes the look of your Download Manager");
		
		
		//this is the array of menuItem for looks
		JMenuItem []installedLooksMenuitem=new JMenuItem[installedLooks.length];
		for (int i=0;i<installedLooks.length;++i){
			//this is required for line UIManager.setLookAndFeel( installedLooks[k].getClassName() );
			//as this requires a final int
			final int k=i;
			
			installedLooksMenuitem[i]=new JMenuItem ( installedLooks[i].getName() );
			
			//this is the action that takes place when the button is pressed
			installedLooksMenuitem[i].addActionListener(new ActionListener() {
				
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					try {
						UIManager.setLookAndFeel( installedLooks[k].getClassName() );
						SwingUtilities.updateComponentTreeUI( referneceToTheScreen );
						SwingUtilities.updateComponentTreeUI( queue1 );
						SwingUtilities.updateComponentTreeUI( queue2 );
						SwingUtilities.updateComponentTreeUI( queue3 );
						SwingUtilities.updateComponentTreeUI( queue4 );
						SwingUtilities.updateComponentTreeUI( batchDownloadScreen );
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			});
			looksMenu.add(installedLooksMenuitem[i]);
			
		}
		
		
		//this is the MenuItem for "features"
		JMenu features=new JMenu("Features");
		
		JMenuItem addDownloadScheduler=new JMenuItem("Add Download with Scheduler");
		
		addDownloadScheduler.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForAddDownloadScheduler();
			}
		});
		
		features.add(addDownloadScheduler);
		
		
		//for batch download MenuItem
		JMenuItem batchDownloadMenuItem=new JMenuItem("Batch Download");
		
		batchDownloadMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//this resets the batchDownloadScreen every time it appears
				batchDownloadScreen.reset();
				batchDownloadScreen.setVisible(true);
				
			}
		});
		features.add(batchDownloadMenuItem);
		
		//MenuItem for speedLimiter
		JMenuItem speedLimiterMenuItem=new JMenuItem("Speed Limiter");
		speedLimiterMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (selectedDownload!=null){
					String speedString=null;
					int speed;
					speedString=JOptionPane.showInputDialog(null, "Enter download limit speed in KB");
					try{
						speed=Integer.parseInt(speedString);
					}
					catch(Exception e){
						JOptionPane.showMessageDialog(null, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
						return ;
					}
					if (speed<5){
						JOptionPane.showMessageDialog(null, "cannot set limit to less than 8", "Error", JOptionPane.ERROR_MESSAGE);
						return ;
					}
					//after checking this sets the speed
					selectedDownload.setSpeed(speed);
					
				}
				else{
					JOptionPane.showMessageDialog(null, "No download selected", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		
		features.add(speedLimiterMenuItem);
		
		
		//MenuItem for Help
		JMenu helpMenu=new JMenu("Help");
		
		JMenuItem helpMenuItem=new JMenuItem("Help");
		
		helpMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try{
					
					File helpFile=new File( "Help.chm" );
					Desktop.getDesktop().open(helpFile);
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(null, "Help file couldnot be found", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		helpMenu.add(helpMenuItem);
		
		
		topMenuBar.add(looksMenu);
		topMenuBar.add(features);
		topMenuBar.add(helpMenu);
		
		setJMenuBar( topMenuBar );
		
		/*
		 * this finishes the work required for MenuBar
		 */
		
		
		//this is the panel that adds the element to the north of the main frame
		JPanel northPanel=new JPanel(new BorderLayout());
		
		
		/*
		 * this is the start of working with Button
		 * this only adds Button
		 * actionListener will be added later
		 */
		
		
		//name says it all
		JPanel panelOfButtons=new JPanel(new GridLayout(1,6));
		
		
		addDownloadButton.setIcon(new ImageIcon(getClass().getResource("downloadNormalButton.png")));
		addDownloadButton.setRolloverIcon(new ImageIcon(getClass().getResource("downloadRollOverButton.png")));
		
		addDownloadButton.setToolTipText("Click to enter download link");
		
		panelOfButtons.add(addDownloadButton);
		
		pauseButton.setIcon(new ImageIcon(getClass().getResource("pauseNormalButton.png")));
		pauseButton.setRolloverIcon(new ImageIcon(getClass().getResource("pauseRollOverButton.png")));
		
		pauseButton.setToolTipText("Click to pause download");
		
		panelOfButtons.add(pauseButton);
		
		resumeButton.setIcon(new ImageIcon(getClass().getResource("resumeNormalButton.png")));
		resumeButton.setRolloverIcon(new ImageIcon(getClass().getResource("resumeRollOverButton.png")));
		
		resumeButton.setToolTipText("Click to resume download");
		
		panelOfButtons.add(resumeButton);
		
		cancelButton.setIcon(new ImageIcon(getClass().getResource("cancelNormalButton.png")));
		cancelButton.setRolloverIcon(new ImageIcon(getClass().getResource("cancelRollOverButton.png")));
		
		cancelButton.setToolTipText("Click to cancel download");
		
		panelOfButtons.add(cancelButton);
		
		clearButton.setIcon(new ImageIcon(getClass().getResource("clearNormalButton.png")));
		clearButton.setRolloverIcon(new ImageIcon(getClass().getResource("clearRollOverButton.png")));
		
		cancelButton.setToolTipText("Click to clear download");
		
		panelOfButtons.add(clearButton);
		
		clearCompletedButton.setIcon(new ImageIcon(getClass().getResource("clearCompletedNormalButton.png")));
		clearCompletedButton.setRolloverIcon(new ImageIcon(getClass().getResource("clearCompletedRollOverButton.png")));
		
		clearCompletedButton.setToolTipText("Click to clear all completed download");
		
		panelOfButtons.add(clearCompletedButton);
		
		northPanel.add( panelOfButtons,BorderLayout.NORTH);
		
		updateButtons();
		
		/*
		 * this finishes adding buttons
		 */
		
		/*
		 * destinationPanel shows the destination of file and 
		 * have selectDirectoryButton
		 */
		JPanel destinationPanel=new JPanel(new BorderLayout());
		JLabel thisJustShowsDestination=new JLabel("Destination:   ");
		
		thisJustShowsDestination.setFont(new Font("Serif", Font.PLAIN, 16));
		
		downloadPathLabel.setFont(new Font("Serif", Font.PLAIN, 16));
		
		//button to select directory of the save file
		JButton selectDirectoryButton=new JButton("Select Destination");
		selectDirectoryButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				updateDirectory();
				
			}
		});
		
		
		destinationPanel.add(thisJustShowsDestination,BorderLayout.WEST);
		destinationPanel.add(downloadPathLabel,BorderLayout.CENTER);
		destinationPanel.add(selectDirectoryButton,BorderLayout.EAST);
		
		northPanel.add(destinationPanel,BorderLayout.SOUTH);
		
		add(northPanel,BorderLayout.NORTH);
		/*
		 * setting up north panel is finished
		 */
		
		
		/*
		 *start of southPanel 
		 */
		JPanel southPanel=new JPanel(new GridLayout(1,4,2,2));
		
		
		
		queue1Button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForQueue1();
			}
		});
		queue2Button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForQueue2();
			}
		});
		queue3Button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForQueue3();
			}
		});
		queue4Button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForQueue4();
			}
		});
		
		
		southPanel.add(queue1Button);
		southPanel.add(queue2Button);
		southPanel.add(queue3Button);
		southPanel.add(queue4Button);
		add(southPanel,BorderLayout.SOUTH);
		
		
		/*
		 * this starts the CenterTable making
		 */
		
		//this creates a table with tableModel as its table Model 
		centerTable=new JTable(tableModel);
		//this enables only one row to be selected at a time
		centerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		//this is the progress bar shown under the progress in the menu
		ProgressRenderer renderer=new ProgressRenderer(0, 100);
		//this enables the string like 60% to be displayed within the progressBar
		renderer.setStringPainted(true);
		
		centerTable.setDefaultRenderer(JProgressBar.class, renderer);
		
		centerTable.setRowHeight((int)renderer.getPreferredSize().getHeight());
		
		
		//this is called every time a new row is selected
		centerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				tableSelectionChanged();
			}
		});
		
		//finally able to add table 
		add(new JScrollPane(centerTable),BorderLayout.CENTER);
		
		
		
		/*
		 * this is all the actionListener of Buttons
		 */
		
		//adding actionListener for addDownloadButton
		addDownloadButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForAddDownloadButton();
			}
		});
		
		//adding actionListener for pauseButton
		pauseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForPauseButton();
			}
		});
		
		//adding actionListener for pauseButton
		resumeButton.addActionListener(new ActionListener() {
					
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForResumeButton();
			}
		});
		
		//adding actionListener for cancelButton
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForCancelButton();
			}
		});
		
		//adding actionListener for cancelButton
		clearButton.addActionListener(new ActionListener() {
					
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForClearButton();
			}
		});
		
		clearCompletedButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				actionListenerForClearCompletedButton();
			}
		});
		
		
	}
	
	//this is called every time a new row is selected in the menu
	private void tableSelectionChanged() {
		if (selectedDownload != null)
			selectedDownload.deleteObserver(DownloadManagerScreen.this);

		if ( centerTable.getSelectedRow() > -1) {
			selectedDownload = tableModel.getDownload( centerTable.getSelectedRow() );
			selectedDownload.addObserver(DownloadManagerScreen.this);
			updateButtons();
		}
	}
	
	//update the buttons for the selected download
	private void updateButtons() {
		if (selectedDownload != null) {
			int status = selectedDownload.getStatus();
			switch (status) {
			case IndividualDownload.DOWNLOADING:
				pauseButton.setEnabled(true);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case IndividualDownload.PAUSED:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				cancelButton.setEnabled(true);
				clearButton.setEnabled(false);
				break;
			case IndividualDownload.ERROR:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(true);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
				break;
			case IndividualDownload.SCHEDULED:
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(false);
				break;
			default: // COMPLETE or CANCELLED or QUEUED
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
			}
		} else {
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
		}
	}
	
	// this is the actionListener function for selectDirectoryButton as 
	// tempFileChoooser.showOpenDialog(this)
	// is not allowed in a actionListener

	private void updateDirectory(){
		
		JFileChooser tempFileChoooser=new JFileChooser(downloadPathLabel.getText());
		
		tempFileChoooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		tempFileChoooser.setApproveButtonText("OK");
		tempFileChoooser.setDialogTitle("Select Destination");
		
		//this is only true when the user presses ok
		if (tempFileChoooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
			downloadPathLabel.setText(tempFileChoooser.getSelectedFile().getPath());
		}
		
	}
	
	/*
	 * this are the actionListener functions
	 */
	private void actionListenerForAddDownloadButton(){
		String enteredUrl=null;
		URL verifiedUrl;
		if ( (enteredUrl=JOptionPane.showInputDialog(this, "Enter url link"))==null ){
			return ;
		}
		try{
			verifiedUrl=new URL(enteredUrl);
			//this ensures that a file is actually specified in the url
			if (verifiedUrl.getFile().length()<2)
				throw new Exception();
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
			return ;
		}
		tableModel.addNewDownloadToTable( new IndividualDownload(verifiedUrl, downloadPathLabel.getText())  );
	}
	
	private void actionListenerForPauseButton() {
		selectedDownload.pause();
		updateButtons();
	}
	private void actionListenerForResumeButton() {
		selectedDownload.resume();
		updateButtons();
	}
	private void actionListenerForCancelButton() {
		selectedDownload.cancel();
		updateButtons();
	}
	private void actionListenerForClearButton(){
		if (selectedDownload!=null){
			actionListenerForCancelButton();
			tableModel.clearDownload(centerTable.getSelectedRow());
			selectedDownload=null;
			updateButtons();
		}
	}
	private void  actionListenerForClearCompletedButton() {
		tableModel.clearAllCompleted();
	}
	
	private void actionListenerForAddDownloadScheduler(){
		
		Date date=null;
		String dateString=null;
		String timeString=null;
		//this sets the time format
	    DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
	    
	    //asks for date
	    dateString=JOptionPane.showInputDialog(this,"Enter date (dd-mm-yyyy)", "Date", JOptionPane.QUESTION_MESSAGE);
	    if (dateString==null || dateString.length()==0)return ;
	    
	    //asks for time
	    timeString=JOptionPane.showInputDialog(this,"Enter time (HH-mm-ss)", "Date", JOptionPane.QUESTION_MESSAGE);
	    if (timeString==null || timeString.length()==0) return ;
	    
	    timeString=timeString.replace('-', ':');
	    System.out.println(dateString+" "+timeString);
	    
	    try{
	    	//this sets if the user inputs invalid input throws an exception
	    	dateFormatter.setLenient(false);
	    	
	    	date = dateFormatter.parse (dateString+" "+timeString);
	    }
	    catch(Exception e){
	    	JOptionPane.showMessageDialog(this, "wrong time entered", "Error", JOptionPane.ERROR_MESSAGE);
	    	return ;
	    }
	    if (date!=null)
	    	System.out.println(date);
	    
	   
	    
	    
	    String enteredUrl=null;
		URL verifiedUrl;
		if ( (enteredUrl=JOptionPane.showInputDialog(this, "Enter url link"))==null ){
			return ;
		}
		try{
			verifiedUrl=new URL(enteredUrl);
			//this ensures that a file is actually specified in the url
			if (verifiedUrl.getFile().length()<2)
				throw new Exception();
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(this, "Invalid URL", "Error", JOptionPane.ERROR_MESSAGE);
			return ;
		}
	    
	    //Used this as I want to execute it only once
		Timer timer = new Timer();
		timer.schedule( new timerClassForSchedulling(verifiedUrl,downloadPathLabel.getText(),date), date);
		
	}
	
	private class timerClassForSchedulling extends TimerTask{
		
		
		private URL verifiedUrl;
		private String path;
		IndividualDownload dummy;
		
		//this constructor work immediately .so this adds a dummy download to the table
		public timerClassForSchedulling(URL url,String path,Date d){
			verifiedUrl=url;
			this.path=path;
			dummy=new IndividualDownload(url,d);
			tableModel.addNewDownloadToTable( dummy );
		}
		@Override
		public void run(){
			//when the desired time comes this starts to execute
			
			//remove dummy download
			tableModel.clearDownload(dummy);
			dummy=null;
			//add the actual download
			tableModel.addNewDownloadToTable( new IndividualDownload(verifiedUrl, this.path)  );
		}
		
	}
	
	private void actionListenerForQueue1(){
		queue1.setVisible(true);
	}
	private void actionListenerForQueue2(){
		queue2.setVisible(true);
	}
	private void actionListenerForQueue3(){
		queue3.setVisible(true);
	}
	private void actionListenerForQueue4(){
		queue4.setVisible(true);
	}
	
	public void addDownloadFromQueue(IndividualDownload reference){
		tableModel.addNewDownloadToTable( reference  );
	}
	
	
	//this is for BatchDownloadScreen
	public void addDownloadForBatchDownload(String enteredUrl,int choice){
		
		URL verifiedUrl;
		try{
			verifiedUrl=new URL( enteredUrl );
			//this ensures that a file is actually specified in the url
			if (verifiedUrl.getFile().length()<2)
				throw new Exception();
		}
		catch(Exception e){
			return ;
		}
		
		if (choice==0){
			tableModel.addNewDownloadToTable( new IndividualDownload(verifiedUrl, downloadPathLabel.getText())  );
		}
		else if (choice==1){
			IndividualDownload ref=new IndividualDownload(verifiedUrl, downloadPathLabel.getText(),IndividualDownload.QUEUED );
			queue1.tableModel.addNewDownloadToTable(ref );
			addDownloadFromQueue(ref);
		}
		else if (choice==2){
			IndividualDownload ref=new IndividualDownload(verifiedUrl, downloadPathLabel.getText(),IndividualDownload.QUEUED );
			queue2.tableModel.addNewDownloadToTable(ref );
			addDownloadFromQueue(ref);
		}
		else if (choice==3){
			IndividualDownload ref=new IndividualDownload(verifiedUrl, downloadPathLabel.getText(),IndividualDownload.QUEUED );
			queue3.tableModel.addNewDownloadToTable(ref );
			addDownloadFromQueue(ref);
		}
		else if (choice==4){
			IndividualDownload ref=new IndividualDownload(verifiedUrl, downloadPathLabel.getText(),IndividualDownload.QUEUED );
			queue4.tableModel.addNewDownloadToTable(ref );
			addDownloadFromQueue(ref);
		}
	}
	
	
	//it updates the GUI of the main screen
	@Override
	public void update(Observable o, Object arg1) {
		// TODO Auto-generated method stub
		
		if (selectedDownload!=null && selectedDownload.equals(o))
			updateButtons();
		
	}
}
