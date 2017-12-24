import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;


public class TableModel extends AbstractTableModel implements Observer{

	//this saves the column names and classes
	private static final String[] columnNames={"File name","Size","Download Speed","Status","Progress"};
	private static final Class[] columnClass={String.class,String.class,String.class,String.class,JProgressBar.class};
	
	private boolean tableOfQueue;
	
	public TableModel(boolean b){
		tableOfQueue=b;
	}
	
	public TableModel(){
		this(false);
	}
	
	
	
	private ArrayList< IndividualDownload > downloadArrayList=new ArrayList<IndividualDownload>();
	
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return downloadArrayList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		// TODO Auto-generated method stub
		
		IndividualDownload selectedDownload=downloadArrayList.get(row);
		
		switch(col){
		case 0:return selectedDownload.fileName;
		case 1:return (float)selectedDownload.getSize()/(1024*1024) + " MB";
		case 2:return selectedDownload.getCurrentSpeed()+" KB";
		case 3:return IndividualDownload.STATUSES[ selectedDownload.getStatus() ];
		case 4:return selectedDownload.getProgress();
		}
		return "";
		
	}
	@Override
	public String getColumnName(int col){
		return columnNames[col];
	}
	@Override
	public Class getColumnClass(int col){
		return columnClass[col];
	}


	@Override
	public void update(Observable download, Object o) {
		// TODO Auto-generated method stub
		
		int row=downloadArrayList.indexOf(download);
		if (row>=0)
			fireTableRowsUpdated(row,row);
		
		if (tableOfQueue ){
			IndividualDownload tmp=null;
			try{
				tmp=(IndividualDownload)download;
			}
			catch(Exception e){
				;
			}
			if (tmp!=null && ( tmp.getStatus()==IndividualDownload.COMPLETE )||(tmp.getStatus()==IndividualDownload.ERROR) ){
				clearDownload(tmp);
			}
		}
		
	}
	
	//this adds a new download to the table
	public void addNewDownloadToTable(IndividualDownload download){
		
		download.addObserver(this);
		
		if(tableOfQueue && downloadArrayList.size()==0)
			download.resume();
		
		downloadArrayList.add(download);
		
		fireTableRowsInserted(getRowCount()-1 , getRowCount()-1 );
	}
	//return the reference to the selected row download for "selectedDownload" in DownloadManagerScreen
	public IndividualDownload getDownload(int row){
		return (IndividualDownload)downloadArrayList.get(row);
	}
	
	//deletes ad download and releases its reference to get collected by the garbage collector
	public void clearDownload(int row){
		
		if (row<0)return ;
		
		downloadArrayList.remove(row);
		fireTableRowsDeleted(row, row);
		
		if (tableOfQueue && downloadArrayList.size()>0){
			downloadArrayList.get(0).resume();
		}
		
	}
	public void clearDownload(IndividualDownload dummy){
		int row=downloadArrayList.indexOf(dummy);
		System.out.println("1\n");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clearDownload(row);
		
	}
	public void clearAllCompleted(){
		for (int i=0;i<downloadArrayList.size();++i)
			if (downloadArrayList.get(i).getStatus()==IndividualDownload.COMPLETE){
				clearDownload(i);
				--i;
			}
					
	}
	
	//this is for swapping in a queue
	public void swapUp(IndividualDownload seleted){
		if (seleted!=null){
			int ind=downloadArrayList.indexOf(seleted);
			if (ind>1){
				Collections.swap(downloadArrayList, ind, ind-1);
				update(downloadArrayList.get(ind), this);
				update(downloadArrayList.get(ind-1), this);
			}
		}
		
	}
	public void swapDown(IndividualDownload seleted){
		if (seleted!=null){
			int ind=downloadArrayList.indexOf(seleted);
			if (ind>0 && ind<downloadArrayList.size()-1){
				Collections.swap(downloadArrayList, ind, ind+1);
				update(downloadArrayList.get(ind), this);
				update(downloadArrayList.get(ind+1), this);
			}
		}
		
	}
	
	public boolean isTop(IndividualDownload download){
		if (downloadArrayList.indexOf(download)<=1){
			return true;
		}
		return false;
	}
	public boolean isBottom(IndividualDownload download){
		if (downloadArrayList.indexOf(download) == downloadArrayList.size()-1 || downloadArrayList.indexOf(download)==0 ){
			return true;
		}
		return false;
	}
	
}
