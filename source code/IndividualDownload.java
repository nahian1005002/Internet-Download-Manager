import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;


public class IndividualDownload extends Observable implements Runnable,Observer{
	
	//the file to split into how many subfile
	private final int fileSplitNumber=4;
	
	public final String fileName;
	
	//these are the possible statuses of a download
	public static final String STATUSES[]={"Downloading","Paused","Complete","Cancelled","Error","Scheduled","Queued"};
	
	//these variable represents the status
	public static final int DOWNLOADING=0;
	public static final int PAUSED=1;
	public static final int COMPLETE=2;
	public static final int CANCELLED=3;
	public static final int ERROR=4;
	public static final int SCHEDULED=5;
	public static final int QUEUED =6;
	
	//this merges the file and deletes the subFiles when completed
	private MergeFile merger=new MergeFile();
	
	
	private int totalSize= -1;
	//private int downloaded= 0;
	//this is the status of this downloading
	private int downloadstatus=DOWNLOADING;
	
	private URL url;
	private String downloadPath;
	
	//private Thread t;
	private Thread t;
	

	
	
	//array of subFiles
	private IndividualPartDownload partArray[]=null;
	
	
	//returns current speed for tableModel
	float getCurrentSpeed() {
		float sum=0;
		for (int i=0;partArray != null && i<partArray.length;++i)
			if (partArray[i]!=null)
				sum+=partArray[i].getCurrentSpeed();
		return Math.round(sum*100)/100;
	}
	
	
	//sets speed of download for speed limiter
	void setSpeed(int x) {
		
		int speedPerPart=x/4;
		
		for (int i=0;partArray!=null && i<partArray.length;++i){
			partArray[i].setSpeed(speedPerPart);
		}
		
	}
	
	
	
	//this is for dummy download of scheduled download
	public IndividualDownload(URL u,Date d){
		
		fileName="Scheduled at "+d+" FileName="+u.getFile().substring( u.getFile().lastIndexOf('/')+1 );
		downloadstatus=SCHEDULED;
		
		
	}
	
	
	//constructor
	public IndividualDownload(URL url,String downloadFolder,int s){
		
		downloadPath=new String(downloadFolder);
		this.url=url;
		downloadstatus=s;
		//System.out.println("Download Status "+downloadstatus);
		
		System.out.println("url: "+url);
		
		fileName=this.url.getFile().substring( this.url.getFile().lastIndexOf('/')+1 );
		
		
		//System.out.println("dispatching thread");
		t=new Thread(this);
		t.start();
		
	}
	
	public IndividualDownload(URL url,String downloadFolder){
		
		downloadPath=new String(downloadFolder);
		this.url=url;
		
		System.out.println("url: "+url);
		
		fileName=this.url.getFile().substring( this.url.getFile().lastIndexOf('/')+1 );
		
		
		//System.out.println("dispatching thread");
		
		t=new Thread(this);
		t.start();
	}
	
	//this notifies its observer ->tableModel
	private void stateChanged(){
		
		setChanged();
		notifyObservers();
	}
	
	//shows Error
	private void error(String str){
		if (downloadstatus!=ERROR){
			downloadstatus=ERROR;
			stateChanged();
			
			JOptionPane.showMessageDialog(null, "error: "+str);
		}
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try{
			
			//connecting to URL
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
			
			//this specifies what portion of the file to download
			connection.setRequestProperty("Range", "bytes=" + 0 + "-");
			
			
			connection.connect();//connected to server
			
			//System.out.println("connected"+connection.getResponseCode());
			
			if (totalSize==-1){
				totalSize=connection.getContentLength();
				stateChanged();
			}
			
			if (connection.getResponseCode()/100 !=2 ){
				error("response code error");
			}
			else {
				if ( connection.getResponseCode()==206 ){
					partArray=new IndividualPartDownload[fileSplitNumber];
					int begin=0;
					int last=connection.getContentLength();
					int partSize=last/fileSplitNumber;
					for (int i=0;i<fileSplitNumber-1;++i){
						partArray[i]=new IndividualPartDownload(url, downloadPath, begin, begin+partSize, i,downloadstatus);
						begin+=partSize;
					}
					partArray[fileSplitNumber-1]=new IndividualPartDownload(
							url, downloadPath, begin, last, fileSplitNumber-1,downloadstatus);
				}
				else{
					partArray=new IndividualPartDownload[1];
					partArray[0]=new IndividualPartDownload(url, downloadPath, 0, totalSize, 0,downloadstatus);
				}
				for (int i=0;i<partArray.length;++i)
					partArray[i].addObserver(this);
			}	
			
			
		}
		catch(Exception e){
			error("exception while running"+e.getMessage());
		}
		
		
	}
	//this is for the size bar of table
	public int getSize(){
		return totalSize;
	}
	//this is for progressBar of table
	public float getProgress(){
		if (totalSize!=0){
			float sum=0;
			for (int i=0;partArray!=null && i<partArray.length;++i){
				if (partArray[i]!=null )
					sum+=partArray[i].getProgress();
			}
			if ( partArray!=null )
				sum/=partArray.length;
			
			return sum;
		}
		return 0;
	}
	//this is for progressBar of table & updateButton of DownloadManagerScreen
	public int getStatus(){

		if (downloadstatus!=DOWNLOADING)
				return downloadstatus;
		int allpartCompleted=0;
		for (int i=0;partArray!=null && i<partArray.length;++i){
			if (partArray[i]!=null &&  partArray[i].getStatus()==COMPLETE ){
				++allpartCompleted;
			}
			if (partArray[i]!=null &&  partArray[i].getStatus()==ERROR){
				
				for (int j=0;j<partArray.length;++j)
					partArray[i].pause();
				return downloadstatus=ERROR;
				
			}
		}
		synchronized (merger) {
			
			if (allpartCompleted==fileSplitNumber && downloadstatus!=COMPLETE){
				
				try{
					return downloadstatus=COMPLETE;
				}finally{
					boolean isOpen=false;
					for (int i=0;i<partArray.length;++i){
						if (partArray[i].isOpen())
							isOpen=true;
						if (isOpen)
							try {
								System.out.println("Going to sleep"+System.currentTimeMillis());
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						else
							break;
					}
					merger.merge(partArray);
				}
				
				
			}else{
				return downloadstatus;
			}
		}
		
		
	}
	//for pausing download
	public void pause(){
		downloadstatus=PAUSED;
		
		for (int i=0;i<partArray.length;++i)
		{
			partArray[i].pause();
		}
		
		stateChanged();
	}
	//for resuming download
	public void resume(){
		
		downloadstatus=DOWNLOADING;
		
		for (int i=0;partArray!=null && i<partArray.length;++i){
			downloadstatus=DOWNLOADING;
			partArray[i].resume();
		}
		stateChanged();
	}
	//for canceling download
	public void cancel(){
		if (downloadstatus!=COMPLETE  ){
			for (int i=0;partArray!=null && i<partArray.length;++i)
					partArray[i].cancel();
		}
		downloadstatus=CANCELLED;
		stateChanged();
	}
	
	@Override
	public String toString(){
		return fileName;
	}

	
	

	@Override
	public void update(Observable partDownload, Object arg) {
		// TODO Auto-generated method stub
		
		if ( ( (IndividualPartDownload)partDownload).getStatus()==ERROR ){
			downloadstatus=ERROR;
			for (int i=0;i<partArray.length;++i){
				partArray[i].pause();
			}
		}
		
		stateChanged();
		
	}


	
}
