import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;


public class IndividualPartDownload extends Observable implements Runnable{
	
	public final String fileName;
	
	//these are the possible statuses of a download
	public static final String STATUSES[]={"Downloading","Paused","Complete","Cancelled","Error"};
	
	//these variable represents the status
	public static final int DOWNLOADING=0;
	public static final int PAUSED=1;
	public static final int COMPLETE=2;
	public static final int CANCELLED=3;
	public static final int ERROR=4;
	public static final int QUEUED =6;
	
	private int totalSize= -1;
	private int downloaded= 0;
	//this is the status of this downloading
	private int downloadstatus=DOWNLOADING;
	
	private URL url;
	private String downloadPath;
	
	private RandomAccessFile file=null;
	
	public String getDownloadPath(){
		return downloadPath;
	}
	
	//private Thread t;
	private Thread t;
	
	//this points to file-part start
	private int start;
	//this points to file-part end
	private int finish;
	
	private static final int MAXBUFFER=1024;

	
	//there required for limiting download & show current speed
	private long timeBuffer;
	private int dataBuffer;
	private int maxSpeed = 1000; // in terms of KiloBytes
	private float currentSpeed=0;
	private int delay = 1000;

	private int saveDownloadedForSpeed=0;
	
	float getCurrentSpeed() {
		return currentSpeed;
	}

	
	
	void setSpeed(int x) {
		
		//sets limit for download
		maxSpeed = x;
		timeBuffer = dataBuffer = 0;
		currentSpeed = 0;
		timeBuffer = System.currentTimeMillis();
	}
	
	

	public IndividualPartDownload(URL url,String downloadFolder,int s,int f,int partNo,int status){
		
		downloadPath=new String(downloadFolder);
		this.url=url;
		
		System.out.println("url: "+url);
		
		start=s;
		finish=f;
		
		downloadstatus=status;
		
		//System.out.println("Download Status in each part "+downloadstatus);
		
		fileName=this.url.getFile().substring( this.url.getFile().lastIndexOf('/')+1 )+"."+partNo;
		
		
		System.out.println("dispatching thread for part "+partNo);
		
		t=new Thread(this);
		t.start();
	}
	
	private void stateChanged(){
		
		setChanged();
		notifyObservers();
	}
	
	private void error(String str){
		if (downloadstatus!=ERROR){
			downloadstatus=ERROR;
			stateChanged();
			
			//JOptionPane.showMessageDialog(null, "error "+str);
		}
		
	}
	public boolean isOpen(){
		if (file==null)return false;
		else	return true;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		file =null;
		InputStream stream=null;
		Timer timer=new Timer();
		try{
			
			//connecting to URL
			HttpURLConnection connection=(HttpURLConnection)url.openConnection();
			
			//this specifies what portion of the file to download
			connection.setRequestProperty("Range", "bytes=" + (start+downloaded) + "-");
			
			
			connection.connect();//connected to server
			
			System.out.println("connected  "+fileName+" "+connection.getResponseCode());
			
			if (connection.getResponseCode()/100 !=2 ){
				error("response code error");
			}
			totalSize=finish-start;
				
			
			
			if (totalSize<1){
				error("size error");
				
			}
			
			System.out.println(downloadPath);
			
			//initializes "file" to the url File
			file =new RandomAccessFile(downloadPath + File.separator+fileName, "rw");
			file.seek(downloaded);
			
			
			//assigning inputStream
			stream=connection.getInputStream();
			
			System.out.println("ok");
			
			dataBuffer = 0;
			timeBuffer = System.currentTimeMillis();
			
			//this is called every 1000 milisecond
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (downloadstatus==DOWNLOADING){
						currentSpeed = ((float) dataBuffer) / 1024;
						dataBuffer = 0;
						timeBuffer = System.currentTimeMillis();
						//System.out.println("\ncurrent speed "+currentSpeed);
						//System.out.println("max speed "+maxSpeed+"\n");
						
					}
					else if (downloadstatus==CANCELLED || downloadstatus==COMPLETE  || downloadstatus==PAUSED){
						this.cancel();
					}

				}
			}, 0, delay);
			
			
			while (downloadstatus==DOWNLOADING){
				byte []buffer;
				
				int len;
				if (totalSize - downloaded > MAXBUFFER) {
					buffer = new byte[MAXBUFFER];
					len=MAXBUFFER;
				} 
				else {
					buffer = new byte[totalSize - downloaded];
					len=totalSize - downloaded;
				}
				
				int bytesReadInThisLoop=stream.read(buffer, 0, len);
				
				
				
				if ( bytesReadInThisLoop<=0 )
					break;
				file.write(buffer, 0, bytesReadInThisLoop);
				
				downloaded+=bytesReadInThisLoop;
				
				dataBuffer+=bytesReadInThisLoop;
                
				//if download does over the limit then it sleeps for the rest of the secon
				if(dataBuffer>maxSpeed*1024)
                {
                    //System.out.println("time left: "+(1000-(System.currentTimeMillis()-timeBuffer)));
					//System.out.println("now: "+System.currentTimeMillis());
					//System.out.println("last call: "+timeBuffer);
                    if ( (1000-(System.currentTimeMillis()-timeBuffer) >0 ) )
                    	Thread.sleep(1000-(System.currentTimeMillis()-timeBuffer));
                }
				
				
				stateChanged();
				
			}
			if (downloadstatus==DOWNLOADING){
				file.close();
				file=null;
				downloadstatus=COMPLETE;
				stateChanged();
				
				System.out.println("download completed");
				System.out.println(fileName+" "+downloadstatus);
			}
		}
		catch(Exception e){
			error("exception while running"+e.getMessage());
		}
		
		finally{
			try{
				
				if (file!=null)
					file.close();
				stream.close();
				
			}
			catch(Exception e){
				error( e.toString() );
			}
			
		}
		
		
	}
	//this is for the size bar of table
	public int getSize(){
		return totalSize;
	}
	//this is for progressBar of table
	public float getProgress(){
		if (totalSize!=0){
			return ( (float)downloaded/totalSize )*100;
		}
		return 0;
	}
	//this is for progressBar of table & updateButton of DownloadManagerScreen
	public int getStatus(){
		return downloadstatus;
	}
	//for pausing download
	public void pause(){
		downloadstatus=PAUSED;
		System.out.println("pause: "+downloaded);
		stateChanged();
	}
	//for resuming download
	public void resume(){
		downloadstatus=DOWNLOADING;
		System.out.println("resume: "+downloaded);
		stateChanged();
		t=new Thread(this);
		t.start();
	}
	//for canceling download
	public void cancel(){
		if ( downloaded!=0 ){
			File f;
			if ( downloadPath.lastIndexOf(File.separator) == downloadPath.length()-1 ){
				f=new File(downloadPath+fileName);
			}
				
			else{
				f=new File(downloadPath+File.separator+fileName);
			}
			System.out.println(f);
			f.delete();
		}
		downloadstatus=CANCELLED;
		stateChanged();
	}
	
	@Override
	public String toString(){
		return fileName;
	}
	
	
	
}
