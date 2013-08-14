import java.net.*;
import java.io.*;
import java.util.*;


class c1
{
    public static void main(String args[]) throws Exception
    {
        Socket soc=new Socket("localhost",5216); //connect to server
        filetransfer t=new filetransfer(soc);
        t.Menu();
        
    }
}
class filetransfer
{
    Socket ClientSoc;
    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;
    filetransfer(Socket soc)
    {
        try
        {
            ClientSoc=soc;
            din=new DataInputStream(ClientSoc.getInputStream()); //creation of input and output streams
            dout=new DataOutputStream(ClientSoc.getOutputStream());
            br=new BufferedReader(new InputStreamReader(System.in));
        }
        catch(Exception ex)
        {
            System.out.println("Error");
        }        
    }
    void put() throws Exception //to transfer file from client to server
    {        
        
        String filename;
        System.out.print("Enter File Name :");
        filename=br.readLine();  //read the file name
            
        File f=new File(filename);
        if(!f.exists())         // check whether file already exists
        {
            System.out.println("File not Exists...");
            dout.writeUTF("File not found");
            return;
        }
        
        dout.writeUTF(filename); 
        
        String msg=din.readUTF();
        if(msg.compareTo("File Already Exists")==0)
        {
            String Option;
            System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
            Option=br.readLine();            
            if(Option=="Y")    
            {
                dout.writeUTF("Y");
            }
            else
            {
                dout.writeUTF("N"); 
                return;
            }
        }
        
        System.out.println("Sending File ...");      
        FileInputStream fin=new FileInputStream(f);    
        int ch;
        do
        {
            ch=fin.read();
            dout.writeUTF(String.valueOf(ch));             
        }
        while(ch!=-1);
        fin.close();
        System.out.println(din.readUTF());          //sending the file to buffer 
        
    }
    
    void get() throws Exception  //transfer file from server to client
    {
        String fileName;
        System.out.print("Enter File Name :");
        fileName=br.readLine();         //reading the file name
        dout.writeUTF(fileName);  
        String msg=din.readUTF();
        
        if(msg.compareTo("File Not Found")==0) //check whether file already exists
        {
            System.out.println("File not found on Server ...");
            return;
        }
        else if(msg.compareTo("READY")==0)
        {
            System.out.println("Receiving File ...");
            File f=new File(fileName);
            if(f.exists())
            {
                String Option;
                System.out.println("File Already Exists. Want to OverWrite (Y/N) ?");
                Option=br.readLine();            
                if(Option=="N")    
                {
                    dout.flush();
                    return;    
                }                
            }
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
            System.out.println(din.readUTF());  //sending the file
                
        }
        
        
    }
     void listfile() throws Exception   //listing all files
     {
        int l=din.read();
        for(int i=0;i<l;i++)
        {
             String list = din.readUTF();
             System.out.println("FROM SERVER - LIST OF FILES:" + list);
        }
    }

    public void Menu() throws Exception      // functions to execute
    {
        while(true)
        {    
            System.out.println("[ MENU ]");
            System.out.println("1. PUT");
            System.out.println("2. GET");
            System.out.println("3. LS");
            System.out.println("4.Exit");
            System.out.print("\nEnter Choice :");
            int choice;
            choice=Integer.parseInt(br.readLine());
            if(choice==1)
            {
                dout.writeUTF("SEND");
                put();
            }
            else if(choice==2)
            {
                dout.writeUTF("GET");
                get();
            }
            else if(choice==3)
            {
                dout.writeUTF("LS");
                listfile();
            }
            else
            {
                dout.writeUTF("DISCONNECT");
                System.exit(1);
            }
        }
    }
}
