/*
	Fuzzy matching autocomplete library
	By Narendra Rajput
	Email:bknarendra2008@gmail.com
	License:Do whatever you want with it.
			Just don't blame me if you screw up using it in your application.
			If possible don't remove author's name.
			Fork the project on github.Link https://github.com/bknarendra/Fuzzy-matching-autocomplete-library
*/

import java.io.*;
import java.util.*;
import java.net.*;
class CHandler implements Runnable
{
	Socket s;
	int currentscr=0;
	public Runtime rt=Runtime.getRuntime();
	CHandler(Socket soc){s=soc;}
	CHandler(){s=null;}
	public void getW(String w){
		Node nod=AutocompleteServer.t.getC(w);
		String tp;
		for(Character c:nod.ch.keySet()) {
			tp=w+String.valueOf(c.charValue());
			if(nod.ch.get(c).e) finalw.put(tp,currentscr);
			else getW(tp);
		}
	}
	public HashMap<String,Integer>finalw=new HashMap<String,Integer>();
	public void getwords(String word,int mc) {
		int len=word.length();
		int[] cr=new int[len + 1];
		for (int i=0;i<=len; i++) 
			cr[i]=i;
		for (Character c:AutocompleteServer.t.root.ch.keySet())
			tr(AutocompleteServer.t.root.getChild(c),c.charValue(),word,cr,"",mc);
	}
	public char tc(char c){
		if(c>64&&c<123) return (char)(c^32);
		return c;
	}
	public Map sortByComparator(Map unsortMap) {
        List list=new LinkedList(unsortMap.entrySet());
        Collections.sort(list, new Comparator() {
        	public int compare(Object o1, Object o2) {
        		return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        Map sortedMap=new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
	     	Map.Entry entry = (Map.Entry)it.next();
	     	sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	public void tr(Node node,char l,String word, int[] pr,String aword,int mc) {
		int size=pr.length;
		int[] cr=new int[size];
		cr[0]=pr[0]+1;
		int min=cr[0];
		if(node.l!='$') aword+=String.valueOf(node.l);
		char character;
		for(int i=1;i<size;i++) {
			character=(word.charAt(i-1));
			cr[i]=Math.min(Math.min(cr[i-1]+1,pr[i]+1),(character==l||character==tc(l))?pr[i-1]:(pr[i-1]+1));
			if (cr[i]<min) min=cr[i];
		}
		if(word.length()==aword.length()){
			if(cr[size-1]<mc) wordist.put(aword,cr[size-1]);
			return;
		}
	    if(min<mc) 
			for(Character c:node.ch.keySet()) 
		        tr(node.ch.get(c),c.charValue(),word,cr,aword,mc);
	}
	public HashMap<String,Integer>wordist=new HashMap<String,Integer>();
	public HashMap<String,Integer> getResult(String wordv,int tf){
		try{
			getwords(wordv,tf);
			for(String word:wordist.keySet()){
				currentscr=((Integer)wordist.get(word)).intValue();
				getW(word);
			}
			int n=finalw.size();
			finalw=(HashMap)sortByComparator(finalw);
			return finalw;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	public void run(){
		try{
			String req="",tmp="",getw[];
			int par=0,tf=2;
			String p1="",wordv="",response="";
			long abcd=System.currentTimeMillis();
			BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter out=new PrintWriter(s.getOutputStream());
			req=in.readLine();
			if(req.indexOf("/autoc?")==-1) {out.print("404 Error");s.close();return;}
			getw=req.split(" ");
			par=getw[1].indexOf("?");
			if(par>=0) req=getw[1].substring(par+1);
			getw=req.split("&");
			for(String param:getw){
				System.out.println(param);
				par=param.indexOf("=");
				if(par>=0) {
					p1=param.substring(0,par);
					if(p1.equals("word")) wordv=URLDecoder.decode(param.substring(par+1));
					else if(p1.equals("tf")) tf=Integer.parseInt(param.substring(par+1));
				}
			}
			getwords(wordv,tf);
			response="{";par=0;
			for(String word:wordist.keySet()){
				currentscr=((Integer)wordist.get(word)).intValue();
				getW(word);
			}
			int n=finalw.size();
			finalw=(HashMap)sortByComparator(finalw);
			for(String word:finalw.keySet()){
				response+="\""+word+"\":"+finalw.get(word);
				par++;
				if(par<n) response+=", ";
			}
			response+="}";
			System.out.println(response);
			out.print("HTTP/1.1 "+"200 OK"+"\r\n"+
				"Connection: Keep-Alive\r\n"+
				"cache-control: no-store\r\n"+
				"Content-Length: "+Integer.toString(response.length())+"\r\n"+
				"Content-Type: "+"application/json"+"\r\n"+
				"\r\n"+response);
			System.out.println("Time to process one request="+((System.currentTimeMillis()-abcd)/1000.0)+" sec");
			System.out.println("Used memory : "+((rt.totalMemory()-rt.freeMemory())/1048576.0)+" MB");
			out.flush();
			s.close();
			response="";
			rt.gc();
			}catch(Exception e){e.printStackTrace();}
	}
}
	class Trie {
	    public Node root;
	    public Trie() {root=new Node('$');}
	    public void add(String word) {
	        int len=word.length();
	        Node c=root;
	        if(len==0) c.e=true;
	        for (int idx=0;idx<len;idx++) {
	            char l=word.charAt(idx);
	            Node chl=c.getChild(l);
	            if(chl!=null) c=chl;
	            else {
	                c.ch.put(l,new Node(l));
	                c=c.getChild(l);
	            }
	            if (idx==len-1) c.e=true;
	        }
	    }
		public Node getC(String wrd){
			int len=wrd.length();
			Node c=root,ch=null;
			if(len==0) return c;
			for(int i=0;i<len;i++){
				ch=c.getChild(wrd.charAt(i));
				if(ch==null) return null;
				c=ch;
			}
			return c;
		}
	}

	class Node {
	    public char l;
	    public boolean e;
	    public Map<Character,Node>ch;
	    public Node(char c) {
	        e=false;
	        l=c;
	        ch=new HashMap<Character,Node>();
	    }
	    public Node getChild(char l) {
	        if(ch!=null) if(ch.containsKey(l)) return ch.get(l); 
	        return null;
	    }
	}
public class AutocompleteServer
{
	public static Trie t;
	public static int port,currentscr=0;
	public static String file;

	/*Use it as a http server for providing autocomplete*/
	public static void main(String args[]) throws Exception{
		long abcd=System.currentTimeMillis();
		t=new Trie();
		Properties prop=new Properties();
		try {
    		prop.load(new FileInputStream("config.ini"));
			file=prop.getProperty("file","file.txt");
			port=Integer.parseInt(prop.getProperty("port","80"));
		}catch(IOException ex){ex.printStackTrace();}
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String a="";
		for(;(a=br.readLine())!=null;) t.add(a);
		long timetobuild=System.currentTimeMillis();
		System.out.println("Time to build the tree : "+((timetobuild-abcd)/1000.0)+" sec");
		ServerSocket ss=new ServerSocket(port);
		System.out.println("Server started........(Send request on http://serverip/autoc?word=word_for_autocomplete&tf=max_typos_allowed)");
		while(Boolean.TRUE){
			new Thread(new CHandler(ss.accept())).start();
		}
	}

	/*Using it in standalone mode.Or embed it in your own code.Example*/

	/*public static void main(String args[]) throws Exception{
		t=new Trie();
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		String a="";
		for(;(a=br.readLine())!=null;) t.add(a);
		HashMap<String,Integer>res=new CHandler().getResult("furci",2);
		for(String str:res.keySet())System.out.println(str+" "+res.get(str));
	}*/
}