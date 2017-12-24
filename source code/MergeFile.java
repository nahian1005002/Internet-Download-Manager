import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;


public class MergeFile {
	
	private boolean called=false;
	
	public void merge( IndividualPartDownload [] partArray ){
		
		if (called)return ;
		
		called=true;
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		FileInputStream []input=new FileInputStream[partArray.length];
		File []inputFile=new File [ partArray.length ];
		try{
			for (int i=0;i<input.length;++i){
				
				
				if ( partArray[i].getDownloadPath().lastIndexOf(File.separator) == partArray[i].getDownloadPath().length()-1 ){
					System.out.println(partArray[i].getDownloadPath() + partArray[i].fileName);
					inputFile[i]=new File( partArray[i].getDownloadPath() + partArray[i].fileName )  ;
				}
					
				else{
					System.out.println(partArray[i].getDownloadPath() + File.separator+partArray[i].fileName);
					inputFile[i]=new File( partArray[i].getDownloadPath() + File.separator +partArray[i].fileName )  ;
				}
				
					
				
				
				input[i]=new FileInputStream(inputFile[i]);
			}
			
			File outputFile=new File(partArray[0].getDownloadPath(),partArray[0].fileName.substring( 
					0, partArray[0].fileName.lastIndexOf('.')));
			
			
			outputFile.createNewFile();
			
			FileOutputStream output=new FileOutputStream(outputFile);
			
			for (int i=0;i<input.length;++i){
				
				int len;
				byte[] buffer=new byte[1024];
				while ( (len=input[i].read(buffer) )>0 ){
					output.write(buffer,0,len);
				}
				input[i].close();
				
				inputFile[i].delete();
				
			}
			output.close();
			
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "Cannot find download filed", "Error", JOptionPane.ERROR_MESSAGE);
			return ;
		}
		
		
		
		
	}
	
}
