import java.net.*;
import java.io.*;
import java.util.*;

public class s1
{
    public static void main(String args[]) throws Exception
    {
        ServerSocket soc=new ServerSocket(5216);//creating a server socket
        while(true)
        {
            System.out.println("Waiting for Connection ...");
            transferfile t=new transferfile(soc.accept());//Waits for an incoming client
        }
    }
}

class transferfile extends Thread
{
    Socket ClientSoc;
    DataInputStream din;
    DataOutputStream dout;
    transferfile(Socket soc)
    {
        try
        {
            ClientSoc=soc;                        
            din=new DataInputStream(ClientSoc.getInputStream());// creating input and output streams for communication
            dout=new DataOutputStream(ClientSoc.getOutputStream());
            System.out.println("FTP Client Connected ...");
            start();
            
        }
        catch(Exception ex)
        {
           System.out.println("Error");
        }        
    }
    void get() throws Exception //to transfer a file from server to client
    {        
        String filename=din.readUTF(); //read the file name to be transferred 
        File f=new File(filename);
        if(!f.exists()) // check whether the file already exists in the client
        {
            dout.writeUTF("File Not Found");
            return;
        }
        else
        {
            dout.writeUTF("READY");
            FileInputStream fin=new FileInputStream(f);
            int ch;
            do
            {
                ch=fin.read();
                dout.writeUTF(String.valueOf(ch)); //Receiving the file from server
            }
            while(ch!=-1);    
            fin.close();    
            dout.writeUTF("File Receive Successfully");                            
        }
    }
    
    void put() throws Exception// to transfer a file from client to server
    {
        String filename=din.readUTF();
        if(filename.compareTo("File not found")==0)
        {
            return;
        }
        File f=new File(filename);
        String option;
        
        if(f.exists())// check whether fiole already exists in the server
        {
            dout.writeUTF("File Already Exists");
            option=din.readUTF();
        }
        else
        {
            dout.writeUTF("SendFile");// writing the file to server
            option="Y";
        }
            
            if(option.compareTo("Y")==0)
            {
                FileOutputStream fout=new FileOutputStream(f);
                int ch;
                String temp;
                do
                {
                    temp=din.readUTF();
                    ch=Integer.parseInt(temp);
                    if(ch!=-1)
                    {
                        fout.write(ch);                    
                    }
                }while(ch!=-1);
                fout.close();
                dout.writeUTF("File Send Successfully");
            }
            else
            {
                return;
            }
            
    }
  void listfile() throws Exception //listing files in the directory specified
      {
         String path = "/home/user/Desktop/";
         String text="";
         File f = new File(path);
         File[] listOfFiles = f.listFiles();
         int l=listOfFiles.length;
         dout.write(l);
         for (int j = 0; j < listOfFiles.length; j++) 
         {
            if (listOfFiles[j].isFile()) // check whether it is file
            {
              text = listOfFiles[j].getName();
              dout.writeUTF(text);
            }
            else if (listOfFiles[j].isDirectory()) //check whether it is directory
            {
               text= listOfFiles[j].getName();
               dout.writeUTF(text);
            }
         }
      }


    public void run() //execution of commands
    {
        while(true)
        {
            try
            {
            System.out.println("Waiting for Command ...");
            String cmd=din.readUTF();
            if(cmd.compareTo("GET")==0)
            {
                System.out.println("\tGET Command Received ...");
                get();
                continue;
            }
            else if(cmd.compareTo("SEND")==0)
            {
                System.out.println("\tSEND Command Receiced ...");                
                put();
                continue;
            }
            else if(cmd.compareTo("LS")==0)
            {
                System.out.println("\tLS Command Receiced ...");                
                listfile();
                continue;
            }
            else if(cmd.compareTo("DISCONNECT")==0)
            {
                System.out.println("\tDisconnect Command Received ...");
                System.exit(1);
            }
            }
            catch(Exception ex)
            {
            }
        }
    }
}
