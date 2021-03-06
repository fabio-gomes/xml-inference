/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufrj.ppgi.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.ufrj.ppgi.io.FileManager;
/**
 *
 * @author Rafael Pinheiro
 */
public class DefaultHandleSAX extends DefaultHandler {
	
	private static final String PATHCONFIG = "config.txt";
    private String strFato;
    private static int contadorIdPai = 0;
    private Stack<ElementoXML> pilha = new Stack<ElementoXML>();
    private FileManager arquivo;
    private String strConteudo = "";
    private Boolean bResetLastId = false;
    private ElementoXML elementoRaiz = null;
    private Boolean bElementoAberto = false;
    private ElementoXML elementoAtual;
    
    private ArrayList<String> orderedElementList = new ArrayList<String>();
        
    public DefaultHandleSAX() {
        super(); 
        arquivo = new FileManager();
        contadorIdPai = 0;
    }
    
    public void setResetLastId(Boolean _bResetLastId)
    {
    	bResetLastId = _bResetLastId;
    }
    
    public ArrayList<String> orderedElementList()
    {
    	return orderedElementList;
    }
    
    private void escreverId(Integer contadorId)
    {
    	File file = new File(PATHCONFIG);   
    	FileWriter fw;
		try {
			fw = new FileWriter(file);
			fw.write(contadorId.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }
    
    private Integer obterId()
    {
    	File file = new File(PATHCONFIG);   
    	FileReader fr;
		try {
			fr = new FileReader(file);  
			BufferedReader br = new BufferedReader(fr);  
    	    String linha = br.readLine();
    	    br.close();
    	    fr.close();
    	    return Integer.parseInt(linha);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return 0;
    }
    
    @Override
    public void startDocument() {
    	if ( bResetLastId )
    	{
    		contadorIdPai = 0;
    	}
    	else
    	{
    		contadorIdPai = obterId();
    	}
    	
    	
    }

    @Override
    public void endDocument() {
		escreverId(contadorIdPai);
    }
        
    private void processarAtributos(Attributes atts )
    {
        for( int i = 0; i < atts.getLength(); i++ )
        {
        	ElementoXML novoElemento = new ElementoXML();
        	novoElemento.setTipo(ElementoXML.TipoElemento.ATRIBUTO);
            String atributo = atts.getQName(i);

            if (atributo.contains(":"))
            {
            	//atributo = atributo.replace(":", "_");
            	String [] split =  atributo.split(":");
            	atributo = "-";
            	for(int j=0; j<split.length-1;j++)
            	{
            		atributo+= split[j] + "-";
            	}
            }
            
            novoElemento.setNome(atributo.toLowerCase());
            
            getElementoAtual().adicionarFilho(novoElemento);
            novoElemento.setPai(getElementoAtual());
            novoElemento.setId(++contadorIdPai);

            ElementoXML conteudo = new ElementoXML();
            conteudo.setId(++contadorIdPai);
            conteudo.setConteudo(atts.getValue(i));
            conteudo.setPai(novoElemento);
            conteudo.setTipo(ElementoXML.TipoElemento.TEXTO);
            
            novoElemento.adicionarFilho(conteudo);
            
            /*
            
            ElementoXML elementoPai = pilha.lastElement();
            //elementoPai.possuiAtributo(true);
            String fatoAtributo = atributo.toLowerCase() + "(" + elementoPai.getId() + ",'";
            fatoAtributo +=  + "').\n";
            System.out.println(fatoAtributo);
            arquivo.writeFacts(fatoAtributo);           */
        }
    }
    
    public String getUltimoElementoPilha(){
    	ElementoXML elementoAtual = (ElementoXML)pilha.lastElement();
    	return elementoAtual.getNome();
    }
    
    public ElementoXML getElementoAtual(){
    	return elementoAtual;
    }
    
    
    public void apontarPai(){
    	elementoAtual = elementoAtual.getPai();
    }
    
    @Override
    public void startElement (String uri, String localName, String qName, Attributes atts) {
    	/*if (qName.equals("ss")){
    		int a =0;
    	}*/
    	
    	if(!orderedElementList.contains(qName))
    		orderedElementList.add(qName);
    	
    	ElementoXML novoElemento = new ElementoXML();
    	novoElemento.setTipo(ElementoXML.TipoElemento.FILHO);
    	
    	if(qName.contains(":"))
    	{
    		//qName = qName.replace(":", "_");
    		String [] split =  qName.split(":");
    		qName = "-";
        	for(int j=0; j<split.length-1;j++)
        	{
        		qName+= split[j] + "-";
        	}
    		
    	}
    	
    	
    	novoElemento.setNome(qName);
    	novoElemento.setId(++contadorIdPai);
    	if ( pilha.size() == 0)
    	{ //elemento Raiz
    		novoElemento.setPai(null);
    		pilha.push(novoElemento);
    		elementoAtual = novoElemento;
    		strConteudo += elementoAtual.getNome().toLowerCase();
	    	strConteudo += "(" + elementoAtual.getId() + ").\n";
	    	
	    	processarAtributos( atts);
	    	escreverElementoNoArquivo(elementoAtual);
    		
	    	return;
    	}
    	else{
    		novoElemento.setPai(getElementoAtual());
    	}    	
    	
    	if ( elementoRaiz == null ){
    		pilha.push(novoElemento);
    		elementoRaiz = novoElemento;
    	}   
    	
    	getElementoAtual().adicionarFilho(novoElemento);
    	elementoAtual = novoElemento;    		
        
        processarAtributos( atts);
    }

    @Override
     public void endElement (String uri, String localName, String qName) throws SAXException {
    	if ( elementoRaiz != null && elementoRaiz.getNome().equals(qName) ){
            strConteudo += elementoRaiz.getNome().toLowerCase();
    	    strConteudo += "(" + elementoRaiz.getPai().getId() + ", " + elementoRaiz.getId() + ").\n";
    	    if ( !elementoRaiz.getConteudo().isEmpty() && elementoRaiz.getFilhos().isEmpty()){
    	    	strConteudo += elementoRaiz.getNome().toLowerCase();
        	strConteudo += "(" + elementoRaiz.getId() + ", '" + elementoRaiz.getConteudo().replace("'", "`") + "').\n";
        	elementoRaiz.setElementoImpresso(true);
    	    }
    	    	
            apontarPai();
    	    pilha.pop();
    	    escreverElementoNoArquivo(elementoRaiz);
    	    elementoRaiz = null;
    	    System.out.println(strConteudo);
    	    arquivo.writeFacts(strConteudo);
            strConteudo = "";
    	}
    	else
    	{
    		if ( getElementoAtual().getFilhos().size() == 0 )
    		{
		       ElementoXML novoElemento = new ElementoXML();
		       novoElemento.setTipo(ElementoXML.TipoElemento.TEXTO);
		       novoElemento.setConteudo("");
		       novoElemento.setId(++contadorIdPai);
		       getElementoAtual().adicionarFilho(novoElemento);
		       novoElemento.setPai(getElementoAtual());
    		}
            apontarPai();
    	}
    	//System.out.println(strFato);
        //arquivo.writeFacts(strFato);
    }

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
       String conteudo = new String( ch, start, length);
       if ( !possuiLetra(conteudo))
    	   return;
       
       ElementoXML novoElemento = new ElementoXML();
       novoElemento.setTipo(ElementoXML.TipoElemento.TEXTO);
       novoElemento.setConteudo(conteudo);
       novoElemento.setId(++contadorIdPai);
       getElementoAtual().adicionarFilho(novoElemento);
       novoElemento.setPai(getElementoAtual());
       //getElementoAtual().setConteudo(conteudo);
    }
    
    private void escreverElementoNoArquivo(ElementoXML elemento){
    	Boolean bElementoMisto = elemento.ehElementoMisto();
    	ArrayList<ElementoXML> filhos = elemento.getFilhos();
    	for( int i = 0; i < filhos.size(); i++){
    		if ( filhos.get(i).getTipo() == ElementoXML.TipoElemento.FILHO &&
                        !filhos.get(i).naoPossuiElementoTexto())
    		{
                    if ( !filhos.get(i).getElementoImpresso() ){
                        if ( filhos.get(i).getNome().toLowerCase().equals("hw")){
                            int a= 0;
                        }
                        
    			strConteudo += filhos.get(i).getNome().toLowerCase();
    			strConteudo += "(" + filhos.get(i).getPai().getId() + ", " + filhos.get(i).getId() + ").\n";
    			filhos.get(i).setElementoImpresso(true);
                    }
    		}
    		if( ElementoXML.TipoElemento.FILHO != filhos.get(i).getTipo() || ElementoXML.TipoElemento.ATRIBUTO != filhos.get(i).getTipo()){
    			if ( bElementoMisto && filhos.get(i).getTipo() == ElementoXML.TipoElemento.TEXTO){
    				if ( !filhos.get(i).getElementoImpresso() ){
                                    imprimirPai(filhos.get(i).getPai());
                                    strConteudo += "xmlMixedElement";
                                    strConteudo += "(" + filhos.get(i).getPai().getId() + ", " + filhos.get(i).getId() + ", '" + filhos.get(i).getConteudo().replace("'", "`") + "').\n";
                                    filhos.get(i).setElementoImpresso(true);
                                }
	    		}
	    		else
	    		{
		    		if( filhos.get(i).getTipo() == ElementoXML.TipoElemento.TEXTO )
		    		{
		    			if ( filhos.get(i).getPai().getTipo() == ElementoXML.TipoElemento.ATRIBUTO )
		    			{
                            if ( !filhos.get(i).getElementoImpresso() )
                            {
			    				strConteudo += filhos.get(i).getPai().getPai().getNome().toLowerCase()+"_attribute_"+filhos.get(i).getPai().getNome().toLowerCase();
			    				//strConteudo += filhos.get(i).getPai().getNome().toLowerCase();
				    			strConteudo += "(" + filhos.get(i).getPai().getPai().getId() + ", " + filhos.get(i).getPai().getId() + ", '" + filhos.get(i).getConteudo().replace("'", "`") + "').\n";
	                            filhos.get(i).setElementoImpresso(true);
                            }
		    			}
		    			else
		    			{
		    				//imprimirPai(filhos.get(i).getPai().getPai());
                                            if ( !filhos.get(i).getElementoImpresso() ){
		    				strConteudo += filhos.get(i).getPai().getNome().toLowerCase();
			    			strConteudo += "(" + filhos.get(i).getPai().getPai().getId() + ", " + filhos.get(i).getPai().getId() + ", '" + 
			    							filhos.get(i).getConteudo().replace("'", "`")  + "').\n";
                                                filhos.get(i).setElementoImpresso(true);
                                            }
			    			
		    			}
		    		}
		    		else
		    		{
		    			//strConteudo += filhos.get(i).getNome().toLowerCase();
		    			//strConteudo += "(" + filhos.get(i).getPai().getId() + ", " + filhos.get(i).getId() + ", '" + filhos.get(i).getConteudo() + "').\n";
		    		}
	    		}
    		}
    		escreverElementoNoArquivo(filhos.get(i));
    	}    	
    }
    
    private void imprimirPai(ElementoXML pai){
    	if ( !pai.getElementoImpresso() ){
 	     	strConteudo += pai.getNome().toLowerCase();
	    	strConteudo += "(" + pai.getPai().getId() + ", " + pai.getId() + ").\n";
	    	pai.setElementoImpresso(true);
    	}
    }
    
    private boolean possuiLetra( String s ) {  
    	  
        // cria um array de char  
        char[] c = s.toCharArray();  
        boolean d = false;  
      
        for ( int i = 0; i < c.length; i++ ){  
            // verifica se o char n�o � um d�gito  
            if ( Character.isLetterOrDigit( c[ i ] ) ) {  
                d = true;
            }  
        }        
      
        return d;      
    }
    
    private boolean possuiSomenteEspacoEmBraco(String s){
    	s.replace("\n", "");
    	s.replace("\t", "");
    	if ( s.indexOf(" ") > - 1)
    		return true;
    	return false;
    }
    
    
}