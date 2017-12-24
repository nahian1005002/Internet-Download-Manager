import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class QueueScreen extends JFrame implements Observer {

	private final DownloadManagerScreen referenceToScreen;
	
	// the selected download of the menu
	private IndividualDownload selectedDownload = null;
	
	//download Menu
	private JTable centerTable;
		
	//this is the tableModel for the downloadMenu
	public final TableModel tableModel=new TableModel(true);
	
	// this are the buttons
	private final JButton addDownloadButton = new JButton("Add Download");
	private final JButton pauseButton = new JButton("Pause");
	private final JButton resumeButton = new JButton("Resume");
	private final JButton cancelButton = new JButton("Cancel");
	private final JButton clearButton = new JButton("Clear");

	private final JButton queueUp=new JButton(); 
	private final JButton queueDown=new JButton(); 
	
	public QueueScreen(String name,DownloadManagerScreen reference) {
		super(name);
		referenceToScreen=reference;
		

		setSize(700, 400);
		setLocation(100, 100);

		

		/*
		 * this is the start of working with Button this only adds Button
		 * actionListener will be added later
		 */

		// name says it all
		JPanel panelOfButtons = new JPanel(new GridLayout(1, 5));

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

		add(panelOfButtons,BorderLayout.NORTH);
		
		updateButtons();

		/*
		 * this finishes adding buttons
		 */
		
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
		
		/*
		 * finished adding actionListener
		 */
		

		/*
		 * this starts the CenterTable making
		 */
		
		//this creates a table with tableModel as its table Model 
		centerTable=new JTable(tableModel);
		//this enables only one row to be selected at a time
		centerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		//this is the progress bar shown under the progress in the menu
		ProgressRenderer renderer=new ProgressRenderer(0, 100);
		//this enables the string like 60% to be displayed in the progressBar
		renderer.setStringPainted(true);
		
		centerTable.setDefaultRenderer(JProgressBar.class, renderer);
		
		centerTable.setRowHeight((int)renderer.getPreferredSize().getHeight());
		
		//this adds the listSelectionListener
		//this is called every time a new row is selected
		centerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				tableSelectionChanged();
			}
		});
		
		//finally table added 
		add(new JScrollPane(centerTable),BorderLayout.CENTER);
		
		
		//up & down button for swapping in the queue
		queueUp.setIcon(new ImageIcon(getClass().getResource("queueUpIcon.png")));
		queueDown.setIcon(new ImageIcon(getClass().getResource("queueDownIcon.png")));
		
		queueUp.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				tableModel.swapUp(selectedDownload);
				tableSelectionChanged();
			}
		});
		queueDown.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				tableModel.swapDown(selectedDownload);
				tableSelectionChanged();
			}
		});
		
		JPanel southPanel=new JPanel(new FlowLayout());
		
		southPanel.add(queueUp);
		southPanel.add(queueDown);
		
		add(southPanel,BorderLayout.SOUTH);
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
		
		IndividualDownload tmp=new IndividualDownload(verifiedUrl, DownloadManagerScreen.downloadPathLabel.getText() ,
				IndividualDownload.QUEUED );
		
		
		referenceToScreen.addDownloadFromQueue(tmp);
		
		tableModel.addNewDownloadToTable( tmp  );
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
			selectedDownload.cancel();
			tableModel.clearDownload(centerTable.getSelectedRow());
			referenceToScreen.tableModel.clearDownload(selectedDownload);
			selectedDownload=null;
			updateButtons();
		}
		
	}
	

	@Override
	public void update(Observable o, Object arg1) {
		// TODO Auto-generated method stub
		if (selectedDownload!=null && selectedDownload.equals(o))
			 updateButtons();
	}
	
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
			default: // COMPLETE or CANCELLED or QUEUED
				pauseButton.setEnabled(false);
				resumeButton.setEnabled(false);
				cancelButton.setEnabled(false);
				clearButton.setEnabled(true);
			}
			if (tableModel.isBottom(selectedDownload)){
				queueDown.setEnabled(false);
			}else{
				queueDown.setEnabled(true);
			}
			if (tableModel.isTop(selectedDownload)){
				queueUp.setEnabled(false);
			}else{
				queueUp.setEnabled(true);
			}
			
		} else {
			pauseButton.setEnabled(false);
			resumeButton.setEnabled(false);
			cancelButton.setEnabled(false);
			clearButton.setEnabled(false);
			queueDown.setEnabled(false);
			queueUp.setEnabled(false);
		}
	}
	
	//this is called every time a new row is selected in the menu
	private void tableSelectionChanged() {
		if (selectedDownload != null)
			selectedDownload.deleteObserver(this);

		if ( centerTable.getSelectedRow() > -1) {
			selectedDownload = tableModel.getDownload( centerTable.getSelectedRow() );
			selectedDownload.addObserver(this);
			updateButtons();
		}
	}

}
